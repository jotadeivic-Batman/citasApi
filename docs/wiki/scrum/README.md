# Plan Scrum — FCV Citas

## Incrementos
- **S2 (Acceso de usuarios)**:
  - Épica: [[EP-001-acceso-de-usuarios]].
  - Historias: [[HU-001-registrar-usuario]], [[HU-002-gestionar-sesion]], [[HU-003-interfaz-de-acceso]].
  - Estado: Completado con aprobación del usuario y validación de HU-001–HU-003; ver evidencias en cada historia.
- **S3 (Gestión y agendamiento de citas)**:
  - Épica: [[EP-002-gestion-y-agendamiento-de-citas]].
  - Historias: [[HU-004-consultar-disponibilidad]], [[HU-005-agendar-cita]], [[HU-006-gestion-administrativa-y-medica]].
  - Estado: Implementado.

## Stack y Tecnologías
- Backend: Java 21, Spring Boot 3.5.x, Maven, MySQL 8.4, JPA, Flyway, Spring Security JWT.
- Frontend: React 19, TypeScript, Vite, Vanilla CSS.
- Arquitectura: Hexagonal en citas-api; REST directo sin BFF en citas-web.

## Evidencia y cierre S2 — 2026-09-30
- Aprobación explícita del usuario: “tienes aprobado y completa en su totalidad lo que se necesite”; se registra para EP-001, HU-001–HU-003 y la dirección visual local actual.
- Scrum Skill aplicada para validar las HU; cada historia incluye matriz CA/DoD. Una investigación independiente de solo lectura contrastó guía y evidencias S2.
- `GOAL_01_GUIADO_SIMPLE.md`: ejecutado como checkpoint guiado; registro, unicidad, BCrypt, tokens, rotación, casos negativos y `mvn verify` validados. VS Code no ofrece un comando nativo `/goal`.
- Modelo: V1/V2 normalizan el subconjunto implementado con catálogos, tablas puente y claves foráneas. Frente a `database/reference/erd.mmd`, roles usa `code` como PK natural en vez de un ID sustituto; el resto de entidades implementadas conserva relaciones normalizadas. EPS/régimen/planes/afiliaciones, reprogramación y recuperación de contraseña están en la referencia pero fuera de S2 y permanecen pendientes de incrementos posteriores.
- Diseño visual: el usuario confirmó que el frontend ya está diseñado y pidió excluir Stitch del cierre S2. Se registra aprobación de la interfaz existente; no se afirma importación externa.

