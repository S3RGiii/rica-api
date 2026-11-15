# RICA — Resumen ejecutivo

**Para quien decide sin haber leído el código.**

---

## Qué es

RICA conecta investigadores, sus publicaciones y sus grupos para sugerir colaboraciones.
El valor está en cruzar datos que hoy viven en sistemas separados: si una persona busca a
un investigador, quiere ver su producción completa en una sola pantalla, no dos.

## Cómo está construido, en una frase

Cinco piezas pequeñas que se hablan entre sí: dos que registran información, una que
responde las consultas, una que las enruta y una que lleva el registro de quién está
disponible. Cada una guarda sus propios datos.

## Por qué no es una sola aplicación

Un solo programa habría sido más barato de operar. Se separó por dos razones concretas:

1. **La consulta no puede caerse con la escritura.** Si el sistema que registra
   investigadores falla, la pantalla que muestra las fichas debe seguir funcionando. Eso
   solo se logra si son piezas distintas.
2. **La información de un investigador y sus publicaciones tiene ciclos de vida
   distintos.** Se guardan y se consultan de maneras diferentes; forzarlas a un mismo
   molde obliga a un esquema que no sirve bien a ninguna de las dos.

## Qué se gana y qué se paga

**Se gana:** una consulta que no depende de que todo esté arriba; la posibilidad de
cambiar una pieza sin tocar las demás; y pruebas automáticas que impiden que el diseño se
degrade.

**Se paga:** los datos no se actualizan todos al mismo tiempo. Al registrar algo, la
pantalla de consulta puede tardar unos instantes en reflejarlo. Es el precio de que esa
pantalla no dependa de los demás sistemas, y se decidió a conciencia.

## Los tres riesgos que hay que conocer

1. **Una eliminación puede quedar a medias.** Si el sistema que confirma la baja no
   responde, el investigador queda marcado como "en eliminación" sin completarse. Hoy no
   hay un mecanismo que lo revierta solo. **Es el riesgo principal y está registrado.**
2. **Un registro podría no reflejarse en la consulta** si el servicio falla justo entre
   guardar y avisar. La consulta se puede reconstruir, pero nadie se enteraría del hueco.
3. **No hay monitoreo en producción.** El sistema se prueba en cada cambio, pero nadie
   vigila el rendimiento mientras está en uso.

## Qué se decidió NO hacer, y por qué

- **No se usó una malla de servicios.** Es infraestructura para decenas de servicios con
  muchos equipos; aquí hay cinco piezas y un equipo pequeño. Añadiría complejidad sin
  resolver un problema existente.
- **No se guarda el historial completo de cada cambio** como secuencia de eventos. El
  estado actual es suficiente; el historial completo multiplicaría la complejidad.
- **No se contenerizó con Kubernetes.** Docker Compose cubre la necesidad actual.

## Lo que se pide

Nada urgente. El sistema funciona y está documentado. Lo que se recomienda, en orden:

1. **Resolver el riesgo 1** (la eliminación que puede quedar a medias) antes de usar el
   sistema con datos reales.
2. **Añadir monitoreo** cuando haya usuarios reales, para saber si el rendimiento aguanta.
3. **Mantener la disciplina de documentar cada decisión** con su alternativa descartada.
   Es lo que permite que alguien nuevo entienda el sistema en una hora.

## En una frase

> RICA es un sistema pequeño y bien separado, que gasta complejidad solo donde el negocio
> la necesita —la consulta y la baja de un investigador— y deja simple todo lo demás; sus
> riesgos están escritos y tienen plan, y lo que no se hizo está decidido y justificado.
