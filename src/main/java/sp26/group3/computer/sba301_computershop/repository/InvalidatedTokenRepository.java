package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sp26.group3.computer.sba301_computershop.entity.InvalidatedToken;

@Repository
public interface InvalidatedTokenRepository
        extends JpaRepository<InvalidatedToken, String> {
}
