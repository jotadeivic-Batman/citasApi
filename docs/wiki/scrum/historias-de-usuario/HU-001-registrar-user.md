---
id: HU-001
tipo: historia-de-usuario
titulo: "Registrar USER"
estado: En desarrollo
epica: "[[EP-001-identidad-y-sesion]]"
esfuerzo: "Alto"
sprint_sugerido: "S2 / Identidad base"
dependencias: []
relacionadas:
  - "[[HU-002-iniciar-sesion-jwt]]"
---

# HU-001 — Registrar USER

## Historia de usuario

**COMO** visitante ficticio  
**QUIERO** crear una cuenta de tipo `USER` con mis datos mínimos  
**PARA** acceder posteriormente al sistema de citas.

## Contexto y descripción

Implementa RF-01. El PRD exige nombres, apellidos, tipo y número de documento,
email, teléfono y contraseña. El contrato HTTP no está aprobado; esta historia no
autoriza inventar una ruta, DTO, formato de errores o esquema.

## Alcance

- Crear exclusivamente usuarios con rol `USER`.
- Validar los datos mínimos y la unicidad de email/documento en el servidor.
- Persistir la contraseña solo mediante hash adaptativo.
- Devolver errores controlados definidos por el contrato aprobado.

## Fuera de alcance

- Login, refresh, recuperación de contraseña y frontend.
- Afiliación, EPS/planes, perfiles de otros roles y datos reales.

## Reglas de negocio

- Email y documento son únicos.
- La contraseña no se persiste, devuelve ni registra en texto plano.
- La validación de cliente no sustituye la validación del servidor.

## Dependencias y relaciones

- Épica: [[EP-001-identidad-y-sesion]]
- Dependencias: contrato REST aprobado; bootstrap Spring/Maven/Flyway; decisión de
  esquema 3FN.
- Relacionadas: [[HU-002-iniciar-sesion-jwt]]

## Esfuerzo

**Nivel:** Alto

**Justificación de dificultad:** requiere dominio, persistencia normalizada,
migración Flyway, validación, seguridad y pruebas de integración.

## Tareas de desarrollo

- [ ] **T-01 — Aprobar el contrato de registro.**  
  Dificultad: Medio  
  Descripción: documentar operación, request, respuesta, errores, autorización y
  compatibilidad antes de implementar.
- [ ] **T-02 — Inicializar el vertical slice backend.**  
  Dificultad: Alto  
  Descripción: crear estructura hexagonal Spring/Maven y una migración Flyway
  inicial coherente con 3FN.
- [ ] **T-03 — Implementar registro seguro.**  
  Dificultad: Alto  
  Descripción: aplicar reglas de unicidad, hash adaptativo y traducción de errores.
- [ ] **T-04 — Cubrir casos positivos y negativos.**  
  Dificultad: Medio  
  Descripción: probar registro válido, duplicados, datos inválidos y ausencia de
  contraseña en persistencia/respuesta.

## Criterios de aceptación

### CA-01 — Registro válido

**Dado** un visitante con todos los datos mínimos válidos  
**Cuando** solicita el registro mediante la operación aprobada  
**Entonces** se crea una cuenta con rol `USER` y una respuesta controlada por el
contrato.

### CA-02 — Email y documento únicos

**Dado** un email o documento ya asociado a una cuenta  
**Cuando** un visitante intenta registrarse con ese valor  
**Entonces** la cuenta no se crea y recibe el error definido por el contrato.

### CA-03 — Contraseña protegida

**Dado** una solicitud de registro válida  
**Cuando** la cuenta se persiste  
**Entonces** la contraseña no queda en texto plano, no se devuelve y no se escribe
en logs.

### CA-04 — Validación server-side

**Dado** datos mínimos ausentes o inválidos  
**Cuando** se solicita el registro  
**Entonces** no se persiste una cuenta y se entrega un error controlado.

## Definition of Done

- [ ] El contrato REST de registro está aprobado y documentado.
- [ ] Existe una migración Flyway nueva, revisable y coherente con 3FN.
- [ ] Todos los criterios de aceptación tienen evidencia de pruebas relevantes.
- [ ] Email/documento se hacen cumplir también ante concurrencia/persistencia.
- [ ] El hash adaptativo y la ausencia de secretos/contraseñas en respuestas/logs
  están verificados.
- [ ] La trazabilidad de esta HU y su épica está actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Pendiente | — | Sin implementación. |
| CA-02 | Pendiente | — | Sin implementación. |
| CA-03 | Pendiente | — | Sin implementación. |
| CA-04 | Pendiente | — | Sin implementación. |
| DoD | Pendiente | — | Requiere aprobación y desarrollo. |

## Historial de validación

- S2 — HU creada en estado `Pendiente de aprobación`.
- S2 — Aprobada explícitamente por el usuario para iniciar desarrollo.
- S2 — Desarrollo iniciado en rama `develop`.

## Notas y decisiones

- La ruta, DTO y formato de errores son una decisión abierta, no una suposición.

<!-- Especificación inicial de S2. -->
