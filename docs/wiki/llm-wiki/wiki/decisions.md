# Decisiones

## DECISIONES aprobadas por especificación

- La raíz coordina dos repositorios independientes y no constituye un tercer repositorio.
- La wiki global se versiona dentro de `citas-api`.
- No existe Express ni BFF.
- `scrum-spec-orchestrator` solo produce documentación dentro de `docs/wiki/scrum/` y no implementa código.
- Los workflows n8n se versionan como JSON dentro de `citas-api/automations/n8n/`.

Las decisiones de diseño aún no aprobadas se registran como preguntas abiertas, no en esta página.

## DECISIONES S2 aprobadas

- Autenticación REST bajo `/api/v1/auth` con registro, login y refresh.
- Access JWT de 15 minutos y refresh opaco rotativo de 7 días.
- Los secretos JWT solo provienen de variables de entorno.

**Fuentes:** `raw/specs/restricciones-tecnicas.md`, `raw/specs/PRD-v1.0.md`.
