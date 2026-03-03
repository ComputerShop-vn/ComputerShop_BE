package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group3.computer.sba301_computershop.entity.Attribute;

import java.util.Optional;

public interface AttributeRepository extends JpaRepository<Attribute, Integer> {
    boolean existsByAttributeName(String attributeName);
    Optional<Attribute> findByAttributeName(String attributeName);
}