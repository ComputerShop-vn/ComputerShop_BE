package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group3.computer.sba301_computershop.entity.Warranty;

import java.util.List;
import java.util.Optional;

public interface WarrantyRepository extends JpaRepository<Warranty, Integer> {
    List<Warranty> findByOrderItem_Order_OrderId(int orderId);

    Optional<Warranty> findByOrderItem_OrderItemId(int orderItemId);
}
