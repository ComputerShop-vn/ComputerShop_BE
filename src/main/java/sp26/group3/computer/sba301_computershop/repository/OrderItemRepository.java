package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group3.computer.sba301_computershop.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
}
