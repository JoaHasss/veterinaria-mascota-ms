"""Productor de prueba: publica un evento cita.creada cada 2 segundos.

Sirve para validar el contrato del evento y el topico antes de que exista
el microservicio que lo publica de verdad (citas-ms).
"""
import json
import os
import random
import time

from kafka import KafkaProducer

BOOTSTRAP = os.getenv("BOOTSTRAP_SERVERS", "localhost:19092")
TOPICO = os.getenv("TOPICO", "cita-eventos")
CANTIDAD = int(os.getenv("CANTIDAD", "5"))

productor = KafkaProducer(
    bootstrap_servers=BOOTSTRAP,
    key_serializer=lambda k: str(k).encode("utf-8"),
    value_serializer=lambda v: json.dumps(v).encode("utf-8"),
)

for i in range(CANTIDAD):
    cita_id = 9000 + i
    evento = {
        "tipoEvento": "cita.creada",
        "citaId": cita_id,
        "idCliente": 7,
        "mascotaId": 1,
        "monto": round(random.choice([50, 80, 120]) * 1.0, 2),
        "metodoPago": random.choice(["TARJETA", "YAPE", "EFECTIVO"]),
        "origen": "eventos-py",
        "timestamp": int(time.time() * 1000),
    }
    meta = productor.send(TOPICO, key=cita_id, value=evento).get(timeout=10)
    print(f"[productor] enviado citaId={cita_id} particion={meta.partition} offset={meta.offset} -> {evento}")
    time.sleep(2)

productor.flush()
print("[productor] fin")
