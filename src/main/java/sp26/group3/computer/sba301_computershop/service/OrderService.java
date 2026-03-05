package sp26.group3.computer.sba301_computershop.service;

import jakarta.servlet.http.HttpServletRequest;
import sp26.group3.computer.sba301_computershop.dto.request.PlaceOrderRequest;
import sp26.group3.computer.sba301_computershop.dto.request.UpdateOrderStatusRequest;
import sp26.group3.computer.sba301_computershop.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(PlaceOrderRequest request, HttpServletRequest servletRequest);

    OrderResponse getOrderById(int orderId);

    List<OrderResponse> getMyOrders();

    List<OrderResponse> getAllOrders();

    OrderResponse updateOrderStatus(int orderId, UpdateOrderStatusRequest request);

    void cancelOrder(int orderId);
}
