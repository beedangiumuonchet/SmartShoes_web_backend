import re
import numpy as np
import unidecode
from rapidfuzz import process as rf_process
from sentence_transformers import SentenceTransformer, CrossEncoder
from app.db_connect import get_connection


# ============================================================
# LOAD MODELS
# ============================================================
BI_ENCODER_PATH = "models/bi_encoder"
CROSS_ENCODER_PATH = "models/cross_encoder"

bi_encoder = SentenceTransformer(BI_ENCODER_PATH)
cross_encoder = CrossEncoder(CROSS_ENCODER_PATH)


# ============================================================
# SPELL FIX (regex chuẩn, không replace bậy)
# ============================================================
SPELL_FIX = {
    "giay": "giày",
    "giayf": "giày",
    "giayj": "giày",
    "tenis": "tennis",
    "tẹnis": "tennis",
    "sneker": "sneaker",
    "sniker": "sneaker",
    "đá banh": "đá bóng",
}

def apply_spell_fix(q: str) -> str:
    q2 = q.lower()
    for bad, good in SPELL_FIX.items():
        # chỉ thay thế khi trùng nguyên từ
        pattern = rf"\b{re.escape(bad)}\b"
        q2 = re.sub(pattern, good, q2)
    return q2.strip()


# ============================================================
# KEYWORD BOOST (ưu tiên brand → category → chất liệu)
# ============================================================
KEYWORD_BOOST = {
    # BRAND
    "nike": 2.5, "adidas": 2.4, "vans": 2.2, "mizuno": 2.2,
    "puma": 2.1, "new balance": 2.2, "nb": 2.2,

    # CATEGORY
    "giày chạy bộ": 2.4, "chạy bộ": 2.4,
    "giày đá bóng": 2.4, "đá bóng": 2.4,
    "sneaker": 2.2, "giày sneaker": 2.2,
    "gym": 2.0, "giày tập gym": 2.0,

    # MATERIAL
    "da": 2.0, "da bò": 2.2, "da lộn": 2.0, "mesh": 1.8,
    "lưới": 1.5, "knit": 1.5, "canvas": 1.4,

    # ATTRIBUTE
    "thoáng khí": 1.4, "nhẹ": 1.3, "chống trượt": 1.5,
}

def apply_keyword_boost(q: str) -> str:
    q2 = q.lower()
    for kw, score in KEYWORD_BOOST.items():
        if kw in q2:
            repeat = max(1, int(score))
            q2 += (" " + kw) * repeat
    return q2.strip()


# ============================================================
# FUZZY FIX – sửa token sai gần đúng
# ============================================================
def apply_fuzzy_fix(q: str, vocab, cutoff=82, max_tokens=12):
    tokens = q.split()[:max_tokens]
    out = []

    for t in tokens:
        if len(t) <= 3:  
            out.append(t)
            continue

        best = rf_process.extractOne(t, vocab, score_cutoff=cutoff)
        out.append(best[0] if best else t)

    return " ".join(out)


# ============================================================
# BUILD VOCAB – từ toàn bộ mô tả sản phẩm
# ============================================================
vocab_global = None

def build_vocab_from_db():
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("SELECT description FROM product_embeddings;")
    rows = cur.fetchall()
    conn.close()

    vocab = set()
    for (desc,) in rows:
        for w in re.findall(r"\w+", desc.lower()):
            vocab.add(w)
    return list(vocab)


# ============================================================
# CLEAN TEXT – remove dấu, ký tự lạ, normalize
# ============================================================
def clean_text(q: str) -> str:
    q = q.lower().strip()
    q = unidecode.unidecode(q)  # remove accents tiếng Việt
    q = re.sub(r"[^a-z0-9\s]", " ", q)
    q = re.sub(r"\s+", " ", q)
    return q.strip()


# ============================================================
# FULL PREPROCESS PIPELINE
# ============================================================
def preprocess_query(q: str) -> str:
    global vocab_global

    if vocab_global is None:
        vocab_global = build_vocab_from_db()

    q = clean_text(q)
    q = apply_spell_fix(q)
    q = apply_fuzzy_fix(q, vocab_global)
    q = apply_keyword_boost(q)

    # chuẩn hoá cuối
    q = re.sub(r"\s+", " ", q).strip()
    return q


# ============================================================
# SEMANTIC SEARCH – pgvector cosine (<=>)
# ============================================================
def semantic_search_db(
    query: str,
    threshold=0.65,
    max_candidates=40,
    fallback_top_k=3,
    rerank=True,
):
    # -----------------------------------------------
    # 1) PREPROCESS QUERY
    # -----------------------------------------------
    q_proc = preprocess_query(query)

    # -----------------------------------------------
    # 2) GET QUERY VECTOR
    # -----------------------------------------------
    q_vec = bi_encoder.encode([q_proc], convert_to_numpy=True)[0]
    q_vec = q_vec / np.linalg.norm(q_vec)

    # -----------------------------------------------
    # 3) QUERY DATABASE USING COSINE (<=>)
    # -----------------------------------------------
    conn = get_connection()
    cur = conn.cursor()

    cur.execute("""
        SELECT id, description,
               1 - (embedding <=> %s::vector) AS score
        FROM product_embeddings
        ORDER BY embedding <=> %s::vector
        LIMIT %s;
    """, (q_vec.tolist(), q_vec.tolist(), max_candidates))

    rows = cur.fetchall()
    conn.close()

    candidates = [
        {"product_id": r[0], "text": r[1], "score": float(r[2])}
        for r in rows
    ]

    # -----------------------------------------------
    # 4) FILTER CANDIDATES
    # -----------------------------------------------
    filtered = [c for c in candidates if c["score"] >= threshold]

    if not filtered:
        return {
            "mode": "fallback",
            "results": candidates[:fallback_top_k]
        }

    # -----------------------------------------------
    # 5) CROSS-ENCODER RERANK (TOP 10)
    # -----------------------------------------------
    if rerank:
        top_items = filtered[:10]
        pairs = [(query, item["text"]) for item in top_items]
        scores = cross_encoder.predict(pairs)

        for item, s in zip(top_items, scores):
            item["rerank_score"] = float(s)

        top_items = sorted(top_items, key=lambda x: x["rerank_score"], reverse=True)

        return {"mode": "semantic", "results": top_items}

    return {"mode": "semantic", "results": filtered}
