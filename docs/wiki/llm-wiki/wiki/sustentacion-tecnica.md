# Guía de Sustentación Técnica — Plataforma FCV Citas

Esta guía resume los puntos clave para la sustentación y evaluación final del proyecto ante el docente o evaluador.

---

## 1. Demostración de Arquitectura Hexagonal y Principios de Diseño
- **Independencia del Dominio:** Las reglas de negocio (slots 30/60 min, anti-doble reserva, transiciones de estado) residen en `co.fcv.citas.application.SchedulingService` sin acoplamiento a frameworks.
- **Adaptadores:** Puertos e interfaces desacoplados implementados mediante adaptadores REST (`co.fcv.citas.adapter.web`) y persistencia JPA (`co.fcv.citas.adapter.persistence`).
- **Persistencia en 3FN:** Modelo relacional normalizado con migraciones Flyway (`V1` a `V4`).
- **Seguridad:** Spring Security con separación estricta de access/refresh JWT, hashes BCrypt, roles (`USER`, `PROFESSIONAL`, `ADMIN`) y autorización por ownership.

---

## 2. Flujo Completo de Negocio Demostrable
1. **Paciente (`USER`):** Registro/Login → Consulta de disponibilidad por sede y especialidad → Agendamiento de Medicina General (`APPROVED`) y Cita Especializada (`REQUESTED`) → Solicitud de Reprogramación (`PENDING`) o Cancelación.
2. **Profesional (`PROFESSIONAL`):** Publicación de bloques de disponibilidad por sede → Consulta de agenda → Cierre de atención médica (`COMPLETED` o `NO_SHOW`).
3. **Administrador (`ADMIN`):** Bandeja de aprobación/rechazo de solicitudes especializadas y reprogramaciones con motivo obligatorio → Gestión de profesionales y catálogos de EPS/Planes.

---

## 3. Red de Calidad y Ciclos Autónomos
- **Pruebas Automatizadas:** Cobertura de autenticación, contención concurrente anti-doble reserva (`concurrentPatientsCannotBookTheSameSlot`), reglas de slots consecutivos 30/60 min y ciclo de vida de reprogramación/recuperación de contraseñas.
- **Git Hooks:** Escaneo preventivo de secretos en stage (`check-staged-secrets.mjs`) que bloquea variables de entorno expuestas o claves privadas.
- **Ciclos Builder / Verifier:** Registro documentado de iteraciones y observabilidad en `s4-autonomous-loops.md`.

---

## 4. Automatizaciones n8n
- **WF-001:** Cron Schedule → Consulta citas próximas → Formateo HTML → Envío por Gmail.
- **WF-002:** Webhook desde backend → Formateo según evento (`APPROVED`, `REJECTED`, `CANCELLED`) → Notificación al paciente.
- **WF-003:** Resumen operativo diario agrupado por sede y estado.
