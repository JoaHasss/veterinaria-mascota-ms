package pe.edu.upeu.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.auth.dto.LoginRequest;
import pe.edu.upeu.auth.dto.LoginResponse;
import pe.edu.upeu.auth.dto.RegistroRequest;
import pe.edu.upeu.auth.dto.RegistroResponse;
import pe.edu.upeu.auth.entity.EstadoUsuario;
import pe.edu.upeu.auth.entity.Rol;
import pe.edu.upeu.auth.entity.Usuario;
import pe.edu.upeu.auth.exception.InvalidCredentialsException;
import pe.edu.upeu.auth.exception.ResourceNotFoundException;
import pe.edu.upeu.auth.kafka.UsuarioEventosProducer;
import pe.edu.upeu.auth.repository.RolRepository;
import pe.edu.upeu.auth.repository.UsuarioRepository;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioEventosProducer usuarioEventosProducer;

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (AuthenticationException e) {
            log.warn("Login fallido para {}: {}", request.getEmail(), e.getClass().getSimpleName());
            throw new InvalidCredentialsException("Email o contraseña inválidos");
        }

        Usuario usuario = usuarioRepository.findVigenteByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado"));

        String token = jwtService.generarToken(usuario);
        log.info("Token emitido para {} (roles={}, idCliente={})", usuario.getEmail(),
                usuario.getRoles().stream().map(Rol::getNombre).toList(), usuario.getIdCliente());

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiracionSegundos())
                .build();
    }

    /**
     * S9: primer paso de la saga de alta de cliente. La cuenta nace PENDIENTE
     * y deshabilitada (reserva el email) y, tras el commit, se publica
     * usuario.registrado. El perfil lo crea veterinaria-cliente-ms; la cuenta
     * se activa o se compensa al recibir su respuesta.
     */
    @Transactional
    public RegistroResponse registro(RegistroRequest request) {
        if (usuarioRepository.findVigenteByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new ResourceNotFoundException("Rol CLIENTE no encontrado"));

        Usuario nuevoUsuario = usuarioRepository.save(Usuario.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .habilitado(false)
                .estado(EstadoUsuario.PENDIENTE)
                .roles(new HashSet<>(Set.of(rolCliente)))
                .build());
        log.info("component=auth-service usuarioId={} email={} status=REGISTRO_PENDIENTE estado={}",
                nuevoUsuario.getId(), nuevoUsuario.getEmail(), nuevoUsuario.getEstado());
        usuarioEventosProducer.publicarUsuarioRegistrado(nuevoUsuario, request);
        return toResponse(nuevoUsuario);
    }

    public RegistroResponse consultarRegistro(Long usuarioId) {
        return toResponse(usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado: " + usuarioId)));
    }

    /**
     * Reaccion a cliente.creado. Guardia de idempotencia: solo una cuenta
     * PENDIENTE se activa; un evento repetido no cambia nada.
     */
    @Transactional
    public void activar(Long usuarioId, Long clienteId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null || usuario.getEstado() != EstadoUsuario.PENDIENTE) {
            log.warn("component=auth-service eventType=cliente.creado usuarioId={} status=IGNORADO motivo=\"la cuenta no existe o ya no esta pendiente\" estadoActual={}",
                    usuarioId, usuario == null ? "-" : usuario.getEstado());
            return;
        }
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setHabilitado(true);
        usuario.setIdCliente(clienteId);
        usuarioRepository.save(usuario);
        log.info("component=auth-service eventType=cliente.creado usuarioId={} status=CUENTA_ACTIVADA idCliente={} estado=PENDIENTE->ACTIVO",
                usuarioId, clienteId);
    }

    /**
     * S9: paso compensatorio, reaccion a cliente.rechazado. Misma guardia que
     * activar(): solo compensa una cuenta PENDIENTE. No borra la fila: la pasa a
     * RECHAZADO (rastro auditable), la deja deshabilitada y libera el email
     * reservado (el indice unico parcial excluye las cuentas RECHAZADAS).
     */
    @Transactional
    public void compensarRegistro(Long usuarioId, String motivo) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null || usuario.getEstado() != EstadoUsuario.PENDIENTE) {
            log.warn("component=auth-service eventType=cliente.rechazado usuarioId={} status=IGNORADO motivo=\"la cuenta no existe o ya no esta pendiente\" estadoActual={}",
                    usuarioId, usuario == null ? "-" : usuario.getEstado());
            return;
        }
        usuario.setEstado(EstadoUsuario.RECHAZADO);
        usuario.setHabilitado(false);
        usuarioRepository.save(usuario);
        log.info("component=auth-service eventType=cliente.rechazado usuarioId={} status=COMPENSADA estado=PENDIENTE->RECHAZADO email={} recurso=email_liberado motivoRechazo=\"{}\"",
                usuarioId, usuario.getEmail(), motivo);
    }

    private RegistroResponse toResponse(Usuario usuario) {
        return RegistroResponse.builder()
                .usuarioId(usuario.getId())
                .email(usuario.getEmail())
                .estado(usuario.getEstado().name())
                .idCliente(usuario.getIdCliente())
                .fechaRegistro(usuario.getFechaRegistro())
                .build();
    }
}
