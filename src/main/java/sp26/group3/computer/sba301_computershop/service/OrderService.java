package sp26.group3.computer.sba301_computershop.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import sp26.group3.computer.sba301_computershop.dto.request.AddToCartRequest;
import sp26.group3.computer.sba301_computershop.dto.request.PlaceOrderRequest;
import sp26.group3.computer.sba301_computershop.dto.request.UpdateOrderStatusRequest;
import sp26.group3.computer.sba301_computershop.dto.response.OrderPaymentResultResponse;
import sp26.group3.computer.sba301_computershop.dto.response.OrderResponse;
import sp26.group3.computer.sba301_computershop.dto.response.PagedResponse;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(PlaceOrderRequest request, HttpServletRequest servletRequest);

    OrderResponse placeOrderFromItems(List<AddToCartRequest> items, PlaceOrderRequest request, HttpServletRequest servletRequest);

    OrderResponse getOrderById(int orderId);

    List<OrderResponse> getMyOrders();

    List<OrderResponse> getAllOrders();

    PagedResponse<OrderResponse> getMyOrdersPaged(Pageable pageable);

    PagedResponse<OrderResponse> getAllOrdersPaged(Pageable pageable);

    OrderResponse updateOrderStatus(int orderId, UpdateOrderStatusRequest request);

    void cancelOrder(int orderId);

    OrderPaymentResultResponse getOrderPaymentResult(int orderId, Integer installmentNo);
}
