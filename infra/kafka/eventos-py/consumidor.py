"""Consumidor de prueba: lee cita-eventos y valida la estructura del contrato."""
import json
import os

from kafka import KafkaConsumer

BOOTSTRAP = os.getenv("BOOTSTRAP_SERVERS", "localhost:19092")
TOPICO = os.getenv("TOPICO", "cita-eventos")
CAMPOS = {"tipoEvento", "citaId", "idCliente", "monto", "metodoPago", "origen", "timestamp"}

consumidor = KafkaConsumer(
    TOPICO,
    bootstrap_servers=BOOTSTRAP,
    group_id="veterinaria-eventos-py",
    auto_offset_reset="earliest",
    consumer_timeout_ms=10000,
    value_deserializer=lambda v: json.loads(v.decode("utf-8")),
)

for msg in consumidor:
    faltan = CAMPOS - msg.value.keys()
    estado = "VALIDO" if not faltan else f"INVALIDO (faltan {sorted(faltan)})"
    print(f"[consumidor] particion={msg.partition} offset={msg.offset} key={msg.key.decode()} {estado} -> {msg.value}")

print("[consumidor] sin mensajes nuevos, fin")
