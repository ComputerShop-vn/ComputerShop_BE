package sp26.group3.computer.sba301_computershop;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import sp26.group3.computer.sba301_computershop.entity.Role;
import sp26.group3.computer.sba301_computershop.entity.User;
import sp26.group3.computer.sba301_computershop.repository.UserRepository;

import java.time.LocalDateTime;

@SpringBootApplication
public class Sba301ComputerShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(Sba301ComputerShopApplication.class, args);
    }

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository) {
        return args -> {

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            User admin = User.builder()
                    .username("admin")
                    .email("admin@gmail.com")
                    .password(encoder.encode("123456"))
                    .phoneNumber("0123456789")
                    .status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .role(
                            Role.builder()
                                    .roleId(1) // ADMIN đã tồn tại
                                    .build()
                    )
                    .build();

            userRepository.save(admin);
        };
    }
}
