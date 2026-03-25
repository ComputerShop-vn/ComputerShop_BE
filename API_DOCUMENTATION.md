# 📘 API Documentation — Computer Shop SBA301

> **Base URL:** `http://localhost:8080/api/v1`  
> **Content-Type mặc định:** `application/json`  
> **Authentication:** `Authorization: Bearer <token>`

---

## 📦 Cấu trúc Response chung

```json
{
  "code": 1000,
  "message": null,
  "result": { ... }
}
```

| Field | Type | Mô tả |
|-------|------|--------|
| `code` | int | `1000` = thành công |
| `message` | string\|null | Thông báo lỗi, null nếu thành công |
| `result` | any\|null | Data trả về, null nếu lỗi hoặc void |

---

## 📋 Bảng mã lỗi

| Code | HTTP | Ý nghĩa |
|------|------|---------|
| 1001 | 400 | Email đã tồn tại khi đăng ký |
| 1002 | 404 | Role không tồn tại |
| 1003 | 401 | Sai email/password, token hết hạn, token đã logout |
| 1004 | 403 | Không có quyền truy cập |
| 1005 | 400 | Invalid key |
| 1007 | 400 | Password không hợp lệ |
| 1008 | 400 | Malformed JSON body |
| 1010 | 404 | User không tồn tại |
| 1011 | 404 | Category không tồn tại |
| 1012 | 400 | Role đã tồn tại |
| 1013 | 400 | Category không thể là parent của chính nó |
| 1014 | 400 | Không thể xoá category có sub-category |
| 2001 | 404 | Blog không tồn tại |
| 2002 | 400 | Blog title đã tồn tại |
| 3001 | 404 | Promotion không tồn tại |
| 3002 | 400 | Promo code đã tồn tại |
| 4001 | 404 | Attribute không tồn tại |
| 5001 | 404 | Brand không tồn tại |
| 6001 | 404 | Product không tồn tại |
| 6101 | 404 | Variant không tồn tại |
| 6102 | 400 | SKU đã tồn tại |
| 7003 | 400 | File vượt quá 10MB |
| 8001 | 404 | Cart không tồn tại |
| 8002 | 404 | Cart item không tồn tại hoặc không thuộc user |
| 8004 | 400 | Quantity phải > 0 |
| 9001 | 404 | Order không tồn tại / không có quyền huỷ |
| 9002 | 400 | Giỏ hàng rỗng |
| 9003 | 400 | Không đủ hàng trong kho |
| 9999  | 500 | Lỗi server không xác định |
| 10001 | 404 | PC build không tồn tại |
| 10003 | 400 | PC build chưa có item nào (không thể lưu/đặt hàng) |
| 10005 | 400 | Mainboard đã hết slot RAM |

**Response lỗi mẫu:**
```json
{
  "code": 6001,
  "message": "Product not found",
  "result": null
}
```

---

## 🔐 Phân quyền

| Role | Mô tả |
|------|--------|
| `ADMIN` | Toàn quyền |
| `STAFF` | Quản lý sản phẩm, đơn hàng, user |
| `MEMBER` | Khách hàng |
| `Authenticated` | Có token hợp lệ, bất kỳ role nào |
| `Public` | Không cần token |

---

---

# 1. 🔑 Authentication — `/auth`

---

## POST `/auth/login`

**Auth:** Public

**Request:**
```json
{
  "email": "user@example.com",
  "password": "123456"
}
```

**Response — Thành công:**
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "authenticated": true,
    "token": "eyJhbGciOiJIUzUxMiJ9..."
  }
}
```

| Case | Điều kiện | Code | Message |
|------|-----------|------|---------|
| ✅ Thành công | Email đúng, password đúng | 1000 | null |
| ❌ Sai email | Email không tồn tại trong DB | 1003 | "Unauthenticated" |
| ❌ Sai password | Email đúng, password sai | 1003 | "Unauthenticated" |
| ❌ Thiếu email/password | Field rỗng | 1003 | "Unauthenticated" |

> **Note:** Server không phân biệt "sai email" vs "sai password" để tránh enumeration attack. Cả 2 đều trả về `1003`.

---

## POST `/auth/introspect`

**Auth:** Public

**Request:**
```json
{ "token": "eyJhbGciOiJIUzUxMiJ9..." }
```

**Response:**
```json
{
  "code": 1000,
  "message": null,
  "result": { "valid": true }
}
```

| Case | Điều kiện | `valid` |
|------|-----------|---------|
| ✅ Token hợp lệ | Signature đúng, chưa hết hạn, chưa logout | `true` |
| ❌ Sai signature | Token bị giả mạo | `false` |
| ❌ Hết hạn | `exp` đã qua | `false` |
| ❌ Đã logout | Token nằm trong blacklist | `false` |

> API này KHÔNG throw lỗi, luôn trả về `code: 1000` với `valid: true/false`.

---

## POST `/auth/logout`

**Auth:** Public (gửi token trong body)

**Request:**
```json
{ "token": "eyJhbGciOiJIUzUxMiJ9..." }
```

**Response:**
```json
{ "code": 1000, "message": null, "result": null }
```

| Case | Điều kiện | Kết quả |
|------|-----------|---------|
| ✅ Logout thành công | Token hợp lệ, chưa logout | Token bị blacklist |
| ✅ Logout token đã logout | Token đã nằm trong blacklist | Vẫn trả `1000` (idempotent, không báo lỗi) |

---

## POST `/auth/refresh`

**Auth:** Public

**Request:**
```json
{ "token": "eyJhbGciOiJIUzUxMiJ9..." }
```

**Response — Thành công:**
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "authenticated": true,
    "token": "eyJhbGciOiJIUzUxMiJ9...new..."
  }
}
```

| Case | Điều kiện | Code | Message |
|------|-----------|------|---------|
| ✅ Thành công | Token chưa logout, còn trong `refreshable-duration` | 1000 | null |
| ❌ Token đã logout | Token trong blacklist | 1003 | "Unauthenticated" |
| ❌ Quá hạn refresh | `issuedAt + refreshable-duration < now` (ngay cả khi token chưa hết hạn) | 1003 | "Unauthenticated" |
| ❌ Lỗi parse token | Token không đúng format JWT | 500 | "Uncategorized Error" |

> Khi refresh thành công: token cũ bị blacklist ngay, trả về token mới.

---

---

# 2. 👤 Users — `/users`

---

## POST `/users` — Đăng ký tài khoản

**Auth:** Public

**Request:**
```json
{
  "username": "nguyenvana",
  "email": "nguyenvana@gmail.com",
  "password": "123456",
  "phoneNumber": "0901234567"
}
```

| Field | Bắt buộc | Validation | Nếu vi phạm |
|-------|----------|-----------|-------------|
| `username` | ✅ | 3–50 ký tự | 400 |
| `email` | ✅ | Định dạng email hợp lệ | 400 |
| `password` | ✅ | Tối thiểu 6 ký tự | 400 |
| `phoneNumber` | ❌ | Tối đa 15 ký tự | 400 |

**Response — Thành công:**
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "userId": 5,
    "username": "nguyenvana",
    "email": "nguyenvana@gmail.com",
    "phoneNumber": "0901234567",
    "status": "ACTIVE",
    "roleName": "MEMBER",
    "createdAt": "2026-03-04T10:00:00"
  }
}
```

| Case | Code | Message |
|------|------|---------|
| ✅ Thành công | 1000 | null |
| ❌ Email đã tồn tại | 1001 | "User already exists" |
| ❌ Password < 6 ký tự | 400 | validation message |
| ❌ Email sai định dạng | 400 | validation message |
| ❌ Thiếu username/email/password | 400 | validation message |

> Account mới luôn được gán role `MEMBER` và status `ACTIVE`.

---

## GET `/users/me` — Xem profile bản thân

**Auth:** Authenticated (mọi role)

**Response:**
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "userId": 5,
    "username": "nguyenvana",
    "email": "nguyenvana@gmail.com",
    "phoneNumber": "0901234567",
    "status": "ACTIVE",
    "roleName": "MEMBER",
    "createdAt": "2026-03-04T10:00:00"
  }
}
```

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ Token không hợp lệ | 1003 |

---

## PUT `/users/me` — Cập nhật profile bản thân

**Auth:** Authenticated (mọi role)

**Request:**
```json
{
  "username": "newname",
  "password": "newpassword123",
  "phoneNumber": "0909999999"
}
```

| Field | Bắt buộc | Validation | Behaviour |
|-------|----------|-----------|-----------|
| `username` | ❌ | 3–50 ký tự | null = không thay đổi |
| `password` | ❌ | Tối thiểu 6 ký tự | null hoặc blank = không thay đổi (password cũ giữ nguyên) |
| `phoneNumber` | ❌ | Tối đa 15 ký tự | null = không thay đổi |

| Case | Code |
|------|------|
| ✅ Cập nhật 1 hoặc nhiều field | 1000 |
| ✅ Body rỗng `{}` | 1000 (không thay đổi gì) |
| ❌ Password < 6 ký tự | 400 |

---

## GET `/users` — Lấy tất cả users

**Auth:** STAFF, ADMIN

> Chỉ trả về users có `status = "ACTIVE"` (đã soft-delete thì không xuất hiện).

**Response:**
```json
{
  "code": 1000,
  "message": null,
  "result": [
    {
      "userId": 1,
      "username": "admin",
      "email": "admin@gmail.com",
      "phoneNumber": null,
      "status": "ACTIVE",
      "roleName": "ADMIN",
      "createdAt": "2026-01-01T08:00:00"
    }
  ]
}
```

---

## GET `/users/{id}`

**Auth:** STAFF, ADMIN

| Case | Code |
|------|------|
| ✅ Tìm thấy | 1000 |
| ❌ Không tồn tại | 1010 "User not existed" |

---

## PUT `/users/{id}` — Cập nhật user (Admin)

**Auth:** STAFF, ADMIN

**Request:**
```json
{
  "username": "newname",
  "password": "newpassword123",
  "phoneNumber": "0909999999",
  "status": "INACTIVE"
}
```

| Field | Behaviour |
|-------|-----------|
| `username` | null = không thay đổi |
| `password` | null hoặc blank = không thay đổi |
| `phoneNumber` | null = không thay đổi |
| `status` | null = không thay đổi; gía trị: `"ACTIVE"`, `"INACTIVE"` |

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ User không tồn tại | 1010 |

---

## DELETE `/users/{id}` — Soft Delete

**Auth:** STAFF, ADMIN

> **Soft delete:** Chỉ set `status = "INACTIVE"`, không xoá khỏi DB. User bị soft-delete sẽ không xuất hiện trong `GET /users`.

| Case | Code |
|------|------|
| ✅ Thành công | 1000, `result: null` |
| ❌ User không tồn tại | 1010 |

---

---

# 3. 📦 Products — `/products`

---

## GET `/products` — Lấy / Lọc sản phẩm

**Auth:** Public

**Query params (đều optional):**

| Param | Type | Mô tả |
|-------|------|-------|
| `categoryId` | int | Lọc theo category |
| `brandId` | int | Lọc theo brand |
| `minPrice` | double | Lọc theo basePrice >= minPrice |
| `maxPrice` | double | Lọc theo basePrice <= maxPrice |

**Ví dụ:**
- `GET /products` → Lấy tất cả
- `GET /products?categoryId=2` → Lọc theo category
- `GET /products?minPrice=5000000&maxPrice=20000000` → Lọc theo giá
- `GET /products?categoryId=2&brandId=3&minPrice=5000000` → Kết hợp nhiều filter

**Response:**
```json
{
  "code": 1000,
  "message": null,
  "result": [
    {
      "productId": 1,
      "name": "Laptop Dell Inspiron 15",
      "description": "Laptop văn phòng",
      "basePrice": 12000000.0,
      "discountedPrice": 10800000.0,
      "categoryId": 2,
      "categoryName": "Laptop",
      "brandId": 3,
      "brandName": "Dell",
      "brandLogoUrl": "https://res.cloudinary.com/.../dell.png",
      "thumbnailUrl": "https://res.cloudinary.com/.../dell_inspiron.jpg",
      "variants": [
        {
          "variantId": 1,
          "productId": 1,
          "sku": "DELL-INS-8GB-256",
          "price": 12000000.0,
          "stockQuantity": 10,
          "variantName": "RAM 8GB - SSD 256GB",
          "attributes": [
            { "attributeId": 1, "attributeName": "RAM", "value": "8GB" },
            { "attributeId": 2, "attributeName": "SSD", "value": "256GB" },
            { "attributeId": 3, "attributeName": "Color", "value": "Silver" }
          ]
        }
      ]
    }
  ]
}
```

| Case | Điều kiện | Kết quả |
|------|-----------|---------|
| ✅ Không có filter | Lấy toàn bộ | Trả về tất cả sản phẩm |
| ✅ Filter hợp lệ | Kết hợp nhiều điều kiện | Chỉ trả về SP khớp |
| ✅ Không tìm thấy SP nào | Filter quá chặt | `result: []` (không lỗi) |
| ✅ `discountedPrice` | SP có promotion active | Giá sau khi giảm |
| ✅ `discountedPrice` | SP không có promotion | `discountedPrice` = null |
| ✅ `thumbnailUrl` | SP không có ảnh | `thumbnailUrl` = null |
| ✅ `variants` | SP không có variant | `variants: []` |

---

## GET `/products/search?keyword=dell`

**Auth:** Public

| Param | Bắt buộc | Mô tả |
|-------|----------|-------|
| `keyword` | ✅ | Tìm theo `name` HOẶC `description` (case-insensitive) |

| Case | Kết quả |
|------|---------|
| ✅ Tìm thấy | Trả về list |
| ✅ Không match | `result: []` |

---

## GET `/products/{id}` — Chi tiết sản phẩm

**Auth:** Public

**Response:**
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "productId": 1,
    "name": "Laptop Dell Inspiron 15",
    "description": "Laptop văn phòng",
    "basePrice": 12000000.0,
    "discountedPrice": 10800000.0,
    "categoryId": 2,
    "categoryName": "Laptop",
    "brandId": 3,
    "brandName": "Dell",
    "brandLogoUrl": "https://res.cloudinary.com/.../dell.png",
    "imageUrls": [
      "https://res.cloudinary.com/.../img1.jpg",
      "https://res.cloudinary.com/.../img2.jpg"
    ],
    "averageRating": 4.5,
    "totalReviews": 28,
    "hasPromotion": true,
    "discountPercent": 10.0,
    "promoCode": "SALE10",
    "variants": [ "..." ]
  }
}
```

| Field | Khi nào null/default |
|-------|---------------------|
| `imageUrls` | SP không có ảnh → `[]` |
| `averageRating` | Chưa có review → `0.0` |
| `totalReviews` | Chưa có review → `0` |
| `hasPromotion` | Không có promotion → `false` |
| `discountPercent` | Không có promotion → `0.0` |
| `promoCode` | Không có promotion → `null` |
| `discountedPrice` | Không có promotion → `null` |

| Case | Code |
|------|------|
| ✅ Tìm thấy | 1000 |
| ❌ Không tồn tại | 6001 "Product not found" |

---

## POST `/products` — Tạo sản phẩm

**Auth:** STAFF, ADMIN  
**Content-Type:** `multipart/form-data`

**Form fields:**
| Field | Type | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `product` | JSON Blob | ✅ | Xem schema bên dưới |
| `images` | File[] | ❌ | Ảnh sản phẩm, tối đa 10MB/file |

**JSON của field `product`:**
```json
{
  "name": "Laptop Dell Inspiron 15",
  "description": "Mô tả sản phẩm",
  "categoryId": 2,
  "brandId": 3,
  "variants": [
    {
      "sku": "DELL-INS-8GB-256",
      "price": 12000000,
      "stockQuantity": 10,
      "variantName": "RAM 8GB - SSD 256GB",
      "attributes": [
        { "attributeId": 1, "attributeName": "RAM", "value": "8GB" },
        { "attributeId": 2, "attributeName": "SSD", "value": "256GB" }
      ]
    }
  ]
}
```

**Cách gọi (JavaScript):**
```javascript
const formData = new FormData();
formData.append(
  "product",
  new Blob([JSON.stringify(productData)], { type: "application/json" })
);
// images là array File objects
images.forEach(img => formData.append("images", img));

fetch("/api/v1/products", {
  method: "POST",
  headers: { Authorization: "Bearer <token>" },
  body: formData
});
```

**Validation `product` object:**

| Field | Bắt buộc | Validation | Nếu vi phạm |
|-------|----------|-----------|-------------|
| `name` | ✅ | Không được rỗng | 400 |
| `categoryId` | ✅ | Phải tồn tại trong DB | 1011 |
| `brandId` | ✅ | Phải tồn tại trong DB | 5001 |
| `variants` | ❌ | Xem bên dưới | — |
| `variants[].sku` | ✅ (trong variant) | Không được rỗng | 400 |
| `variants[].price` | ✅ (trong variant) | >= 0 | 400 |
| `variants[].stockQuantity` | ❌ | >= 0, mặc định `0` nếu null | — |
| `variants[].attributes` | ❌ | Xem giải thích attribute | — |

**Behaviour chi tiết cho `variants` khi CREATE:**

| Case | Input | Kết quả |
|------|-------|---------|
| ✅ Có variants | `variants: [...]` | Tạo từng variant |
| ✅ Không có variants | `variants: null` hoặc omit | Sản phẩm không có variant, `basePrice` = null |
| ✅ Variants rỗng | `variants: []` | Sản phẩm không có variant |
| ❌ SKU trùng | SKU đã tồn tại trong DB | 6102 "SKU already exists" |

**Behaviour `attributes` trong variant khi CREATE:**

| Case | Input | Kết quả |
|------|-------|---------|
| ✅ Có `attributeId` | `{ "attributeId": 1, "value": "8GB" }` | Tìm attribute theo ID. Nếu không tồn tại → 4001 |
| ✅ Không có `attributeId` | `{ "attributeName": "RAM", "value": "8GB" }` | Tìm attribute theo tên. Nếu không tìm thấy → **tự động tạo mới** |
| ✅ Không có attributes | `attributes: null` hoặc omit | Variant không có attribute |
| ✅ Attributes rỗng | `attributes: []` | Variant không có attribute |

**Behaviour ảnh khi CREATE:**

| Case | Kết quả |
|------|---------|
| Gửi `images` | Upload lên Cloudinary, ảnh đầu tiên là thumbnail |
| Không gửi `images` | Sản phẩm không có ảnh, `thumbnailUrl` = null |
| File > 10MB | 7003 "File size exceeds maximum" |

**`basePrice` sau khi tạo:**
> Tự động set = giá thấp nhất trong các variant. Nếu không có variant → `null`.

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ categoryId không tồn tại | 1011 "Category not found" |
| ❌ brandId không tồn tại | 5001 "Brand not found" |
| ❌ SKU trùng | 6102 "SKU already exists" |
| ❌ attributeId không tồn tại | 4001 "Attribute not found" |
| ❌ File > 10MB | 7003 |

---

## PUT `/products/{id}` — Cập nhật sản phẩm

**Auth:** STAFF, ADMIN  
**Content-Type:** `multipart/form-data`

**Form fields:**
| Field | Type | Bắt buộc |
|-------|------|----------|
| `product` | JSON Blob | ✅ |
| `images` | File[] | ❌ |

**JSON của field `product`:**
```json
{
  "name": "Tên mới",
  "description": "Mô tả mới",
  "categoryId": 3,
  "brandId": 2,
  "variants": [ "..." ]
}
```

**Behaviour các field cơ bản:**

| Field | Behaviour |
|-------|-----------|
| `name` | null = không thay đổi |
| `description` | null = không thay đổi |
| `categoryId` | null = không thay đổi; có value → validate tồn tại |
| `brandId` | null = không thay đổi; có value → validate tồn tại |
| `variants` | null / omit = **giữ nguyên** variants hiện có; truyền mảng → xử lý theo logic variants |

**Behaviour `images` khi UPDATE:**

| Case | Kết quả |
|------|---------|
| Không gửi `images` | Ảnh cũ giữ nguyên, không thay đổi |
| Gửi `images` mới | **Xoá toàn bộ ảnh cũ** khỏi Cloudinary và DB, upload ảnh mới. Ảnh đầu tiên làm thumbnail |

---

### ⚠️ Behaviour `variants` khi UPDATE (quan trọng nhất)

> **Logic cốt lõi:** Nếu `variants` **không được truyền (null / omit)** → giữ nguyên toàn bộ variants hiện tại, không thay đổi gì.  
> Nếu `variants` **được truyền** (kể cả mảng rỗng `[]`) → danh sách gửi lên là **trạng thái sau cùng** mong muốn.

**Phân loại item trong `variants`:**
- Item có `variantId` → **cập nhật** variant có ID đó
- Item không có `variantId` (hoặc `variantId: null`) → **tạo mới** variant

**Variant nào không xuất hiện trong list gửi lên → bị XOÁ.**

---

**Case 1 — `variants: null` hoặc omit hoàn toàn field `variants`**
```json
{
  "name": "Tên mới"
}
```
> **Kết quả: GIỮ NGUYÊN toàn bộ variants** hiện có của sản phẩm.  
> Dùng khi chỉ muốn sửa thông tin cơ bản (name, description, category, brand) mà không động đến variants.

---

**Case 2 — `variants: []` (empty array)**
```json
{
  "name": "Tên mới",
  "variants": []
}
```
> **Kết quả: XOÁ TOÀN BỘ variants** (truyền tường minh mảng rỗng).

---

**Case 3 — Giữ nguyên tất cả, chỉ update info sản phẩm**

Cách đơn giản nhất: **không truyền field `variants`**:
```json
{
  "name": "Tên mới"
}
```
> Tất cả variants giữ nguyên.

Hoặc truyền lại tất cả `variantId` hiện có:
```json
{
  "name": "Tên mới",
  "variants": [
    { "variantId": 1 },
    { "variantId": 2 },
    { "variantId": 3 }
  ]
}
```
> Các variant 1, 2, 3 không bị xoá. Vì không có field nào thay đổi, các variant giữ nguyên giá trị cũ.

---

**Case 4 — Update một số variant, xoá các variant còn lại**

Giả sử sản phẩm hiện có variant ID: 1, 2, 3. Muốn giữ lại 1 & 2, xoá 3:
```json
{
  "variants": [
    { "variantId": 1, "price": 13000000 },
    { "variantId": 2, "stockQuantity": 20 }
  ]
}
```
> Variant 1, 2 được update. **Variant 3 bị XOÁ**.

---

**Case 5 — Thêm variant mới, giữ lại variant cũ**
```json
{
  "variants": [
    { "variantId": 1, "price": 13000000 },
    {
      "sku": "DELL-INS-32GB-1T",
      "price": 25000000,
      "stockQuantity": 3,
      "variantName": "RAM 32GB - SSD 1TB",
      "attributes": [
        { "attributeName": "RAM", "value": "32GB" },
        { "attributeName": "SSD", "value": "1TB" }
      ]
    }
  ]
}
```
> Variant 1 được cập nhật. Item không có `variantId` → tạo mới variant với SKU "DELL-INS-32GB-1T".

---

**Case 6 — Gửi `variantId` không thuộc sản phẩm này**
```json
{
  "variants": [
    { "variantId": 999 }
  ]
}
```
> Lỗi **6101** "Product variant not found".

---

**Case 7 — SKU trùng khi tạo/đổi SKU**

Tạo mới hoặc đổi SKU của variant sang SKU đã tồn tại trong DB (bất kỳ sản phẩm nào):
> Lỗi **6102** "SKU already exists".  
> Nếu SKU giữ nguyên (không đổi) → Không lỗi.

---

### ⚠️ Behaviour từng field của variant khi UPDATE (có `variantId`)

> **Kết luận nhanh:** Tất cả field đều partial update — null = giữ nguyên. `attributes` hoạt động giống `variants`: merge theo `attributeId`.

| Field | null / omit | `[]` (mảng rỗng) | Có value |
|-------|-------------|------------------|----------|
| `sku` | Giữ nguyên SKU cũ | — | Validate unique → đổi SKU |
| `price` | Giữ nguyên giá cũ | — | Đổi giá |
| `stockQuantity` | Giữ nguyên tồn kho cũ | — | Đổi số lượng |
| `variantName` | Giữ nguyên tên cũ | — | Đổi tên |
| `attributes` | Giữ nguyên toàn bộ | Xoá toàn bộ | Merge: giữ item có `attributeId`, thêm mới item không có `attributeId`, xoá item cũ không có trong list |

---

**Ví dụ — chỉ sửa giá, giữ nguyên mọi thứ còn lại (kể cả attributes):**
```json
{
  "variants": [
    { "variantId": 5, "price": 19000000 }
  ]
}
```
> `sku`, `stockQuantity`, `variantName`, `attributes` đều không gửi → **giữ nguyên tất cả**.

---

**Ví dụ — sửa giá và xoá toàn bộ attributes:**
```json
{
  "variants": [
    { "variantId": 5, "price": 19000000, "attributes": [] }
  ]
}
```
> `price` được cập nhật. `attributes: []` → xoá toàn bộ attributes của variant 5.

---

### ⚠️ Behaviour `attributes` trong variant khi UPDATE variant có `variantId`

> **Logic đồng bộ với `variants`:** null / omit = giữ nguyên. Truyền list → merge (giống cách variants hoạt động).

| Case | Input | Kết quả |
|------|-------|---------|
| ✅ Giữ nguyên hoàn toàn | `attributes: null` (omit field) | Giữ nguyên toàn bộ attributes cũ |
| ✅ Xoá tất cả | `attributes: []` | Xoá toàn bộ, không insert |
| ✅ Giữ nguyên attribute đã có | `{ "attributeId": 1, "value": "8GB" }` | Tìm theo ID → chỉ update value nếu khác (không xoá-reinsert) |
| ✅ Thêm attribute mới | Item không có `attributeId`, chỉ có `attributeName` | Resolve/tạo Attribute → insert `ProductVariantAttribute` mới |
| ✅ Xoá một phần | Attribute cũ không xuất hiện trong list gửi lên | Bị xoá |

> **Tóm lại:** Gửi `attributeId` → giữ nguyên record đó (chỉ update value nếu đổi). Không gửi `attributeId` của attribute cũ → attribute đó bị xoá. Không có `attributeId` trong item → tạo mới.


**Attribute resolution (tìm/tạo attribute theo input):**

| Input | Logic |
|-------|-------|
| `{ "attributeId": 1, "value": "8GB" }` | Tìm attribute theo ID. Nếu không tồn tại → **4001** |
| `{ "attributeName": "RAM", "value": "8GB" }` | Tìm theo tên. Không tìm thấy → **tự động tạo mới** attribute |
| Cả `attributeId` và `attributeName` | Ưu tiên `attributeId` |
| Cả hai đều null/blank | **4001** "Attribute not found" |

---

**`basePrice` sau khi update:**
> Tự động set = giá variant thấp nhất. Nếu sau update không còn variant nào → `basePrice` không thay đổi.

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ Product không tồn tại | 6001 |
| ❌ categoryId không tồn tại | 1011 |
| ❌ brandId không tồn tại | 5001 |
| ❌ variantId không tồn tại hoặc thuộc product khác | 6101 |
| ❌ SKU trùng | 6102 |
| ❌ attributeId không tồn tại | 4001 |

---

## DELETE `/products/{id}`

**Auth:** STAFF, ADMIN

> Xoá hoàn toàn (hard delete): Xoá variants, attributes, ảnh trên Cloudinary, associations với promotion.

| Case | Code |
|------|------|
| ✅ Thành công | 1000, `result: null` |
| ❌ Không tồn tại | 6001 |

---

---

# 4. 📁 Categories — `/categories`

---

## GET `/categories`

**Auth:** Public

**Response:**
```json
{
  "code": 1000,
  "message": null,
  "result": [
    {
      "categoryId": 1,
      "categoryName": "Điện tử",
      "parentCategoryId": null,
      "parentCategoryName": null
    },
    {
      "categoryId": 2,
      "categoryName": "Laptop",
      "parentCategoryId": 1,
      "parentCategoryName": "Điện tử"
    }
  ]
}
```

> Trả về **tất cả** categories trong một list phẳng, không phân cấp tree.

---

## GET `/categories/{id}`

**Auth:** Public

| Case | Code |
|------|------|
| ✅ Tìm thấy | 1000 |
| ❌ Không tồn tại | 1011 "Category not found" |

---

## POST `/categories`

**Auth:** STAFF, ADMIN

**Request:**
```json
{
  "categoryName": "Laptop Gaming",
  "parentCategoryId": 2
}
```

| Field | Bắt buộc | Validation |
|-------|----------|-----------|
| `categoryName` | ✅ | 1–100 ký tự |
| `parentCategoryId` | ❌ | Phải tồn tại trong DB |

| Case | Code |
|------|------|
| ✅ Tạo root category | `parentCategoryId: null` hoặc omit | 1000 |
| ✅ Tạo sub-category | `parentCategoryId: <id>` hợp lệ | 1000 |
| ❌ parentCategoryId không tồn tại | — | 1011 "Category not found" |

---

## PUT `/categories/{id}`

**Auth:** STAFF, ADMIN

**Request:**
```json
{
  "categoryName": "Laptop Gaming Updated",
  "parentCategoryId": 1
}
```

| Case | Code |
|------|------|
| ✅ Đổi tên | `categoryName: "mới"` | 1000 |
| ✅ Chuyển sang root | `parentCategoryId: null` | 1000 — parentCategory bị xoá |
| ✅ Đổi parent | `parentCategoryId: <id>` hợp lệ | 1000 |
| ❌ Category tự đặt mình làm parent | `parentCategoryId == id của chính nó` | 1013 "Category cannot be parent of itself" |
| ❌ parentCategoryId không tồn tại | — | 1011 |
| ❌ Category không tồn tại | — | 1011 |

---

## DELETE `/categories/{id}`

**Auth:** STAFF, ADMIN

| Case | Code |
|------|------|
| ✅ Xoá thành công | 1000, `result: null` |
| ❌ Category không tồn tại | 1011 |
| ❌ Category có sub-categories | 1014 "Cannot delete category with child categories" |

> Phải xoá **tất cả sub-categories** trước rồi mới xoá được category cha.

---

---

# 5. 🏷️ Brands — `/brands`

---

## GET `/brands`

**Auth:** Public

**Response:**
```json
{
  "code": 1000,
  "message": null,
  "result": [
    { "brandId": 1, "brandName": "Dell", "logoUrl": "https://res.cloudinary.com/.../dell.png" },
    { "brandId": 2, "brandName": "Apple", "logoUrl": null }
  ]
}
```

---

## GET `/brands/{id}`

**Auth:** Public

| Case | Code |
|------|------|
| ✅ Tìm thấy | 1000 |
| ❌ Không tồn tại | 5001 "Brand not found" |

---

## POST `/brands`

**Auth:** STAFF, ADMIN  
**Content-Type:** `multipart/form-data`

**Cách gọi:**
```javascript
const formData = new FormData();
formData.append("brandName", "Samsung"); // string thường, KHÔNG phải JSON
formData.append("logo", logoFile);       // optional
```

| Field | Type | Bắt buộc |
|-------|------|----------|
| `brandName` | form-param (string) | ✅ |
| `logo` | File | ❌ |

| Case | Code |
|------|------|
| ✅ Có logo | 1000, `logoUrl` = URL trên Cloudinary |
| ✅ Không có logo | 1000, `logoUrl` = null |
| ❌ `brandName` rỗng | 400 |
| ❌ File > 10MB | 7003 |

---

## PUT `/brands/{id}`

**Auth:** STAFF, ADMIN  
**Content-Type:** `multipart/form-data`

| Field | Type | Mô tả |
|-------|------|-------|
| `data` | JSON Blob (field name phải là `data`) | `{ "brandName": "Samsung Electronics" }` |
| `logo` | File | Optional |

**Behaviour ảnh:**

| Case | Kết quả |
|------|---------|
| Không gửi `logo` | Logo cũ giữ nguyên |
| Gửi `logo` mới | Upload logo mới lên Cloudinary, `logoUrl` được update |

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ Brand không tồn tại | 5001 |

---

## DELETE `/brands/{id}`

**Auth:** STAFF, ADMIN

| Case | Code |
|------|------|
| ✅ Thành công | 1000, `result: null` |
| ❌ Không tồn tại | 5001 |

---

---

# 6. 🔖 Attributes — `/attributes`

---

## GET `/attributes`

**Auth:** Public  
**Response:**
```json
{
  "code": 1000,
  "message": null,
  "result": [
    { "attributeId": 1, "attributeName": "RAM" },
    { "attributeId": 2, "attributeName": "SSD" }
  ]
}
```

---

## GET `/attributes/{id}`

**Auth:** Public

| Case | Code |
|------|------|
| ✅ Tìm thấy | 1000 |
| ❌ Không tồn tại | 4001 "Attribute not found" |

---

## POST `/attributes`

**Auth:** STAFF, ADMIN

**Request:**
```json
{ "attributeName": "GPU" }
```

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ `attributeName` rỗng | 400 |

---

## PUT `/attributes/{id}`

**Auth:** STAFF, ADMIN

**Request:**
```json
{ "attributeName": "Graphics Card" }
```

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ Không tồn tại | 4001 |

---

## DELETE `/attributes/{id}`

**Auth:** STAFF, ADMIN

| Case | Code |
|------|------|
| ✅ Thành công | 1000, `result: null` |
| ❌ Không tồn tại | 4001 |

---

---

# 7. 🛒 Cart — `/cart`

> **Cart được tự động tạo** khi user lần đầu gọi bất kỳ API cart nào (không cần tạo thủ công).

---

## GET `/cart`

**Auth:** Authenticated (mọi role)

**Response:**
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "cartId": 3,
    "items": [
      {
        "cartItemId": 12,
        "variantId": 1,
        "variantName": "RAM 8GB - SSD 256GB",
        "sku": "DELL-INS-8GB-256",
        "price": 12000000.0,
        "stockQuantity": 10,
        "quantity": 2,
        "productId": 1,
        "productName": "Laptop Dell Inspiron 15",
        "thumbnailUrl": "https://res.cloudinary.com/.../thumbnail.jpg"
      }
    ],
    "totalPrice": 24000000.0,
    "totalItems": 2
  }
}
```

> `totalItems` = tổng số lượng sản phẩm (sum of quantities).  
> `totalPrice` = sum(price × quantity) cho tất cả items.

| Case | Kết quả |
|------|---------|
| ✅ Cart có hàng | Trả về đầy đủ |
| ✅ Cart rỗng hoặc chưa tồn tại | `items: [], totalPrice: 0.0, totalItems: 0` |

---

## POST `/cart/items` — Thêm vào giỏ

**Auth:** Authenticated

**Request:**
```json
{
  "variantId": 1,
  "quantity": 2
}
```

| Field | Bắt buộc | Default | Validation |
|-------|----------|---------|-----------|
| `variantId` | ✅ | — | Phải tồn tại |
| `quantity` | ❌ | `1` | >= 1 |

**Response:** Trả về `CartResponse` (toàn bộ giỏ hàng sau khi thêm).

| Case | Điều kiện | Kết quả |
|------|-----------|---------|
| ✅ Variant chưa có trong giỏ | — | Tạo cart item mới |
| ✅ Variant đã có trong giỏ | `variantId` đã tồn tại | **Cộng thêm** quantity, **không** tạo item mới |
| ❌ variantId không tồn tại | — | 6101 "Product variant not found" |
| ❌ `quantity` < 1 | — | 400 |

> **Lưu ý:** Không kiểm tra stock khi thêm vào giỏ. Chỉ kiểm tra stock khi **đặt hàng**.

---

## PUT `/cart/items/{cartItemId}` — Cập nhật số lượng

**Auth:** Authenticated

**Request:**
```json
{ "quantity": 3 }
```

| Case | Điều kiện | Code |
|------|-----------|------|
| ✅ Cập nhật thành công | item thuộc cart của user | 1000 |
| ❌ cartItemId không tồn tại | — | 8002 "Cart item not found" |
| ❌ Item không thuộc user hiện tại | Item thuộc cart của user khác | 8002 |
| ❌ `quantity` <= 0 | — | 8004 "Quantity must be greater than 0" |

> Muốn xoá item → dùng `DELETE /cart/items/{cartItemId}`, **không** set quantity = 0 (sẽ báo lỗi 8004).

---

## DELETE `/cart/items/{cartItemId}` — Xoá 1 item

**Auth:** Authenticated

**Response:** Trả về `CartResponse` sau khi đã xoá item.

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ cartItemId không tồn tại | 8002 |
| ❌ Item không thuộc user hiện tại | 8002 |

---

## DELETE `/cart` — Xoá toàn bộ giỏ hàng

**Auth:** Authenticated

**Response:**
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "cartId": 3,
    "items": [],
    "totalPrice": 0.0,
    "totalItems": 0
  }
}
```

| Case | Code |
|------|------|
| ✅ Giỏ có hàng | 1000, xoá tất cả items |
| ✅ Giỏ đã rỗng | 1000, không thay đổi gì |

---

---

# 8. 📋 Orders — `/orders`

---

## POST `/orders` — Đặt hàng

**Auth:** Authenticated

> Đặt hàng lấy **toàn bộ sản phẩm** hiện có trong giỏ hàng. Sau khi đặt thành công, **giỏ hàng bị xoá**.

**Request — Thanh toán một lần (FULL):**
```json
{
  "recipientName": "Nguyễn Văn A",
  "recipientPhone": "0901234567",
  "shippingAddress": "123 Đường ABC, Q1, TP.HCM",
  "paymentType": "FULL"
}
```

**Request — Trả góp (INSTALLMENT):**
```json
{
  "recipientName": "Nguyễn Văn A",
  "recipientPhone": "0901234567",
  "shippingAddress": "123 Đường ABC, Q1, TP.HCM",
  "paymentType": "INSTALLMENT",
  "packageId": 2
}
```

| Field | Bắt buộc | Mặc định | Điều kiện                                                                                 |
|-------|----------|----------|-------------------------------------------------------------------------------------------|
| `recipientName` | ✅ | — | Không được rỗng                                                                           |
| `recipientPhone` | ✅ | — | Không được rỗng                                                                           |
| `shippingAddress` | ✅ | — | Không được rỗng                                                                           |
| `paymentType` | ✅ | — | `"COD, "FULL"` hoặc `"INSTALLMENT"`                                                       |
| `packageId` | ❌ | null | **Bắt buộc khi `paymentType = "INSTALLMENT"`**. Lấy từ `GET /installment-packages/active` |

**Validation khi chọn INSTALLMENT:**
- `packageId` phải tồn tại và `isActive = true`
- Tổng tiền đơn hàng phải >= `minOrderAmount` của gói trả góp
- Nếu vi phạm → lỗi 400 hoặc lỗi custom (tuỳ implementation)

**Response:**
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "orderId": 10,
    "userId": 5,
    "username": "nguyenvana",
    "totalAmount": 24000000.0,
    "status": "PENDING",
    "paymentType": "INSTALLMENT",
    "orderDate": "2026-03-04T10:30:00",
    "installmentPackage": {
      "packageId": 2,
      "name": "Trả góp 6 tháng - Lãi suất 0%",
      "durationMonths": 6,
      "interestRate": 1.0,
      "minOrderAmount": 5000000.0,
      "isActive": true
    },
    "items": [
      {
        "orderItemId": 21,
        "quantity": 2,
        "unitPrice": 12000000.0,
        "subtotal": 24000000.0,
        "variantId": 1,
        "variantName": "RAM 8GB - SSD 256GB",
        "sku": "DELL-INS-8GB-256",
        "productId": 1,
        "productName": "Laptop Dell Inspiron 15",
        "thumbnailUrl": "https://res.cloudinary.com/...",
        "recipientName": "Nguyễn Văn A",
        "recipientPhone": "0901234567",
        "shippingAddress": "123 Đường ABC, Q1, TP.HCM",
        "serialNumber": "DELL-INS-8GB-256-A1B2C3D4"
      }
    ],
    "paymentSchedule": [
      {
        "paymentScheduleId": 1,
        "installmentNo": 1,
        "amount": 4040000.0,
        "dueDate": "2026-04-04",
        "paidDate": null,
        "vnpTransactionNo": null,
        "status": "UNPAID"
      },
      {
        "paymentScheduleId": 2,
        "installmentNo": 2,
        "amount": 4040000.0,
        "dueDate": "2026-05-04",
        "paidDate": null,
        "vnpTransactionNo": null,
        "status": "UNPAID"
      },
      {
        "paymentScheduleId": 3,
        "installmentNo": 3,
        "amount": 4040000.0,
        "dueDate": "2026-06-04",
        "paidDate": null,
        "vnpTransactionNo": null,
        "status": "UNPAID"
      },
      {
        "paymentScheduleId": 4,
        "installmentNo": 4,
        "amount": 4040000.0,
        "dueDate": "2026-07-04",
        "paidDate": null,
        "vnpTransactionNo": null,
        "status": "UNPAID"
      },
      {
        "paymentScheduleId": 5,
        "installmentNo": 5,
        "amount": 4040000.0,
        "dueDate": "2026-08-04",
        "paidDate": null,
        "vnpTransactionNo": null,
        "status": "UNPAID"
      },
      {
        "paymentScheduleId": 6,
        "installmentNo": 6,
        "amount": 4040000.0,
        "dueDate": "2026-09-04",
        "paidDate": null,
        "vnpTransactionNo": null,
        "status": "UNPAID"
      }
    ]
  }
}
```

**Công thức tính INSTALLMENT:**
- Lấy thông tin gói: `durationMonths`, `interestRate` từ bảng `installment_package`
- `totalWithInterest = totalAmount × (1 + interestRate / 100)`
- `monthlyAmount = totalWithInterest / durationMonths` (làm tròn nếu cần)
- `dueDate` kỳ thứ i = `today + i tháng`

**Ví dụ tính toán:**
- Tổng tiền: 24,000,000 VNĐ
- Gói: 6 tháng, lãi suất 1%
- Tổng có lãi: 24,000,000 × 1.01 = 24,240,000 VNĐ
- Mỗi kỳ: 24,240,000 / 6 = 4,040,000 VNĐ

**Số payment records theo loại:**

| paymentType | Số records | dueDate |
|------------|------------|---------|
| `FULL` | 1 | today + 7 ngày |
| `INSTALLMENT` | N (= durationMonths) | today + 1 tháng, +2 tháng, ..., +N tháng |

**Flow sau khi đặt hàng:**
1. Validate stock cho TẤT CẢ items trước khi tạo bất kỳ thứ gì
2. Nếu INSTALLMENT: validate `packageId` hợp lệ và `totalAmount >= minOrderAmount`
3. Tạo Order (lưu `payment_type` và `installment_package_id`)
4. Giảm stock của từng variant (`stockQuantity -= quantity`)
5. Tạo `ProductItem` với `serialNumber = "{sku}-{8 ký tự đầu UUID}"`
6. Tạo `OrderItem` cho từng sản phẩm
7. Tạo payment schedule (1 kỳ nếu FULL, N kỳ nếu INSTALLMENT)
8. **Xoá toàn bộ giỏ hàng**

| Case | Code |
|------|------|
| ✅ Thành công (FULL) | 1000, 1 payment record |
| ✅ Thành công (INSTALLMENT) | 1000, N payment records |
| ❌ Giỏ hàng rỗng | 9002 "Cart is empty, cannot place order" |
| ❌ Bất kỳ item nào không đủ stock | 9003 "Insufficient stock for variant" (không tạo order) |
| ❌ `paymentType` null | 400 validation |
| ❌ Thiếu recipientName/Phone/Address | 400 validation |
| ❌ `paymentType = INSTALLMENT` nhưng thiếu `packageId` | 400 validation |
| ❌ `packageId` không tồn tại hoặc `isActive = false` | 400 hoặc custom error |
| ❌ `totalAmount < minOrderAmount` của gói trả góp | 400 hoặc custom error |

---

## GET `/orders/me` — Đơn hàng của tôi

**Auth:** Authenticated

> Trả về đơn hàng của user đang đăng nhập, **mới nhất trước** (sắp xếp theo `orderDate DESC`).

**Response:** `List<OrderResponse>`, mỗi order có đầy đủ `items` và `payments`.

| Case | Kết quả |
|------|---------|
| ✅ Có đơn | Trả về list |
| ✅ Chưa có đơn | `result: []` |

---

## GET `/orders/{id}` — Chi tiết đơn hàng

**Auth:** Authenticated

| Case | Code |
|------|------|
| ✅ Tìm thấy | 1000 |
| ❌ Không tồn tại | 9001 "Order not found" |

---

## GET `/orders` — Tất cả đơn hàng

**Auth:** STAFF, ADMIN

> Trả về **tất cả** đơn hàng, mới nhất trước.

---

## PUT `/orders/{id}/status` — Cập nhật trạng thái

**Auth:** STAFF, ADMIN

**Request:**
```json
{ "status": "PROCESSING" }
```

**Luồng trạng thái gợi ý:**
```
PENDING → PROCESSING → SHIPPING → DELIVERED
              ↓
          CANCELLED
```

| Case | Code |
|------|------|
| ✅ Cập nhật thành công | 1000, trả về OrderResponse mới |
| ❌ Order không tồn tại | 9001 |
| ❌ `status` rỗng | 400 |

---

## PUT `/orders/{id}/cancel` — Huỷ đơn hàng

**Auth:** Authenticated

**Response:**
```json
{
  "code": 1000,
  "message": "Order cancelled successfully",
  "result": null
}
```

**Flow khi huỷ thành công:**
1. Validate owner + status PENDING
2. **Hoàn lại stock** cho từng variant trong đơn (`stockQuantity += quantity`)
3. Set `status = "CANCELLED"`

| Case | Code | Điều kiện |
|------|------|----------|
| ✅ Huỷ thành công | 1000 | User là chủ đơn VÀ status = `"PENDING"` |
| ❌ Order không tồn tại | 9001 | Order ID không có trong DB |
| ❌ Không phải chủ đơn | 9001 | User khác cố huỷ đơn của người khác (không phân biệt với "không tồn tại") |
| ❌ Status không phải PENDING | 9001 | Đơn đã PROCESSING, SHIPPING, DELIVERED, hoặc CANCELLED |

> STAFF/ADMIN muốn huỷ dùng `PUT /orders/{id}/status` với `status: "CANCELLED"`.

---

---

# 9. 💳 Payment (VNPay) — `/orders/payment`

---

## GET `/orders/payment/createPayment`

**Auth:** Public

**Query params:**

| Param | Bắt buộc | Mô tả |
|-------|----------|-------|
| `orderId` | ✅ | ID đơn hàng cần thanh toán |
| `bankCode` | ❌ | Mã ngân hàng VD: `NCB`, `VIETINBANK`, `VCB`. Null = hiện màn hình chọn ngân hàng |

**Ví dụ:**
- `GET /orders/payment/createPayment?orderId=10` → VNPay hiện page chọn ngân hàng
- `GET /orders/payment/createPayment?orderId=10&bankCode=NCB` → Thẳng vào thanh toán NCB

**Response:**
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "code": "00",
    "message": "success",
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=..."
  }
}
```

| `result.code` | Ý nghĩa |
|--------------|---------|
| `"00"` | Tạo URL thành công, redirect user đến `paymentUrl` |

**Flow thanh toán VNPay:**
```
1. FE → Tạo Order → nhận paymentUrl
2. FE → redirect user đến paymentUrl
3. User → hoàn tất thanh toán trên VNPay
4. VNPay → GET /callback (FE)
6. FE → GET /orders/{id} để hiển thị thông tin đơn hàng và trạng thái đơn hàng/thanh toán
```

---

## GET `/orders/payment/callback`

> **FE không gọi API này.** VNPay gọi về server sau khi user thanh toán. Server redirect (HTTP 302) về FE.

---

## GET `/orders/payment/vnp-ipn`

> **FE không cần dùng.** Webhook server-to-server VNPay gọi để confirm payment.

---

---

# 10. � Installment Packages — `/installment-packages`

---

## GET `/installment-packages/active`

**Auth:** Public

> Lấy danh sách **chỉ các gói trả góp đang hoạt động** (`isActive = true`).

**Response:**
```json
{
  "code": 1000,
  "message": null,
  "result": [
    {
      "packageId": 1,
      "name": "Trả góp 3 tháng - Lãi suất 0% (Trả trước 0%)",
      "durationMonths": 3,
      "interestRate": 0.0,
      "minOrderAmount": 3000000.0,
      "downPaymentPercentage": 0.0,
      "isActive": true
    },
    {
      "packageId": 2,
      "name": "Trả góp 6 tháng - Lãi suất 1% (Trả trước 10%)",
      "durationMonths": 6,
      "interestRate": 1.0,
      "minOrderAmount": 5000000.0,
      "downPaymentPercentage": 10.0,
      "isActive": true
    }
  ]
}
```

| Case | Kết quả |
|------|---------|
| ✅ Có gói active | Trả về list |
| ✅ Không có gói active | `result: []` |

---

## GET `/installment-packages`

**Auth:** Public

> Lấy **tất cả** gói trả góp, bao gồm cả không hoạt động.

**Response:** `List<InstallmentPackageResponse>` (giống `/active`).

---

## POST `/installment-packages`

**Auth:** STAFF, ADMIN

**Request:**
```json
{
  "name": "Trả góp 18 tháng - Lãi suất 2% (Trả trước 25%)",
  "durationMonths": 18,
  "interestRate": 2.0,
  "minOrderAmount": 15000000.0,
  "downPaymentPercentage": 25.0,
  "isActive": true
}
```

| Field | Bắt buộc | Validation |
|-------|----------|-----------|
| `name` | ✅ | Không được rỗng |
| `durationMonths` | ✅ | Số nguyên dương |
| `interestRate` | ✅ | Số thực >= 0 (%) |
| `minOrderAmount` | ✅ | Số thực >= 0 |
| `downPaymentPercentage` | ✅ | Số thực >= 0 (%) |
| `isActive` | ✅ | `true` hoặc `false` |

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ Thiếu field bắt buộc | 400 |

---

## PUT `/installment-packages/{id}`

**Auth:** STAFF, ADMIN

**Request:**
```json
{
  "name": "Trả góp 6 tháng - Lãi suất 0.5%",
  "durationMonths": 6,
  "interestRate": 0.5,
  "minOrderAmount": 5000000.0,
  "downPaymentPercentage": 10.0,
  "isActive": false
}
```

| Field | Behaviour |
|-------|-----------|
| `name` | null = không thay đổi |
| `durationMonths` | null = không thay đổi |
| `interestRate` | null = không thay đổi |
| `minOrderAmount` | null = không thay đổi |
| `downPaymentPercentage` | null = không thay đổi |
| `isActive` | null = không thay đổi |

> **Partial update:** Mỗi field đều nullable. Gửi gì thì update nấy.

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ Package không tồn tại | 404 (hoặc lỗi phù hợp, tuỳ implementation) |

---

## DELETE `/installment-packages/{id}`

**Auth:** STAFF, ADMIN

> Xoá hoàn toàn gói trả góp.

**Response:**
```json
{
  "code": 1000,
  "message": "Installment package deleted successfully",
  "result": null
}
```

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ Không tồn tại | 404 |

---

## POST `/installment-packages/calculate` — Xem trước trả góp

**Auth:** Public

**Request:**
```json
{
  "packageId": 2,
  "orderAmount": 20000000
}
```

**Response:**
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "orderAmount": 20000000.0,
    "downPaymentPercentage": 10.0,
    "downPaymentAmount": 2000000.0,
    "remainingBalance": 18000000.0,
    "monthlyInstallmentAmount": 305282.0,
    "interestRate": 1.0,
    "durationMonths": 6,
    "totalPayableAmount": 20305282.0,
    "schedule": [
      {
        "installmentNo": 1,
        "amount": 305282.0,
        "dueDate": "2026-04-15"
      },
      {
        "installmentNo": 2,
        "amount": 305282.0,
        "dueDate": "2026-05-15"
      }
    ]
  }
}
```

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ Package không tồn tại | 9999 (Uncategorized) |

---

---

# 11. �📝 Blogs — `/blogs`

---

## GET `/blogs`

**Auth:** Public

**Response:**
```json
{
  "code": 1000,
  "message": null,
  "result": [
    {
      "blogId": 1,
      "userId": 5,
      "userName": "nguyenvana",
      "title": "Top 5 Laptop Gaming 2026",
      "content": "Nội dung bài viết...",
      "publishedAt": "2026-03-01T09:00:00"
    }
  ]
}
```

---

## GET `/blogs/{id}`

**Auth:** Public

| Case | Code |
|------|------|
| ✅ Tìm thấy | 1000 |
| ❌ Không tồn tại | 2001 "Blog not found" |

---

## GET `/blogs/user/{userId}`

**Auth:** Public

| Case | Code |
|------|------|
| ✅ User tồn tại, có blog | 1000, trả về list |
| ✅ User tồn tại, không có blog | 1000, `result: []` |
| ❌ User không tồn tại | 1010 "User not existed" |

---

## POST `/blogs`

**Auth:** MEMBER, STAFF, ADMIN

**Request:**
```json
{
  "userId": 5,
  "title": "Top 5 Laptop Gaming 2026",
  "content": "Nội dung bài viết chi tiết..."
}
```

| Field | Bắt buộc | Validation |
|-------|----------|-----------|
| `userId` | ✅ | Phải tồn tại trong DB |
| `title` | ✅ | Không được rỗng |
| `content` | ✅ | Không được rỗng |

> ⚠️ Server dùng `userId` từ **request body** để gán tác giả, **không lấy từ JWT token**. FE phải tự truyền `userId` của user đang đăng nhập (lấy từ `GET /users/me`).

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ userId không tồn tại | 1010 "User not existed" |
| ❌ title rỗng | 400 |
| ❌ content rỗng | 400 |

---

## PUT `/blogs/{id}`

**Auth:** MEMBER, STAFF, ADMIN

**Request:**
```json
{
  "title": "Tiêu đề mới",
  "content": "Nội dung mới..."
}
```

| Field | Behaviour |
|-------|-----------|
| `title` | null = không thay đổi |
| `content` | null = không thay đổi |

> Server không kiểm tra xem người gọi có phải chủ blog không. Bất kỳ Authenticated user nào cũng có thể sửa blog của người khác.

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ Blog không tồn tại | 2001 "Blog not found" |

---

## DELETE `/blogs/{id}`

**Auth:** STAFF, ADMIN

| Case | Code |
|------|------|
| ✅ Thành công | 1000, `result: null` |
| ❌ Không tồn tại | 2001 |

---

---

# 12. 🎁 Promotions — `/promotions`

---

## GET `/promotions`

**Auth:** Public

**Response:**
```json
{
  "code": 1000,
  "message": null,
  "result": [
    {
      "promotionId": 1,
      "promoCode": "SALE10",
      "discountPercent": 10,
      "startDate": "2026-03-01",
      "endDate": "2026-03-31"
    }
  ]
}
```

---

## GET `/promotions/{id}`

**Auth:** Public

| Case | Code |
|------|------|
| ✅ Tìm thấy | 1000 |
| ❌ Không tồn tại | 3001 "Promotion not found" |

---

## GET `/promotions/code/{promoCode}`

**Auth:** Public

**Ví dụ:** `GET /promotions/code/SALE10`

| Case | Code |
|------|------|
| ✅ Tìm thấy | 1000 |
| ❌ Không tồn tại | 3001 |

---

## POST `/promotions`

**Auth:** STAFF, ADMIN

**Request:**
```json
{
  "promoCode": "SUMMER2026",
  "discountPercent": 15,
  "startDate": "2026-06-01",
  "endDate": "2026-06-30"
}
```

| Field | Bắt buộc | Validation |
|-------|----------|-----------|
| `promoCode` | ✅ | Không được rỗng, phải unique |
| `discountPercent` | ✅ | 1–100 |
| `startDate` | ❌ | Format: `YYYY-MM-DD` |
| `endDate` | ❌ | Format: `YYYY-MM-DD` |

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ promoCode đã tồn tại | 3002 "Promo code already exists" |
| ❌ discountPercent < 1 hoặc > 100 | 400 |

---

## PUT `/promotions/{id}`

**Auth:** STAFF, ADMIN

**Request:**
```json
{
  "promoCode": "SUMMER2026_V2",
  "discountPercent": 20,
  "startDate": "2026-06-01",
  "endDate": "2026-07-31"
}
```

| Field | Behaviour |
|-------|-----------|
| `promoCode` | null = không thay đổi; đổi sang code đã có của promotion khác → 3002 |
| `discountPercent` | null = không thay đổi |
| `startDate` | null = không thay đổi |
| `endDate` | null = không thay đổi |

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ Promotion không tồn tại | 3001 |
| ❌ promoCode trùng với promotion khác | 3002 |
| ✅ promoCode giữ nguyên (không đổi) | 1000 (không bị coi là trùng) |

---

## DELETE `/promotions/{id}`

**Auth:** STAFF, ADMIN

> Khi xoá: tự động xoá tất cả liên kết `PromotionProduct` trước để tránh FK violation.

| Case | Code |
|------|------|
| ✅ Thành công | 1000, `result: null` |
| ❌ Không tồn tại | 3001 |

---

## POST `/promotions/add-to-products`

**Auth:** STAFF, ADMIN

**Request:**
```json
{
  "promotionId": 1,
  "productIds": [1, 2, 3, 5]
}
```

| Case | Behaviour |
|------|-----------|
| ✅ Product chưa có promotion này | Thêm liên kết |
| ✅ Product đã có promotion này | **Skip** (không lỗi, không tạo duplicate) |
| ✅ Mix (vài đã có, vài chưa) | Chỉ thêm những cái chưa có |
| ❌ promotionId không tồn tại | 3001 |
| ❌ Bất kỳ productId nào không tồn tại | 6001 "Product not found" |

**Response:** `1000, result: null` khi thành công.

---

## POST `/promotions/add-to-category`

**Auth:** STAFF, ADMIN

**Request:**
```json
{
  "promotionId": 1,
  "categoryId": 2
}
```

> Áp dụng promotion cho **tất cả sản phẩm thuộc category đó**. Sản phẩm đã có promotion này → skip.

| Case | Code |
|------|------|
| ✅ Thành công | 1000, `result: null` |
| ✅ Category không có sản phẩm | 1000 (không làm gì) |
| ❌ promotionId không tồn tại | 3001 |
| ❌ categoryId không tồn tại | 1011 |

---

## POST `/promotions/add-to-brand`

**Auth:** STAFF, ADMIN

**Request:**
```json
{
  "promotionId": 1,
  "brandId": 3
}
```

> Áp dụng promotion cho **tất cả sản phẩm của brand đó**. Sản phẩm đã có promotion này → skip.

| Case | Code |
|------|------|
| ✅ Thành công | 1000, `result: null` |
| ✅ Brand không có sản phẩm | 1000 (không làm gì) |
| ❌ promotionId không tồn tại | 3001 |
| ❌ brandId không tồn tại | 5001 |

---

---

# 13. 🛡️ Roles — `/roles`

> **Chỉ ADMIN** mới có quyền truy cập module này.

---

## GET `/roles`

**Auth:** ADMIN

**Response:**
```json
{
  "code": 1000,
  "message": null,
  "result": [
    { "roleId": 1, "name": "ADMIN", "description": "Administrator" },
    { "roleId": 2, "name": "STAFF", "description": "Staff member" },
    { "roleId": 3, "name": "MEMBER", "description": "Customer member" }
  ]
}
```

---

## GET `/roles/{id}`

**Auth:** ADMIN

| Case | Code |
|------|------|
| ✅ Tìm thấy | 1000 |
| ❌ Không tồn tại | 1002 "Role not found" |

---

## POST `/roles`

**Auth:** ADMIN

**Request:**
```json
{
  "name": "MODERATOR",
  "description": "Content moderator"
}
```

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ Role name đã tồn tại | 1012 "Role already exists" |

---

## PUT `/roles/{id}`

**Auth:** ADMIN

**Request:**
```json
{
  "name": "MODERATOR",
  "description": "Updated description"
}
```

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ Không tồn tại | 1002 |

---

## DELETE `/roles/{id}`

**Auth:** ADMIN

| Case | Code |
|------|------|
| ✅ Thành công | 1000, `result: null` |
| ❌ Không tồn tại | 1002 |

---

---

# 🔧 Lưu ý quan trọng cho FE

### 1. Token Management
```javascript
// Lưu token sau khi login
localStorage.setItem("token", result.token);

// Gắn vào mọi request cần auth
headers: { "Authorization": "Bearer " + localStorage.getItem("token") }
```

Khi nhận `code: 1003` → gọi `POST /auth/refresh`:
- Thành công → lưu token mới, retry request cũ
- Thất bại → logout user (xoá token, redirect login)

### 2. Multipart Form Data (Product & Brand)

```javascript
// Product — field name là "product", phải là Blob với type application/json
const formData = new FormData();
formData.append(
  "product",
  new Blob([JSON.stringify(productData)], { type: "application/json" })
);
images.forEach(img => formData.append("images", img));

// Brand create — brandName là form param thường
const formData = new FormData();
formData.append("brandName", "Samsung");
formData.append("logo", logoFile);

// Brand update — field name là "data", phải là Blob
const formData = new FormData();
formData.append(
  "data",
  new Blob([JSON.stringify({ brandName: "New Name" })], { type: "application/json" })
);
formData.append("logo", newLogoFile);
```

### 3. Product Update — Không làm mất variants ngoài ý muốn

```javascript
// Nếu chỉ muốn đổi tên, KHÔNG cần gửi variants — variants tự động giữ nguyên:
const updateBody = {
  name: "Tên mới"
  // không truyền variants → giữ nguyên toàn bộ variants hiện có
};

// Nếu muốn XOÁ toàn bộ variants, truyền mảng rỗng tường minh:
const updateBody = {
  name: "Tên mới",
  variants: []  // tường minh → xoá hết variants
};
```

### 4. Attribute trong Variant — 2 cách dùng

```javascript
// Cách 1: attributeId — an toàn hơn (không tự tạo mới)
{ "attributeId": 1, "value": "8GB" }

// Cách 2: attributeName — tự động tạo attribute mới nếu chưa tồn tại
{ "attributeName": "RAM", "value": "8GB" }
```

### 5. VNPay Payment Flow

```javascript
// Tạo URL thanh toán
const { result } = await fetch(
  `/api/v1/orders/payment/createPayment?orderId=${orderId}`
).then(r => r.json());

// Redirect user
window.location.href = result.paymentUrl;

// Sau khi VNPay redirect về FE, kiểm tra trạng thái đơn
const orderRes = await fetch(`/api/v1/orders/${orderId}`, {
  headers: { Authorization: "Bearer " + token }
}).then(r => r.json());
```

### 6. Blog — userId phải lấy từ /users/me

```javascript
const { result: me } = await fetch("/api/v1/users/me", {
  headers: { Authorization: "Bearer " + token }
}).then(r => r.json());

// Truyền userId khi tạo blog
const blog = {
  userId: me.userId,  // bắt buộc, server không tự lấy từ JWT
  title: "...",
  content: "..."
};
```

### 7. Các API trả về `result: null`

| API | Ghi chú |
|-----|---------|
| `POST /auth/logout` | `result: null` |
| `DELETE` bất kỳ | `result: null` |
| `PUT /orders/{id}/cancel` | `result: null` |
| `POST /promotions/add-to-*` | `result: null` |

### 8. Soft Delete vs Hard Delete

| Resource | Delete type | Có thể khôi phục |
|----------|------------|------------------|
| User | Soft (status → INACTIVE) | ✅ Có (set status ACTIVE lại) |
| Product | Hard | ❌ Không |
| Category | Hard | ❌ Không |
| Brand | Hard | ❌ Không |
| Blog | Hard | ❌ Không |
| Promotion | Hard | ❌ Không |

---

# 14. 🛡️ Warranties — `/warranties`

---

## GET `/warranties/{id}`

**Auth:** MEMBER, STAFF, ADMIN

| Case | Code |
|------|------|
| ✅ Tìm thấy | 1000 |
| ❌ Không tồn tại | 404 |

---

## GET `/warranties/order/{orderId}`

**Auth:** MEMBER, STAFF, ADMIN

| Case | Code |
|------|------|
| ✅ Thành công | 1000, trả về list warranties |
| ❌ Không tìm thấy order | 404 |

---

## PUT `/warranties/{id}/status`

**Auth:** STAFF, ADMIN

**Request:**
```json
{
  "status": "ACTIVE"
}
```

* `status`: `ACTIVE`, `EXPIRED`, `VOIDED`

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ Không tồn tại | 404 |
| ❌ Thiếu status | 400 |

---

---

# 15. 🛠️ Warranty Claims — `/claims`

---

## POST `/claims` — Tạo claim mới

**Auth:** MEMBER, STAFF, ADMIN

**Request:**
```json
{
  "warrantyId": 1,
  "customerNote": "Màn hình bị sọc"
}
```

| Field | Bắt buộc | Validation |
|-------|----------|-----------|
| `warrantyId` | ✅ | Phải tồn tại |
| `customerNote` | ✅ | Không được rỗng |

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ Thiếu field / Rỗng | 400 |

---

## GET `/claims/{id}`

**Auth:** MEMBER, STAFF, ADMIN

| Case | Code |
|------|------|
| ✅ Tìm thấy | 1000 |
| ❌ Không tồn tại | 404 |

---

## GET `/claims/warranty/{warrantyId}`

**Auth:** MEMBER, STAFF, ADMIN

| Case | Code |
|------|------|
| ✅ Thành công | 1000, trả về list |
| ❌ Không tồn tại | 404 |

---

## PUT `/claims/{id}` — Cập nhật claim

**Auth:** STAFF, ADMIN

**Request:**
```json
{
  "status": "PROCESSING",
  "technicianNote": "Đang kiểm tra",
  "solutionType": "REPAIR"
}
```

* `status`: `PENDING`, `PROCESSING`, `COMPLETED`, `REJECTED`
* `solutionType`: `REPAIR`, `REPLACE`, `REFUND`

| Case | Code |
|------|------|
| ✅ Thành công | 1000 |
| ❌ Không tồn tại | 404 |

---

# 📋 Tóm tắt tất cả Endpoints

| Method | Endpoint | Auth | Mô tả |
|--------|----------|------|-------|
| **AUTH** | | | |
| POST | `/auth/login` | Public | Đăng nhập |
| POST | `/auth/introspect` | Public | Kiểm tra token |
| POST | `/auth/logout` | Public | Đăng xuất (blacklist token) |
| POST | `/auth/refresh` | Public | Refresh token |
| **USERS** | | | |
| POST | `/users` | Public | Đăng ký (role: MEMBER, status: ACTIVE) |
| GET | `/users` | STAFF/ADMIN | Danh sách user (chỉ ACTIVE) |
| GET | `/users/me` | Authenticated | Profile bản thân |
| PUT | `/users/me` | Authenticated | Cập nhật profile |
| GET | `/users/{id}` | STAFF/ADMIN | User theo ID |
| PUT | `/users/{id}` | STAFF/ADMIN | Cập nhật user |
| DELETE | `/users/{id}` | STAFF/ADMIN | Soft delete (→ INACTIVE) |
| **PRODUCTS** | | | |
| GET | `/products` | Public | List + filter products |
| GET | `/products/search?keyword=` | Public | Tìm kiếm |
| GET | `/products/{id}` | Public | Chi tiết sản phẩm |
| POST | `/products` | STAFF/ADMIN | Tạo sản phẩm (multipart) |
| PUT | `/products/{id}` | STAFF/ADMIN | Cập nhật sản phẩm (multipart) |
| DELETE | `/products/{id}` | STAFF/ADMIN | Xoá sản phẩm (hard delete) |
| **CATEGORIES** | | | |
| GET | `/categories` | Public | Tất cả category (flat list) |
| GET | `/categories/{id}` | Public | Category theo ID |
| POST | `/categories` | STAFF/ADMIN | Tạo category |
| PUT | `/categories/{id}` | STAFF/ADMIN | Cập nhật |
| DELETE | `/categories/{id}` | STAFF/ADMIN | Xoá (chặn nếu có sub-categories) |
| **BRANDS** | | | |
| GET | `/brands` | Public | Tất cả brand |
| GET | `/brands/{id}` | Public | Brand theo ID |
| POST | `/brands` | STAFF/ADMIN | Tạo brand (multipart) |
| PUT | `/brands/{id}` | STAFF/ADMIN | Cập nhật (multipart) |
| DELETE | `/brands/{id}` | STAFF/ADMIN | Xoá |
| **ATTRIBUTES** | | | |
| GET | `/attributes` | Public | Tất cả attribute |
| GET | `/attributes/{id}` | Public | Attribute theo ID |
| POST | `/attributes` | STAFF/ADMIN | Tạo attribute |
| PUT | `/attributes/{id}` | STAFF/ADMIN | Cập nhật |
| DELETE | `/attributes/{id}` | STAFF/ADMIN | Xoá |
| **CART** | | | |
| GET | `/cart` | Authenticated | Xem giỏ hàng (auto-create nếu chưa có) |
| POST | `/cart/items` | Authenticated | Thêm item (merge quantity nếu đã có) |
| PUT | `/cart/items/{id}` | Authenticated | Đổi số lượng (phải > 0) |
| DELETE | `/cart/items/{id}` | Authenticated | Xoá 1 item |
| DELETE | `/cart` | Authenticated | Xoá hết giỏ |
| **ORDERS** | | | |
| POST | `/orders` | Authenticated | Đặt hàng từ giỏ (tự xoá giỏ sau khi đặt) |
| GET | `/orders/me` | Authenticated | Đơn hàng của tôi |
| GET | `/orders/{id}` | Authenticated | Chi tiết đơn |
| GET | `/orders` | STAFF/ADMIN | Tất cả đơn hàng |
| PUT | `/orders/{id}/status` | STAFF/ADMIN | Cập nhật trạng thái |
| PUT | `/orders/{id}/cancel` | Authenticated | Huỷ đơn (chỉ PENDING + chủ đơn) |
| **PAYMENT** | | | |
| GET | `/orders/payment/createPayment` | Public | Tạo URL VNPay |
| **BLOGS** | | | |
| GET | `/blogs` | Public | Tất cả blog |
| GET | `/blogs/{id}` | Public | Blog theo ID |
| GET | `/blogs/user/{userId}` | Public | Blog theo user |
| POST | `/blogs` | Authenticated | Tạo blog (userId trong body, không từ JWT) |
| PUT | `/blogs/{id}` | Authenticated | Cập nhật blog |
| DELETE | `/blogs/{id}` | STAFF/ADMIN | Xoá blog |
| **PROMOTIONS** | | | |
| GET | `/promotions` | Public | Tất cả promotion |
| GET | `/promotions/{id}` | Public | Promotion theo ID |
| GET | `/promotions/code/{code}` | Public | Promotion theo mã |
| POST | `/promotions` | STAFF/ADMIN | Tạo promotion |
| PUT | `/promotions/{id}` | STAFF/ADMIN | Cập nhật |
| DELETE | `/promotions/{id}` | STAFF/ADMIN | Xoá (cascade xoá PromotionProduct links) |
| POST | `/promotions/add-to-products` | STAFF/ADMIN | Gắn vào sản phẩm (skip nếu đã có) |
| POST | `/promotions/add-to-category` | STAFF/ADMIN | Gắn vào category (skip nếu đã có) |
| POST | `/promotions/add-to-brand` | STAFF/ADMIN | Gắn vào brand (skip nếu đã có) |
| **ROLES** | | | |
| GET | `/roles` | ADMIN | Tất cả role |
| GET | `/roles/{id}` | ADMIN | Role theo ID |
| POST | `/roles` | ADMIN | Tạo role |
| PUT | `/roles/{id}` | ADMIN | Cập nhật |
| DELETE | `/roles/{id}` | ADMIN | Xoá |
| **WARRANTIES** | | | |
| GET | `/warranties/{id}` | Authenticated | Chi tiết warranty |
| GET | `/warranties/order/{orderId}` | Authenticated | Warranties của đơn hàng |
| PUT | `/warranties/{id}/status` | STAFF/ADMIN | Cập nhật trạng thái warranty |
| **CLAIMS** | | | |
| POST | `/claims` | Authenticated | Tạo yêu cầu bảo hành |
| GET | `/claims/{id}` | Authenticated | Chi tiết yêu cầu bảo hành |
| GET | `/claims/warranty/{warrantyId}` | Authenticated | Các yêu cầu bảo hành của warranty |
| PUT | `/claims/{id}` | STAFF/ADMIN | Cập nhật yêu cầu bảo hành |
| **PC BUILD** | | | |
| POST | `/pc-builds/compatible-variants` | MEMBER+ | Lấy categoryId + filter hints cho loại linh kiện |
| PUT | `/pc-builds/draft/items` | MEMBER+ | Upsert linh kiện vào draft build |
| PUT | `/pc-builds/draft/save` | MEMBER+ | Lưu draft với tên, chuyển sang SAVED |
| POST | `/pc-builds/draft/order` | MEMBER+ | Đặt hàng trực tiếp từ build hiện tại |

---

## 12. PC Build (Build PC)

Build PC là tính năng cho phép user lắp ráp cấu hình máy tính từng bước.  
FE gọi `compatible-variants` để lấy filter hints trước mỗi lần chọn linh kiện,  
sau đó dùng hints đó để lọc sản phẩm qua `/products`, chọn xong thì `upsert` vào draft.

### Sơ đồ luồng

```
Bước 0: Đăng nhập lấy token (member)
  │
  ▼
Bước 1: POST /pc-builds/compatible-variants  ← currentItems: []
  │      → { categoryId, hints: [] }
  │
  ▼
Bước 2: GET /products?categoryId={categoryId}&...hints...
  │      → Danh sách sản phẩm phù hợp
  │
  ▼
Bước 3: PUT /pc-builds/draft/items  ← chọn 1 variantId
  │
  └─ Lặp lại Bước 1-3 cho từng loại linh kiện (thêm item đã chọn vào currentItems)
  │
  ▼
Bước 4: PUT /pc-builds/draft/save   ← đặt tên build
  │
  ▼
Bước 5: POST /pc-builds/draft/order ← thông tin giao hàng + thanh toán
```

---

### 12.0 Đăng nhập lấy Bearer token

```http
POST /api/v1/auth/login
Content-Type: application/json
```
```json
{
  "email": "member1@example.com",
  "password": "Admin@123"
}
```
**Response:** `data.token` — dùng cho tất cả request tiếp theo.

---

### 12.1 Endpoint: Lấy compatible variants

```http
POST /api/v1/pc-builds/compatible-variants
Authorization: Bearer {token}
Content-Type: application/json
```

| Field | Type | Mô tả |
|---|---|---|
| `targetComponentType` | String (enum) | Loại linh kiện cần tìm |
| `currentItems` | Array | Danh sách linh kiện đã chọn trước đó |
| `currentItems[].componentType` | String | Enum: CPU, MAINBOARD, RAM, GPU, PSU, CASE, COOLING, ... |
| `currentItems[].variantId` | Integer | ID của variant đã chọn |

**Enum `targetComponentType`:** `CPU`, `MAINBOARD`, `RAM`, `GPU`, `STORAGE_PRIMARY`, `STORAGE_SECONDARY`, `PSU`, `CASE`, `COOLING`, `MONITOR`, `KEYBOARD`, `MOUSE`

**Response:**
```json
{
  "code": 1000,
  "result": {
    "categoryId": 3,
    "hints": [
      { "attributeName": "Socket", "requiredValue": "LGA1700", "ruleType": "MUST_MATCH", "comparison": "eq" },
      { "attributeName": "Memory Type", "requiredValue": "DDR5", "ruleType": "MUST_MATCH", "comparison": "eq" }
    ]
  }
}
```

| Field | Mô tả |
|---|---|
| `categoryId` | FE dùng để gọi `GET /products?categoryId={categoryId}` |
| `hints[].attributeName` | Tên attribute để filter |
| `hints[].requiredValue` | Giá trị filter |
| `hints[].ruleType` | `MUST_MATCH`, `MUST_FIT`, `MUST_SUPPORT`, `MIN_WATTAGE` |
| `hints[].comparison` | `eq` (bằng), `lte` (≤), `gte` (≥) |

---

### 12.2 Endpoint: Upsert item vào draft

```http
PUT /api/v1/pc-builds/draft/items
Authorization: Bearer {token}
Content-Type: application/json
```

| Field | Type | Mô tả |
|---|---|---|
| `componentType` | String (enum) | Loại linh kiện |
| `variantId` | Integer | ID variant đã chọn |
| `quantity` | Integer | Số lượng (MULTI_SLOT: RAM, STORAGE, ...) |

**Response:** Object PCBuildResponse chứa toàn bộ build hiện tại.

**Lỗi:**
- `10005` — Mainboard đã hết slot RAM khi thêm RAM thứ (maxSlots + 1)

---

### 12.2a Endpoint: Xem draft hiện tại

```http
GET /api/v1/pc-builds/draft
Authorization: Bearer {token}
```

Trả về DRAFT build hiện tại của user. Nếu chưa có DRAFT, server tự tạo mới (rỗng).

**Response:**
```json
{
  "code": 1000,
  "result": {
    "buildId": 1,
    "status": "DRAFT",
    "totalPrice": 15990000,
    "createdAt": "2026-03-12T22:41:17.43",
    "updatedAt": "2026-03-12T22:41:17.50",
    "items": [
      {
        "buildItemId": 1,
        "componentType": "CPU",
        "componentTypeName": "Bộ xử lý (CPU)",
        "variantId": 1,
        "variantName": "Intel Core i9-13900K Box",
        "sku": "CPU-I9-13900K-BOX",
        "price": 15990000,
        "quantity": 1,
        "subtotal": 15990000,
        "productId": 1,
        "productName": "Intel Core i9-13900K",
        "thumbnailUrl": "https://..."
      }
    ]
  }
}
```

---

### 12.2b Endpoint: Xem tất cả builds của tôi

```http
GET /api/v1/pc-builds
Authorization: Bearer {token}
```

Trả về toàn bộ builds (DRAFT, SAVED, ORDERED) của user, sắp xếp theo ngày tạo mới nhất.

**Response:**
```json
{
  "code": 1000,
  "result": [
    {
      "buildId": 2,
      "buildName": "Gaming PC DDR5",
      "status": "SAVED",
      "totalPrice": 45000000,
      "createdAt": "...",
      "updatedAt": "...",
      "items": [...]
    },
    {
      "buildId": 1,
      "status": "DRAFT",
      "totalPrice": 15990000,
      "createdAt": "...",
      "updatedAt": "...",
      "items": [...]
    }
  ]
}
```

---

### 12.3 Endpoint: Lưu build

```http
PUT /api/v1/pc-builds/draft/save
Authorization: Bearer {token}
Content-Type: application/json
```
```json
{ "buildName": "Intel Gaming Build 2026" }
```
**Response:** PCBuildResponse với `status: "SAVED"`.

**Lỗi:**
- `10003` — Build không có bất kỳ item nào

---

### 12.4 Endpoint: Đặt hàng từ build

```http
POST /api/v1/pc-builds/draft/order
Authorization: Bearer {token}
Content-Type: application/json
```
```json
{
  "paymentType": "FULL",
  "recipientName": "Nguyen Van A",
  "recipientPhone": "0901234567",
  "shippingAddress": "123 Nguyen Trai, Q1, TP.HCM"
}
```
**Response:** OrderResponse với `orderId`, `totalAmount`, `status: "PENDING"`.

**Lỗi:**
- `10003` — Build chưa có item
- `9003` — Không đủ hàng trong kho

---

### 12.5 Hướng dẫn test toàn bộ flow (Happy Path — Intel + DDR5 + ATX)

> **Dữ liệu init.sql:** Tất cả `variantId` tham chiếu dữ liệu seeded sẵn. Login bằng `member1@example.com` / `Admin@123`.

#### Bước 1 — Chọn CPU

**1a. Lấy hints (currentItems rỗng → không có rule nào → hints = [])**
```json
POST /pc-builds/compatible-variants
{
  "targetComponentType": "CPU",
  "currentItems": []
}
```
```json
// Response
{
  "code": 1000,
  "data": {
    "categoryId": 1,
    "hints": []
  }
}
```

**1b. Tìm CPU**
```
GET /api/v1/products?categoryId=1
```
→ Trả về tất cả CPU: i9-14900K Box (v1), i9-14900K Tray (v2), Ryzen 9 7950X (v3), Ryzen 5 5600X (v25)

**1c. Chọn i9-14900K Box (variantId = 1)**
```json
PUT /pc-builds/draft/items
{
  "componentType": "CPU",
  "variantId": 1,
  "quantity": 1
}
```
```json
// Response (build tự tạo DRAFT nếu chưa có)
{
  "code": 1000,
  "data": {
    "buildId": 1,
    "status": "DRAFT",
    "totalPrice": 589.99,
    "items": [
      { "componentType": "CPU", "variantId": 1, "variantName": "i9-14900K Box", "price": 589.99, "quantity": 1 }
    ]
  }
}
```

---

#### Bước 2 — Chọn Mainboard (lọc theo socket CPU)

**2a. Lấy hints (đưa CPU vào currentItems)**
```json
POST /pc-builds/compatible-variants
{
  "targetComponentType": "MAINBOARD",
  "currentItems": [
    { "componentType": "CPU", "variantId": 1 }
  ]
}
```
```json
// Response
{
  "code": 1000,
  "result": {
    "categoryId": 3,
    "hints": [
      { "attributeName": "Socket", "requiredValue": "LGA1700", "ruleType": "MUST_MATCH", "comparison": "eq" },
      { "attributeName": "Memory Type", "requiredValue": "DDR5", "ruleType": "MUST_MATCH", "comparison": "eq" }
    ]
  }
}
```

**2b. Tìm Mainboard phù hợp**
```
GET /api/v1/products?categoryId=3&attributes=Socket:LGA1700&attributes=Memory Type:DDR5
```
→ Chỉ Z790-E (v8) khớp `Socket=LGA1700 + MemType=DDR5`.  
X670E (v9) bị loại (AM5), B450M (v10) bị loại (AM4 + DDR4).

**2c. Chọn Z790-E (variantId = 8)**
```json
PUT /pc-builds/draft/items
{ "componentType": "MAINBOARD", "variantId": 8, "quantity": 1 }
```

---

#### Bước 3 — Chọn RAM (lọc theo MB: DDR5, ≤ 7200 MHz)

**3a. Lấy hints**
```json
POST /pc-builds/compatible-variants
{
  "targetComponentType": "RAM",
  "currentItems": [
    { "componentType": "CPU", "variantId": 1 },
    { "componentType": "MAINBOARD", "variantId": 8 }
  ]
}
```
```json
// Response
{
  "code": 1000,
  "data": {
    "categoryId": 4,
    "hints": [
      { "attributeName": "Memory Type", "value": "DDR5", "operator": "eq" },
      { "attributeName": "Memory Speed", "value": "7200", "operator": "lte" }
    ]
  }
}
```

**3b. Tìm RAM phù hợp**
```
GET /api/v1/products?categoryId=4&attributes=Memory Type:DDR5&attributes=Memory Speed:lte:7200
```
→ v11 (DDR5 16GB 6000MHz ✓), v12 (DDR5 32GB ✓), v13 (DDR5 64GB ✓) pass.  
v14 (DDR4) và v15 (DDR4) **không xuất hiện** (bị lọc bởi Memory Type=DDR5).

**3c. Thêm 2 thanh RAM 16GB (Z790-E có 4 slots)**
```json
PUT /pc-builds/draft/items
{ "componentType": "RAM", "variantId": 11, "quantity": 1 }
// → slot 1/4
PUT /pc-builds/draft/items
{ "componentType": "RAM", "variantId": 11, "quantity": 1 }
// → slot 2/4 (cùng variantId → quantity +1)
```

**3d. Test lỗi RAM_SLOTS_EXCEEDED (4-slot B450M)**

> Thay thế: nếu dùng B450M (v10, 2 slot DDR4) với Ryzen 5 5600X (v25, AM4, DDR4):
```json
// Thêm lần 3 vào build đang có 2 RAM và MB 2-slot → lỗi
PUT /pc-builds/draft/items
{ "componentType": "RAM", "variantId": 14, "quantity": 1 }
```
```json
// Response lỗi
{ "code": 10005, "message": "RAM slots exceeded" }
```

---

#### Bước 4 — Chọn GPU

**4a. Lấy hints (GPU chưa có rule với CPU/MB → hints có thể rỗng)**
```json
POST /pc-builds/compatible-variants
{
  "targetComponentType": "GPU",
  "currentItems": [
    { "componentType": "CPU", "variantId": 1 },
    { "componentType": "MAINBOARD", "variantId": 8 },
    { "componentType": "RAM", "variantId": 11 }
  ]
}
```
```json
{ "code": 1000, "data": { "categoryId": 2, "hints": [] } }
```

**4b. Tìm GPU**
```
GET /api/v1/products?categoryId=2
```

**4c. Chọn RTX 4090 Founders Edition (variantId = 6, length = 336mm, TDP = 450W)**
```json
PUT /pc-builds/draft/items
{ "componentType": "GPU", "variantId": 6, "quantity": 1 }
```

---

#### Bước 5 — Chọn Case (lọc theo GPU length + MB form factor)

**5a. Lấy hints**
```json
POST /pc-builds/compatible-variants
{
  "targetComponentType": "CASE",
  "currentItems": [
    { "componentType": "CPU", "variantId": 1 },
    { "componentType": "MAINBOARD", "variantId": 8 },
    { "componentType": "RAM", "variantId": 11 },
    { "componentType": "GPU", "variantId": 6 }
  ]
}
```
```json
{
  "code": 1000,
  "data": {
    "categoryId": 8,
    "hints": [
      { "attributeName": "Max GPU Length", "value": "336mm", "operator": "gte" },
      { "attributeName": "Form Factor", "value": "ATX", "operator": "eq" }
    ]
  }
}
```

> `requiredValue` giữ nguyên đơn vị từ DB ("336mm"). FE truyền nguyên vào URL — server dùng `extractNumber()` để parse.

**5b. Tìm Case**
```
GET /api/v1/products?categoryId=8&attributes=Max GPU Length:gte:336mm&attributes=Form Factor:ATX
```
→ H7 Flow Black (v19, ATX, 400mm ≥ 336mm ✓), H7 Flow White (v20 ✓) **pass**.  
Fractal Pop Mini (v21, mATX + maxGPU=300mm < 336mm) **bị lọc**.

**5c. Chọn H7 Flow Black (variantId = 19)**
```json
PUT /pc-builds/draft/items
{ "componentType": "CASE", "variantId": 19, "quantity": 1 }
```

---

#### Bước 6 — Chọn Cooling (lọc theo chiều cao tối đa của Case)

**6a. Lấy hints**
```json
POST /pc-builds/compatible-variants
{
  "targetComponentType": "COOLING",
  "currentItems": [
    { "componentType": "CPU", "variantId": 1 },
    { "componentType": "MAINBOARD", "variantId": 8 },
    { "componentType": "RAM", "variantId": 11 },
    { "componentType": "GPU", "variantId": 6 },
    { "componentType": "CASE", "variantId": 19 }
  ]
}
```
```json
{
  "code": 1000,
  "data": {
    "categoryId": 9,
    "hints": [
      { "attributeName": "Cooler Height", "value": "185mm", "operator": "lte" }
    ]
  }
}
```

**6b. Tìm Cooling**
```
GET /api/v1/products?categoryId=9&attributes=Cooler Height:lte:185mm
```
→ Noctua NH-D15 (v22, 165mm ≤ 185mm ✓) **pass**.

**6c. Chọn Noctua NH-D15 (variantId = 22)**
```json
PUT /pc-builds/draft/items
{ "componentType": "COOLING", "variantId": 22, "quantity": 1 }
```

---

#### Bước 7 — Chọn PSU (MIN_WATTAGE: CPU 125W + GPU 450W = 575W → min 700W)

**7a. Lấy hints**
```json
POST /pc-builds/compatible-variants
{
  "targetComponentType": "PSU",
  "currentItems": [
    { "componentType": "CPU", "variantId": 1 },
    { "componentType": "MAINBOARD", "variantId": 8 },
    { "componentType": "RAM", "variantId": 11 },
    { "componentType": "GPU", "variantId": 6 },
    { "componentType": "CASE", "variantId": 19 },
    { "componentType": "COOLING", "variantId": 22 }
  ]
}
```
```json
{
  "code": 1000,
  "data": {
    "categoryId": 7,
    "hints": [
      { "attributeName": "Wattage", "value": "700", "operator": "gte" }
    ]
  }
}
```

> **Tính toán:** TDP tổng = 125W (CPU) + 450W (GPU) = 575W → minWattage = ⌈575×1.2/50⌉×50 = ⌈13.8⌉×50 = **700W**

**7b. Tìm PSU**
```
GET /api/v1/products?categoryId=7&attributes=Wattage:gte:700
```
→ RM1000x 1000W (v18 ✓), RM750x 750W (v27 ✓) **pass**.  
CV650 650W (v26, 650 < 700) **KHÔNG xuất hiện** — đây là PSU boundary test case.

**7c. Chọn RM1000x (variantId = 18)**
```json
PUT /pc-builds/draft/items
{ "componentType": "PSU", "variantId": 18, "quantity": 1 }
```

---

#### Bước 8 — Chọn SSD (không có rule → hints rỗng)

```json
POST /pc-builds/compatible-variants
{
  "targetComponentType": "STORAGE_PRIMARY",
  "currentItems": [...]
}
// → { "categoryId": 5, "hints": [] }
```
```
GET /api/v1/products?categoryId=5
```
```json
PUT /pc-builds/draft/items
{ "componentType": "STORAGE_PRIMARY", "variantId": 16, "quantity": 1 }
```

---

#### Bước 9 — Lưu build

```json
PUT /api/v1/pc-builds/draft/save
{
  "buildName": "Intel Gaming Build i9 2026"
}
```
```json
// Response
{
  "code": 1000,
  "data": {
    "buildId": 1,
    "buildName": "Intel Gaming Build i9 2026",
    "status": "SAVED",
    "totalPrice": 2859.93,
    "items": [ ... ]
  }
}
```

---

#### Bước 10 — Đặt hàng từ build

```json
POST /api/v1/pc-builds/draft/order
{
  "paymentType": "FULL",
  "recipientName": "Nguyen Van A",
  "recipientPhone": "0901234567",
  "shippingAddress": "123 Nguyen Trai, Q1, TP.HCM"
}
```
```json
// Response
{
  "code": 1000,
  "data": {
    "orderId": 1,
    "totalAmount": 2859.93,
    "status": "PENDING",
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?..."
  }
}
```

---

### 12.6 Test Cases bổ sung

#### TC-01: Socket mismatch (AM4 CPU + LGA1700 Mainboard)
Lọc hints sẽ loại Mainboard không khớp socket — lỗi xảy ra ở **bước tìm sản phẩm**, không phải khi add.
1. Chọn CPU Ryzen 5 5600X (v25, AM4)
2. `POST /pc-builds/compatible-variants` targetType=MAINBOARD → hints: `[{Socket, AM4, eq}]`
3. `GET /products?categoryId=3&attributes=Socket:AM4` → Chỉ B450M (v10) xuất hiện. Z790-E (v8) và X670E (v9) bị lọc.

#### TC-02: DDR4 RAM với DDR5 Mainboard
1. Chọn CPU v1 (LGA1700, DDR5) + Mainboard v8 (DDR5)
2. Hints cho RAM: `[{Memory Type, DDR5, eq}, {Memory Speed, 7200, lte}]`
3. `GET /products?categoryId=4&attributes=Memory Type:DDR5` → RAM DDR4 (v14, v15) **không xuất hiện**.

#### TC-03: Tràn slot RAM
1. B450M (v10) chỉ có 2 RAM slots
2. Add RAM lần 1 → thành công (slot 1/2)
3. Add RAM lần 2 → thành công (slot 2/2)
4. Add RAM lần 3 → **lỗi 10005** (RAM_SLOTS_EXCEEDED)

#### TC-04: PSU không đủ wattage (650W < 700W)
Sau khi chọn i9-14900K (125W) + RTX 4090 FE (450W):
1. Hints PSU: `[{Wattage, 700, gte}]`
2. `GET /products?categoryId=7&attributes=Wattage:gte:700`
3. CV650 650W (v26) **không xuất hiện** trong kết quả — bị lọc bởi Wattage:gte:700.
4. RM750x 750W (v27) và RM1000x 1000W (v18) xuất hiện.

#### TC-05: Case quá nhỏ cho GPU
1. Chọn GPU RTX 4090 ASUS (v4, length=357mm)
2. Hints CASE: `[{Max GPU Length, 357mm, gte}, {Form Factor, ATX, eq}]`
3. `GET /products?categoryId=8&attributes=Max GPU Length:gte:357mm`
4. H7 Flow (v19, max=400mm ✓), H7 Flow White (v20 ✓) — pass.  
   Fractal Pop Mini (v21, max=300mm < 357mm) — không xuất hiện.

#### TC-06: Đặt hàng khi build chưa có item (lỗi 10003)
```json
POST /api/v1/pc-builds/draft/order
{ "paymentType": "FULL", "recipientName": "A", "recipientPhone": "0900000000", "shippingAddress": "ABC" }
// → { "code": 10003, "message": "PC build has no items" }
```

---

### 12.7 Lưu ý kỹ thuật

| Điểm | Chi tiết |
|---|---|
| **Stateless hints** | `POST /compatible-variants` không đọc DB build — FE tự giữ state `currentItems` |
| **SINGLE_SLOT types** | CPU, MAINBOARD, GPU, PSU, CASE — chỉ 1 item mỗi loại; add lại sẽ **overwrite** |
| **MULTI_SLOT types** | RAM, STORAGE_PRIMARY, STORAGE_SECONDARY, COOLING, MONITOR, KEYBOARD, MOUSE — có thể có nhiều |
| **RAM slot check** | Chỉ áp dụng cho RAM. Số slot lấy từ attribute `RAM Slots` của Mainboard variant |
| **Draft auto-create** | Nếu user chưa có DRAFT build, cả `GET /draft` lẫn `PUT /draft/items` đều tự tạo DRAFT mới rỗng |
| **Sau khi order** | Build status chuyển sang `ORDERED`. User cần build mới nếu muốn tiếp tục |
| **Compatibility filter** | Chỉ lọc ở tầng search — server **không** chặn add item không tương thích (trừ RAM slot) |

---

---

# 13. 📊 Reports — `/reports`

> **Auth chung:** Tất cả endpoint trong section này yêu cầu role `ADMIN` hoặc `STAFF`.
> Các endpoint `/export` trả về file `.xlsx` thay vì JSON.

---

## GET `/reports/revenue/time` — Doanh thu theo thời gian

**Auth:** ADMIN, STAFF

**Query Parameters:**

| Param | Bắt buộc | Kiểu | Mặc định | Mô tả |
|-------|----------|------|----------|-------|
| `fromDate` | ❌ | `date` (yyyy-MM-dd) | 1 tháng trước | Ngày bắt đầu |
| `toDate` | ❌ | `date` (yyyy-MM-dd) | Hôm nay | Ngày kết thúc |
| `groupBy` | ❌ | `DAY` \| `MONTH` \| `YEAR` | `MONTH` | Đơn vị nhóm |

**Ví dụ request:**
```
GET /api/v1/reports/revenue/time?fromDate=2025-01-01&toDate=2025-03-31&groupBy=MONTH
```

**Response — Thành công:**
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "fromDate": "2025-01-01",
    "toDate": "2025-03-31",
    "groupBy": "MONTH",
    "totalRevenue": 250000000.0,
    "totalOrders": 18,
    "breakdown": [
      { "period": "2025-01", "revenue": 75000000.0, "orderCount": 5 },
      { "period": "2025-02", "revenue": 90000000.0, "orderCount": 7 },
      { "period": "2025-03", "revenue": 85000000.0, "orderCount": 6 }
    ]
  }
}
```

> Chỉ tính các đơn có `status = COMPLETED`.
> Khi `groupBy=DAY`, `period` có dạng `yyyy-MM-dd` (ví dụ `"2025-03-15"`).
> Khi `groupBy=YEAR`, `period` có dạng `"2025"`.

| Case | Code | Message |
|------|------|---------|
| ✅ Thành công | 1000 | null |
| ❌ Không có quyền | 1004 | "Access Denied" |
| ❌ Token không hợp lệ | 1003 | "Unauthenticated" |

---

## GET `/reports/revenue/time/export` — Export Excel doanh thu theo thời gian

**Auth:** ADMIN, STAFF

**Query Parameters:** Giống hệt `GET /reports/revenue/time`

**Ví dụ request:**
```
GET /api/v1/reports/revenue/time/export?fromDate=2025-01-01&toDate=2025-03-31&groupBy=MONTH
```

**Response:**
File `.xlsx` được tải về trực tiếp.

| Header | Giá trị |
|--------|---------|
| `Content-Type` | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` |
| `Content-Disposition` | `attachment; filename="doanh-thu-theo-thoi-gian_2025-01-01_2025-03-31.xlsx"` |

**Cấu trúc file Excel:**

| Cột | Nội dung |
|-----|---------|
| A | STT |
| B | Kỳ (ngày / tháng / năm tuỳ `groupBy`) |
| C | Doanh thu (VNĐ) — format `#,##0` |
| D | Số đơn hàng |

> Phần đầu sheet gồm tiêu đề, khoảng thời gian và tổng doanh thu / tổng đơn.

| Case | Kết quả |
|------|---------|
| ✅ Thành công | Download file `.xlsx` |
| ❌ Không có quyền | 1004 |
| ❌ Token không hợp lệ | 1003 |

---

## GET `/reports/revenue/product` — Doanh thu theo sản phẩm

**Auth:** ADMIN, STAFF

**Query Parameters:**

| Param | Bắt buộc | Kiểu | Mặc định | Mô tả |
|-------|----------|------|----------|-------|
| `fromDate` | ❌ | `date` (yyyy-MM-dd) | 1 tháng trước | Ngày bắt đầu |
| `toDate` | ❌ | `date` (yyyy-MM-dd) | Hôm nay | Ngày kết thúc |
| `limit` | ❌ | int | `10` | Giới hạn số sản phẩm trả về (top N) |

**Ví dụ request:**
```
GET /api/v1/reports/revenue/product?fromDate=2025-01-01&toDate=2025-03-31&limit=5
```

**Response — Thành công:**
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "fromDate": "2025-01-01",
    "toDate": "2025-03-31",
    "totalRevenue": 250000000.0,
    "products": [
      {
        "productName": "ASUS ROG Strix G16",
        "variantName": "RTX 4070 / 16GB / 1TB",
        "totalSold": 5,
        "revenue": 120000000.0
      },
      {
        "productName": "Dell XPS 15",
        "variantName": "Core i7 / 32GB / 512GB",
        "totalSold": 3,
        "revenue": 75000000.0
      }
    ]
  }
}
```

> Danh sách được sắp xếp theo `revenue` giảm dần (sản phẩm doanh thu cao nhất đứng đầu).
> Chỉ tính các đơn có `status = COMPLETED`.
> `totalRevenue` là tổng doanh thu của các sản phẩm trong danh sách (sau khi áp dụng `limit`).

| Case | Code | Message |
|------|------|---------|
| ✅ Thành công | 1000 | null |
| ❌ Không có quyền | 1004 | "Access Denied" |
| ❌ Token không hợp lệ | 1003 | "Unauthenticated" |

---

## GET `/reports/revenue/product/export` — Export Excel doanh thu theo sản phẩm

**Auth:** ADMIN, STAFF

**Query Parameters:** Giống hệt `GET /reports/revenue/product`

**Ví dụ request:**
```
GET /api/v1/reports/revenue/product/export?fromDate=2025-01-01&toDate=2025-03-31&limit=10
```

**Response:**
File `.xlsx` được tải về trực tiếp.

| Header | Giá trị |
|--------|---------|
| `Content-Type` | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` |
| `Content-Disposition` | `attachment; filename="doanh-thu-theo-san-pham_2025-01-01_2025-03-31.xlsx"` |

**Cấu trúc file Excel:**

| Cột | Nội dung |
|-----|---------|
| A | STT |
| B | Tên sản phẩm |
| C | Biến thể (variant) |
| D | Số lượng bán |
| E | Doanh thu (VNĐ) — format `#,##0` |

| Case | Kết quả |
|------|---------|
| ✅ Thành công | Download file `.xlsx` |
| ❌ Không có quyền | 1004 |
| ❌ Token không hợp lệ | 1003 |

---

## GET `/reports/revenue/installment` — Doanh thu trả góp

**Auth:** ADMIN, STAFF

**Không có query parameter.**

**Ví dụ request:**
```
GET /api/v1/reports/revenue/installment
```

**Response — Thành công:**
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "summary": {
      "totalPaid": 45000000.0,
      "totalUnpaid": 20000000.0,
      "totalOverdue": 5000000.0,
      "total": 70000000.0
    },
    "orders": [
      {
        "orderId": 12,
        "customerUsername": "nguyenvana",
        "orderTotal": 30000000.0,
        "totalInstallments": 6,
        "paidInstallments": 4,
        "remainingInstallments": 2,
        "nextDueDate": "2025-04-15"
      },
      {
        "orderId": 8,
        "customerUsername": "tranthib",
        "orderTotal": 40000000.0,
        "totalInstallments": 12,
        "paidInstallments": 12,
        "remainingInstallments": 0,
        "nextDueDate": "Đã hoàn tất"
      }
    ]
  }
}
```

**Giải thích `summary`:**

| Field | Mô tả |
|-------|-------|
| `totalPaid` | Tổng tiền đã thu thành công (status = `PAID`) |
| `totalUnpaid` | Tổng tiền chưa thu, chưa đến hạn (status = `UNPAID`) |
| `totalOverdue` | Tổng tiền quá hạn chưa thanh toán (status = `OVERDUE`) |
| `total` | Tổng cộng toàn bộ (paid + unpaid + overdue) |

**Giải thích từng đơn trong `orders`:**

| Field | Mô tả |
|-------|-------|
| `orderId` | Mã đơn hàng |
| `customerUsername` | Username của khách |
| `orderTotal` | Tổng giá trị đơn hàng |
| `totalInstallments` | Tổng số kỳ trả góp |
| `paidInstallments` | Số kỳ đã thanh toán |
| `remainingInstallments` | Số kỳ còn lại |
| `nextDueDate` | Ngày đến hạn kỳ gần nhất chưa trả (`yyyy-MM-dd`), hoặc `"Đã hoàn tất"` nếu trả hết |

> Chỉ bao gồm các đơn có `payment_type = INSTALLMENT`.
> Danh sách sắp xếp theo `orderId` giảm dần (đơn mới nhất đứng đầu).

| Case | Code | Message |
|------|------|---------|
| ✅ Thành công | 1000 | null |
| ❌ Không có quyền | 1004 | "Access Denied" |
| ❌ Token không hợp lệ | 1003 | "Unauthenticated" |

---

## GET `/reports/revenue/installment/export` — Export Excel doanh thu trả góp

**Auth:** ADMIN, STAFF

**Không có query parameter.**

**Ví dụ request:**
```
GET /api/v1/reports/revenue/installment/export
```

**Response:**
File `.xlsx` được tải về trực tiếp.

| Header | Giá trị |
|--------|---------|
| `Content-Type` | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` |
| `Content-Disposition` | `attachment; filename="doanh-thu-tra-gop_2025-03-31.xlsx"` |

**Cấu trúc file Excel — 2 sheets:**

**Sheet 1: "Tổng quan trả góp"**

| Hàng | Nội dung |
|------|---------|
| 1 | Tiêu đề: `TỔNG QUAN DOANH THU TRẢ GÓP` |
| 3 | Tổng đã thu (PAID) |
| 4 | Tổng chưa thu (UNPAID) |
| 5 | Tổng quá hạn (OVERDUE) |
| 7 | TỔNG CỘNG |

**Sheet 2: "Chi tiết đơn trả góp"**

| Cột | Nội dung |
|-----|---------|
| A | STT |
| B | Mã đơn |
| C | Khách hàng (username) |
| D | Tổng đơn (VNĐ) — format `#,##0` |
| E | Tổng kỳ |
| F | Đã trả (số kỳ) |
| G | Còn lại (số kỳ) |
| H | Ngày đến hạn kỳ gần nhất |

| Case | Kết quả |
|------|---------|
| ✅ Thành công | Download file `.xlsx` |
| ❌ Không có quyền | 1004 |
| ❌ Token không hợp lệ | 1003 |
