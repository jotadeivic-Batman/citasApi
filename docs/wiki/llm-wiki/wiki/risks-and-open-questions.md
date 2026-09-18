# Riesgos y preguntas abiertas

## PREGUNTAS ABIERTAS

- No existe rama `develop` en ninguno de los dos repositorios.
- El remoto de API usa `main`, mientras el remoto del frontend muestra `master`; se requiere acordar la estrategia de ramas remotas.
- No se ha importado el frontend, por lo que React o Angular no debe inferirse.
- Falta contrato REST: recursos, errores, paginación, fechas/zona horaria y versionado.
- Falta definir la caducidad y recuperación de retenciones de cita y reprogramación.
- Falta formalizar transiciones de estados, estados terminales y permisos por transición.
- Falta definir el tratamiento inicial de `Medicina General` dentro del catálogo configurable de especialidades.
- Falta acordar el mecanismo transaccional concreto para proteger la reserva concurrente.
- Falta definir el mecanismo seguro de recuperación de contraseña en desarrollo.

## RIESGOS

- Usar `database/reference/` antes de autorización puede sustituir indebidamente el ejercicio de normalización del estudiante.
- Los workflows presentes son especificaciones Markdown; todavía no hay exportaciones JSON n8n.
- Persistir secretos o datos no sintéticos en la wiki, código, logs o workflows vulneraría las restricciones del laboratorio.
