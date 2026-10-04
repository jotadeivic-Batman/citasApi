---
id: HU-001
tipo: historia-de-usuario
titulo: Registrar usuario
estado: Completada
epica: "[[EP-001-acceso-de-usuarios]]"
esfuerzo: Medio
sprint_sugerido: S2
dependencias: []
relacionadas:
  - "[[HU-002-gestionar-sesion]]"
  - "[[HU-003-interfaz-de-acceso]]"
---

# HU-001 — Registrar usuario

**COMO** visitante ficticio, **QUIERO** registrar mi cuenta, **PARA** acceder como paciente al sistema.

## Contexto y alcance
PRD RF-01. Capturar nombres, apellidos, tipo/número de documento, email, teléfono y contraseña. Persistencia MySQL y migración inicial. No incluye afiliación ni recuperación de contraseña.

## Reglas de negocio
Email y documento únicos. Normalizar email; almacenar exclusivamente hash de contraseña. Asignar USER desde el servidor, sin admitir escalamiento por datos del cliente.

## Dependencias y relaciones
Épica [[EP-001-acceso-de-usuarios]]. Habilita [[HU-002-gestionar-sesion]] y [[HU-003-interfaz-de-acceso]].

## Esfuerzo
Medio: coordina validación, caso de uso y persistencia.

## Tareas
- [x] T-01 (Medio): modelar usuarios, roles y migración Flyway en 3FN.
- [x] T-02 (Medio): implementar registro, validación y hash adaptativo.
- [x] T-03 (Medio): verificar persistencia, duplicados y rol del usuario registrado.

## Criterios de aceptación
- CA-01: con datos válidos, el registro responde 201 con identificador y rol USER, sin contraseña ni hash.
- CA-02: email repetido, incluso con diferencias de mayúsculas, o documento repetido responde 409 sin crear otra cuenta.
- CA-03: campos obligatorios vacíos, email inválido o contraseña fuera de límites documentados responde 400.
- CA-04: una petición que intente asignar ADMIN no crea una cuenta privilegiada.

## Definition of Done
- [x] CA-01 a CA-04 verificados con evidencia.
- [x] Migración inicial ejecutada en MySQL; esquema de identidad justificado en 3FN.
- [x] Contraseña persistida como hash BCrypt; no aparece en respuestas.
- [x] Contrato y trazabilidad actualizados.

## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `AuthIntegrationTest#registrationPersistsHashAndOnlyUserRole`; smoke-auth | Respuesta 201, rol USER y sin contraseña/hash en respuesta. |
| CA-02 | Cumple | `AuthIntegrationTest#registrationPersistsHashAndOnlyUserRole` | Conflicto por email normalizado duplicado y documento duplicado. |
| CA-03 | Cumple | `AuthIntegrationTest#validationAndPrivilegeEscalationAreRejected` | Valida campos, email y límites de contraseña. |
| CA-04 | Cumple | `AuthIntegrationTest#validationAndPrivilegeEscalationAreRejected` | Rechaza asignación de ADMIN desde el payload. |
| DoD: migración/3FN | Cumple | `V1__identity_and_sessions.sql`; `modelo-datos.md`; ejecución MySQL documentada | El subconjunto de identidad separa usuarios, roles, relación N:M y sesiones. |
| DoD: hash y no exposición | Cumple | `AuthIntegrationTest#registrationPersistsHashAndOnlyUserRole` | Comprueba BCrypt y ausencia de contraseña/hash en respuesta. |
| DoD: contrato/trazabilidad | Cumple | `contrato-auth.md`; esta matriz | Contrato de registro y errores enlazado desde la wiki global. |

## Historial
S2: propuesta inicial. 2026-09-30: usuario autorizó completar S2; HU-001 validada y completada con la matriz de evidencia anterior.
