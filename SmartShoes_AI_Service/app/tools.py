# app/tools.py
from app.db_connect import get_connection

# ---------------------------
# Recommend products (fallback)
# ---------------------------
def recommend_products():
    # Gợi ý mẫu không giới hạn
    suggestions = [
        "Giày chạy bộ Nike thoáng khí",
        "Giày tennis Asics chống trượt",
        "Giày sneaker Adidas cổ thấp",
        "Giày tập gym Puma đế êm",
    ]
    return {
        "suggestions": suggestions,
        "note": "Đây là gợi ý mẫu vì truy vấn quá chung chung."
    }

# ---------------------------
# Check stock for a list of product_ids
# ---------------------------
def filter_in_stock(products):
    """
    products: list of dicts [{product_id, text, score}]
    ✓ Sản phẩm đang ACTIVE
    Trả về danh sách chỉ sản phẩm còn hàng, thêm field 'stock', 'status'
    """
    conn = get_connection()
    cur = conn.cursor()

    results = []

    for p in products:
        product_id = p["product_id"]

        # 1. Check status trong bảng products
        cur.execute("""
            SELECT status 
            FROM products 
            WHERE id = %s
        """, (product_id,))
        row = cur.fetchone()

        if not row:
            continue  # không tồn tại product
        status = row[0]

        if status != "ACTIVE":
            continue  # chỉ lấy ACTIVE

        # 2. Check tổng tồn kho
        cur.execute("""
            SELECT SUM(stock)
            FROM product_variants
            WHERE product_id = %s
        """, (product_id,))
        stock_row = cur.fetchone()

        stock_qty = stock_row[0] if stock_row and stock_row[0] else 0

        if stock_qty <= 0:
            continue  # hết hàng thì bỏ qua

        # 3. Gán lại vào result
        p["status"] = status
        p["stock"] = int(stock_qty)

        results.append(p)

    conn.close()
    return results
