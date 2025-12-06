import os
import tempfile
import base64
import pickle
import numpy as np
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

# from cbir.extract_img import extract_images_from_pdf
from cbir.export_features import load_features_from_db  # file bạn tự viết
from cbir.service import load_model, get_latent_features_img, transformations, perform_search


app = Flask(__name__)

# ---- Load trained embedding model ----
MODEL_PATH = "cbir/conv_autoencoderv2_200ep_3.pt"
model = load_model(MODEL_PATH)

# ---- Load DB features into RAM ----
db_features = load_features_from_db()
print(f"✅ Loaded {len(db_features)} image embeddings")


# =======================
# 📌 Extract images from PDF + get features
# =======================
@app.route("/extract-features", methods=["POST"])
def extract_features_api():
    if "file" not in request.files:
        return jsonify({"error": "No file uploaded"}), 400

    file = request.files["file"]
    filename = secure_filename(file.filename)

    # Lưu tạm file để model đọc
    temp_dir = tempfile.gettempdir()
    temp_path = os.path.join(temp_dir, filename)
    file.save(temp_path)

    try:
        # Trích xuất embedding trực tiếp từ ảnh gửi lên
        vec = get_latent_features_img(temp_path, model, transformations)
        if vec is None:
            return jsonify({"error": "Failed to extract embedding"}), 500

        # Trả về JSON giống frontend/Java client mong đợi
        result = {
            "images": [
                {
                    "filename": filename,
                    "url": "",          # URL có thể bỏ trống hoặc upload sau
                    "features": vec.tolist()
                }
            ]
        }

        return jsonify(result)

    finally:
        # Xoá file tạm
        if os.path.exists(temp_path):
            os.remove(temp_path)



# =======================
# 🔍 Search similar images
# =======================
@app.route("/search-image", methods=["POST"])
def search_image_api():
    if "file" not in request.files:
        return jsonify({"error": "No file uploaded"}), 400

    file = request.files["file"]

    with tempfile.NamedTemporaryFile(delete=False, suffix=".jpg") as tmp:
        file.save(tmp.name)

    try:
        query_features = get_latent_features_img(tmp.name, model, transformations)
        if query_features is None:
            return jsonify({"error": "Failed to extract embedding"}), 500

        results = perform_search(query_features, db_features)

        return jsonify({"results": results})

    finally:
        if os.path.exists(tmp.name):
            os.remove(tmp.name)


# =======================
# ♻️ Reload features from DB without restarting server
# =======================
# @app.route("/refresh-features", methods=["POST"])
# def refresh_features():
#         global db_features
#         db_features = load_features_from_db()
#         return jsonify({"message": f"Reloaded {len(db_features)} features"})


# =======================
# ➕ Add new features without DB
# =======================
@app.route("/add-features", methods=["POST"])
def add_features():
    global db_features
    data = request.get_json()
    # print("📥 Received JSON:", data)

    if not data or "items" not in data:
        return jsonify({"error": "Missing 'items'"}), 400

    count = 0
    for item in data["items"]:
        try:
            # Lấy thông tin từ JSON
            # print(f"➡️ Processing item {i}: {item}")
            feature_vec = np.array(item["featureVector"], dtype=np.float32)
            
            # Tạo object tương tự ProductImage
            db_features.append({
                "id": item["id"],                        # ID giống ProductImage.id
                "imagePath": item.get("imagePath"),                      # URL ảnh đã upload
                "productVariantId": item.get("variantId"), # liên kết variant
                "featureVector": feature_vec                 # embedding vector
            })
            count += 1
        except Exception as e:
            print("⚠️ Item gây lỗi:", item)
            print("⚠️ Keys trong item:", item.keys())
            print("❌ Error adding feature:", e)

    # print(f"✅ Total features in memory: {len(db_features)}")
    return jsonify({
        "message": f"Added {count} features",
        "total": len(db_features)
    })

# =======================
# ❌ Delete feature by image ID
# =======================
# @app.route("/delete-feature/<image_id>", methods=["DELETE"])
# def delete_feature(image_id):
#     global db_features

#     # Tìm các index có id trùng
#     to_remove = [i for i, item in enumerate(db_features) if str(item["id"]) == str(image_id)]

#     if not to_remove:
#         return jsonify({"message": "Feature not found"}), 404

#     # Xóa từng item theo index (ngược để tránh bị lệch)
#     for idx in reversed(to_remove):
#         del db_features[idx]

#     return jsonify({
#         "message": "Feature removed",
#         "removed": len(to_remove),
#         "total": len(db_features)
#     }), 200


if __name__ == "__main__":
    app.run(debug=False)
