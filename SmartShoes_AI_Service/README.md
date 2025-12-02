1. Cài đặt thư viện
  pip install -r requirements.txt
2. Biển dữ liệu thành embeding (chỉ chạy khi lần đầu hoặc thêm sản phẩm mới)
  python -m app.ingest
3. Chạy lúc search AI
  uvicorn app.main:app --reload