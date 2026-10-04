---
id: EP-001
tipo: epica
titulo: Acceso de usuarios
estado: Completada
historias:
  - "[[HU-001-registrar-usuario]]"
  - "[[HU-002-gestionar-sesion]]"
  - "[[HU-003-interfaz-de-acceso]]"
dependencias: []
---

# EP-001 — Acceso de usuarios

## Objetivo y valor
Permitir a un paciente ficticio crear su cuenta y acceder de forma autenticada al primer incremento S2.

## Actores
Visitante y USER. ADMIN y PROFESSIONAL no se crean por registro público.

## Alcance
Registro, login, tokens access/refresh, logout, consulta de identidad e interfaz de acceso.

## Fuera de alcance
Recuperación de contraseña, citas y administración.

## Reglas y dependencias
Email y documento únicos, hash de contraseña adaptativo, rol USER asignado en servidor, validación de datos y separación entre access y refresh. Requiere MySQL y toolchains del stack objetivo.

## Historias de usuario
- [[HU-001-registrar-usuario]]
- [[HU-002-gestionar-sesion]]
- [[HU-003-interfaz-de-acceso]]

## Criterio de completitud
- [x] Historias obligatorias completadas con evidencia de sus CA y DoD.
- [x] Sin dependencias bloqueantes para ejecutar el incremento.

## Riesgos e incógnitas
El usuario confirmó que el frontend ya está diseñado y pidió excluir Stitch→AI Studio del cierre S2. La dirección visual existente queda aprobada; no se afirma exportación/importación externa.

## Evidencia de cierre
- [[HU-001-registrar-usuario]] y [[HU-002-gestionar-sesion]]: CA/DoD verificados con pruebas automatizadas y smoke HTTP.
- [[HU-003-interfaz-de-acceso]]: CA/DoD verificados con build y navegador (incluyendo red fallida, error 409, móvil y CORS).
- Aprobación: el usuario autorizó completar S2 el 2026-09-30; su mensaje cubre el alcance HU-001–003 y la dirección visual local actual.

## Historial
2026-09-30: aprobación del usuario y cierre Scrum basado en las matrices de evidencia de las tres HU.
