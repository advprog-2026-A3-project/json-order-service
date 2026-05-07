# API Endpoints

Dokumen ini dirangkum dari controller di codebase (`src/main/java/id/ac/ui/cs/advprog/order/controller`).

## REST Endpoints (JSON API)

| Controller | Method | Path | Body | Response | Fungsi/Deskripsi |
| --- | --- | --- | --- | --- | --- |
| OrderController | POST | /api/v1/orders | `OrderCreateRequest` (JSON) | `Order` (JSON) | Membuat order baru |
| OrderController | GET | /api/v1/orders | - | `List<Order>` (JSON) | Ambil semua order |
| OrderController | GET | /api/v1/orders/{id} | - | `Order` (JSON) | Ambil order berdasarkan id |
| OrderController | PUT | /api/v1/orders/{id} | `OrderCreateRequest` (JSON) | `Order` (JSON) | Update order |
| OrderController | PATCH | /api/v1/orders/{id}/status | - (query `status`) | `Order` (JSON) | Update status order |
| OrderController | POST | /api/v1/orders/{id}/cancel | - | `Order` (JSON) | Batalkan order |
| RatingController | POST | /api/v1/orders/{id}/rating | `RatingCreateRequest` (JSON) | `Rating` (JSON) | Simpan rating untuk order |
| OrderRestController | POST | /api/v1/orders/checkout | `CheckoutRequest` (JSON) | `CheckoutResponse` (JSON) | Checkout order |
