# Dominio y reglas

## HECHOS

- Los actores son USER, PROFESSIONAL y ADMIN.
- Existen dos sedes fijas de laboratorio; los datos privados reales están fuera de alcance.
- Una especialidad dura 30 o 60 minutos; 60 minutos requiere dos slots consecutivos de 30 minutos.
- Las citas generales se aprueban automáticamente; las especializadas inician como `REQUESTED` y requieren decisión de ADMIN.
- Las reprogramaciones pendientes retienen una nueva franja sin liberar la franja de la cita original hasta la aprobación.
- Todo cambio de estado de una cita requiere historial de auditoría.

## Reglas críticas

- No puede existir doble reserva o retención de slots.
- No se permiten bloques ni citas en el pasado.
- El profesional solo publica agenda en sedes asignadas.
- Cancelaciones y rechazos liberan las reservas correspondientes.

**Fuente:** `raw/specs/PRD-v1.0.md`.
