package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp26.group3.computer.sba301_computershop.dto.request.PlaceOrderRequest;
import sp26.group3.computer.sba301_computershop.dto.request.UpdateOrderStatusRequest;
import sp26.group3.computer.sba301_computershop.dto.response.*;
import sp26.group3.computer.sba301_computershop.entity.*;
import sp26.group3.computer.sba301_computershop.enums.PaymentStatus;
import sp26.group3.computer.sba301_computershop.enums.PaymentType;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.repository.*;
import sp26.group3.computer.sba301_computershop.service.OrderService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderServiceImpl implements OrderService {

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    OrderPaymentScheduleRepository paymentScheduleRepository;
    ProductItemRepository productItemRepository;
    ProductVariantRepository productVariantRepository;
    ProductImageRepository productImageRepository;
    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    UserRepository userRepository;

    // ======================== PLACE ORDER ========================

    @Override
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_CART);
        }

        // 1. Validate stock for all items
        for (CartItem cartItem : cartItems) {
            ProductVariant variant = cartItem.getVariant();
            if (variant.getStockQuantity() < cartItem.getQuantity()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }
        }

        // 2. Calculate total
        double totalAmount = cartItems.stream()
                .mapToDouble(item -> item.getVariant().getPrice() * item.getQuantity())
                .sum();

        // 3. Create Order
        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status("PENDING")
                .orderDate(LocalDateTime.now())
                .build();
        order = orderRepository.save(order);
        log.info("Created order | orderId={} totalAmount={}", order.getOrderId(), totalAmount);

        // 4. Create OrderItems + ProductItems + reduce stock
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            ProductVariant variant = cartItem.getVariant();

            // Reduce stock
            variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
            productVariantRepository.save(variant);

            // Create ProductItem (physical tracking with serial number)
            ProductItem productItem = ProductItem.builder()
                    .variant(variant)
                    .serialNumber(generateSerialNumber(variant.getSku()))
                    .build();
            productItem = productItemRepository.save(productItem);

            // Create OrderItem
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productItem(productItem)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(variant.getPrice())
                    .recipientName(request.getRecipientName())
                    .recipientPhone(request.getRecipientPhone())
                    .shippingAddress(request.getShippingAddress())
                    .build();
            orderItems.add(orderItemRepository.save(orderItem));
        }

        // 5. Create payment schedule
        createPaymentSchedules(order, request);

        // 6. Clear cart
        cartItemRepository.deleteAllByCartCartId(cart.getCartId());
        log.info("Cleared cart after placing order | cartId={}", cart.getCartId());

        return toOrderResponse(order, orderItems);
    }

    // ======================== GET ORDER BY ID ========================

    @Override
    public OrderResponse getOrderById(int orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);
        return toOrderResponse(order, items);
    }

    // ======================== GET MY ORDERS ========================

    @Override
    public List<OrderResponse> getMyOrders() {
        User user = getCurrentUser();
        List<Order> orders = orderRepository.findByUserUserIdOrderByOrderDateDesc(user.getUserId());

        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderOrderId(order.getOrderId());
                    return toOrderResponse(order, items);
                })
                .toList();
    }

    // ======================== GET ALL ORDERS (ADMIN/STAFF) ========================

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc().stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderOrderId(order.getOrderId());
                    return toOrderResponse(order, items);
                })
                .toList();
    }

    // ======================== UPDATE ORDER STATUS ========================

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(int orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        order.setStatus(request.getStatus());
        orderRepository.save(order);
        log.info("Updated order status | orderId={} status={}", orderId, request.getStatus());

        List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);
        return toOrderResponse(order, items);
    }

    // ======================== CANCEL ORDER ========================

    @Override
    @Transactional
    public void cancelOrder(int orderId) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Only the owner can cancel, and only PENDING orders
        if (order.getUser().getUserId() != user.getUserId()) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        // Restore stock
        List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);
        for (OrderItem item : items) {
            ProductVariant variant = item.getProductItem().getVariant();
            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
            productVariantRepository.save(variant);
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);
        log.info("Cancelled order | orderId={}", orderId);
    }

    // ======================== HELPER METHODS ========================

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private String generateSerialNumber(String sku) {
        return sku + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void createPaymentSchedules(Order order, PlaceOrderRequest request) {
        if (request.getPaymentType() == PaymentType.FULL) {
            // Single full payment
            OrderPaymentSchedule schedule = OrderPaymentSchedule.builder()
                    .order(order)
                    .paymentType(PaymentType.FULL)
                    .totalAmount(order.getTotalAmount())
                    .installmentNo(1)
                    .amount(order.getTotalAmount())
                    .dueDate(LocalDate.now().plusDays(7))
                    .status(PaymentStatus.PENDING)
                    .build();
            paymentScheduleRepository.save(schedule);
        } else {
            // Installment payments
            int months = request.getDurationMonths() != null ? request.getDurationMonths() : 12;
            double rate = request.getInterestRate() != null ? request.getInterestRate() : 0.0;
            double totalWithInterest = order.getTotalAmount() * (1 + rate / 100);
            double monthlyAmount = totalWithInterest / months;

            for (int i = 1; i <= months; i++) {
                OrderPaymentSchedule schedule = OrderPaymentSchedule.builder()
                        .order(order)
                        .paymentType(PaymentType.INSTALLMENT)
                        .providerName(request.getProviderName())
                        .durationMonths(months)
                        .interestRate(rate)
                        .totalAmount(totalWithInterest)
                        .installmentNo(i)
                        .amount(Math.round(monthlyAmount * 100.0) / 100.0)
                        .dueDate(LocalDate.now().plusMonths(i))
                        .status(PaymentStatus.PENDING)
                        .build();
                paymentScheduleRepository.save(schedule);
            }
            log.info("Created {} installment schedules for orderId={}", months, order.getOrderId());
        }
    }

    private OrderResponse toOrderResponse(Order order, List<OrderItem> items) {
        List<OrderItemResponse> itemResponses = items.stream()
                .map(this::toOrderItemResponse)
                .toList();

        List<PaymentScheduleResponse> paymentResponses = paymentScheduleRepository
                .findByOrderOrderId(order.getOrderId())
                .stream()
                .map(this::toPaymentResponse)
                .toList();

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUser().getUserId())
                .username(order.getUser().getUsername())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .items(itemResponses)
                .payments(paymentResponses)
                .build();
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        ProductItem productItem = item.getProductItem();
        ProductVariant variant = productItem.getVariant();
        Product product = variant.getProduct();

        String imageUrl = productImageRepository.findByProductProductId(product.getProductId())
                .stream()
                .filter(ProductImage::isThumbnail)
                .findFirst()
                .or(() -> productImageRepository.findByProductProductId(product.getProductId()).stream().findFirst())
                .map(ProductImage::getImageUrl)
                .orElse(null);

        return OrderItemResponse.builder()
                .orderItemId(item.getOrderItemId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getUnitPrice() * item.getQuantity())
                .variantId(variant.getVariantId())
                .variantName(variant.getVariantName())
                .sku(variant.getSku())
                .productId(product.getProductId())
                .productName(product.getName())
                .productImageUrl(imageUrl)
                .recipientName(item.getRecipientName())
                .recipientPhone(item.getRecipientPhone())
                .shippingAddress(item.getShippingAddress())
                .serialNumber(productItem.getSerialNumber())
                .build();
    }

    private PaymentScheduleResponse toPaymentResponse(OrderPaymentSchedule schedule) {
        return PaymentScheduleResponse.builder()
                .paymentScheduleId(schedule.getPaymentScheduleId())
                .paymentType(schedule.getPaymentType())
                .providerName(schedule.getProviderName())
                .durationMonths(schedule.getDurationMonths())
                .interestRate(schedule.getInterestRate())
                .totalAmount(schedule.getTotalAmount())
                .installmentNo(schedule.getInstallmentNo())
                .amount(schedule.getAmount())
                .dueDate(schedule.getDueDate())
                .paidDate(schedule.getPaidDate())
                .status(schedule.getStatus())
                .build();
    }
}
