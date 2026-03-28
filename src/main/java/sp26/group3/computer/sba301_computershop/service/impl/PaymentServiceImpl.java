package sp26.group3.computer.sba301_computershop.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.NotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp26.group3.computer.sba301_computershop.config.VNPayConfig;
import sp26.group3.computer.sba301_computershop.dto.response.PaymentDTO;
import sp26.group3.computer.sba301_computershop.entity.Cart;
import sp26.group3.computer.sba301_computershop.entity.Order;
import sp26.group3.computer.sba301_computershop.entity.OrderPaymentSchedule;
import sp26.group3.computer.sba301_computershop.entity.User;
import sp26.group3.computer.sba301_computershop.enums.PaymentStatus;
import sp26.group3.computer.sba301_computershop.enums.PaymentMethod;
import sp26.group3.computer.sba301_computershop.enums.PaymentMode;
import sp26.group3.computer.sba301_computershop.enums.OrderStatus;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.repository.CartItemRepository;
import sp26.group3.computer.sba301_computershop.repository.CartRepository;
import sp26.group3.computer.sba301_computershop.repository.OrderPaymentScheduleRepository;
import sp26.group3.computer.sba301_computershop.repository.OrderRepository;
import sp26.group3.computer.sba301_computershop.service.PaymentService;
import sp26.group3.computer.sba301_computershop.util.VNPayUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    VNPayConfig vnPayConfig;
    OrderRepository orderRepository;
    OrderPaymentScheduleRepository orderPaymentScheduleRepository;
    CartRepository cartRepository;
    CartItemRepository cartItemRepository;

    @Override
    public PaymentDTO createVnPayPayment(HttpServletRequest request, int orderId, String bankCode,
            Integer installmentNo) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        BigDecimal amount;
        int finalInstallmentNo = 0;
        if (order.getPaymentMode() == PaymentMode.INSTALLMENT) {
            List<OrderPaymentSchedule> schedules = orderPaymentScheduleRepository
                    .findByOrderOrderIdOrderByInstallmentNoAsc(orderId);
            OrderPaymentSchedule targetPayment;
            if (installmentNo != null) {
                targetPayment = schedules.stream()
                        .filter(s -> s.getInstallmentNo() == installmentNo)
                        .findFirst()
                        .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
            } else {
                targetPayment = schedules.stream()
                        .filter(schedule -> schedule.getStatus() == PaymentStatus.UNPAID)
                        .findFirst()
                        .orElseThrow(() -> new AppException(ErrorCode.ORDER_ALREADY_PAID));
            }
            amount = BigDecimal.valueOf(targetPayment.getAmount());
            finalInstallmentNo = targetPayment.getInstallmentNo();
        } else {
            amount = BigDecimal.valueOf(order.getTotalAmount());
        }
        long finalAmount = amount
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
        // long finalAmount = amount * 100L;
        String vnp_TxnRef = VNPayUtil.getRandomNumber(8) + "_" + orderId + "_" + finalInstallmentNo;
        String vnp_IpAddr = VNPayUtil.getIpAddress(request);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnPayConfig.getVnp_Version());
        vnp_Params.put("vnp_Command", vnPayConfig.getVnp_Command());
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(finalAmount));
        vnp_Params.put("vnp_CurrCode", "VND");

        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + orderId);
        vnp_Params.put("vnp_OrderType", vnPayConfig.getOrderType());
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(vnPayConfig.getVnp_HashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;

        return PaymentDTO.builder()
                .code("00")
                .message("success")
                .paymentUrl(paymentUrl)
                .build();
    }

    @Override
    @Transactional
    public String handleVnPayCallback(HttpServletRequest request) {

        log.info("Received VNPay Callback return url hit");

        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);

            if (fieldValue != null && fieldValue.length() > 0) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnpSecureHash = request.getParameter("vnp_SecureHash");

        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        String signValue = vnPayConfig.hashAllFields(fields);

        String txnRef = request.getParameter("vnp_TxnRef");
        String[] parts = (txnRef != null) ? txnRef.split("_") : new String[0];
        String orderIdStr = (parts.length > 1) ? parts[1] : "";
        String instNoStr = (parts.length > 2) ? parts[2] : "0";
        String returnUrl = "http://localhost:3000/payment-result?orderId=" + orderIdStr + "&installmentNo=" + instNoStr;
        return returnUrl;
    }

    @Override
    @Transactional
    public Map<String, String> handleVnPayIpn(HttpServletRequest request) {
        log.info("Received VNPay IPN notification");

        // 1. Thu thập tham số y hệt như hàm Callback (KHÔNG dùng URLEncoder)
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        // 2. Kiểm tra chữ ký
        String signValue = vnPayConfig.hashAllFields(fields);
        Map<String, String> response = new HashMap<>();

        if (signValue.equals(vnp_SecureHash)) {
            // 3. Logic xử lý đơn hàng
            String txnRef = request.getParameter("vnp_TxnRef");
            String responseCode = request.getParameter("vnp_ResponseCode");
            String vnpTransactionNo = request.getParameter("vnp_TransactionNo");

            try {
                String[] parts = txnRef.split("_");
                int orderId = Integer.parseInt(parts[1]);
                int installmentNo = Integer.parseInt(parts[2]);
                Order order = orderRepository.findById(orderId).orElse(null);

                if (order == null) {
                    response.put("RspCode", "01");
                    response.put("Message", "Order not found");
                } else if (order.getPaymentMode() == PaymentMode.FULL
                        && order.getOrderPaymentSchedule().get(0).getStatus() == PaymentStatus.PAID) {
                    response.put("RspCode", "02");
                    response.put("Message", "Order already confirmed");
                } else {
                    if ("00".equals(responseCode)) {
                        // Update specific payment schedule
                        OrderPaymentSchedule schedule = orderPaymentScheduleRepository
                                .findByOrderOrderIdAndInstallmentNo(orderId, installmentNo)
                                .orElse(null);

                        if (schedule != null && schedule.getStatus() == PaymentStatus.UNPAID) {
                            schedule.setStatus(PaymentStatus.PAID);
                            schedule.setPaidDate(java.time.LocalDate.now());
                            schedule.setVnpTransactionNo(vnpTransactionNo);
                            orderPaymentScheduleRepository.saveAndFlush(schedule);
                            log.info("Schedule updated and FLUSHED to PAID for OrderId={} and InstallmentNo={}", orderId, installmentNo);
                        }

                        // Clear cart for both FULL and Down payment (installmentNo=0 or full)
                        if (installmentNo == 0 || order.getPaymentMode() == PaymentMode.FULL) {
                            User user = order.getUser();
                            Cart cart = cartRepository.findByUserUserId(user.getUserId()).orElse(null);
                            if (cart != null) {
                                cartItemRepository.deleteAllByCartCartId(cart.getCartId());
                                log.info("Cart cleared via IPN for user {}", user.getUserId());
                            }
                        }
                    } else {
                        log.warn("Giao dịch VNPay thất bại cho Order ID: {}. Mã lỗi: {}", orderId, responseCode);

                        log.info("Order {} marked as FAILED via IPN", orderId);

                        OrderPaymentSchedule schedule = orderPaymentScheduleRepository
                                .findByOrderOrderIdAndInstallmentNo(orderId, installmentNo)
                                .orElse(null);

                        if (schedule != null && schedule.getStatus() == PaymentStatus.UNPAID) {
                            schedule.setVnpTransactionNo(vnpTransactionNo);
                            schedule.setStatus(PaymentStatus.FAILED);
                            orderPaymentScheduleRepository.save(schedule);
                        }
                    }
                    response.put("RspCode", "00");
                    response.put("Message", "Confirm Success");
                }
            } catch (Exception e) {
                log.error("IPN Process Error: {}", e.getMessage());
                response.put("RspCode", "99");
                response.put("Message", "Unknown error");
            }
        } else {
            log.error("VNPay IPN Invalid Signature");
            response.put("RspCode", "97");
            response.put("Message", "Invalid signature");
        }
        return response;
    }
}
