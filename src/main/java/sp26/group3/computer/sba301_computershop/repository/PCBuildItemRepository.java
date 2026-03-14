package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group3.computer.sba301_computershop.entity.PCBuildItem;
import sp26.group3.computer.sba301_computershop.enums.ComponentType;

import java.util.List;
import java.util.Optional;

public interface PCBuildItemRepository extends JpaRepository<PCBuildItem, Integer> {

    List<PCBuildItem> findByBuildBuildId(int buildId);

    // SINGLE_SLOT: tìm 1 item theo type (dùng cho upsert check và removeItem)
    Optional<PCBuildItem> findByBuildBuildIdAndComponentType(int buildId, ComponentType componentType);

    // MULTI_SLOT: tìm tất cả items cùng type (dùng để removeItem xóa hết)
    List<PCBuildItem> findAllByBuildBuildIdAndComponentType(int buildId, ComponentType componentType);

    // MULTI_SLOT upsert: tìm theo cả type và variantId
    Optional<PCBuildItem> findByBuildBuildIdAndComponentTypeAndVariantVariantId(
            int buildId, ComponentType componentType, int variantId);

    boolean existsByBuildBuildIdAndComponentType(int buildId, ComponentType componentType);

    void deleteByBuildBuildId(int buildId);
}
