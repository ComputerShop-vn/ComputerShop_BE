package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sp26.group3.computer.sba301_computershop.entity.PCBuild;
import sp26.group3.computer.sba301_computershop.enums.BuildStatus;

import java.util.List;
import java.util.Optional;

public interface PCBuildRepository extends JpaRepository<PCBuild, Integer> {

    // Tìm draft hiện tại của user (mỗi user chỉ có 1 DRAFT)
    Optional<PCBuild> findByUserUserIdAndStatus(int userId, BuildStatus status);

    // Lấy tất cả builds của user theo status
    List<PCBuild> findByUserUserIdAndStatusOrderByCreatedAtDesc(int userId, BuildStatus status);

    // Lấy tất cả builds của user
    List<PCBuild> findByUserUserIdOrderByCreatedAtDesc(int userId);

    // Fetch build with items in one query (bypasses L1 cache — used after saving items)
    @Query("SELECT b FROM PCBuild b LEFT JOIN FETCH b.items WHERE b.buildId = :id")
    Optional<PCBuild> findWithItemsById(@Param("id") int id);
}
