from app.search_engine import semantic_search

query = "giày tenis xanh"
result = semantic_search(query=query, threshold=0.7, max_candidates=50, rerank=True)
print(result)
