# Retrospectiva — Nicolás Ruiz

**RICA · Ingeniería de Software II · Cierre del curso**

---

## Qué salió bien

**El puerto de dominio fue la decisión que más rindió.** En el taller L5,
`PublicacionService` pasó a depender de `VerificadorInvestigador` en vez del repositorio
de investigadores. Cuando en L6 hubo que reemplazar el adaptador falso por gRPC, **el
caso de uso no cambió una línea**. Eso es lo que la teoría llama "el puerto aísla al
núcleo de la tecnología", y verlo funcionar sobre el propio código es distinto a leerlo.

**Las funciones de aptitud cambiaron cómo trabajo.** Escribir el `ArquitecturaTest` con
ArchUnit obligó a revisar si el código cumplía lo que la documentación decía. Encontré que
la respuesta era sí, pero solo porque pude verificarlo. Una regla que no se ejecuta es una
intención, no una garantía.

**Separar la lectura de la escritura resolvió un problema real.** La ficha del
investigador cruzaba dos bases. Con CQRS la pantalla dejó de depender de que ambos
servicios estuvieran arriba. El costo —consistencia eventual— es visible y está
documentado.

## Qué no salió bien

**Documenté después de construir, no mientras.** Los ADR los escribí al final, cuando el
código ya estaba. Eso hizo que las "alternativas descartadas" fueran en parte
reconstrucciones de lo que recordaba haber pensado, no registros de la decisión real. Un
ADR escrito al momento cuesta cinco minutos; escrito después, cuesta una hora y pierde
fidelidad.

**Subestimé el costo de la evidencia.** Cité tres hashes que no existían en el repositorio
y afirmé que las fechas no se habían reescrito cuando en otro repositorio sí se habían
modificado. Los corregí, pero el problema de fondo fue asumir que lo escrito era cierto
sin verificarlo con `git cat-file` y `git log`.

**La Saga quedó sin compensación.** El escenario de mayor riesgo del ATAM —el que menos
dificultad tenía de resolverse— sigue abierto. Añadir una marca de tiempo y un job de
reversión habría costado menos que analizarlo.

## Qué haría distinto

1. **Un ADR por decisión, en el momento.** Junto al commit que la implementa, no en un
   bloque al final.
2. **Verificar cada afirmación de la documentación contra el repositorio**, con un comando,
   antes de escribirla.
3. **Empezar por el escenario de mayor riesgo del ATAM**, no por el que más ganas daba.
4. **Escribir el test de arquitectura antes de refactorizar**: habría detectado el
   acoplamiento antes de tener que mover paquetes con prisa.

## Lo que me llevo

La idea que más me cambió la forma de mirar el diseño: **toda decisión de arquitectura es
un compromiso, y un compromiso sin costo escrito es una decisión que no se entendió.** El
ejercicio de sensibilidad de la matriz de decisión —descubrir que con otro tamaño de equipo
el monolito habría ganado— es más valioso que el número final, porque muestra que la
decisión depende del contexto y no de la moda.

Y una segunda: **distribuir no es gratis.** El caso de Prime Video de 2023 y el propio
taller lo confirman. Cada frontera que se abre tiene que resolver un problema que se pueda
nombrar; si no, es complejidad sin beneficio.
