import ast
import numpy as np
import psycopg2
import psycopg2.extras

def load_features_from_db():
    conn = psycopg2.connect(
        host="localhost",
        port=5432,
        user="postgres",
        password="2211",
        database="shoesstore_db"
    )

    cursor = conn.cursor(cursor_factory=psycopg2.extras.DictCursor)
    cursor.execute("""
        SELECT id, url, embedding, product_variant_id
        FROM product_image
    """)

    features = []

    for row in cursor.fetchall():
        emb = row["embedding"]

        if emb is None:
            continue
        
        # Nếu embedding dạng text → parse
        if isinstance(emb, str):
            emb = np.array(ast.literal_eval(emb), dtype=np.float32)
        else:
            # Nếu embedding là Postgres float[]
            emb = np.array(emb, dtype=np.float32)

        features.append({
            "id": row["id"],
            "imagePath": row["url"],
            "productVariantId": row["product_variant_id"],
            "featureVector": emb
        })

    cursor.close()
    conn.close()
    return features
