package pe.edu.upeu.cliente.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.cliente.dto.ClienteRequest;
import pe.edu.upeu.cliente.dto.ClienteResponse;
import pe.edu.upeu.cliente.entity.Cliente;
import pe.edu.upeu.cliente.exception.ResourceNotFoundException;
import pe.edu.upeu.cliente.mapper.ClienteMapper;
import pe.edu.upeu.cliente.repository.ClienteRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream()
                .map(clienteMapper::toResponse)
                .toList();
    }

    public ClienteResponse obtener(Long id) {
        log.info("Consultando perfil del cliente {}", id);
        return clienteMapper.toResponse(buscarOFallar(id));
    }

    public ClienteResponse crear(ClienteRequest request) {
        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un cliente con el email " + request.getEmail());
        }
        return clienteMapper.toResponse(clienteRepository.save(clienteMapper.toEntity(request)));
    }

    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscarOFallar(id);
        clienteMapper.actualizar(cliente, request);
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    private Cliente buscarOFallar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
    }
}
