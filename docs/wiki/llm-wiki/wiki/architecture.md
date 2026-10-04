# Arquitectura y límites

## HECHOS

- Backend: Java 21, Spring Boot 3.5.x, Maven, arquitectura hexagonal, Spring Data JPA, MySQL 8.4, Flyway y Spring Security con JWT access/refresh.
- Frontend: TypeScript, React o Angular según el resultado aprobado de Stitch y Google AI Studio.
- El frontend consume directamente API REST JSON de Spring Boot; no hay Express ni BFF.
- La URL de backend debe ser configurable por environment.
- Los catálogos fijos se cargan por seed y el modelo debe justificarse hasta 3FN.

## Separación de responsabilidades

- La lógica de negocio y persistencia pertenecen a `citas-api`.
- La interfaz y consumo REST pertenecen a `citas-web`.
- Un cambio de contrato exige evidencia de backend y frontend.

**Fuentes:** `raw/specs/restricciones-tecnicas.md`, `raw/specs/PRD-v1.0.md`, `raw/specs/requisitos-normalizacion-3fn.md`.
