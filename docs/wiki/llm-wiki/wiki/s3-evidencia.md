# Evidencia y cierre técnico — Sesión S3

## 1. Alcance de S3
Implementación completa del flujo de reservas de citas y verificación automatizada:
- **Catálogos y Disponibilidad:** Consulta pública de sedes, especialidades y horarios disponibles discretizados en bloques de 30 minutos.
- **Regla 30/60 min (RN-05):** Medicina General requiere 1 slot de 30 min; Cardiología / Especialidades de 60 min requieren 2 slots consecutivos en el mismo bloque de disponibilidad. Si existe un slot aislado de 30 min, se excluye de la disponibilidad de 60 min.
- **Citas Generales (RN-02):** Aprobación automática en estado `APPROVED` sin intervención administrativa.
- **Citas Especializadas (RN-03):** Creación en estado `REQUESTED` con retención preventiva de slots para evitar doble reserva (RN-01).
- **Gestión Administrativa (RN-04):** Aprobación a `APPROVED` y rechazo a `REJECTED` exigiendo obligatoriamente un motivo descriptivo.
- **Cancelación y Liberación (RF-14, RN-09):** Cancelación por parte del paciente o admin liberando inmediatamente los slots reservados para nuevas citas.
- **Anti-doble reserva (RN-01):** Prevención estricta de colisiones concurrentes o repetidas en el mismo horario.
- **Validación de reglas RN-06 y RN-08:** Bloqueo de creación de bloques o citas en el pasado y validación de correspondencia entre especialidad y profesional.

## 2. Cobertura y Verificación de Pruebas
Pruebas automatizadas en `SchedulingIntegrationTest.java`:
- `testCatalogsArePublic`: Valida endpoints públicos de sedes y especialidades.
- `availabilityReturnsOneOrTwoConsecutiveSlotsBySpecialtyDuration`: Valida generación y consumo de slots de 30 y 60 min.
- `concurrentPatientsCannotBookTheSameSlot`: Prueba de contención concurrente para dos solicitudes simultáneas al mismo slot (retorna 201 y 409).
- `testGeneralAppointmentIsAutoApprovedAndCannotBeDoubleBooked`: Ciclo completo de agendamiento general, rechazo por doble reserva, consulta en Mis Citas, cancelación y re-reserva exitosa tras liberación.
- `testSpecializedAppointmentRequiresAdminApprovalAndRejectionRequiresReason`: Ciclo de cita especializada (`REQUESTED`), validación de motivo obligatorio en rechazo (400 si está vacío), rechazo y liberación de cupo, y aprobación administrativa exitosa.
- `testProfessionalManagementAndListing`: Registro y listado de profesionales con autorización protegida para administradores.
- `testSixtyMinuteSpecialtyRequiresTwoConsecutiveSlotsAndRejectsIsolatedSlots`: Valida explícitamente que un slot aislado de 30 min no se ofrezca para citas de 60 min.
- `testBusinessRulesValidationRN06AndRN08`: Valida rechazo de bloques/citas en fechas pasadas y asignación errónea de especialidad no habilitada.

## 3. Red de Calidad y Git Hooks
- Hook `pre-commit` configurado localmente en ambos repositorios (`citas-api` y `citas-web`) mediante `scripts/install-hooks.ps1`.
- Escáner de secretos `scripts/check-staged-secrets.mjs` validado mediante autoprueba (`--self-test`) garantizando el bloqueo de secretos/tokens expuestos en el stage de git.
