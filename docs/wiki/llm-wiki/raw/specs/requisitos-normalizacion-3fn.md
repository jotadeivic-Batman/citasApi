# Registro RAW — Requisitos de normalización 3FN

**Origen canónico:** `../../../../../../database/REQUISITOS_NORMALIZACION_3FN.md` desde la raíz del workspace.  
**Estado:** fuente aprobada para la actividad antes de consultar la referencia del trainer.

## Alcance curado

El diseño debe soportar usuarios/roles, profesionales, especialidades y sedes N:M, EPS-régimen-plan-afiliación, disponibilidad, citas de 30/60 minutos, estados, historial, reprogramación y tokens compatibles con el PRD.

Exige 1FN, 2FN y 3FN; exige justificar claves, cardinalidades, catálogos, prevención de doble reserva, representación de duración, conservación de cita original, auditoría, snapshots/FK e índices. No impone tablas concretas.
