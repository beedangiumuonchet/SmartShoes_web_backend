# main.py
from fastapi import FastAPI
from pydantic import BaseModel
from app.mcp import handle_query
import traceback
from fastapi.responses import JSONResponse

app = FastAPI(title="Semantic Search Shoes API")

class SearchRequest(BaseModel):
    query: str
    threshold: float = 0.7
    rerank: bool = True
    fallback_top_k: int = 3

@app.post("/search")
def ai_search(req: SearchRequest):
    try:
        results = handle_query(
            query=req.query,
            threshold=req.threshold,
            rerank=req.rerank,
            fallback_top_k=req.fallback_top_k
        )
        return results
    except Exception as e:
        return JSONResponse(
            status_code=500,
            content={
                "error": str(e),
                "trace": traceback.format_exc()
            }
        )

@app.get("/")
def root():
    return {"message": "Semantic Search Shoes API with MCP is running!"}
