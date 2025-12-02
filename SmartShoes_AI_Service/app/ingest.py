from app.db_connect import get_connection, setup_database
from app.utils import clean_text
from sentence_transformers import SentenceTransformer
import numpy as np

BI_ENCODER_PATH = "models/bi_encoder"


def insert_product_embedding(conn, product):
    # Chuyển embedding thành list để psycopg2/pgvector nhận
    embedding_list = product["embedding"].tolist()

    with conn.cursor() as cur:
        cur.execute("""
            INSERT INTO product_embeddings
            (id, description, brand, category, material, color, attributes, embedding)
            VALUES (%s,%s,%s,%s,%s,%s,%s,%s)
            ON CONFLICT (id) DO UPDATE SET
                description = EXCLUDED.description,
                brand = EXCLUDED.brand,
                category = EXCLUDED.category,
                material = EXCLUDED.material,
                color = EXCLUDED.color,
                attributes = EXCLUDED.attributes,
                embedding = EXCLUDED.embedding;
        """, (
            product["id"],
            product["description"],
            product.get("brand"),
            product.get("category"),
            product.get("material"),
            product.get("colors"),
            product.get("attributes"),
            embedding_list  # <- list, không phải string
        ))
    conn.commit()



def main():
    setup_database()
    conn = get_connection()
    cur = conn.cursor()

    print("🔍 Fetching products from database...")

    # Truy vấn lấy dữ liệu đầy đủ từ các bảng
    cur.execute("""
        SELECT 
            p.id AS product_id,
            p.name AS product_name,
            p.description AS product_description,

            b.name AS brand_name,
            c.name AS category_name,

            STRING_AGG(DISTINCT col.name, ', ') AS colors,
            STRING_AGG(DISTINCT pv.size::text, ', ') AS sizes,
            STRING_AGG(DISTINCT a.value, ', ') AS attributes

        FROM products p
        LEFT JOIN brands b ON b.id = p.brand_id
        LEFT JOIN categories c ON c.id = p.category_id

        LEFT JOIN product_variants pv ON pv.product_id = p.id
        LEFT JOIN colors col ON col.id = pv.color_id

        LEFT JOIN product_attributes pa ON pa.product_id = p.id
        LEFT JOIN attributes a ON a.id = pa.attribute_id

        GROUP BY p.id, p.name, p.description, b.name, c.name;
    """)

    rows = cur.fetchall()
    conn.close()

    print(f"📦 Loaded {len(rows)} products")

    # Load bi-encoder
    print("🧠 Loading bi-encoder...")
    model = SentenceTransformer(BI_ENCODER_PATH)

    conn = get_connection()

    for r in rows:
        (
            pid,
            name,
            desc,
            brand,
            category,
            colors,
            sizes,
            attributes
        ) = r

        # Build semantic description
        text_parts = [
            clean_text(name),
            clean_text(desc or ""),
            f"thương hiệu {brand}" if brand else "",
            f"danh mục {category}" if category else "",
            f"màu: {colors}" if colors else "",
            f"kích cỡ size: {sizes}" if sizes else "",
            f"chất liệu: {attributes}" if attributes else "",
        ]
        final_text = " ".join([x for x in text_parts if x]).strip()

        embedding = model.encode([final_text])[0].astype(np.float32)

        product = {
            "id": pid,
            "description": final_text,
            "brand": brand,
            "category": category,
            "material": None,   # database hiện tại không có material
            "colors": colors,
            "sizes": sizes,
            "attributes": attributes,
            "embedding": embedding,
        }

        insert_product_embedding(conn, product)

    conn.close()
    print("🎉 DONE: All product embeddings stored to PostgreSQL!")


if __name__ == "__main__":
    main()
