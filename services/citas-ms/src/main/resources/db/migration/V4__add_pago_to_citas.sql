-- S8: datos que viajan en el evento cita.creada hacia veterinaria-pago-ms.
-- Nullable para conservar las citas creadas antes de S8.
ALTER TABLE citas
    ADD COLUMN monto NUMERIC(10, 2);

ALTER TABLE citas
    ADD COLUMN metodo_pago VARCHAR(20);
