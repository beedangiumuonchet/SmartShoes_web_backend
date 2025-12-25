# ============================================================
# test_semantic_threshold.py
# ============================================================

import psycopg2
from psycopg2.extensions import register_adapter, AsIs
import numpy as np
from sentence_transformers import SentenceTransformer

# ============================================================
# 1️⃣ LOAD BI-ENCODER (ĐÚNG MODEL BẠN ĐÃ TRAIN)
# ============================================================
BI_ENCODER_PATH = "models/bi_encoder"   # chỉnh nếu khác
bi_encoder = SentenceTransformer(BI_ENCODER_PATH)

# ============================================================
# 2️⃣ NUMPY → PGVECTOR ADAPTER
# ============================================================
def adapt_numpy_float32(numpy_float32):
    return AsIs(float(numpy_float32))

def adapt_numpy_array(numpy_array):
    vector_str = ",".join(str(float(x)) for x in numpy_array)
    return AsIs("'" + "{" + vector_str + "}" + "'")

register_adapter(np.float32, adapt_numpy_float32)
register_adapter(np.ndarray, adapt_numpy_array)

# ============================================================
# 3️⃣ POSTGRES CONNECTION
# ============================================================
def get_connection():
    return psycopg2.connect(
        host="localhost",
        port=5432,
        dbname="data",
        user="postgres",
        password="123456",
    )

# ============================================================
# 4️⃣ DANH SÁCH QUERY TEST
# ============================================================
TEST_QUERIES = [
    "Tôi muốn mua giày tennis tông xanh form ôm chân",
    "Tôi cần giày tennis màu đỏ chống trơn trượt của Puma",
    "Tôi tìm giày chạy bộ màu đỏ hỗ trợ cổ chân",
    "Giày đá bóng màu xám bền chắc của Converse",
    "Giày đá bóng màu vàng đế cao su của Adidas",
    "Giày training màu đen phong cách trẻ trung",
    "Reebok phiên bản Giày tennis cam độ bền độ bám tốt",
    "Vans Giày cầu lông kiểu dáng năng động",
    "Tìm Giày leo núi màu xám size 38",
    "Giày chạy bộ màu nâu phong cách mạnh mẽ size 41",
    "Giày sneaker Reebok sắc nâu kháng nước",
    "Giày cầu lông xanh với độ bền bền bỉ size 39",
    "Giày đá bóng chống trượt tốt",
    "Giày tập gym Mizuno sắc be phù hợp thi đấu, size 43",
    "Giày tập gym của Nike tông xanh navy, form ôm chân",
    "Giày màu vàng để thi đấu",
    "Giày đá bóng Asics",
    "Giày sneaker, phù hợp cho người mới",
    "Giày tập gym màu đỏ, thiết kế thoải mái",
    "Giày training",
    "Giày chạy bộ sắc nâu phù hợp dùng hàng ngày",
    "Giày training đỏ với độ bền đệm êm",
    "Giày đá bóng màu vàng size 37",
    "Giày cầu lông màu trắng",
    "Tìm giày sneaker form nhẹ màu trắng",
    "Tìm Giày training của Adidas màu xanh",
    "Tìm Giày đá bóng của Adidas tông xanh navy",
    "Giày đá bóng Adidas sắc be, thiết kế chuyên dụng",
    "Giày sneaker màu cam của Puma",
    "Giày đá bóng Puma phù hợp tập gym buổi sáng, size 43",
    "Tìm giày leo núi size 39",
    "Tìm Giày cầu lông chống trượt size 41",
    "Giày training Under Armour màu sáng đẹp",
]

# ============================================================
# 5️⃣ SEMANTIC SEARCH (COSINE SIMILARITY)
# ============================================================
def semantic_search(query, top_k=1):
    q_vec = bi_encoder.encode([query], convert_to_numpy=True)[0]
    q_vec = q_vec / np.linalg.norm(q_vec)

    conn = get_connection()
    cur = conn.cursor()

    cur.execute("""
        SELECT id,
               1 - (embedding <=> %s::vector) AS score
        FROM product_embeddings
        ORDER BY embedding <=> %s::vector
        LIMIT %s;
    """, (q_vec.tolist(), q_vec.tolist(), top_k))

    rows = cur.fetchall()
    conn.close()

    return rows

# ============================================================
# 6️⃣ TEST & TÍNH NGƯỠNG TRUNG BÌNH
# ============================================================
def run_test():
    scores = []

    print("\n========= SEMANTIC SEARCH TEST =========\n")

    for idx, q in enumerate(TEST_QUERIES, start=1):
        result = semantic_search(q, top_k=1)

        if result:
            product_id, score = result[0]
            scores.append(score)

            print(f"{idx:02d}. Query: {q}")
            print(f"    → Top product: {product_id}")
            print(f"    → Cosine score: {score:.4f}\n")
        else:
            print(f"{idx:02d}. Query: {q}")
            print("    → No result\n")

    print("======================================")
    print(f"Total queries: {len(scores)}")
    print(f"Average score: {sum(scores)/len(scores):.4f}")
    print(f"Min score:     {min(scores):.4f}")
    print(f"Max score:     {max(scores):.4f}")
    print("======================================")

# ============================================================
# 7️⃣ MAIN
# ============================================================
if __name__ == "__main__":
    run_test()
