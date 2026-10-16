-- S9: saga de alta de cliente. Un registro nuevo nace PENDIENTE (sin poder
-- iniciar sesion) hasta que veterinaria-cliente-ms crea el perfil
-- (cliente.creado -> ACTIVO) o lo rechaza (cliente.rechazado -> RECHAZADO).
ALTER TABLE usuarios
    ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO';

ALTER TABLE usuarios
    ADD COLUMN fecha_registro TIMESTAMP NOT NULL DEFAULT now();

-- El email es el recurso que reserva el registro. La compensacion lo libera:
-- la unicidad solo aplica a cuentas no rechazadas, asi la fila RECHAZADA se
-- conserva como rastro auditable y el email puede volver a usarse.
ALTER TABLE usuarios
    DROP CONSTRAINT uk_usuarios_email;

CREATE UNIQUE INDEX uk_usuarios_email_vigente
    ON usuarios (email)
    WHERE estado <> 'RECHAZADO';
