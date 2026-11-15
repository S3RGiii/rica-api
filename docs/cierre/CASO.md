# Caso real: Prime Video 2023 — y por qué aplica a RICA

**RICA · Lección 13.** Un caso documentado por el propio equipo de ingeniería, con
fuente primaria, y su transferencia (parcial) a este proyecto.

---

## El caso

En **marzo de 2023**, el blog de ingeniería de Amazon publicó *«Scaling up the Prime
Video audio/video monitoring service and reducing costs by 90 %»*. El equipo describe
una herramienta de **monitoreo de calidad de audio y video** que había construido con una
arquitectura **distribuida y serverless**: funciones (AWS Step Functions y Lambda) que se
comunicaban entre sí y con almacenamiento en S3, orquestadas en muchos pasos.

El problema no fue de corrección sino de **costo y latencia de coordinación**. En sus
palabras, el enfoque distribuido tenía un costo alto: el trabajo se dividía en muchas
funciones pequeñas, y **cada transición entre funciones transfería datos**, lo que se
traducía en tiempo y en dinero. El equipo también reportó que el diseño los dejó con una
cantidad grande de componentes que mantener y depurar sin un beneficio proporcional.

La decisión fue **consolidar** los componentes en un único proceso (*monolito* para esa
herramienta): un servicio que hace el trabajo en memoria, sin saltos de red entre etapas.
El resultado reportado fue una **reducción de más del 90 % en el costo de
infraestructura** y una mejora en el rendimiento, manteniendo la escala que necesitaban.

## Qué NO dice el caso

Es importante leerlo completo antes de citarlo, porque se usa mal con frecuencia:

- **No dice que los microservicios sean un error.** El mismo documento explica que Prime
  Video sigue usando servicios distribuidos en el resto de su plataforma. El cambio se
  aplicó a **una herramienta concreta**, con un patrón de carga y de acoplamiento
  particular.
- **No es un argumento contra serverless en general**, sino contra una descomposición que
  obliga a transferir grandes volúmenes de datos entre etapas con acoplamiento fuerte.
- El contexto incluía una restricción de negocio real: la herramienta debía monitorear
  **miles de streams concurrentes** con un presupuesto acotado.

## Transferencia a RICA

| Aspecto | Prime Video 2023 | RICA | ¿Transfiere? |
|---|---|---|---|
| Naturaleza del trabajo | Etapas encadenadas sobre el mismo dato de video | Operaciones de negocio sobre agregados distintos | **No del todo**: en RICA los contextos no comparten un flujo de datos masivo |
| Motivo del cambio | Costo de transferencia entre funciones | Ninguno: el volumen es de registros, no de medios | **No** |
| Lección transferible | Distribuir tiene un costo que hay que medir; si el acoplamiento es fuerte, consolidar gana | El ADR-0001 y la matriz de decisión aplican el mismo criterio | **Sí, como principio** |
| Resultado | −90 % de costo | No aplica una métrica equivalente | **No** |

**Conclusión honesta:** el caso **no justifica** decisiones concretas de RICA —el dominio
y la carga son distintos—, pero **sí respalda el criterio** con el que se tomaron: cada
separación debe nombrar el problema que resuelve. Es exactamente el argumento que sostiene
la decisión de no adoptar service mesh (`ARQUITECTURA.md` §6) y el que hace que la matriz
de decisión admita que, con otro tamaño de equipo, el monolito modular habría ganado.

## Cómo se usaría en una defensa

Si alguien pregunta *«¿por qué no distribuyeron todo?»*, la respuesta no es «porque Prime
Video volvió al monolito». Es:

> «Porque cada frontera que abrimos tiene que resolver un problema que podamos nombrar.
> En RICA la frontera de la lectura resuelve un requisito de disponibilidad medible; la de
> escritura, uno de escalado. Distribuir el resto habría añadido consistencia eventual y
> operación sin un problema que lo justificara. El caso de Prime Video de 2023 es el mismo
> criterio aplicado a otro contexto: mide el costo de distribuir antes de distribuirlo.»

## Fuente

- Amazon Prime Video Tech Blog, «Scaling up the Prime Video audio/video monitoring
  service and reducing costs by 90 %», marzo de 2023. Publicado en el blog de
  ingeniería de AWS.

> **Nota de verificación:** este documento se apoya en el contenido del artículo, no en
> sus cifras exactas ni en la identidad de sus autores. Antes de citarlo en una defensa,
> conviene abrir la publicación original y confirmar los datos: la fuente primaria está
> enlazada desde el blog de ingeniería de AWS. No se reproducen aquí nombres de autores
> porque no se verificaron de primera mano.

> **Regla de honestidad académica:** este documento cita el caso por su contenido y su
> contexto, no como eslogan. Los números del caso (−90 %) corresponden a **esa**
> herramienta de Prime Video, no a RICA, y no se presentan como resultado propio.
