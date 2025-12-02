import os
import json
import numpy as np
import faiss
import pandas as pd
import re
from sentence_transformers import SentenceTransformer, CrossEncoder

# ---------------------------------------------------------
# Paths
# ---------------------------------------------------------
BI_ENCODER_PATH = "models/bi_encoder"
CROSS_ENCODER_PATH = "models/cross_encoder"
CSV_PATH = "data/dataset_giay_2000_material_added.csv"
EMB_PATH = "embeddings/product_embeddings.npy"
ID_MAP_PATH = "embeddings/id_map.json"

# ---------------------------------------------------------
# Load CSV
# ---------------------------------------------------------
df = pd.read_csv(CSV_PATH)
df.columns = [c.strip().lower().replace(" ", "").replace("-", "") for c in df.columns]

products = []
for _, row in df.iterrows():
    pid = str(row["productid"]).strip()
    desc = str(row["descriptionproduct"]).strip()
    products.append({"id": pid, "description": desc})

product_texts = {p["id"]: p["description"] for p in products}

# ---------------------------------------------------------
# Load models
# ---------------------------------------------------------
bi_encoder = SentenceTransformer(BI_ENCODER_PATH)
cross_encoder = CrossEncoder(CROSS_ENCODER_PATH)

# ---------------------------------------------------------
# Load or build FAISS index
# ---------------------------------------------------------
if os.path.exists(EMB_PATH) and os.path.exists(ID_MAP_PATH):
    embeddings = np.load(EMB_PATH)
    with open(ID_MAP_PATH, "r", encoding="utf-8") as f:
        id_map = json.load(f)
else:
    corpus = [p["description"] for p in products]
    embeddings = bi_encoder.encode(corpus, convert_to_numpy=True, show_progress_bar=True)
    embeddings = embeddings / np.linalg.norm(embeddings, axis=1, keepdims=True)

    id_map = {i: products[i]["id"] for i in range(len(products))}
    os.makedirs("embeddings", exist_ok=True)
    np.save(EMB_PATH, embeddings)
    with open(ID_MAP_PATH, "w", encoding="utf-8") as f:
        json.dump(id_map, f, ensure_ascii=False, indent=2)

dim = embeddings.shape[1]
index = faiss.IndexFlatIP(dim)
index.add(embeddings)

# ----------------------------- # Spell fix + fuzzy token # ----------------------------- 
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
    q2 = re.sub(r"\s+", " ", q2).strip()
    return q2

def build_vocab_from_texts(texts):
    vocab = set()
    for v in texts.values():
        for t in re.findall(r"\w+", v.lower()):
            vocab.add(t)
    return list(vocab)

vocab_global = build_vocab_from_texts(product_texts)

def apply_token_fuzzy(q: str, vocab, score_cutoff=80, max_tokens=10):
    try:
        from rapidfuzz import process as rf_process
    except ModuleNotFoundError:
        # rapidfuzz không cài, fallback: trả query gốc
        print("⚠️ rapidfuzz not installed, skipping fuzzy token correction.")
        return q

    tokens = q.split()[:max_tokens]  # chỉ lấy 10 token đầu
    out = []
    for t in tokens:
        if len(t) <= 3:
            out.append(t)
            continue
        best = rf_process.extractOne(t, vocab, score_cutoff=score_cutoff)
        out.append(best[0] if best else t)
    return " ".join(out)

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
    "xanh": 1.1,
    "vàng": 1.1,
    "đen": 1.1,
    "trắng": 1.1,
    "nâu": 1.1,
    "xám": 1.1,
    "cam": 1.1,
    "be": 1.1,
}

def apply_keyword_boost(q: str):
    """
    Lặp lại các keyword quan trọng để bộ bi-encoder hiểu trọng tâm tốt hơn.
    Ví dụ: 'đá bóng' +2.2 → câu query sẽ được append thêm 'đá bóng đá bóng'
    """
    q2 = q.lower()
    for kw, factor in KEYWORD_BOOST.items():
        if kw in q2:
            repeat = max(1, int(factor))     # nhân số lần
            boost_part = (" " + kw) * repeat
            q2 = q2 + boost_part
    return q2.strip()

def preprocess_query(q: str):
    q = apply_spell_fix_dict(q)
    q = apply_token_fuzzy(q, vocab_global)
    q = apply_keyword_boost(q)
    return q

# ---------------------------------------------------------
# SMART SEARCH FUNCTION
# ---------------------------------------------------------
def semantic_search(query: str, threshold=0.7, max_candidates=50, rerank=True, fallback_top_k=3):
    print(embeddings.mean(), embeddings.std())

    # preprocess query
    q_proc = preprocess_query(query)

    # encode query
    q_vec = bi_encoder.encode([q_proc], convert_to_numpy=True)
    q_vec = q_vec / np.linalg.norm(q_vec)

    # FAISS search
    D, I = index.search(q_vec, max_candidates)

    candidates = []
    for score, idx in zip(D[0], I[0]):
        pid = id_map[str(idx)] if str(idx) in id_map else id_map[idx]
        candidates.append({
            "product_id": pid,
            "text": product_texts[pid],
            "score": float(score)
        })

    # filter by threshold
    filtered = [c for c in candidates if c["score"] >= threshold]

    # fallback top-k
    if len(filtered) == 0:
        fallback = candidates[:fallback_top_k]
        return {"mode": "fallback", "results": fallback}

    # rerank with cross encoder
    if rerank and cross_encoder is not None:
        pairs = [(query, c["text"]) for c in filtered]
        scores = cross_encoder.predict(pairs)
        for c, s in zip(filtered, scores):
            c["rerank_score"] = float(s)
        filtered = sorted(filtered, key=lambda x: x["rerank_score"], reverse=True)

    return {"mode": "threshold", "results": filtered}