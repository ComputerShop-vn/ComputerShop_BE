package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp26.group3.computer.sba301_computershop.dto.request.UpdateWarrantyStatusRequest;
import sp26.group3.computer.sba301_computershop.dto.response.WarrantyResponse;
import sp26.group3.computer.sba301_computershop.entity.Order;
import sp26.group3.computer.sba301_computershop.entity.OrderItem;
import sp26.group3.computer.sba301_computershop.entity.Product;
import sp26.group3.computer.sba301_computershop.entity.Warranty;
import sp26.group3.computer.sba301_computershop.enums.WarrantyStatus;
import sp26.group3.computer.sba301_computershop.enums.WarrantyType;
import sp26.group3.computer.sba301_computershop.repository.OrderItemRepository;
import sp26.group3.computer.sba301_computershop.repository.WarrantyRepository;
import sp26.group3.computer.sba301_computershop.service.WarrantyService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WarrantyServiceImpl implements WarrantyService {

    WarrantyRepository warrantyRepository;
    OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public void createWarrantiesForOrder(Order order) {
        log.info("Creating warranties for orderId={}", order.getOrderId());

        List<OrderItem> items = orderItemRepository.findByOrderOrderId(order.getOrderId());

        for (OrderItem item : items) {
            Product product = item.getProductItem().getVariant().getProduct();
            int warrantyMonths = product.getWarrantyMonths();

            if (warrantyMonths > 0) {
                // Check if warranty already exists for this order item
                if (warrantyRepository.findByOrderItem_OrderItemId(item.getOrderItemId()).isEmpty()) {
                    LocalDate startDate = LocalDate.now();
                    LocalDate endDate = startDate.plusMonths(warrantyMonths);

                    Warranty warranty = Warranty.builder()
                            .orderItem(item)
                            .serialNumber(item.getProductItem().getSerialNumber())
                            .startDate(startDate)
                            .endDate(endDate)
                            .description("Warranty for " + product.getName())
                            .status(WarrantyStatus.ACTIVE)
                            .type(WarrantyType.MANUFACTURER) // Default type
                            .build();

                    warrantyRepository.save(warranty);
                    log.info("Created warranty for orderItemId={} with endDate={}", item.getOrderItemId(), endDate);
                }
            }
        }
    }

    @Override
    public WarrantyResponse getWarrantyById(int id) {
        Warranty warranty = warrantyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warranty not found with id: " + id));
        return toWarrantyResponse(warranty);
    }

    @Override
    public List<WarrantyResponse> getWarrantiesByOrderId(int orderId) {
        return warrantyRepository.findByOrderItem_Order_OrderId(orderId)
                .stream()
                .map(this::toWarrantyResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WarrantyResponse updateWarrantyStatus(int id, UpdateWarrantyStatusRequest request) {
        Warranty warranty = warrantyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warranty not found with id: " + id));

        warranty.setStatus(request.getStatus());
        Warranty updated = warrantyRepository.save(warranty);

        log.info("Updated warranty id={} to status={}", id, request.getStatus());
        return toWarrantyResponse(updated);
    }

    private WarrantyResponse toWarrantyResponse(Warranty warranty) {
        Product product = warranty.getOrderItem().getProductItem().getVariant().getProduct();
        return WarrantyResponse.builder()
                .id(warranty.getId())
                .orderItemId(warranty.getOrderItem().getOrderItemId())
                .productId(product.getProductId())
                .productName(product.getName())
                .serialNumber(warranty.getOrderItem().getProductItem().getSerialNumber())
                .startDate(warranty.getStartDate())
                .endDate(warranty.getEndDate())
                .description(warranty.getDescription())
                .status(warranty.getStatus())
                .type(warranty.getType())
                .build();
    }
}
