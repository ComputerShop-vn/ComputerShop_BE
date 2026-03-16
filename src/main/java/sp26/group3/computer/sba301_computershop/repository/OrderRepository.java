package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sp26.group3.computer.sba301_computershop.entity.Order;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserUserIdOrderByOrderDateDesc(int userId);
    Page<Order> findByUserUserIdOrderByOrderDateDesc(int userId, Pageable pageable);
    Page<Order> findByUserUserId(int userId, Pageable pageable);
    List<Order> findAllByOrderByOrderDateDesc();
    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);
}
