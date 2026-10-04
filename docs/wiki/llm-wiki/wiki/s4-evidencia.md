# Evidencia y cierre técnico — Sesión S4

## 1. Alcance y Funcionalidades del MVP Completadas
- **RF-15 Reprogramación de Citas:**
  - El paciente puede solicitar reprogramación de citas aprobadas a una nueva fecha disponible.
  - La nueva franja queda retenida preventivamente en estado `PENDING`.
  - La cita original permanece vigente hasta la decisión del Administrador.
  - Al aprobar: se liberan los slots viejos, se asignan los nuevos y la cita se actualiza.
  - Al rechazar: se exige motivo de rechazo y se libera la nueva franja retenida conservando la cita original.
- **RF-03 Recuperación y Restablecimiento de Contraseña:**
  - Generación de token temporal de un solo uso con validez de 30 minutos.
  - Restablecimiento seguro de contraseña y actualización del hash BCrypt.
  - Inactivación inmediata del token tras su uso para prevenir ataques de repetición.
- **RF-04 / RF-06 Catálogo de Aseguramiento EPS y Afiliación:**
  - Administración de EPS y planes de aseguramiento (PBS Contributivo, Subsidiado, Particular).
  - Consulta pública de EPS/Planes disponibles.
  - Registro de afiliación de paciente a plan de salud.
- **RF-17 Cierre de Atención Médica:**
  - Marcación de citas como `COMPLETED` o `NO_SHOW` por el profesional asignado.

## 2. Cobertura de Pruebas Automatizadas
Se implementó `MvpIntegrationTest.java` validando:
- `testRescheduleFlowLifecycle`: Ciclo de vida completo de solicitud, rechazo con motivo, re-solicitud y aprobación de reprogramación.
- `testPasswordRecoveryAndReset`: Solicitud de token, cambio de contraseña, login con nuevas credenciales y verificación de un solo uso.
- `testInsuranceAndAffiliations`: Consulta de catálogos, afiliación de usuario y creación administrativa de EPS.

## 3. Registro de Ciclos Autónomos
Se documentó la evidencia de dos ciclos autónomos (Builder / Verifier) con formato estructurado JSON en [`s4-autonomous-loops.md`](s4-autonomous-loops.md).
