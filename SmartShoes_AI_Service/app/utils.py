import re
import unicodedata

# -----------------------------
# CLEAN TEXT
# -----------------------------
def clean_text(text: str):
    if not isinstance(text, str):
        return ""
    text = unicodedata.normalize("NFC", text.lower().strip())
    return re.sub(r"\s+", " ", text)

# -----------------------------
# SPELL FIX + FUZZY + KEYWORD_BOOST
# -----------------------------
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

def preprocess_query(q: str, vocab_global=None):
    q = apply_spell_fix_dict(q)
    if vocab_global:
        q = apply_token_fuzzy(q, vocab_global)
    q = apply_keyword_boost(q)
    return q
