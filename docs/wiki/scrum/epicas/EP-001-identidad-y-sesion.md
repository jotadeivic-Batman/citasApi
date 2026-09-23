---
id: EP-001
tipo: epica
titulo: "Identidad y sesión"
estado: Borrador
historias:
  - "[[HU-001-registrar-user]]"
  - "[[HU-002-iniciar-sesion-jwt]]"
dependencias: []
---

# EP-001 — Identidad y sesión

## Objetivo

Permitir que una persona ficticia cree una cuenta `USER` y obtenga una sesión
autorizada sin exponer credenciales.

## Valor esperado

Establecer el primer vertical slice seguro sobre el que se apoyarán las
funcionalidades posteriores del producto.

## Actores

- Visitante.
- USER.

## Alcance

- Registro de USER con datos mínimos del PRD.
- Unicidad de email y documento.
- Hash adaptativo de contraseña.
- Login y emisión de access/refresh token.
- Renovación de sesión según contrato aprobado.

## Fuera de alcance

- Recuperación de contraseña.
- UI, perfil, afiliación y catálogos.
- Roles PROFESSIONAL y ADMIN.

## Reglas de negocio

- Solo un USER puede registrarse por sí mismo.
- Email y documento son únicos.
- Las contraseñas no se almacenan ni devuelven en texto plano.
- Access y refresh token son distintos y no se registran en logs.

## Dependencias

- Decisión aprobada sobre contrato REST y política de tokens.
- Inicialización de Spring Boot, MySQL y Flyway en una rama de trabajo.

## Historias de usuario

- [[HU-001-registrar-user]]
- [[HU-002-iniciar-sesion-jwt]]

## Criterio de completitud de la épica

- [ ] Las dos HU están `Completada` con evidencia de sus criterios y DoD.
- [ ] No hay credenciales ni secretos versionados.

## Riesgos e incógnitas

- El contrato REST aún no está aprobado.
- El repositorio no tiene una aplicación base ni una rama `develop`.

<!-- Especificación inicial de S2. -->
