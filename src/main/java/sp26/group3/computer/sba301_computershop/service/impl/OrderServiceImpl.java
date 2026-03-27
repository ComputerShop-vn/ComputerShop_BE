package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp26.group3.computer.sba301_computershop.dto.request.AddToCartRequest;
import sp26.group3.computer.sba301_computershop.dto.request.PlaceOrderRequest;
import sp26.group3.computer.sba301_computershop.dto.request.UpdateOrderStatusRequest;
import sp26.group3.computer.sba301_computershop.dto.response.*;
import sp26.group3.computer.sba301_computershop.entity.*;
import sp26.group3.computer.sba301_computershop.enums.PaymentStatus;
import sp26.group3.computer.sba301_computershop.enums.PaymentMethod;
import sp26.group3.computer.sba301_computershop.enums.PaymentMode;
import sp26.group3.computer.sba301_computershop.enums.OrderStatus;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.repository.*;
import sp26.group3.computer.sba301_computershop.service.OrderService;
import sp26.group3.computer.sba301_computershop.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    InstallmentPackageRepository installmentPackageRepository;
    PromotionProductRepository promotionProductRepository;
    PaymentService paymentService;
    sp26.group3.computer.sba301_computershop.service.WarrantyService warrantyService;

    // ======================== PLACE ORDER ========================

    @Override
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request, jakarta.servlet.http.HttpServletRequest servletRequest) {
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

        // 2. Calculate total (apply active promotions)
        double totalAmount = cartItems.stream()
                .mapToDouble(item -> getEffectivePrice(item.getVariant()) * item.getQuantity())
                .sum();

        // 3. Create Order
        InstallmentPackage pack = null;
        if (request.getPaymentMode() == PaymentMode.INSTALLMENT) {
            pack = installmentPackageRepository.findById(request.getPackageId())
                    .orElseThrow(() -> new RuntimeException("Installment package not found"));
            if (totalAmount < pack.getMinOrderAmount()) {
                throw new RuntimeException("Order total amount does not meet package minimum order amount");
            }
        }

        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .paymentMethod(request.getPaymentMethod())
                .paymentMode(request.getPaymentMode())
                .installmentPackage(pack)
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

            // Create OrderItem (unitPrice = effective price after discount)
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productItem(productItem)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(getEffectivePrice(variant))
                    .recipientName(request.getRecipientName())
                    .recipientPhone(request.getRecipientPhone())
                    .shippingAddress(request.getShippingAddress())
                    .build();
            orderItems.add(orderItemRepository.save(orderItem));
        }

        // 5. Create payment schedule
        createPaymentSchedules(order, request);

        // 6. Clear cart (Only clear now for COD. For others, clear on payment success)
        if (request.getPaymentMethod() == PaymentMethod.COD) {
            cartItemRepository.deleteAllByCartCartId(cart.getCartId());
            log.info("Cleared cart after placing COD order | cartId={}", cart.getCartId());
        }

        // 7. Handle Payment URL for VNPAY
        String paymentUrl = null;
        if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
            var paymentDto = paymentService.createVnPayPayment(servletRequest, order.getOrderId(), null, null);
            paymentUrl = paymentDto.getPaymentUrl();
        }

        return toOrderResponse(order, orderItems, paymentUrl);
    }

    // ======================== PLACE ORDER FROM BUILD ITEMS
    // ========================

    @Override
    @Transactional
    public OrderResponse placeOrderFromItems(List<AddToCartRequest> items,
            PlaceOrderRequest request,
            HttpServletRequest servletRequest) {
        User user = getCurrentUser();

        // 1. Load variants and validate stock
        Map<Integer, ProductVariant> variantMap = new HashMap<>();
        for (AddToCartRequest item : items) {
            ProductVariant variant = productVariantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
            if (variant.getStockQuantity() < item.getQuantity()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }
            variantMap.put(item.getVariantId(), variant);
        }

        // 2. Calculate total (apply active promotions)
        double totalAmount = items.stream()
                .mapToDouble(i -> getEffectivePrice(variantMap.get(i.getVariantId())) * i.getQuantity())
                .sum();

        // 3. Create Order
        InstallmentPackage pack = null;
        if (request.getPaymentMode() == PaymentMode.INSTALLMENT) {
            pack = installmentPackageRepository.findById(request.getPackageId())
                    .orElseThrow(() -> new RuntimeException("Installment package not found"));
            if (totalAmount < pack.getMinOrderAmount()) {
                throw new RuntimeException("Order total amount does not meet package minimum order amount");
            }
        }

        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .paymentMethod(request.getPaymentMethod())
                .paymentMode(request.getPaymentMode())
                .installmentPackage(pack)
                .build();
        order = orderRepository.save(order);
        log.info("Created order from build | orderId={} totalAmount={}", order.getOrderId(), totalAmount);

        // 4. Create OrderItems + ProductItems + reduce stock (no cart clearing)
        List<OrderItem> orderItems = new ArrayList<>();
        for (AddToCartRequest item : items) {
            ProductVariant variant = variantMap.get(item.getVariantId());

            variant.setStockQuantity(variant.getStockQuantity() - item.getQuantity());
            productVariantRepository.save(variant);

            ProductItem productItem = ProductItem.builder()
                    .variant(variant)
                    .serialNumber(generateSerialNumber(variant.getSku()))
                    .build();
            productItem = productItemRepository.save(productItem);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productItem(productItem)
                    .quantity(item.getQuantity())
                    .unitPrice(getEffectivePrice(variant))
                    .recipientName(request.getRecipientName())
                    .recipientPhone(request.getRecipientPhone())
                    .shippingAddress(request.getShippingAddress())
                    .build();
            orderItems.add(orderItemRepository.save(orderItem));
        }

        // 5. Payment schedule
        createPaymentSchedules(order, request);

        // 6. VNPay URL
        String paymentUrl = null;
        if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
            var paymentDto = paymentService.createVnPayPayment(servletRequest, order.getOrderId(), null, null);
            paymentUrl = paymentDto.getPaymentUrl();
        }

        return toOrderResponse(order, orderItems, paymentUrl);
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

    // ======================== GET ALL ORDERS (ADMIN/STAFF)
    // ========================

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc().stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderOrderId(order.getOrderId());
                    return toOrderResponse(order, items);
                })
                .toList();
    }

    // ======================== PAGED: GET MY ORDERS ========================

    @Override
    public PagedResponse<OrderResponse> getMyOrdersPaged(Pageable pageable) {
        User user = getCurrentUser();
        Page<Order> page = orderRepository.findByUserUserId(user.getUserId(), pageable);
        List<OrderResponse> content = page.getContent().stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderOrderId(order.getOrderId());
                    return toOrderResponse(order, items);
                })
                .toList();
        return PagedResponse.<OrderResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    // ======================== PAGED: GET ALL ORDERS (ADMIN/STAFF)
    // ========================

    @Override
    public PagedResponse<OrderResponse> getAllOrdersPaged(Pageable pageable) {
        Page<Order> page = orderRepository.findAll(pageable);
        List<OrderResponse> content = page.getContent().stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderOrderId(order.getOrderId());
                    return toOrderResponse(order, items);
                })
                .toList();
        return PagedResponse.<OrderResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
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

        if (request.getStatus() == OrderStatus.COMPLETED) {
            warrantyService.createWarrantiesForOrder(order);
        }

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
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        // Restore stock
        List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);
        for (OrderItem item : items) {
            ProductVariant variant = item.getProductItem().getVariant();
            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
            productVariantRepository.save(variant);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Cancelled order | orderId={}", orderId);
    }

    @Override
    public OrderPaymentResultResponse getOrderPaymentResult(int orderId, Integer installmentNo) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        List<OrderPaymentSchedule> schedules = paymentScheduleRepository
                .findByOrderOrderIdOrderByInstallmentNoAsc(orderId);

        boolean isPaid;
        double amount;
        if (installmentNo != null && installmentNo > 0) {
            OrderPaymentSchedule specific = schedules.stream()
                    .filter(s -> s.getInstallmentNo() == installmentNo)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Installment not found"));
            isPaid = (specific.getStatus() == PaymentStatus.PAID);
            amount = specific.getAmount() + specific.getPenaltyAmount();
        } else {
            isPaid = schedules.stream().anyMatch(s -> s.getStatus() == PaymentStatus.PAID);
            amount = order.getTotalAmount();
        }

        return OrderPaymentResultResponse.builder()
                .orderId(order.getOrderId())
                .amount(amount)
                .status(isPaid ? "success" : "failed")
                .installmentNo((installmentNo == null || installmentNo == 0) ? null : installmentNo)
                .build();
    }

    // ======================== HELPER METHODS ========================

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private double getEffectivePrice(ProductVariant variant) {
        List<PromotionProduct> promos = promotionProductRepository
                .findActivePromotionByProductId(variant.getProduct().getProductId());
        if (!promos.isEmpty()) {
            double discountPercent = promos.get(0).getPromotion().getDiscountPercent();
            return variant.getPrice() * (1 - discountPercent / 100.0);
        }
        return variant.getPrice();
    }

    private String generateSerialNumber(String sku) {
        return sku + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void createPaymentSchedules(Order order, PlaceOrderRequest request) {
        if (request.getPaymentMethod() == PaymentMethod.COD) {
            OrderPaymentSchedule schedule = OrderPaymentSchedule.builder()
                    .order(order)
                    .installmentNo(1)
                    .amount(order.getTotalAmount())
                    .dueDate(LocalDate.now().plusDays(7))
                    .status(PaymentStatus.UNPAID)
                    .build();
            paymentScheduleRepository.save(schedule);
            log.info("Created COD payment schedule for orderId={}", order.getOrderId());
        } else if (request.getPaymentMode() == PaymentMode.FULL) {
            OrderPaymentSchedule schedule = OrderPaymentSchedule.builder()
                    .order(order)
                    .installmentNo(0)
                    .amount(order.getTotalAmount())
                    .dueDate(LocalDate.now().plusDays(7))
                    .status(PaymentStatus.UNPAID)
                    .build();
            paymentScheduleRepository.save(schedule);
        } else {
            // Installment payments
            InstallmentPackage pack = order.getInstallmentPackage();
            double orderAmount = order.getTotalAmount();
            double downPaymentPercentage = pack.getDownPaymentPercentage();
            double downPaymentAmount = orderAmount * (downPaymentPercentage / 100.0);
            double remainingBalance = orderAmount - downPaymentAmount;

            // 1. Create Down Payment Record (Month 0)
            OrderPaymentSchedule downPayment = OrderPaymentSchedule.builder()
                    .order(order)
                    .installmentNo(0) // 0 represents the down payment
                    .amount(Math.round(downPaymentAmount * 100.0) / 100.0)
                    .dueDate(LocalDate.now()) // Payable now
                    .status(PaymentStatus.UNPAID)
                    .build();
            paymentScheduleRepository.save(downPayment);

            // 2. Create Installment Records
            double interestRatePerMonth = (pack.getInterestRate() / 100.0) / 12.0;
            int durationMonths = pack.getDurationMonths();

            double monthlyPayment;
            if (interestRatePerMonth > 0) {
                monthlyPayment = (remainingBalance * interestRatePerMonth
                        * Math.pow(1 + interestRatePerMonth, durationMonths))
                        / (Math.pow(1 + interestRatePerMonth, durationMonths) - 1);
            } else {
                monthlyPayment = remainingBalance / durationMonths;
            }
            monthlyPayment = Math.round(monthlyPayment * 100.0) / 100.0;

            for (int i = 1; i <= durationMonths; i++) {
                OrderPaymentSchedule schedule = OrderPaymentSchedule.builder()
                        .order(order)
                        .installmentNo(i)
                        .amount(monthlyPayment)
                        .dueDate(LocalDate.now().plusMonths(i)) // Requirement 6: First installment 1 month after
                                                                // purchase
                        .status(PaymentStatus.UNPAID)
                        .build();
                paymentScheduleRepository.save(schedule);
            }
            log.info("Created down payment + {} installment schedules for orderId={}", durationMonths,
                    order.getOrderId());
        }
    }

    private OrderResponse toOrderResponse(Order order, List<OrderItem> items, String paymentUrl) {
        List<OrderItemResponse> itemResponses = items.stream()
                .map(this::toOrderItemResponse)
                .toList();

        List<PaymentScheduleResponse> paymentResponses = paymentScheduleRepository
                .findByOrderOrderIdOrderByInstallmentNoAsc(order.getOrderId())
                .stream()
                .map(this::toPaymentResponse)
                .toList();

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUser().getUserId())
                .username(order.getUser().getUsername())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentMode(order.getPaymentMode())
                .orderDate(order.getOrderDate())
                .items(itemResponses)
                .payments(paymentResponses)
                .paymentUrl(paymentUrl)
                .build();
    }

    private OrderResponse toOrderResponse(Order order, List<OrderItem> items) {
        return toOrderResponse(order, items, null);
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        ProductItem productItem = item.getProductItem();
        ProductVariant variant = productItem.getVariant();
        Product product = variant.getProduct();

        String imageUrl = productImageRepository
                .findFirstByProductProductIdAndIsThumbnailTrue(product.getProductId())
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
                .thumbnailUrl(imageUrl)
                .recipientName(item.getRecipientName())
                .recipientPhone(item.getRecipientPhone())
                .shippingAddress(item.getShippingAddress())
                .serialNumber(productItem.getSerialNumber())
                .build();
    }

    private PaymentScheduleResponse toPaymentResponse(OrderPaymentSchedule schedule) {
        return PaymentScheduleResponse.builder()
                .paymentScheduleId(schedule.getPaymentScheduleId())
                .installmentNo(schedule.getInstallmentNo())
                .amount(schedule.getAmount())
                .dueDate(schedule.getDueDate())
                .paidDate(schedule.getPaidDate())
                .vnpTransactionNo(schedule.getVnpTransactionNo())
                .status(schedule.getStatus())
                .build();
    }
}
