# Agente principal de `citas-api`

## Estado comprobado del repositorio

Al generar estas instrucciones, el repositorio contiene documentación y la estructura de wiki/automatizaciones, pero aún no tiene proyecto Spring Boot: no existen `pom.xml`, wrapper Maven, `src/`, configuración de aplicación ni migraciones Flyway. Tampoco hay historias de usuario ni DoD aprobados en `docs/wiki/scrum/`.

No asumir paquetes Java, comandos de build, endpoints ni esquema hasta que existan y se inspeccionen. No implementar funcionalidad mientras no haya una HU aprobada con DoD aplicable y una rama de trabajo acordada; el repositorio parte en `main` y aún no tiene `develop`.

## Alcance exclusivo

Este agente trabaja solo en `citas-api`:

- Java 21, Spring Boot 3.5.x y Maven cuando el proyecto sea inicializado.
- REST/JSON, Spring Security y JWT de access/refresh.
- MySQL 8.4, Spring Data JPA y Flyway.
- Dominio, casos de uso, puertos, adaptadores, pruebas backend y contratos del backend.
- Exportaciones JSON de n8n en `automations/n8n/`, solo durante las sesiones previstas y sin credenciales.

No editar `citas-web`, no introducir Express/BFF y no acoplar el backend a React o Angular.

## Fuentes que gobiernan el trabajo

Antes de intervenir, leer:

1. `../AGENTS.md`.
2. `README.md`, `../PRD.md`, `../RESTRICCIONES_TECNICAS.md` y `../database/REQUISITOS_NORMALIZACION_3FN.md`.
3. `docs/wiki/llm-wiki/wiki/index.md` y las páginas pertinentes.
4. La HU y el DoD aprobados en `docs/wiki/scrum/`, cuando existan.
5. El código, `pom.xml`, configuración, migraciones y pruebas ya existentes.

El agente no mantiene una wiki propia ni altera la wiki global por iniciativa propia. Debe entregar al orquestador evidencia y preguntas que ameriten actualizarla.

## Arquitectura obligatoria

- El dominio expresa reglas del PRD y no depende de Spring, JPA, HTTP ni clases de adaptadores.
- La aplicación contiene casos de uso y coordina el dominio.
- Los puertos definen las dependencias entre aplicación y mundo exterior.
- REST, persistencia JPA, seguridad/infraestructura y clientes externos son adaptadores.
- Los controladores traducen HTTP a casos de uso y respuestas; no contienen reglas de negocio ni acceso directo a repositorios.
- Respetar la estructura de paquetes que se establezca al inicializar el proyecto; no crear una segunda arquitectura paralela.

## Flujo obligatorio por HU

1. Localizar HU, criterios de aceptación y DoD aprobados. Si faltan, detener implementación y solicitar o proponer su definición en Scrum.
2. Identificar RF/RN del PRD, roles, ownership, reglas de estado, datos y contrato REST afectados.
3. Antes de editar, presentar un plan con archivos, migraciones, pruebas y efectos de contrato.
4. Implementar el mínimo coherente a través de dominio → aplicación/puertos → adaptadores.
5. Ejecutar las pruebas relevantes usando los comandos confirmados por el proyecto real.
6. Verificar separación arquitectónica, validación, autorización y DoD.
7. Informar evidencia ejecutada, resultados y todo lo no verificado.

## Persistencia y migraciones

- Todo cambio de esquema requiere una migración Flyway nueva, versionada y revisable; no modificar una migración ya aplicada.
- Justificar claves, cardinalidades, restricciones e índices frente a los requisitos de 3FN.
- Usar relaciones normalizadas para roles, especialidades, sedes, afiliación, estados, historial y reservas; no serializar listas en columnas.
- Diseñar y probar explícitamente la protección contra doble reserva, slots consecutivos de 60 minutos y conservación de la cita original durante una reprogramación pendiente.
- No usar `../database/reference/` como fuente de diseño sin autorización explícita del trainer.

## Seguridad y datos

- Secretos exclusivamente por variables de entorno; `.env.example` no contiene valores reales.
- No abrir, imprimir ni registrar passwords, JWT, refresh tokens, secretos de DB, OAuth/Gmail/n8n/MCP o credenciales personales.
- Aplicar hash adaptativo a passwords, separar access/refresh JWT y verificar rol y ownership en cada caso de uso expuesto.
- Aplicar validación del lado servidor y CORS explícito.
- Usar únicamente datos sintéticos del laboratorio, salvo información pública incluida de forma explícita en los requisitos.

## Contratos y coordinación

- No existe aún contrato REST aprobado. No inventar rutas, payloads, errores, paginación ni convenciones temporales como hechos.
- Si una HU requiere cambiar un contrato, informar al orquestador antes de implementarlo y aportar: recurso, método, autorización, request, response, errores y compatibilidad esperada.
- Un cambio contractual no se considera completo sin evidencia de backend y validación coordinada con `citas-web`.

## Pruebas y Git

- Desde S3, cubrir dominio, aplicación e integración REST/persistencia relevantes; añadir pruebas de autorización y reglas críticas cuando apliquen.
- No declarar una prueba como ejecutada hasta confirmar el comando en `pom.xml` o wrapper real y obtener su resultado.
- `main` es estable y `develop` es trabajo. Comprobar estado y rama antes de editar; no reescribir el historial ni ocultar progreso.
