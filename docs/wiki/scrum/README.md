---
tipo: indice-scrum
estado: Borrador
---

# Mapa Scrum — Sistema de citas

## Propósito

Este índice inicia S2 con el primer incremento verificable: identidad de USER y
sesión JWT. El alcance se deriva de RF-01 y RF-02 del PRD, las restricciones
técnicas y la normalización 3FN. Ninguna historia se considera aprobada todavía.

## Estado del repositorio al iniciar S2

- `citas-api` no tiene todavía proyecto Spring Boot, Maven, código ni migraciones.
- La rama de trabajo `develop` aún no existe.
- No existe un contrato REST aprobado.
- El frontend y su diseño aprobado se abordarán después del backend base, según la
  guía S2; no forman parte de estas dos HU.

## Épicas propuestas

- [[EP-001-identidad-y-sesion]] — registro, autenticación y ciclo de sesión.
- Futuras, pendientes de especificación: perfil y afiliación; catálogos y oferta;
  disponibilidad y citas; operaciones y auditoría.

## Incrementos sugeridos

1. **S2 / Identidad base:** [[HU-001-registrar-user]] y [[HU-002-iniciar-sesion-jwt]].
2. **S2 / Sesión renovable:** refresh y revocación, una vez se apruebe el contrato
   de autenticación.
3. **S3+:** las historias de agenda, reservas y administración se especificarán y
   aprobarán antes de desarrollo.

## Decisiones pendientes de aprobación humana

1. Contrato REST de registro, login, refresh y logout: rutas, DTOs, códigos y
   formato de errores.
2. Política de expiración de access/refresh token y mecanismo de revocación.
3. Estrategia inicial de esquema/migración, incluido qué versión Flyway inaugura la
   base de datos.

## Regla de aprobación

[[HU-001-registrar-user]] y [[HU-002-iniciar-sesion-jwt]] fueron aprobadas
explícitamente para S2. El contrato REST y la política de tokens siguen siendo
decisiones necesarias antes de implementar autenticación.

<!-- Especificación inicial de S2. -->
