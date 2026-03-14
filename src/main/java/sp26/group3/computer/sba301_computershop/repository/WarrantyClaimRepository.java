package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group3.computer.sba301_computershop.entity.WarrantyClaim;

import java.util.List;

public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim, Integer> {
    List<WarrantyClaim> findByWarranty_Id(int warrantyId);
}
