package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group3.computer.sba301_computershop.entity.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

}
