package sp26.group3.computer.sba301_computershop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ComputerShop REST API")
                        .version("1.0.0")
                        .description(
                                "API documentation for the ComputerShop project. " +
                                        "This Spring Boot REST API supports computer product management, " +
                                        "user authentication, order processing, and role-based access control."
                        )
                        .contact(new Contact()
                                .name("ComputerShop Development Team")
                                .email("support@computershop.com")
                                .url("https://computershop.com"))
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                )
                .addSecurityItem(
                        new SecurityRequirement().addList("Bearer Authentication")
                )
                .components(new Components()
                        .addSecuritySchemes(
                                "Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT Bearer token to authorize")
                        )
                );
    }
}