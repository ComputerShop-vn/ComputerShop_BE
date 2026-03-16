package sp26.group3.computer.sba301_computershop.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group3.computer.sba301_computershop.entity.OtpVerification;

import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Integer> {
    Optional<OtpVerification> findByEmail(String email);
    void deleteByEmail(String email);
}