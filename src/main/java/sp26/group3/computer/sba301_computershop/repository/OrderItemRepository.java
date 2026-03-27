package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sp26.group3.computer.sba301_computershop.entity.OrderItem;
import sp26.group3.computer.sba301_computershop.repository.projection.ProductRevenueProjection;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByOrderOrderId(int orderId);

    @Query(value = "SELECT p.name as productName, pv.variant_name as variantName, " +
                   "SUM(oi.quantity) as totalSold, SUM(oi.unit_price * oi.quantity) as revenue " +
                   "FROM order_items oi " +
                   "JOIN product_items pi ON oi.item_id = pi.item_id " +
                   "JOIN product_variants pv ON pi.variant_id = pv.variant_id " +
                   "JOIN products p ON pv.product_id = p.product_id " +
                   "JOIN orders o ON oi.order_id = o.order_id " +
                   "WHERE o.status = 'COMPLETED' " +
                   "AND o.order_date >= :from AND o.order_date <= :to " +
                   "GROUP BY p.product_id, p.name, pv.variant_id, pv.variant_name " +
                   "ORDER BY revenue DESC",
           nativeQuery = true)
    List<ProductRevenueProjection> findTopProducts(@Param("from") LocalDateTime from,
                                                    @Param("to") LocalDateTime to);
}
