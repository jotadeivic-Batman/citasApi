---
id: HU-002
tipo: historia-de-usuario
titulo: "Iniciar sesión con JWT"
estado: Bloqueada
epica: "[[EP-001-identidad-y-sesion]]"
esfuerzo: "Alto"
sprint_sugerido: "S2 / Identidad base"
dependencias:
  - "[[HU-001-registrar-user]]"
relacionadas: []
---

# HU-002 — Iniciar sesión con JWT

## Historia de usuario

**COMO** USER registrado  
**QUIERO** autenticarme con email y contraseña y renovar mi sesión  
**PARA** consumir únicamente las operaciones autorizadas del sistema.

## Contexto y descripción

Implementa RF-02. El backend emitirá access y refresh token diferenciados, pero
las rutas, la forma de las respuestas, expiraciones, rotación y revocación siguen
pendientes de decisión explícita. Esta historia depende de [[HU-001-registrar-user]].

## Alcance

- Verificar credenciales de USER de forma segura.
- Emitir access y refresh token separados según contrato aprobado.
- Renovar una sesión mediante refresh token válido según la política aprobada.
- Rechazar credenciales y refresh inválidos sin filtrar información sensible.

## Fuera de alcance

- UI, logout/revocación si no está incluida en el contrato aprobado, recuperación
  de contraseña y autorización de las demás funcionalidades.

## Reglas de negocio

- La contraseña se compara contra un hash adaptativo.
- Access y refresh token tienen propósitos y ciclos de vida separados.
- Tokens y contraseñas no se persisten o registran en texto plano.

## Dependencias y relaciones

- Épica: [[EP-001-identidad-y-sesion]]
- Dependencias: [[HU-001-registrar-user]], contrato REST y política JWT aprobados.
- Relacionadas: ninguna.

## Esfuerzo

**Nivel:** Alto

**Justificación de dificultad:** exige seguridad, modelado de ciclo de tokens,
adaptadores HTTP y pruebas negativas de autenticación.

## Tareas de desarrollo

- [ ] **T-01 — Aprobar contrato y política JWT.**  
  Dificultad: Medio  
  Descripción: definir operaciones, claims mínimos, expiraciones, refresh,
  revocación y errores sin introducir secretos en la documentación.
- [ ] **T-02 — Implementar autenticación.**  
  Dificultad: Alto  
  Descripción: verificar credenciales y emitir tokens separados desde la capa de
  aplicación, con controladores sin reglas de dominio.
- [ ] **T-03 — Implementar renovación de sesión.**  
  Dificultad: Alto  
  Descripción: validar el refresh token y emitir la sesión renovada según la
  política aprobada.
- [ ] **T-04 — Probar seguridad y errores.**  
  Dificultad: Medio  
  Descripción: cubrir login/refresh válidos e inválidos, sin exponer credenciales
  ni tokens en logs.

## Criterios de aceptación

### CA-01 — Login válido

**Dado** un USER registrado y activo con credenciales correctas  
**Cuando** solicita inicio de sesión mediante la operación aprobada  
**Entonces** recibe access y refresh token diferenciados conforme al contrato.

### CA-02 — Credenciales inválidas

**Dado** email inexistente o contraseña incorrecta  
**Cuando** se intenta iniciar sesión  
**Entonces** el servidor no emite tokens y responde con el error controlado del
contrato sin revelar información sensible.

### CA-03 — Renovación válida

**Dado** un refresh token vigente y válido  
**Cuando** se solicita renovación de sesión  
**Entonces** el servidor entrega la sesión renovada según la política aprobada.

### CA-04 — Refresh inválido

**Dado** un refresh ausente, inválido, vencido o revocado según la política  
**Cuando** se solicita renovación  
**Entonces** no se emiten tokens y se devuelve un error controlado.

## Definition of Done

- [ ] El contrato de login/refresh y la política de tokens están aprobados.
- [ ] Los access y refresh token son distintos y no contienen ni exponen secretos.
- [ ] Login y refresh válidos/negativos tienen pruebas automatizadas relevantes.
- [ ] La autorización y manejo de errores se validan en el adaptador REST.
- [ ] `mvn test` pasa en el proyecto inicializado.
- [ ] La trazabilidad de esta HU y su épica está actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Pendiente | — | Sin implementación. |
| CA-02 | Pendiente | — | Sin implementación. |
| CA-03 | Pendiente | — | Política no aprobada. |
| CA-04 | Pendiente | — | Política no aprobada. |
| DoD | Pendiente | — | Requiere aprobación y desarrollo. |

## Historial de validación

- S2 — HU creada en estado `Pendiente de aprobación`.
- S2 — Aprobada explícitamente por el usuario para iniciar desarrollo.
- S2 — Desarrollo iniciado en rama `develop`.
- S2 — Bloqueada tras tres intentos de `mvn test`: el refresh token carga un
  USER cuyo identificador no está disponible durante la rotación. Evidencia:
  `target/surefire-reports/co.fcv.citas.identity.AuthControllerTest.txt`.

## Notas y decisiones

- No se fijan rutas, expiraciones, claims ni formato de errores antes de la
  aprobación explícita del contrato.

<!-- Especificación inicial de S2. -->
