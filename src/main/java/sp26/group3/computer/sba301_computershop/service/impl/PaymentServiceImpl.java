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
import sp26.group3.computer.sba301_computershop.entity.Order;
import sp26.group3.computer.sba301_computershop.entity.OrderPaymentSchedule;
import sp26.group3.computer.sba301_computershop.enums.PaymentStatus;
import sp26.group3.computer.sba301_computershop.enums.PaymentType;
import sp26.group3.computer.sba301_computershop.enums.OrderStatus;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
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

    @Override
    public PaymentDTO createVnPayPayment(HttpServletRequest request, int orderId, String bankCode) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        BigDecimal amount = BigDecimal.valueOf(order.getTotalAmount());
        long finalAmount = amount
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
        // long finalAmount = amount * 100L;
        String vnp_TxnRef = VNPayUtil.getRandomNumber(8) + "_" + orderId;
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
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        String signValue = vnPayConfig.hashAllFields(fields);

        String successUrl = "http://localhost:3000/payment-success";
        String failUrl = "http://localhost:3000/payment-failed";

        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                log.info("Thanh toán thành công: {}, TransactionNo: {}", request.getParameter("vnp_TxnRef"),
                        request.getParameter("vnp_TransactionNo"));
                return successUrl;
            } else {
                log.warn("Thanh toán không thành công, ResponseCode: {}", request.getParameter("vnp_ResponseCode"));
                return failUrl;
            }
        } else {
            log.error("CẢNH BÁO: Chữ ký VNPAY không hợp lệ (Checksum fail)");
            return failUrl;
        }
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
                int orderId = Integer.parseInt(txnRef.split("_")[1]);
                Order order = orderRepository.findById(orderId).orElse(null);

                if (order == null) {
                    response.put("RspCode", "01");
                    response.put("Message", "Order not found");
                } else if (order.getPaymentType() == PaymentType.FULL && order.getStatus() == OrderStatus.PAID) {
                    response.put("RspCode", "02");
                    response.put("Message", "Order already confirmed");
                } else {
                    if ("00".equals(responseCode)) {
                        // Update payment schedule
                        List<OrderPaymentSchedule> schedules = orderPaymentScheduleRepository
                                .findByOrderOrderIdOrderByInstallmentNoAsc(orderId);
                        boolean allPaid = true;
                        if (schedules != null && !schedules.isEmpty()) {
                            boolean scheduleUpdated = false;
                            for (OrderPaymentSchedule schedule : schedules) {
                                if (!scheduleUpdated && schedule.getStatus() == PaymentStatus.UNPAID) {
                                    schedule.setStatus(PaymentStatus.PAID);
                                    schedule.setPaidDate(java.time.LocalDate.now());
                                    schedule.setVnpTransactionNo(vnpTransactionNo);
                                    orderPaymentScheduleRepository.save(schedule);
                                    scheduleUpdated = true;
                                    // We don't break here because we need to check if all are paid
                                } else if (schedule.getStatus() != PaymentStatus.PAID) {
                                    allPaid = false;
                                }
                            }
                        } else {
                            allPaid = false;
                        }

                        // Update status for installment payment ONLY when all durations are paid
                        // if (order.getPaymentType() == PaymentType.INSTALLMENT) {
                        // if (allPaid) {
                        // //order.setStatus(OrderStatus.PAID);
                        // orderRepository.save(order);
                        // log.info("Order {} fully paid - marked as PAID via IPN", orderId);
                        // } else {
                        // log.info("Order {} installment payment received - waiting for remaining
                        // schedules",
                        // orderId);
                        // }
                        // } else {
                        // log.info("Order {} full payment - schedule updated, status unchanged",
                        // orderId);
                        // }
                    } else {
                        log.warn("Giao dịch VNPay thất bại cho Order ID: {}. Mã lỗi: {}", orderId, responseCode);
                        // Only set FAILED if it's not already PAID
                        // if (order.getStatus() != OrderStatus.PAID) {
                        // order.setStatus(OrderStatus.FAILED);
                        // orderRepository.save(order);
                        // }
                        // log.info("Order {} marked as FAILED via IPN", orderId);

                        // List<OrderPaymentSchedule> schedules = orderPaymentScheduleRepository
                        // .findByOrderOrderIdOrderByInstallmentNoAsc(orderId);
                        // if (schedules != null && !schedules.isEmpty()) {
                        // for (OrderPaymentSchedule schedule : schedules) {
                        // if (schedule.getStatus() == PaymentStatus.UNPAID) {
                        // schedule.setVnpTransactionNo(vnpTransactionNo);
                        // orderPaymentScheduleRepository.save(schedule);
                        // break;
                        // }
                        // }
                        // }
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
