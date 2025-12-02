import numpy as np
import re
from sentence_transformers import SentenceTransformer, CrossEncoder
from app.db_connect import get_connection


# =====================================================
# LOAD MODELS
# =====================================================
BI_ENCODER_PATH = "models/bi_encoder"
CROSS_ENCODER_PATH = "models/cross_encoder"

bi_encoder = SentenceTransformer(BI_ENCODER_PATH)
cross_encoder = CrossEncoder(CROSS_ENCODER_PATH)


# =====================================================
# SPELL FIX
# =====================================================
SPELL_FIX = {
    "tenis": "tennis",
    "tẹnis": "tennis",
    "giay": "giày",
    "giàyy": "giày",
    "sneker": "sneaker",
    "sniker": "sneaker",
    "tậpgym": "tập gym",
    "đá banh": "đá bóng",
    "đábanh": "đá bóng",
    "đas banh": "đá bóng",
}

def apply_spell_fix_dict(q: str):
    q2 = q.lower()
    for bad, good in SPELL_FIX.items():
        q2 = q2.replace(bad, good)
    return re.sub(r"\s+", " ", q2).strip()


# =====================================================
# KEYWORD BOOST
# =====================================================
KEYWORD_BOOST = {
    # --------------------
    # CATEGORY (quan trọng nhất)
    # --------------------
    "giày chạy bộ": 2.4,
    "chạy bộ": 2.4,
    "giày sneaker": 2.2,
    "sneaker": 2.2,
    "giày đá bóng": 2.4,
    "đá bóng": 2.4,
    "giày leo núi": 2.3,
    "leo núi": 2.3,
    "giày cầu lông": 2.3,
    "cầu lông": 2.3,
    "giày tập gym": 2.2,
    "tập gym": 2.2,
    "giày tennis": 2.4,
    "tennis": 2.4,
    "giày training": 2.2,
    "training": 2.2,

    # --------------------
    # BRAND (cực kỳ quan trọng)
    # --------------------
    "adidas": 2.6,
    "asics": 2.5,
    "converse": 2.4,
    "mizuno": 2.4,
    "nike": 2.8,
    "puma": 2.4,
    "reebok": 2.3,
    "vans": 2.3,
    "new balance": 2.5,
    "nb": 2.5,  # nhiều người gõ tắt
    "under armour": 2.4,
    "ua": 2.4,  # nhiều người gõ tắt

    # --------------------
    # CHẤT LIỆU (rất quan trọng - mới thêm)
    # --------------------
    "da tự nhiên": 2.3,
    "da thật": 2.3,
    "da bò": 2.2,
    "da lộn": 2.1,
    "da tổng hợp": 2.0,
    "da công nghiệp": 2.0,
    "mesh": 2.2,
    "lưới thoáng khí": 2.2,
    "lưới": 2.1,
    "knit": 2.1,
    "vải dệt": 2.0,
    "vải dệt kỹ thuật": 2.1,
    "vải dệt kĩ thuật cao": 2.1,
    "canvas": 2.0,
    "vải canvas": 2.0,
    "vải tổng hợp": 1.9,
    "cao su": 1.8,
    "đế cao su": 1.8,
    
    # --------------------
    # ATTRIBUTE (ưu tiên trung bình)
    # --------------------
    "chống trượt": 1.8,
    "độ bám tốt": 1.7,
    "ôm chân": 1.6,
    "giảm chấn": 1.7,
    "thoáng khí": 1.6,
    "nhẹ": 1.4,
    "nhẹ nhàng": 1.4,
    "bền bỉ": 1.5,
    "thoải mái": 1.5,
    "hỗ trợ cổ chân": 1.6,

    # --------------------
    # COLORS (ưu tiên thấp)
    # --------------------
    "đỏ": 1.1,
    "xanh dương": 1.1,
    "xanh lá": 1.1,
    "vàng": 1.1,
    "đen": 1.1,
    "trắng": 1.1,
    "nâu": 1.1,
    "xám": 1.1,
    "cam": 1.1,
    "be": 1.1,
    "tím": 1.1,
    "hồng": 1.1,
}

def apply_keyword_boost(q: str):
    q2 = q.lower()
    for kw, factor in KEYWORD_BOOST.items():
        if kw in q2:
            repeat = max(1, int(factor))
            q2 = q2 + (" " + kw) * repeat
    return q2.strip()


# =====================================================
# BUILD VOCAB FROM DB (one-time)
# =====================================================
def build_vocab_from_db():
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("SELECT description FROM product_embeddings;")
    rows = cur.fetchall()
    conn.close()

    vocab = set()
    for r in rows:
        for t in re.findall(r"\w+", r[0].lower()):
            vocab.add(t)
    return list(vocab)

vocab_global = None


def apply_token_fuzzy(q: str, vocab, score_cutoff=80, max_tokens=10):
    try:
        from rapidfuzz import process as rf_process
    except ModuleNotFoundError:
        print("⚠️ rapidfuzz not installed, skipping fuzzy correction.")
        return q

    tokens = q.split()[:max_tokens]
    out = []
    for t in tokens:
        if len(t) <= 3:
            out.append(t)
            continue
        best = rf_process.extractOne(t, vocab, score_cutoff=score_cutoff)
        out.append(best[0] if best else t)
    return " ".join(out)


# =====================================================
# FULL PREPROCESS
# =====================================================
def preprocess_query(q: str):
    global vocab_global
    if vocab_global is None:
        vocab_global = build_vocab_from_db()

    q = apply_spell_fix_dict(q)
    q = apply_token_fuzzy(q, vocab_global)
    q = apply_keyword_boost(q)
    return q


# =====================================================
# SEMANTIC SEARCH - PostgreSQL version (no pgvector)
# =====================================================
def semantic_search_db(
    query: str,
    threshold=0.7,
    max_candidates=50,
    fallback_top_k=3,
    rerank=True
):
    # preprocess
    q_proc = preprocess_query(query)

    # encode
    q_vec = bi_encoder.encode([q_proc], convert_to_numpy=True)[0]
    q_vec = q_vec / np.linalg.norm(q_vec)

    # fetch DB results
    conn = get_connection()
    cur = conn.cursor()

    # ⭐ CAST trực tiếp list → vector bằng SQL
    cur.execute("""
        SELECT id, description,
               1 - (embedding <-> %s::vector) AS score
        FROM product_embeddings
        ORDER BY embedding <-> %s::vector
        LIMIT %s;
    """, (q_vec.tolist(), q_vec.tolist(), max_candidates))

    rows = cur.fetchall()
    conn.close()

    candidates = [
        {"product_id": str(r[0]), "text": r[1], "score": float(r[2])}
        for r in rows
    ]

    # threshold filtering
    filtered = [c for c in candidates if c["score"] >= threshold]

    if len(filtered) == 0:
        return {"mode": "fallback", "results": candidates[:fallback_top_k]}

    # cross-encoder rerank
    if rerank:
        pairs = [(query, c["text"]) for c in filtered]
        scores = cross_encoder.predict(pairs)
        for c, s in zip(filtered, scores):
            c["rerank_score"] = float(s)
        filtered = sorted(filtered, key=lambda x: x["rerank_score"], reverse=True)

    return {"mode": "threshold", "results": filtered}
q_proc = preprocess_query("GIAY chạy bộ nike da")
print(q_proc)
