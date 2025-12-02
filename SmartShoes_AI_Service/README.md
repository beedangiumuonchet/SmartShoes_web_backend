1. Khởi tạo môi trường ảo

Khởi tạo môi trường ảo lần đầu tiên khi chạy chương trình, nếu đã chạy trước đó bỏ qua bước này.
```bash
python -m venv venv
```

2. Activate môi trường ảo

```bash
./venv/Scripts/activate
```
3. Cập nhật phiên bản pip
```bash
python -m pip install --upgrade pip
```
4. Cài đặt thư viện
```bash
pip install -r requirements.txt
```
5. Biển dữ liệu thành embeding (chỉ chạy khi lần đầu hoặc thêm sản phẩm mới)
```bash
python -m app.ingest
```
6. Chạy lúc search AI
```bash
uvicorn app.main:app --reload
```