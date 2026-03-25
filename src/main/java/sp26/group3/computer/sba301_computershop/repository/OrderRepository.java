package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sp26.group3.computer.sba301_computershop.entity.Order;
import sp26.group3.computer.sba301_computershop.repository.projection.RevenuePeriodProjection;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserUserIdOrderByOrderDateDesc(int userId);
    Page<Order> findByUserUserIdOrderByOrderDateDesc(int userId, Pageable pageable);
    Page<Order> findByUserUserId(int userId, Pageable pageable);
    List<Order> findAllByOrderByOrderDateDesc();
    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);

    @Query(value = "SELECT CAST(order_date AS DATE) as period, " +
                   "SUM(total_amount) as revenue, COUNT(*) as orderCount " +
                   "FROM orders " +
                   "WHERE status = 'COMPLETED' AND order_date >= :from AND order_date <= :to " +
                   "GROUP BY CAST(order_date AS DATE) " +
                   "ORDER BY CAST(order_date AS DATE)",
           nativeQuery = true)
    List<RevenuePeriodProjection> findRevenueGroupByDay(@Param("from") LocalDateTime from,
                                                         @Param("to") LocalDateTime to);

    @Query(value = "SELECT FORMAT(order_date, 'yyyy-MM') as period, " +
                   "SUM(total_amount) as revenue, COUNT(*) as orderCount " +
                   "FROM orders " +
                   "WHERE status = 'COMPLETED' AND order_date >= :from AND order_date <= :to " +
                   "GROUP BY FORMAT(order_date, 'yyyy-MM') " +
                   "ORDER BY FORMAT(order_date, 'yyyy-MM')",
           nativeQuery = true)
    List<RevenuePeriodProjection> findRevenueGroupByMonth(@Param("from") LocalDateTime from,
                                                           @Param("to") LocalDateTime to);

    @Query(value = "SELECT CAST(YEAR(order_date) AS NVARCHAR(4)) as period, " +
                   "SUM(total_amount) as revenue, COUNT(*) as orderCount " +
                   "FROM orders " +
                   "WHERE status = 'COMPLETED' AND order_date >= :from AND order_date <= :to " +
                   "GROUP BY YEAR(order_date) " +
                   "ORDER BY YEAR(order_date)",
           nativeQuery = true)
    List<RevenuePeriodProjection> findRevenueGroupByYear(@Param("from") LocalDateTime from,
                                                          @Param("to") LocalDateTime to);
}
