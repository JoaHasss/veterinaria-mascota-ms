-- Duenio de la cita: se toma del claim idCliente del JWT, nunca del body.
-- Nullable para conservar las citas creadas antes de S7.
ALTER TABLE citas
    ADD COLUMN id_cliente BIGINT;

CREATE INDEX IF NOT EXISTS idx_citas_id_cliente ON citas (id_cliente);
