package sp26.group3.computer.sba301_computershop.service;

import sp26.group3.computer.sba301_computershop.dto.request.UpdateWarrantyStatusRequest;
import sp26.group3.computer.sba301_computershop.dto.response.WarrantyResponse;
import sp26.group3.computer.sba301_computershop.entity.Order;

import java.util.List;

public interface WarrantyService {
    void createWarrantiesForOrder(Order order);

    WarrantyResponse getWarrantyById(int id);

    List<WarrantyResponse> getWarrantiesByOrderId(int orderId);

    WarrantyResponse updateWarrantyStatus(int id, UpdateWarrantyStatusRequest request);
}
