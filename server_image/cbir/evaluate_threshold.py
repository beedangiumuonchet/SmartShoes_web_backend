import numpy as np
from cbir.service import perform_search

def evaluate_threshold(
    queries,           # list: {"feature": np.array, "variantId": int}
    db_features,       # list DB embeddings
    thresholds=np.arange(0.5, 0.91, 0.05)
):
    """
    Đánh giá precision, recall, F1-score với các ngưỡng similarity khác nhau.
    - queries: list embedding + variantId làm ground truth
    - db_features: list embedding từ DB
    - thresholds: mảng ngưỡng để thử
    """
    results = []

    for t in thresholds:
        TP = FP = FN = 0

        for q in queries:
            gt_variant = q["variantId"]

            # Lấy kết quả search với threshold t
            retrieved = perform_search(
                q["feature"],
                db_features,
                threshold=t
            )

            # Các variantId trong kết quả search
            retrieved_variants = {item["productVariantId"] for item in retrieved}

            # Tính TP, FN, FP
            if gt_variant in retrieved_variants:
                TP += 1
            else:
                FN += 1

            FP += len(retrieved_variants - {gt_variant})

        # Tính precision, recall, F1
        precision = TP / (TP + FP) if (TP + FP) > 0 else 0
        recall = TP / (TP + FN) if (TP + FN) > 0 else 0
        f1 = 2 * precision * recall / (precision + recall) if (precision + recall) > 0 else 0

        results.append({
            "threshold": round(t, 2),
            "precision": round(precision, 3),
            "recall": round(recall, 3),
            "f1": round(f1, 3)
        })

    return results
