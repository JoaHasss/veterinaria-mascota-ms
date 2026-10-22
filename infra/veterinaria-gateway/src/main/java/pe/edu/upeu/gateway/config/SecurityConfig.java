package pe.edu.upeu.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import pe.edu.upeu.gateway.security.JwtRoleConverter;

/**
 * El Gateway actúa como Resource Server: valida firma, expiración e issuer
 * del JWT contra el realm "veterinaria" de Keycloak (S07b, issuer-uri) y
 * aplica RBAC por ruta. Como el Gateway es Spring Cloud Gateway Server
 * WebMVC (servlet), las reglas por rol se declaran aquí con requestMatchers
 * en lugar de un GatewayFilter reactivo.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String CLIENTE = "CLIENTE";

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName("email");
        converter.setJwtGrantedAuthoritiesConverter(new JwtRoleConverter());
        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // S11: CORS antes que la autorizacion; sin esto el preflight OPTIONS
                // (que nunca lleva token) cae en anyRequest().authenticated() y da 401.
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // S07b: login y registro ya no pasan por el Gateway, los atiende Keycloak
                        .requestMatchers("/actuator/**").permitAll()
                        // Catálogo de mascotas/especies: lectura CLIENTE o ADMIN, escritura solo ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/v1/mascotas/**", "/api/v1/especies/**")
                                .hasAnyRole(CLIENTE, ADMIN)
                        .requestMatchers("/api/v1/mascotas/**", "/api/v1/especies/**").hasRole(ADMIN)
                        // Insumos (S9): consulta CLIENTE o ADMIN; descontar/restaurar stock solo ADMIN
                        // (citas-ms los invoca directo por Eureka, sin pasar por el Gateway)
                        .requestMatchers(HttpMethod.GET, "/api/v1/insumos/**").hasAnyRole(CLIENTE, ADMIN)
                        .requestMatchers("/api/v1/insumos/**").hasRole(ADMIN)
                        // Citas: solo CLIENTE (el idCliente se toma del JWT en citas-ms)
                        .requestMatchers("/api/v1/citas/**").hasRole(CLIENTE)
                        // Perfiles de cliente: el control fino (ABAC) lo hace veterinaria-cliente-ms
                        .requestMatchers("/api/v1/clientes/**").hasAnyRole(CLIENTE, ADMIN)
                        // Pagos (S8): solo consulta; se registran al consumir el evento cita.creada.
                        // S11: el listado completo es solo ADMIN (ocultarlo en Angular no basta,
                        // la SPA no es el limite de seguridad); el pago de una cita lo ve CLIENTE.
                        .requestMatchers(HttpMethod.GET, "/api/v1/pagos").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/v1/pagos/**").hasAnyRole(CLIENTE, ADMIN)
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }
}
