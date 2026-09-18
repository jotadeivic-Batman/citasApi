# Operaciones de la wiki

## INGEST

1. Confirmar que la fuente está aprobada y no contiene información sensible.
2. Guardar una copia inmutable y versionada en `raw/`.
3. Integrar solo conocimiento durable en las páginas existentes de `wiki/`.
4. Añadir enlaces cruzados, actualizar `wiki/index.md` y registrar el evento en `wiki/log.md`.

## QUERY

1. Leer `wiki/index.md`.
2. Consultar las páginas pertinentes.
3. Verificar contra fuente o código cuando el dato pueda haber cambiado.
4. Separar evidencia de inferencia en la respuesta.

## LEARN

Después de una interacción sustancial, persistir solamente conocimiento durable y clasificarlo según `content-model.md`. Verificar HECHOS antes de guardarlos.

## LINT

Detectar contradicciones, contenido obsoleto o duplicado, páginas huérfanas, enlaces rotos, decisiones sin aprobación y material sensible. Registrar hallazgos y correcciones en el log.
