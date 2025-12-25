import torch
import torch.nn as nn
from torchvision import transforms
from PIL import Image
from scipy.spatial.distance import euclidean
import numpy as np

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# ============================
#   MODEL (Encoder)
# ============================
class ConvAutoencoder_v2(nn.Module):
    def __init__(self):
        super(ConvAutoencoder_v2, self).__init__()
        self.encoder = nn.Sequential(
            nn.Conv2d(3, 64, 3, stride=1, padding=1),
            nn.ReLU(True),
            nn.Conv2d(64, 64, 3, stride=1, padding=1),
            nn.ReLU(True),
            nn.MaxPool2d(2, stride=2),

            nn.Conv2d(64, 128, 3, stride=2, padding=1),
            nn.ReLU(True),
            nn.Conv2d(128, 128, 3, stride=1, padding=0),
            nn.ReLU(True),
            nn.MaxPool2d(2, stride=2),

            nn.Conv2d(128, 256, 3, stride=2, padding=1),
            nn.ReLU(True),
            nn.Conv2d(256, 256, 3, stride=1, padding=1),
            nn.ReLU(True),
            nn.Conv2d(256, 256, 3, stride=1, padding=1),
            nn.ReLU(True),
            nn.MaxPool2d(2, stride=2)
        )

    def forward(self, x):
        return self.encoder(x)

# ============================
#   LOAD MODEL
# ============================
def load_model(model_path):
    model = ConvAutoencoder_v2().to(device)
    checkpoint = torch.load(model_path, map_location=device)

    if "model_state_dict" in checkpoint:
        model.load_state_dict(checkpoint["model_state_dict"], strict=False)
        print("⚠️ Loaded model with non-strict mode (decoder weights ignored)")

    else:
        model.load_state_dict(checkpoint)

    model.eval()
    return model

# ============================
#   TRANSFORM ẢNH
# ============================
transformations = transforms.Compose([
    transforms.Resize((256, 256)),
    transforms.ToTensor(),
    transforms.Normalize((0.5, 0.5, 0.5), (0.5, 0.5, 0.5))
])

# ============================
#   EXTRACT FEATURE – MULTIPLE
# ============================
def get_latent_features(image_paths, model, transformations):
    features_list = []

    for path in image_paths:
        try:
            img = Image.open(path).convert("RGB")
            tensor = transformations(img).unsqueeze(0).to(device)

            with torch.no_grad():
                latent = model.encoder(tensor)

            latent_np = latent.cpu().numpy().astype(np.float32).squeeze(0).flatten()
            features_list.append(latent_np)

        except Exception as e:
            print(f"⚠️ Lỗi ảnh: {path} ({e})")

    return np.stack(features_list, axis=0) if features_list else None

# ============================
#   EXTRACT FEATURE – SINGLE
# ============================
def get_latent_features_img(image_path, model, transformations):
    try:
        img = Image.open(image_path).convert("RGB")
        tensor = transformations(img).unsqueeze(0).to(device)

        with torch.no_grad():
            latent = model.encoder(tensor)
            pooled = torch.mean(latent, dim=[2, 3])  # Global Average Pool
        return pooled.cpu().numpy().astype(np.float32).flatten()


    except Exception as e:
        print(f"⚠️ Lỗi ảnh: {image_path} ({e})")
        return None

def perform_search(queryFeatures, db_features, threshold=0.8):
    """
    Tìm các ảnh tương tự dựa trên Euclidean distance.
    - queryFeatures: numpy array của ảnh truy vấn
    - db_features: list of dict với keys: id, imageUrl, productVariantId, featureVector
    - threshold: similarity tối thiểu để trả về
    """
    results = []

    if not db_features:
        return results

    distances = []
    for i, item in enumerate(db_features):
        d = euclidean(queryFeatures, item["featureVector"])
        distances.append((float(d), i))

    max_d = max(d for d, _ in distances) or 1.0  # tránh chia 0

    for d, i in distances:
        sim = 1 - (d / max_d) if max_d != 0 else 0.0
        feat = db_features[i]

        
        if sim >= threshold:
            results.append({
                "similarity": float(sim),
                "id": feat.get("id"),
                "imagePath": feat.get("imagePath"),
                "productVariantId": feat.get("productVariantId")
            })
            print(f"🔍 ID: {feat.get('id')}, Distance: {d:.4f}, Similarity: {sim:.4f}")

    # Sắp xếp giảm dần theo similarity
    results = sorted(results, key=lambda x: x["similarity"], reverse=True)
    return results