package pe.edu.upeu.veterinaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.veterinaria.entity.Insumo;
import pe.edu.upeu.veterinaria.exception.ResourceNotFoundException;
import pe.edu.upeu.veterinaria.exception.StockInsuficienteException;
import pe.edu.upeu.veterinaria.repository.InsumoRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsumoService {

    private final InsumoRepository insumoRepository;

    public List<Insumo> listar() {
        return insumoRepository.findAll();
    }

    public Insumo obtener(Long id) {
        return buscarOFallar(id);
    }

    /**
     * Leer, comparar y guardar van en una sola transaccion: sin ella dos
     * reservas casi simultaneas podrian leer el mismo stock y perder un descuento.
     */
    @Transactional
    public Insumo descontarStock(Long id, Integer cantidad) {
        Insumo insumo = buscarOFallar(id);
        if (insumo.getStock() < cantidad) {
            log.warn("component=insumo-service insumoId={} cantidad={} stock={} status=STOCK_INSUFICIENTE", id, cantidad, insumo.getStock());
            throw new StockInsuficienteException("Stock insuficiente para el insumo " + id
                    + ": disponible " + insumo.getStock() + ", solicitado " + cantidad);
        }
        int anterior = insumo.getStock();
        insumo.setStock(anterior - cantidad);
        log.info("component=insumo-service insumoId={} cantidad={} status=STOCK_DESCONTADO stock={}->{}", id, cantidad, anterior, insumo.getStock());
        return insumoRepository.save(insumo);
    }

    @Transactional
    public Insumo restaurarStock(Long id, Integer cantidad) {
        Insumo insumo = buscarOFallar(id);
        int anterior = insumo.getStock();
        insumo.setStock(anterior + cantidad);
        log.info("component=insumo-service insumoId={} cantidad={} status=STOCK_RESTAURADO stock={}->{}", id, cantidad, anterior, insumo.getStock());
        return insumoRepository.save(insumo);
    }

    private Insumo buscarOFallar(Long id) {
        return insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado: " + id));
    }
}
