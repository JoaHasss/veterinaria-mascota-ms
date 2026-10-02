package pe.edu.upeu.auth.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.auth.dto.LoginRequest;
import pe.edu.upeu.auth.dto.LoginResponse;
import pe.edu.upeu.auth.dto.RegistroRequest;
import pe.edu.upeu.auth.service.AuthService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RSAKey rsaKey;

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/api/v1/auth/registro")
    public ResponseEntity<Void> registro(@Valid @RequestBody RegistroRequest request) {
        authService.registro(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Publica solo la clave pública (JWKS). Disponible en la ruta estándar y
     * bajo /api/v1/auth para poder consultarla también a través del Gateway.
     */
    @GetMapping({"/.well-known/jwks.json", "/api/v1/auth/.well-known/jwks.json"})
    public Map<String, Object> jwks() {
        return new JWKSet(rsaKey.toPublicJWK()).toJSONObject();
    }
}
