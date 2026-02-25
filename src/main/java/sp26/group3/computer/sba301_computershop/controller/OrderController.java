package sp26.group3.computer.sba301_computershop.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sp26.group3.computer.sba301_computershop.dto.request.PlaceOrderRequest;
import sp26.group3.computer.sba301_computershop.dto.request.UpdateOrderStatusRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.OrderResponse;
import sp26.group3.computer.sba301_computershop.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {

    OrderService orderService;

    // ================= PLACE ORDER (from cart) =================
    @PostMapping
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    public ApiResponse<OrderResponse> placeOrder(
            @RequestBody @Valid PlaceOrderRequest request) {

        log.info("[POST] /orders - Place order | paymentType={}", request.getPaymentType());

        ApiResponse<OrderResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.placeOrder(request));

        log.info("[POST] /orders - SUCCESS | orderId={}", apiResponse.getResult().getOrderId());
        return apiResponse;
    }

    // ================= GET MY ORDERS =================
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    public ApiResponse<List<OrderResponse>> getMyOrders() {
        log.info("[GET] /orders/me - Get my orders");

        ApiResponse<List<OrderResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.getMyOrders());

        log.info("[GET] /orders/me - Total orders={}", apiResponse.getResult().size());
        return apiResponse;
    }

    // ================= GET ORDER BY ID =================
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable int id) {
        log.info("[GET] /orders/{} - Get order by id", id);

        ApiResponse<OrderResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.getOrderById(id));

        log.info("[GET] /orders/{} - SUCCESS", id);
        return apiResponse;
    }

    // ================= GET ALL ORDERS (STAFF/ADMIN) =================
    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<List<OrderResponse>> getAllOrders() {
        log.info("[GET] /orders - Get all orders");

        ApiResponse<List<OrderResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.getAllOrders());

        log.info("[GET] /orders - Total orders={}", apiResponse.getResult().size());
        return apiResponse;
    }

    // ================= UPDATE ORDER STATUS (STAFF/ADMIN) =================
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable int id,
            @RequestBody @Valid UpdateOrderStatusRequest request) {

        log.info("[PUT] /orders/{}/status - Update status to {}", id, request.getStatus());

        ApiResponse<OrderResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.updateOrderStatus(id, request));

        log.info("[PUT] /orders/{}/status - SUCCESS", id);
        return apiResponse;
    }

    // ================= CANCEL ORDER (MEMBER) =================
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    public ApiResponse<Void> cancelOrder(@PathVariable int id) {
        log.info("[PUT] /orders/{}/cancel - Cancel order", id);

        orderService.cancelOrder(id);

        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Order cancelled successfully");

        log.info("[PUT] /orders/{}/cancel - SUCCESS", id);
        return apiResponse;
    }
}
