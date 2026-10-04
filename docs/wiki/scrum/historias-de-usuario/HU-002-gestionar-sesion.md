---
id: HU-002
tipo: historia-de-usuario
titulo: Gestionar sesión JWT
estado: Completada
epica: "[[EP-001-acceso-de-usuarios]]"
esfuerzo: Alto
sprint_sugerido: S2
dependencias:
  - "[[HU-001-registrar-usuario]]"
relacionadas:
  - "[[HU-003-interfaz-de-acceso]]"
---

# HU-002 — Gestionar sesión JWT

**COMO** usuario registrado, **QUIERO** iniciar, renovar y cerrar mi sesión, **PARA** acceder a mis datos con autenticación.

## Contexto y alcance
PRD RF-02. Login por email/contraseña, JWT access de corta duración, refresh, logout e identidad autenticada. No incluye recuperación de contraseña ni pantallas de administración.

## Reglas de negocio
Credenciales incorrectas producen un error genérico. Tokens separados por propósito y clave, con expiración. Refresh de un solo uso mediante rotación y revocación persistente. Logout revoca la sesión; no conservar tokens en logs.

## Dependencias y relaciones
Épica [[EP-001-acceso-de-usuarios]]. Depende de [[HU-001-registrar-usuario]]; consumida por [[HU-003-interfaz-de-acceso]].

## Esfuerzo
Alto: coordina seguridad, persistencia y concurrencia de renovación.

## Tareas
- [x] T-01 (Medio): definir contrato de autenticación y errores.
- [x] T-02 (Alto): implementar emisión/validación, rotación y revocación.
- [x] T-03 (Medio): restringir rutas y CORS al origen configurado.
- [x] T-04 (Alto): probar acceso inválido, propósito de token y renovación repetida.

## Criterios de aceptación
- CA-01: credenciales válidas retornan access/refresh JWT con expiración e identidad; inválidas responden 401 genérico.
- CA-02: la consulta de identidad sin token, con token manipulado, expirado o refresh en lugar de access responde 401.
- CA-03: refresh válido emite un nuevo par; el anterior no puede reutilizarse.
- CA-04: logout invalida los tokens de esa sesión; otras sesiones independientes conservan su validez.
- CA-05: el origen permitido puede consumir la API; un origen distinto no recibe autorización CORS.

## Definition of Done
- [x] Todos los CA verificados con H2 y smoke HTTP contra la API conectada a MySQL.
- [x] Secretos externos al repositorio; access y refresh se configuran por variables distintas.
- [x] Pruebas de autenticación y autorización pasan.
- [x] Contrato, documentación y enlaces actualizados.

## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `AuthIntegrationTest#sessionLifecycleAndTokenSeparation`; smoke-auth | Login correcto/incorrecto y tokens separados. |
| CA-02 | Cumple | `AuthIntegrationTest#expiredAccessAndMissingAuthenticationAreRejected`; `sessionLifecycleAndTokenSeparation` | Rechaza ausencia, expiración, manipulación y token de propósito incorrecto. |
| CA-03 | Cumple | `AuthIntegrationTest#sessionLifecycleAndTokenSeparation`; `concurrentRefreshHasExactlyOneWinner` | Rotación de refresh; el token anterior se rechaza y solo una renovación concurrente vence. |
| CA-04 | Cumple | `AuthIntegrationTest#sessionLifecycleAndTokenSeparation` | Logout revoca esa sesión y conserva otra sesión independiente. |
| CA-05 | Cumple | `AuthIntegrationTest#corsOnlyAllowsConfiguredFrontend`; smoke-auth | Origen configurado permitido; origen no confiable rechazado. |
| DoD: persistencia | Cumple | `V1__identity_and_sessions.sql`; smoke-auth contra API/MySQL | Ciclo de sesión y revocación comprobados en el entorno local conectado a MySQL; pruebas de concurrencia en H2. |
| DoD: secretos separados | Cumple | `application.yml`; `docker-compose.yml`; `.env.example` | Access/refresh se configuran por variables distintas; no se incluyen valores secretos en esta evidencia. |
| DoD: pruebas/contrato | Cumple | `mvn verify` (10 pruebas); `contrato-auth.md`; smoke-auth (18 comprobaciones) | Resultados ejecutados el 2026-09-30. |

## Historial
S2: propuesta inicial. 2026-09-30: usuario autorizó completar S2; HU-002 validada y completada con la matriz de evidencia anterior.
