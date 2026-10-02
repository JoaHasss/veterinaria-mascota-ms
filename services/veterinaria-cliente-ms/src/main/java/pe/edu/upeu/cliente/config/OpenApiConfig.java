package pe.edu.upeu.cliente.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Habilita el botón "Authorize" de Swagger UI para enviar el Bearer token. */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "veterinaria-cliente-ms", version = "v1",
                description = "Perfiles de clientes protegidos con JWT (RBAC + ABAC)"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
