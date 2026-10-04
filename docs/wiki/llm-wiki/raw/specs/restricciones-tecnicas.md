# Registro RAW — Restricciones técnicas

**Origen canónico:** `../../../../../../RESTRICCIONES_TECNICAS.md` desde la raíz del workspace.  
**Estado:** fuente aprobada para el laboratorio.

## Alcance curado

- Backend: Java 21, Spring Boot 3.5.x, Maven, arquitectura hexagonal, JPA, MySQL 8.4, Flyway, Spring Security y JWT access/refresh.
- Frontend: Node 24, TypeScript, React o Angular; REST directo a API, sin Express ni BFF.
- Base de datos: 3FN, catálogos fijos por seed y datos sintéticos.
- Git: solo dos repositorios, `main` estable y `develop` de trabajo, sin reescritura para ocultar progreso.
- Seguridad: secretos fuera del repositorio y `.env.example` sin valores reales.
- Pruebas: backend, frontend y validaciones cross-repo desde S3.
- n8n: workflows exportados como JSON en `citas-api/automations/n8n/`.
