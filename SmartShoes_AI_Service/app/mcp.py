# app/mcp.py
from app.emantic_search_db import semantic_search_db
from app.tools import recommend_products, filter_in_stock

def handle_query(query: str, threshold=0.7, rerank=True, fallback_top_k=3):
    search_result = semantic_search_db(
        query=query,
        threshold=threshold,
        rerank=rerank,
        fallback_top_k=fallback_top_k
    )

    # Luôn lọc stock, bất kể fallback hay search
    filtered = filter_in_stock(search_result["results"])

    # Nếu không còn sản phẩm nào sau khi filter
    if not filtered:
        return {
            "mode": "empty",
            "results": [],
            "suggestions": recommend_products()["suggestions"],
            "note": "Không tìm thấy sản phẩm còn hàng. Gợi ý mẫu được trả về."
        }

    # Nếu fallback
    if search_result["mode"] == "fallback":
        return {
            "mode": "fallback",
            "results": filtered,      # fallback nhưng đã có stock, status
            "suggestions": recommend_products()["suggestions"]
        }

    # Nếu search
    return {
        "mode": "search",
        "results": filtered
    }

