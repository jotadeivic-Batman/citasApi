# Evidencia S2 — 2026-09-16

## Estado del entregable

| Punto mínimo | Resultado verificable |
|---|---|
| Dos repos inicializados | `citas-api/.git` y `citas-web/.git`, ramas main y develop; trabajo actual en develop |
| AGENTS principales | AGENTS raíz, backend y frontend creados |
| Scrum con HU aprobadas | EP-001 y HU-001–HU-003 aprobadas por el usuario el 2026-09-30; las tres HU se validaron y cerraron con matrices CA/DoD en `docs/wiki/scrum/` |
| Wiki global iniciada | Índice, dominio, arquitectura, contrato, modelo, fuentes, convenciones y log |
| BD y migraciones iniciales | MySQL 8.4 conectado; Flyway V1 aplicada y JPA valida el esquema |
| Registro y login JWT | Implementados, con refresh rotativo, logout revocable e identidad |
| Frontend diseñado/ejecutable | React ejecutable, build/responsive verificados; el usuario confirmó el diseño existente y excluyó Stitch del cierre. Creación local documentada. |
| Commit S2 en develop | Commit `feat(s2): bootstrap specs auth and frontend baseline` en cada repo; obtener hash con `git -C <repo> log -1 --oneline` |

## Validaciones ejecutadas

1. `docker compose exec -T citas-api-dev mvn -B -ntp verify`: **BUILD SUCCESS**, 10 tests, 0 fallos, 0 errores el 2026-09-30. Pruebas REST/persistencia en H2 aislado; el smoke-auth separado valida el API conectado a MySQL.
2. `node citas-api/scripts/smoke-auth.mjs`: **PASS, 18 comprobaciones HTTP**, API real conectada a MySQL. Health, registro, duplicados email/documento, intento de asignar roles, validación, login incorrecto/correcto, acceso sin token, propósito incorrecto, manipulación, refresh, reutilización y revocación.
3. `docker compose exec -T citas-web-dev npm run build`: **PASS**, TypeScript y build Vite 7.3.6. `npm install` reportó 0 vulnerabilidades en la instalación realizada.
4. Navegador Chrome local: registro sintético → mensaje de cuenta creada → login incorrecto con error genérico → login correcto con identidad → logout con confirmación. Ningún dato real fue enviado mediante las pruebas.
5. Inspección visual: formulario de registro en escritorio y login en móvil; corregida separación de palabras del título móvil. Viewport móvil solicitado 390×844; ancho útil medido 375px, scrollWidth 375px (sin desbordamiento horizontal). Restaurado viewport original al terminar.
6. `docker compose ps`: API y frontend Up; MySQL healthy. `git diff --check` sin errores de espacios. `.env.s2` confirmado como ignorado por Git.

## Cobertura de historias

| Historia | Evidencia técnica | Pendiente |
|---|---|---|
| HU-001 | `AuthIntegrationTest` registro/hash/duplicados/validación/rol; smoke-auth | Completada y aprobada por el usuario 2026-09-30 |
| HU-002 | `AuthIntegrationTest` sesión/rotación/concurrencia/CORS; smoke MySQL | Completada y aprobada por el usuario 2026-09-30; concurrencia medida en H2 |
| HU-003 | Build; registro/login/error/logout documentados; prueba de red fallida y 409 en navegador; vista móvil sin desbordamiento | Completada y dirección visual aprobada 2026-09-30; el usuario excluyó Stitch del cierre |

La aprobación del usuario y el cierre técnico de cada HU constan en sus notas Scrum. La aprobación no se presenta como ejecución de herramientas externas.

## Arranque y configuración

Desde PowerShell en la raíz: `./scripts/start-s2.ps1`.
Frontend http://localhost:5173; health http://localhost:8080/actuator/health.

Se conservó el volumen existente. El bootstrap Compose asegura el esquema configurado y sus permisos sin borrar datos. Se generaron secretos JWT independientes en `.env.s2`, sin leer/imprimir ni modificar el `.env` original.

Compose incluye `citas-db-ensure` como dependencia completada antes de iniciar la API. En cada `up`, crea el esquema seleccionado si falta y concede acceso al usuario de aplicación; no elimina datos ni volúmenes. El overlay S2 ahora hereda `MYSQL_DATABASE` para que bootstrap y API apunten al mismo esquema.

El 2026-09-30 se validó el arranque estándar repetido con el bootstrap: el servicio completó ambas ejecuciones. También se validó `scripts/start-s2.ps1` después de alinear el overlay con `MYSQL_DATABASE`: API health, `GET /api/catalogs/locations` con CORS y frontend respondieron HTTP 200; smoke-auth pasó 18 comprobaciones y el build web fue correcto. Se conservó el volumen sin borrar datos.

## Comparación de modelo y limitaciones S2

La comparación con `database/reference/erd.mmd` queda documentada en [modelo-datos.md](wiki/modelo-datos.md): V1/V2 separan identidad/roles/sesiones, catálogos, relaciones N:M, disponibilidad, citas, estados e historial. Roles usa `code` como PK natural en lugar del ID sustituto de la referencia, sin dependencia transitiva. EPS/régimen/planes/afiliaciones, solicitudes de reprogramación y recuperación de contraseña están en la referencia, pero son alcance posterior, no entregables S2.

- Por instrucción explícita del usuario, el handoff Stitch → AI Studio no es requisito de cierre S2; el diseño existente queda aprobado. No se afirma que haya sido importado desde una herramienta externa.
- GOAL_01 se validó como checkpoint guiado contra sus seis condiciones observables; el host VS Code no ofrece un comando nativo `/goal`.
- La aprobación de HU y dirección visual sí fue otorgada por el usuario el 2026-09-30.
- Git raíz preexistente conservado con cambios del workspace sin commit raíz; los dos repos de aplicación contienen sus commits propios. No se han creado remotos ni publicado repos.
- Flyway administrado por Spring Boot emitió aviso de compatibilidad probada hasta MySQL 8.1. En este entorno MySQL 8.4 sí ejecutó V1 y pasó el smoke; revisar actualización específica antes de ampliar migraciones.
- No incluye citas/agenda, recuperación de contraseña, administración ni automatizaciones de sesiones posteriores.
- Las pruebas crean cuentas sintéticas. No se eliminaron datos/volúmenes para preparar o cerrar la sesión.
