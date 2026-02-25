# =====================================================
# API Test Script - SBA301 Computer Shop
# Base URL: http://localhost:8080/api/v1
# Run backend first, then execute this script.
# =====================================================

$BASE = "http://localhost:8080/api/v1"
$ADMIN_EMAIL   = "admin@computershop.com"
$STAFF_EMAIL   = "staff1@computershop.com"
$MEMBER_EMAIL  = "member1@example.com"
$PASSWORD      = "Admin@123"

$global:passCount = 0
$global:failCount = 0

function Log-Pass([string]$msg) {
    Write-Host "  [PASS] $msg" -ForegroundColor Green
    $global:passCount++
}

function Log-Fail([string]$msg, [string]$detail = "") {
    Write-Host "  [FAIL] $msg" -ForegroundColor Red
    if ($detail) { Write-Host "         $detail" -ForegroundColor DarkRed }
    $global:failCount++
}

function Log-Section([string]$title) {
    Write-Host ""
    Write-Host "=====================================================" -ForegroundColor Cyan
    Write-Host "  $title" -ForegroundColor Cyan
    Write-Host "=====================================================" -ForegroundColor Cyan
}

function Call-API {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body = $null,
        [string]$Token = $null,
        [string]$Label = "Request"
    )
    $Uri = "$BASE$Path"
    $Headers = @{}
    if ($Token) { $Headers["Authorization"] = "Bearer $Token" }

    try {
        $params = @{
            Method      = $Method
            Uri         = $Uri
            ContentType = "application/json"
            ErrorAction = "Stop"
        }
        if ($Headers.Count -gt 0) { $params["Headers"] = $Headers }
        if ($Body) { $params["Body"] = ($Body | ConvertTo-Json -Depth 10) }

        $resp = Invoke-RestMethod @params
        return $resp
    } catch {
        $statusCode = $null
        if ($_.Exception.Response) { $statusCode = [int]$_.Exception.Response.StatusCode }
        $raw = $null
        if ($_.ErrorDetails) { $raw = $_.ErrorDetails.Message }
        $parsed = $null
        if ($raw) { try { $parsed = $raw | ConvertFrom-Json } catch {} }
        $msg = if ($parsed -and $parsed.message) { $parsed.message } else { $_.Exception.Message }
        return @{ _error = $true; _status = $statusCode; _msg = $msg }
    }
}

function Assert-OK {
    param([object]$resp, [string]$label)
    if ($resp._error) {
        Log-Fail $label "HTTP $($resp._status): $($resp._msg)"
    } else {
        Log-Pass $label
    }
}

function Assert-Fail {
    param([object]$resp, [string]$label)
    if ($resp._error) {
        Log-Pass "$label (expected error OK)"
    } else {
        Log-Fail "$label (expected error but got success)"
    }
}

# ─────────────────────────────────────────────
# 1. AUTH
# ─────────────────────────────────────────────
Log-Section "1. AUTHENTICATION"

# Login as ADMIN
$r = Call-API POST "/auth/login" @{ email=$ADMIN_EMAIL; password=$PASSWORD } -Label "Login admin"
Assert-OK $r "Login admin"
$ADMIN_TOKEN = $r.result.token

# Login as STAFF
$r = Call-API POST "/auth/login" @{ email=$STAFF_EMAIL; password=$PASSWORD }
Assert-OK $r "Login staff"
$STAFF_TOKEN = $r.result.token

# Login as MEMBER
$r = Call-API POST "/auth/login" @{ email=$MEMBER_EMAIL; password=$PASSWORD }
Assert-OK $r "Login member"
$MEMBER_TOKEN = $r.result.token

# Wrong password
$r = Call-API POST "/auth/login" @{ email=$MEMBER_EMAIL; password="WrongPass!" }
Assert-Fail $r "Login with wrong password"

# Introspect valid token
$r = Call-API POST "/auth/introspect" @{ token=$ADMIN_TOKEN }
Assert-OK $r "Introspect valid token"
if ($r.result.valid -eq $true) { Log-Pass "Token is valid=true" } else { Log-Fail "Token valid should be true" }

# Refresh token
$r = Call-API POST "/auth/refresh" @{ token=$MEMBER_TOKEN }
Assert-OK $r "Refresh member token"
if ($r.result.token) { $MEMBER_TOKEN = $r.result.token; Log-Pass "New token received" }

# Logout
$r = Call-API POST "/auth/logout" @{ token=$STAFF_TOKEN }
Assert-OK $r "Logout staff"
# Introspect after logout should show invalid
$r = Call-API POST "/auth/introspect" @{ token=$STAFF_TOKEN }
if ($r.result.valid -eq $false) { Log-Pass "Token marked invalid after logout" } else { Log-Fail "Token should be invalid after logout" }
# Re-login staff
$r = Call-API POST "/auth/login" @{ email=$STAFF_EMAIL; password=$PASSWORD }
$STAFF_TOKEN = $r.result.token


# ─────────────────────────────────────────────
# 2. USER
# ─────────────────────────────────────────────
Log-Section "2. USERS"

$r = Call-API GET "/users/me" -Token $MEMBER_TOKEN
Assert-OK $r "GET /users/me (member)"
if ($r.result.email -eq $MEMBER_EMAIL) { Log-Pass "Email matches" } else { Log-Fail "Email mismatch: $($r.result.email)" }

$r = Call-API GET "/users" -Token $ADMIN_TOKEN
Assert-OK $r "GET /users (admin)"
if ($r.result.Count -ge 4) { Log-Pass "At least 4 users returned" } else { Log-Fail "Expected >=4 users, got $($r.result.Count)" }

# Member trying to list all users → forbidden
$r = Call-API GET "/users" -Token $MEMBER_TOKEN
Assert-Fail $r "GET /users as MEMBER (should be 403)"

$r = Call-API PUT "/users/me" -Token $MEMBER_TOKEN -Body @{ phoneNumber="0999888777" }
Assert-OK $r "PUT /users/me update phone"
if ($r.result.phoneNumber -eq "0999888777") { Log-Pass "Phone updated" }


# ─────────────────────────────────────────────
# 3. CATEGORIES
# ─────────────────────────────────────────────
Log-Section "3. CATEGORIES"

$r = Call-API GET "/categories"
Assert-OK $r "GET /categories (public)"
if ($r.result.Count -ge 16) { Log-Pass "At least 16 categories" }

$r = Call-API GET "/categories/1"
Assert-OK $r "GET /categories/1 (CPU)"
if ($r.result.categoryName -eq "CPU") { Log-Pass "Category name = CPU" }

# Create new sub-category
$r = Call-API POST "/categories" -Token $ADMIN_TOKEN -Body @{ categoryName="Gaming Mice"; parentCategoryId=12 }
Assert-OK $r "POST /categories (create sub-category)"
$newCatId = $r.result.categoryId

# Self-parent check
$r = Call-API PUT "/categories/$newCatId" -Token $ADMIN_TOKEN -Body @{ categoryName="Gaming Mice"; parentCategoryId=$newCatId }
Assert-Fail $r "PUT /categories - self-parent (should error)"

# Delete the new category
$r = Call-API DELETE "/categories/$newCatId" -Token $ADMIN_TOKEN
Assert-OK $r "DELETE /categories/$newCatId"

# Delete category with children → should fail
$r = Call-API DELETE "/categories/1" -Token $ADMIN_TOKEN
Assert-Fail $r "DELETE /categories/1 (has children - should error)"


# ─────────────────────────────────────────────
# 4. BRANDS
# ─────────────────────────────────────────────
Log-Section "4. BRANDS"

$r = Call-API GET "/brands"
Assert-OK $r "GET /brands (public)"

$r = Call-API GET "/brands/1"
Assert-OK $r "GET /brands/1"
if ($r.result.brandName -eq "Intel") { Log-Pass "Brand name = Intel" }


# ─────────────────────────────────────────────
# 5. ATTRIBUTES
# ─────────────────────────────────────────────
Log-Section "5. ATTRIBUTES"

$r = Call-API GET "/attributes"
Assert-OK $r "GET /attributes (public)"

$r = Call-API POST "/attributes" -Token $ADMIN_TOKEN -Body @{ attributeName="Backlight"; description="RGB lighting type" }
Assert-OK $r "POST /attributes"
$newAttrId = $r.result.attributeId

$r = Call-API PUT "/attributes/$newAttrId" -Token $ADMIN_TOKEN -Body @{ attributeName="Backlight Type"; description="RGB or single-zone" }
Assert-OK $r "PUT /attributes/$newAttrId"

$r = Call-API DELETE "/attributes/$newAttrId" -Token $ADMIN_TOKEN
Assert-OK $r "DELETE /attributes/$newAttrId"


# ─────────────────────────────────────────────
# 6. PRODUCTS
# ─────────────────────────────────────────────
Log-Section "6. PRODUCTS"

$r = Call-API GET "/products"
Assert-OK $r "GET /products (public)"
if ($r.result.Count -ge 10) { Log-Pass "At least 10 products" }

$r = Call-API GET "/products/3"
Assert-OK $r "GET /products/3 (RTX 4090)"
if ($r.result.name -like "*4090*") { Log-Pass "Product name contains 4090" }

# Search
$r = Call-API GET "/products?search=Corsair"
Assert-OK $r "GET /products?search=Corsair"

# Filter by brand
$r = Call-API GET "/products?brandId=1"
Assert-OK $r "GET /products?brandId=1 (Intel)"

# (POST/PUT/DELETE product tests skipped - require multipart/form-data upload)


# ─────────────────────────────────────────────
# 7. PROMOTIONS
# ─────────────────────────────────────────────
Log-Section "7. PROMOTIONS"

$r = Call-API GET "/promotions"
Assert-OK $r "GET /promotions (public)"

$r = Call-API GET "/promotions/1"
Assert-OK $r "GET /promotions/1"

$promoCode = "TESTPROMO-$(Get-Date -Format 'MMddHHmmss')"
$r = Call-API POST "/promotions" -Token $ADMIN_TOKEN -Body @{
    promoCode=$promoCode; discountPercent=20
    startDate="2026-01-01"; endDate="2026-12-31"
}
Assert-OK $r "POST /promotions"
$newPromoId = $r.result.promotionId

# Apply promotion to product
$r = Call-API POST "/promotions/add-to-products" -Token $ADMIN_TOKEN -Body @{ promotionId=$newPromoId; productIds=@(1,2) }
Assert-OK $r "POST /promotions/add-to-products"

# Check product now has discount
$r = Call-API GET "/products/1"
if ($r.result.hasPromotion -eq $true) { Log-Pass "Product 1 has promotion" } else { Log-Fail "Product 1 should have promotion" }

if ($newPromoId) {
    $r = Call-API DELETE "/promotions/$newPromoId" -Token $ADMIN_TOKEN
    Assert-OK $r "DELETE /promotions/$newPromoId"
} else {
    Log-Fail "DELETE /promotions/\$newPromoId (skipped - POST failed)"
}


# ─────────────────────────────────────────────
# 8. BLOGS
# ─────────────────────────────────────────────
Log-Section "8. BLOGS"

$r = Call-API GET "/blogs"
Assert-OK $r "GET /blogs (public)"
if ($r.result.Count -ge 3) { Log-Pass "At least 3 blogs" }

$r = Call-API POST "/blogs" -Token $MEMBER_TOKEN -Body @{
    title="My First Test Blog"
    content="This is test content from the API test script."
}
Assert-OK $r "POST /blogs (member)"
$newBlogId = $r.result.blogId

$r = Call-API PUT "/blogs/$newBlogId" -Token $MEMBER_TOKEN -Body @{
    title="My First Test Blog (Edited)"
    content="Updated content."
}
Assert-OK $r "PUT /blogs/$newBlogId"

$r = Call-API DELETE "/blogs/$newBlogId" -Token $ADMIN_TOKEN
Assert-OK $r "DELETE /blogs/$newBlogId"


# ─────────────────────────────────────────────
# 9. CART
# ─────────────────────────────────────────────
Log-Section "9. CART"

# Get cart (auto-creates if not exists)
$r = Call-API GET "/cart" -Token $MEMBER_TOKEN
Assert-OK $r "GET /cart (member)"
if ($r.result.totalItems -eq 0) { Log-Pass "Cart starts empty" }

# Add item - RTX 4090 FE (variantId=6), qty=1
$r = Call-API POST "/cart/items" -Token $MEMBER_TOKEN -Body @{ variantId=6; quantity=1 }
Assert-OK $r "POST /cart/items - add RTX 4090"
if ($r.result.totalItems -eq 1) { Log-Pass "Cart has 1 item" }

# Add same variant again (should merge)
$r = Call-API POST "/cart/items" -Token $MEMBER_TOKEN -Body @{ variantId=6; quantity=1 }
Assert-OK $r "POST /cart/items - add same variant (merge)"
if ($r.result -and $r.result.items -and $r.result.items.Count -gt 0 -and $r.result.items[0].quantity -eq 2) { Log-Pass "Quantity merged to 2" } else { Log-Fail "Quantity should have merged to 2" }

# Add second item - SSD 2TB (variantId=13)
$r = Call-API POST "/cart/items" -Token $MEMBER_TOKEN -Body @{ variantId=13; quantity=2 }
Assert-OK $r "POST /cart/items - add SSD 2TB x2"
if ($r.result.totalItems -eq 4) { Log-Pass "Total 4 items in cart" }
$cartItemId = if ($r.result.items -and $r.result.items.Count -gt 0) { $r.result.items[0].cartItemId } else { $null }

# Update quantity
$r = Call-API PUT "/cart/items/$cartItemId" -Token $MEMBER_TOKEN -Body @{ quantity=1 }
Assert-OK $r "PUT /cart/items/$cartItemId - set qty to 1"
if ($r.result -and $r.result.items -and $r.result.items.Count -gt 0 -and $r.result.items[0].quantity -eq 1) { Log-Pass "Quantity updated to 1" }

# Try invalid quantity
$r = Call-API PUT "/cart/items/$cartItemId" -Token $MEMBER_TOKEN -Body @{ quantity=0 }
Assert-Fail $r "PUT /cart/items with qty=0 (should error)"

# Remove one item
$r2 = Call-API GET "/cart" -Token $MEMBER_TOKEN
$ssdCartItemId = $r2.result.items | Where-Object { $_.variantId -eq 13 } | Select-Object -First 1 -ExpandProperty cartItemId
if ($ssdCartItemId) {
    $r = Call-API DELETE "/cart/items/$ssdCartItemId" -Token $MEMBER_TOKEN
    Assert-OK $r "DELETE /cart/items/$ssdCartItemId (remove SSD)"
}

# Verify cart state
$r = Call-API GET "/cart" -Token $MEMBER_TOKEN
Assert-OK $r "GET /cart after remove"
if ($r.result.items.Count -eq 1) { Log-Pass "Cart has 1 item remaining" } else { Log-Fail "Expected 1 item, got $($r.result.items.Count)" }


# ─────────────────────────────────────────────
# 10. ORDERS
# ─────────────────────────────────────────────
Log-Section "10. ORDERS"

# Ensure cart has something before ordering (add SSD back)
$r = Call-API POST "/cart/items" -Token $MEMBER_TOKEN -Body @{ variantId=12; quantity=1 }
Assert-OK $r "POST /cart/items - add SSD 1TB before order"

# Place order (FULL payment)
$orderBody = @{
    recipientName    = "Nguyen Van A"
    recipientPhone   = "0901111222"
    shippingAddress  = "123 Nguyen Hue, Q1, TP.HCM"
    paymentType      = "FULL"
}
$r = Call-API POST "/orders" -Token $MEMBER_TOKEN -Body $orderBody
Assert-OK $r "POST /orders (FULL payment)"
$orderId = $r.result.orderId
if ($r.result.status -eq "PENDING") { Log-Pass "Order status = PENDING" }
if ($r.result.payments.Count -ge 1) { Log-Pass "Payment schedule created" }

# Verify cart is cleared after order
$r = Call-API GET "/cart" -Token $MEMBER_TOKEN
if ($r.result.totalItems -eq 0) { Log-Pass "Cart cleared after placing order" }

# Get order by ID
$r = Call-API GET "/orders/$orderId" -Token $MEMBER_TOKEN
Assert-OK $r "GET /orders/$orderId"
if ($r.result.orderId -eq $orderId) { Log-Pass "Order ID matches" }

# Get my orders
$r = Call-API GET "/orders/me" -Token $MEMBER_TOKEN
Assert-OK $r "GET /orders/me"
if ($r.result.Count -ge 1) { Log-Pass "At least 1 order in history" }

# Staff/Admin get all orders
$r = Call-API GET "/orders" -Token $STAFF_TOKEN
Assert-OK $r "GET /orders (staff - all orders)"

# Update order status (staff)
$r = Call-API PUT "/orders/$orderId/status" -Token $STAFF_TOKEN -Body @{ status="PROCESSING" }
Assert-OK $r "PUT /orders/$orderId/status -> PROCESSING"
if ($r.result.status -eq "PROCESSING") { Log-Pass "Status updated to PROCESSING" }

# Member tries to update status → forbidden
$r = Call-API PUT "/orders/$orderId/status" -Token $MEMBER_TOKEN -Body @{ status="CANCELLED" }
Assert-Fail $r "PUT /orders/$orderId/status as MEMBER (should 403)"

# Place another order and cancel it
$r = Call-API POST "/cart/items" -Token $MEMBER_TOKEN -Body @{ variantId=16; quantity=1 }
$r = Call-API POST "/orders" -Token $MEMBER_TOKEN -Body $orderBody
$cancelOrderId = $r.result.orderId
Assert-OK $r "POST /orders - order to cancel"

$r = Call-API PUT "/orders/$cancelOrderId/cancel" -Token $MEMBER_TOKEN
Assert-OK $r "PUT /orders/$cancelOrderId/cancel"

# Try installment order
$r = Call-API POST "/cart/items" -Token $MEMBER_TOKEN -Body @{ variantId=18; quantity=1 }
$installOrderBody = @{
    recipientName   = "Tran Thi B"
    recipientPhone  = "0902222333"
    shippingAddress = "456 Le Loi, Q3, TP.HCM"
    paymentType     = "INSTALLMENT"
    providerName    = "FPT Finance"
    durationMonths  = 12
    interestRate    = 1.5
}
$r = Call-API POST "/orders" -Token $MEMBER_TOKEN -Body $installOrderBody
Assert-OK $r "POST /orders (INSTALLMENT 12 months)"
if ($r.result.payments.Count -eq 12) { Log-Pass "12 installment schedules created" } else { Log-Fail "Expected 12 payments, got $($r.result.payments.Count)" }


# ─────────────────────────────────────────────
# 11. CLEAR CART
# ─────────────────────────────────────────────
Log-Section "11. CLEAR CART"

# Add a few items then clear
Call-API POST "/cart/items" -Token $MEMBER_TOKEN -Body @{ variantId=15; quantity=1 } | Out-Null
Call-API POST "/cart/items" -Token $MEMBER_TOKEN -Body @{ variantId=9; quantity=2 } | Out-Null
$r = Call-API DELETE "/cart" -Token $MEMBER_TOKEN
Assert-OK $r "DELETE /cart (clear all)"
if ($r.result.totalItems -eq 0) { Log-Pass "Cart is empty after clear" }


# ─────────────────────────────────────────────
# 12. ROLES (Admin only)
# ─────────────────────────────────────────────
Log-Section "12. ROLES"

$r = Call-API GET "/roles" -Token $ADMIN_TOKEN
Assert-OK $r "GET /roles (admin)"
if ($r.result.Count -ge 3) { Log-Pass "At least 3 roles" }

$r = Call-API POST "/roles" -Token $ADMIN_TOKEN -Body @{ name="MODERATOR" }
Assert-OK $r "POST /roles (create)"
$newRoleId = $r.result.roleId

# Duplicate role
$r = Call-API POST "/roles" -Token $ADMIN_TOKEN -Body @{ name="MODERATOR" }
Assert-Fail $r "POST /roles duplicate (should error)"

$r = Call-API DELETE "/roles/$newRoleId" -Token $ADMIN_TOKEN
Assert-OK $r "DELETE /roles/$newRoleId"

# Member cannot manage roles
$r = Call-API GET "/roles" -Token $MEMBER_TOKEN
Assert-Fail $r "GET /roles as MEMBER (should 403)"


# ─────────────────────────────────────────────
# SUMMARY
# ─────────────────────────────────────────────
Write-Host ""
Write-Host "=====================================================" -ForegroundColor Yellow
Write-Host "  TEST SUMMARY" -ForegroundColor Yellow
Write-Host "=====================================================" -ForegroundColor Yellow
Write-Host "  PASSED: $global:passCount" -ForegroundColor Green
Write-Host "  FAILED: $global:failCount" -ForegroundColor Red
$total = $global:passCount + $global:failCount
Write-Host "  TOTAL:  $total"
$pct = if ($total -gt 0) { [math]::Round($global:passCount / $total * 100, 1) } else { 0 }
Write-Host "  SUCCESS RATE: $pct%" -ForegroundColor $(if ($pct -ge 90) { "Green" } elseif ($pct -ge 70) { "Yellow" } else { "Red" })
Write-Host "=====================================================" -ForegroundColor Yellow
