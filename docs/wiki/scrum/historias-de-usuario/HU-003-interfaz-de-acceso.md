---
id: HU-003
tipo: historia-de-usuario
titulo: Interfaz de acceso
estado: Completada
epica: "[[EP-001-acceso-de-usuarios]]"
esfuerzo: Medio
sprint_sugerido: S2
dependencias:
  - "[[HU-001-registrar-usuario]]"
  - "[[HU-002-gestionar-sesion]]"
relacionadas: []
---

# HU-003 — Interfaz de acceso

**COMO** paciente, **QUIERO** usar formularios de registro e inicio de sesión, **PARA** acceder desde el navegador.

## Contexto y alcance
Primer frontend S2. Login, registro, confirmación de sesión e integración REST. React/TypeScript propuesto si no existe exportación. No incluye agenda ni recuperación de contraseña.

## Reglas de negocio
Mostrar errores sin revelar credenciales. Evitar doble envío. Etiquetas accesibles y diseño adaptable. URL de API configurable. Identificar el entorno como académico.

## Dependencias y relaciones
Épica [[EP-001-acceso-de-usuarios]]. Depende de [[HU-001-registrar-usuario]] y [[HU-002-gestionar-sesion]].

## Esfuerzo
Medio: interfaz, validación y comunicación REST.

## Tareas
- [x] T-01 (Medio): confirmar origen local del diseño y framework React; crear la interfaz según la decisión documentada.
- [x] T-02 (Medio): integrar formularios y estados de envío/error/éxito.
- [x] T-03 (Medio): verificar build, tipos y flujo en navegador.

## Criterios de aceptación
- CA-01: el frontend arranca y permite alternar entre login y registro mediante controles accesibles.
- CA-02: registro exitoso permite iniciar sesión; errores 400/409 se muestran sin perder los datos no sensibles del formulario.
- CA-03: login válido muestra identidad y logout; login inválido muestra error genérico.
- CA-04: una falla de red muestra un mensaje accionable y restablece la posibilidad de enviar.
- CA-05: formularios utilizables en móvil y escritorio, con etiquetas, foco visible y estado de carga.

## Definition of Done
- [x] CA verificados y build/typecheck correctos.
- [x] URL configurable y contrato REST coherente con backend.
- [x] Evidencia visual y procedencia local del diseño documentadas; dirección visual aprobada por el usuario el 2026-09-30.
- [x] Trazabilidad actualizada.

## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `main.tsx`; navegador | Login/registro, controles `aria-pressed` y etiquetas accesibles observados. |
| CA-02 | Cumple | `api.ts`; smoke-auth; navegador | Registro/login integrados; error 409 sintético muestra mensaje y conserva correo/datos no sensibles. |
| CA-03 | Cumple | Evidencia de navegador S2 en `s2-evidencia.md`; smoke-auth | Se documenta login correcto, identidad y logout; login inválido muestra error genérico. |
| CA-04 | Cumple | `api.ts`; navegador con request interceptada | Error accionable ante fallo de red; formulario queda habilitado. No se enviaron credenciales a la API en esta prueba. |
| CA-05 | Cumple | Build frontend; viewport 390×844 | Etiquetas/foco visibles; ancho de contenido 375px, sin desbordamiento horizontal. |
| DoD: build/API | Cumple | `npm run build`; `VITE_API_URL`; `contrato-auth.md` | Build y URL configurable verificados. |
| DoD: evidencia/procedencia | Cumple | `citas-web/README.md`; `styles.css`; aprobación del usuario en chat 2026-09-30 | Frontend de origen local; el usuario autorizó completar S2 y aprobó la dirección visual actual. No se afirma importación de Stitch/AI Studio. |
| DoD: trazabilidad | Cumple | Esta matriz; README Scrum | Evidencia y estado documental actualizados. |

## Historial
S2: propuesta local. 2026-09-30: usuario autorizó completar S2, confirmó que el frontend ya está diseñado y excluyó Stitch del cierre. HU-003 validada y completada; no se afirma una importación externa.
