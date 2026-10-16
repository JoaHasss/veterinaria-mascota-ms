package pe.edu.upeu.cliente.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.cliente.entity.AltaCliente;
import pe.edu.upeu.cliente.entity.Cliente;
import pe.edu.upeu.cliente.event.UsuarioRegistradoEvento;
import pe.edu.upeu.cliente.repository.AltaClienteRepository;
import pe.edu.upeu.cliente.repository.ClienteRepository;

import java.time.LocalDateTime;

/**
 * Transaccion local del paso 2 de la saga: el perfil (si procede) y el
 * registro de idempotencia se guardan juntos o no se guarda nada. Va en un
 * bean aparte para que AltaClienteService pueda capturar la violacion de
 * uk_altas_cliente_usuario fuera de la transaccion que la produjo.
 */
@Component
@RequiredArgsConstructor
public class AltaClienteTx {

    static final String MOTIVO_EMAIL_EN_USO =
            "El email ya pertenece a un cliente registrado en mostrador; requiere verificacion presencial";

    private final ClienteRepository clienteRepository;
    private final AltaClienteRepository altaClienteRepository;

    @Transactional
    public AltaCliente registrar(UsuarioRegistradoEvento evento) {
        AltaCliente.AltaClienteBuilder alta = AltaCliente.builder()
                .usuarioId(evento.getUsuarioId())
                .email(evento.getEmail())
                .fechaProceso(LocalDateTime.now());

        // Condicion de fallo de negocio: el email ya tiene un perfil de cliente.
        if (clienteRepository.existsByEmail(evento.getEmail())) {
            return altaClienteRepository.saveAndFlush(alta
                    .resultado(AltaCliente.Resultado.RECHAZADO)
                    .motivo(MOTIVO_EMAIL_EN_USO)
                    .build());
        }

        Cliente cliente = clienteRepository.save(Cliente.builder()
                .nombres(evento.getNombres())
                .apellidos(evento.getApellidos())
                .email(evento.getEmail())
                .telefono(evento.getTelefono())
                .direccion(evento.getDireccion())
                .build());
        return altaClienteRepository.saveAndFlush(alta
                .resultado(AltaCliente.Resultado.CREADO)
                .clienteId(cliente.getId())
                .build());
    }
}
