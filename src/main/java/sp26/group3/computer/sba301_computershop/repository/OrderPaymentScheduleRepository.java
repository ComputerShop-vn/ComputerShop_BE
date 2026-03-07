package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sp26.group3.computer.sba301_computershop.entity.OrderPaymentSchedule;

import java.util.List;

@Repository
public interface OrderPaymentScheduleRepository extends JpaRepository<OrderPaymentSchedule, Integer> {
    List<OrderPaymentSchedule> findByOrderOrderIdOrderByInstallmentNoAsc(int orderId);
}
