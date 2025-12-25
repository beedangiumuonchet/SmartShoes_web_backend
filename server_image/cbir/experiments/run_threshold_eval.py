from cbir.export_features import load_features_from_db
from cbir.evaluate_threshold import evaluate_threshold

# 1️⃣ Load embedding DB
db_features = load_features_from_db()
print(f"✅ Loaded {len(db_features)} embeddings from DB")

# 2️⃣ Tạo tập query (ground truth) - lấy mẫu 50 ảnh đầu
queries = [
    {
        "feature": item["featureVector"],
        "variantId": item["productVariantId"]
    }
    for item in db_features[:50]
]

# 3️⃣ Chạy đánh giá threshold
results = evaluate_threshold(queries, db_features)

# 4️⃣ In kết quả
print("Threshold evaluation results:")
for r in results:
    print(r)
