package sp26.group3.computer.sba301_computershop.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sp26.group3.computer.sba301_computershop.dto.request.PlaceOrderRequest;
import sp26.group3.computer.sba301_computershop.dto.request.UpdateOrderStatusRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.OrderPaymentResultResponse;
import sp26.group3.computer.sba301_computershop.dto.response.OrderResponse;
import sp26.group3.computer.sba301_computershop.dto.response.PagedResponse;
import sp26.group3.computer.sba301_computershop.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {

    OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    public ApiResponse<OrderResponse> placeOrder(
            @RequestBody @Valid PlaceOrderRequest request,
            jakarta.servlet.http.HttpServletRequest servletRequest) {
        ApiResponse<OrderResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.placeOrder(request, servletRequest));
        return apiResponse;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    public ApiResponse<List<OrderResponse>> getMyOrders() {
        ApiResponse<List<OrderResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.getMyOrders());
        return apiResponse;
    }

    @GetMapping("/me/paged")
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    public ApiResponse<PagedResponse<OrderResponse>> getMyOrdersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        ApiResponse<PagedResponse<OrderResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.getMyOrdersPaged(pageable));
        return apiResponse;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable int id) {
        ApiResponse<OrderResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.getOrderById(id));
        return apiResponse;
    }

    @GetMapping("/{id}/payment-result")
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    public ApiResponse<OrderPaymentResultResponse> getOrderPaymentResult(
            @PathVariable int id,
            @RequestParam(required = false) Integer installmentNo) {
        ApiResponse<OrderPaymentResultResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.getOrderPaymentResult(id, installmentNo));
        return apiResponse;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<List<OrderResponse>> getAllOrders() {
        ApiResponse<List<OrderResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.getAllOrders());
        return apiResponse;
    }

    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<PagedResponse<OrderResponse>> getAllOrdersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        ApiResponse<PagedResponse<OrderResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.getAllOrdersPaged(pageable));
        return apiResponse;
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable int id,
            @RequestBody @Valid UpdateOrderStatusRequest request) {
        ApiResponse<OrderResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.updateOrderStatus(id, request));
        return apiResponse;
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    public ApiResponse<Void> cancelOrder(@PathVariable int id) {
        orderService.cancelOrder(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Order cancelled successfully");
        return apiResponse;
    }
}
