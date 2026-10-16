-- S9 (Parte E): insumo clinico reservado al crear la cita (vacuna,
-- antiparasitario...). Se guarda para poder devolver la reserva exacta
-- si la cita se compensa (pago.fallido). Nullable: no toda cita usa insumo.
ALTER TABLE citas
    ADD COLUMN insumo_id BIGINT;

ALTER TABLE citas
    ADD COLUMN cantidad_insumo INTEGER;
