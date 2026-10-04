# Fuentes RAW

Esta carpeta almacena fuentes curadas e inmutables usadas por la LLM Wiki. Cada ingest debe registrar en `../wiki/log.md` la fuente, versión o fecha conocida, destino actualizado y resultado.

No guardar aquí secretos, tokens, credenciales, `.env`, PII innecesaria, conversaciones ni contenido no validado. No se modifica una fuente ya ingresada: una revisión aprobada se incorpora como un nuevo archivo versionado.

## Lote inicial aprobado

- `specs/PRD-v1.0.md`
- `specs/restricciones-tecnicas.md`
- `specs/requisitos-normalizacion-3fn.md`
- `governance/evidencias-y-trazabilidad.md`

`database/reference/` no forma parte del lote inicial. Es una solución de referencia del trainer y solo se ingesta tras autorización explícita.
