# Contratos REST

## Estado

## Autenticación S2 — aprobado

Base: `/api/v1/auth`. Operaciones públicas JSON:

| Operación | Request | Éxito | Errores |
| --- | --- | --- | --- |
| `POST /register` | nombres, apellidos, tipo/número documento, email, teléfono, password | `201` con id, email y rol | `400`, `409` |
| `POST /login` | email, password | `200` con accessToken, refreshToken, tipo y expiración access | `400`, `401` |
| `POST /refresh` | refreshToken | `200` con tokens renovados | `400`, `401` |

El access token JWT tiene vigencia de 15 minutos. El refresh token es opaco,
rotativo, tiene vigencia de 7 días y se persiste solo como hash. Los secretos de
firma llegan por `JWT_ACCESS_SECRET` y `JWT_REFRESH_SECRET`; nunca se exponen en
respuestas o logs. El formato de error contiene `status`, `code` y `message`.

**Compatibilidad:** es la primera versión del contrato; `citas-web` no lo consume
aún porque su diseño/importación se realiza después en S2.

Cuando se diseñe, documentar por recurso: método, ruta, autorización, request, response, errores, paginación/filtros, fechas/zona horaria, compatibilidad y evidencia de consumo desde `citas-web`.

**Fuente:** `raw/specs/PRD-v1.0.md` establece REST/JSON directo; los detalles están pendientes de diseño y aprobación.
