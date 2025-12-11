# ================================
# embed_service.py
# Microservice for product embeddings
# ================================

from fastapi import FastAPI
from pydantic import BaseModel
import numpy as np
from sentence_transformers import SentenceTransformer
from app.db_connect import get_connection, setup_database
from app.utils import clean_text

BI_ENCODER_PATH = "models/bi_encoder"

app = FastAPI(title="Product Embedding Service")

# Load encoder once
print("🧠 Loading bi-encoder...")
model = SentenceTransformer(BI_ENCODER_PATH)


# ============================
# INSERT or UPDATE embedding
# ============================
def insert_product_embedding(conn, product):
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
            product.get("color"),
            product.get("attributes"),
            embedding_list
        ))
    conn.commit()


# ============================
# QUERY RAW PRODUCT DATA
# ============================
def fetch_all_products():
    conn = get_connection()
    cur = conn.cursor()

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
    return rows


# ============================
# API 1: Update all embeddings
# ============================
@app.post("/update-all")
def update_all_embeddings():

    setup_database()

    rows = fetch_all_products()
    print(f"📦 Loaded {len(rows)} products")

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
            "material": None,
            "color": colors,
            "attributes": attributes,
            "embedding": embedding,
        }

        insert_product_embedding(conn, product)

    conn.close()

    return {"status": "success", "message": "All product embeddings updated!"}


# ============================
# API 2: Delete embedding
# ============================
class DeleteRequest(BaseModel):
    product_id: str # UUID dạng string


@app.delete("/delete")
def delete_embedding(req: DeleteRequest):

    conn = get_connection()
    with conn.cursor() as cur:
        cur.execute("DELETE FROM product_embeddings WHERE id = %s", (req.product_id,))
    conn.commit()
    conn.close()

    return {"status": "success", "message": f"Deleted embedding for product {req.product_id}"}
