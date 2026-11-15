# Reparto — cierre del curso

**RICA · Ingeniería de Software II · Lección 13**

El taller de talleres es individual en este repositorio (RICA es el caso de estudio del
curso, no el proyecto de grupo). El reparto refleja la autoría real del historial de Git.

| Parte | Lidera | Evidencia en el repositorio |
|---|---|---|
| `ARQUITECTURA.md` (L12) | Nicolás Ruiz | `docs/ARQUITECTURA.md`, commits `docs(l7)` y `docs(l9-l11)` |
| `ATAM-LITE.md` | Nicolás Ruiz | `docs/ATAM-LITE.md` |
| `MATRIZ.md` | Nicolás Ruiz | `docs/cierre/MATRIZ.md` |
| `CASO.md` | Nicolás Ruiz | `docs/cierre/CASO.md` |
| `GOBERNANZA.md` | Nicolás Ruiz | `docs/cierre/GOBERNANZA.md` |
| `RESUMEN-EJECUTIVO.md` | Nicolás Ruiz | `docs/cierre/RESUMEN-EJECUTIVO.md` |
| Talleres L3–L11 | Nicolás Ruiz | ramas `taller-ddd`, `taller-hexagonal`, `taller-microservicios-lineal`, `taller-l8-cqrs`; commits en `main` |

**Verificación del historial:**

```
git shortlog -sne --all
```

El `.mailmap` de la raíz unifica las dos identidades usadas (`sergio.ruiz01@uptc.edu.co`
y el correo noreply de GitHub), de modo que el historial reporta un solo autor.

**Nota sobre el proyecto de grupo:** el proyecto final del G5 (aseguradora digital) vive
en un repositorio separado y su documentación de cierre es independiente de este. RICA
es el sistema sobre el que se practicaron los patrones; la aseguradora es donde se
aplicaron.
