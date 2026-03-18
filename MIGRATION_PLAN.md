# Plan de Migración: ZIO 2 → Kyo

## Resumen Ejecutivo

**Proyecto:** kyo-protoquill (clonado de zio/zio-protoquill)  
**Objetivo:** Migración completa del sistema de efectos ZIO 2.x a Kyo  
**Rama de trabajo:** `kyo-ready`  
**Fecha de inicio:** 2026-03-18

---

## 1. Análisis del Proyecto

### 1.1 Estructura de Módulos

| Módulo | Descripción | Dependencias ZIO |
|--------|-------------|------------------|
| `quill-sql` | Motor SQL base | zio |
| `quill-sql-tests` | Tests de SQL | quill-sql |
| `quill-jdbc` | Contexto JDBC base | - |
| `quill-doobie` | Integración Doobie | - |
| `quill-zio` | **Contexto ZIO principal** | zio, zio-streams |
| `quill-jdbc-zio` | **JDBC + ZIO** | quill-zio, zio, zio-json |
| `quill-cassandra` | Motor Cassandra | - |
| `quill-cassandra-zio` | **Cassandra + ZIO** | quill-cassandra, quill-zio, zio, zio-streams |
| `quill-caliban` | Integración GraphQL (Caliban) | - |

### 1.2 Mapeo de APIs ZIO → Kyo

| ZIO 2 | Kyo | Descripción |
|--------|-----|-------------|
| `ZIO[R, E, A]` | `A < (Abort[E] & Env[R] & Sync & Async)` | Efectos con ambiente y errores |
| `ZIO.succeed` | `A < S` (valor puro) | Éxito sin efectos |
| `ZIO.fail` | `Abort.fail[E]` | Falla con error tipado |
| `ZIO.attempt` | `Sync.defer` o `Abort.catching` | Suspender efectos |
| `ZIO.environment[R]` | `Env.get[R]` | Obtener dependencia |
| `ZLayer` | `Layer[Out, S]` | Capas de dependencias |
| `ZStream[R, E, A]` | `Stream[A, S]` | Streams efectos |
| `Scope.global` | `Scope.run` | Alcance de recursos |
| `ZIO.scoped` | `Scope.run` | Recursos con cleanup |
| `ZIO.acquireRelease` | `Scope.acquireRelease` | Adquisición/ Liberación |
| `ZIO.blocking` | `Sync.defer (blocking ops)` | Operaciones bloqueantes |
| `FiberRef` | `Local[T]` | Estado fiber-local |
| `Runtime.unsafeRun` | `KyoApp` | Ejecución de efectos |
| `ZIO.collectAll` | `Async.collectAll` | Colección paralela |
| `ZIO.foreach` | `Async.collectAll` | Iteración paralela |
| `ZIO.foreachDiscard` | `Async.collectAll.map(_.size)` | Iteración sin resultado |
| `ZIO.zip` | `Async.zip` | Composición paralela |
| `ZIO.race` | `Async.race` | Carrera de efectos |
| `ZIO.mapN` | `.map` chaining | Transformación |
| `ZIO.flatMap` | `for/yield` | Monádico |

### 1.3 Dependencias a Modificar en build.sbt

```scala
// ELIMINAR:
"dev.zio" %% "zio" % zioVersion
"dev.zio" %% "zio-streams" % zioVersion
"dev.zio" %% "zio-json" % "0.8.0"

// AGREGAR (versiones estables recientes de Kyo):
"io.getkyo" %% "kyo-core" % "0.30.0"
"io.getkyo" %% "kyo-prelude" % "0.30.0"
"io.getkyo" %% "kyo-data" % "0.30.0"
```

---

## 2. Estrategia de Migración por Módulo

### Orden de Migración (dependencias primero):

```
1. quill-sql (sin cambios ZIO, solo limpieza opcional)
2. quill-jdbc (sin cambios ZIO, solo limpieza opcional)
3. quill-zio ⭐ (PRINCIPAL - toda la base ZIO)
4. quill-jdbc-zio ⭐ (usa quill-zio)
5. quill-cassandra-zio ⭐ (usa quill-zio)
6. quill-cassandra (sin cambios ZIO)
7. quill-caliban (usa ZIO, requiere kyo-caliban)
8. quill-doobie (sin cambios ZIO)
```

### 2.1 Módulo `quill-zio` (PRIORIDAD ALTA)

**Archivos a modificar:**
- `ZioTranslateContext.scala` → Renombrar a `KyoTranslateContext`
- `ZioContext.scala` → Renombrar a `KyoContext`
- `ImplicitSyntax.scala` → Renombrar a `KyoImplicitSyntax`

**Cambios específicos:**
```scala
// ANTES (ZIO):
import zio.ZIO
import zio.stream.ZStream
import zio.{Tag, IO, ZIO}
import zio.ZEnvironment

// DESPUÉS (Kyo):
import kyo.*
```

### 2.2 Módulo `quill-jdbc-zio` (PRIORIDAD ALTA)

**Archivos principales:**
- `ZioJdbc.scala` → Renombrar a `KyoJdbc`
- `ZioJdbcContext.scala` → Renombrar a `KyoJdbcContext`
- `ZioJdbcUnderlyingContext.scala`
- `Quill.scala` → Renombrar a `KyoQuill`
- `ResultSetIterator.scala`

### 2.3 Módulo `quill-cassandra-zio` (PRIORIDAD MEDIA)

**Archivos principales:**
- `CassandraZioContext.scala` → Renombrar a `CassandraKyoContext`
- `cassandrazio/Quill.scala` → Renombrar
- `CassandraZioSession.scala`

### 2.4 Módulo `quill-caliban` (PRIORIDAD BAJA)

**Nota:** Requiere verificar compatibilidad con `kyo-caliban`

---

## 3. Estrategia de Pruebas

### 3.1 Opción Recomendada: munit

Kyo recomienda `munit` para testing:
```scala
// build.sbt
libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test
```

### 3.2 Alternativa: kyo-zio-test

Si hay muchas pruebas ZIO existentes, usar `kyo-zio-test`:
```scala
"io.getkyo" %% "kyo-zio-test" % kyoVersion % Test
"dev.zio" %% "zio-test-sbt" % zioVersion % Test
```

---

## 4. Riesgos Identificados y Mitigaciones

| Riesgo | Severidad | Mitigación |
|--------|-----------|------------|
| API de Kyo Stream diferente a ZStream | Alta | Estudiar `kyo.streams` extensivamente |
| `FiberRef` → `Local[T]` semántica diferente | Media | Revisar documentación de `Local` |
| ZLayer → Layer tiene diferencias | Alta | Seguir patrón de Layer en skill |
| `Scope` comportamiento diferente a ZIO | Alta | Usar `Scope.run` con `acquireRelease` |
| Pruebas existentes muy acopladas a ZIO | Media | Rewriter masivo o reescritura selectiva |
| `zio-json` → sin equivalente directo | Baja | Usar bilby o circe manualmente |

---

## 5. Plan de Ejecución Detallado

### Fase 1: Preparación (YA COMPLETADO)
- [x] Análisis de código fuente
- [x] Identificación de dependencias ZIO
- [x] Mapeo de APIs
- [x] Creación de este plan

### Fase 2: Migración Core
- [ ] Modificar `build.sbt` - eliminar ZIO, agregar Kyo
- [ ] Migrar `quill-zio` (3 archivos principales)
- [ ] Migrar `quill-jdbc-zio` (8+ archivos)
- [ ] Migrar `quill-cassandra-zio` (3 archivos)
- [ ] Migrar `quill-caliban` tests
- [ ] Compilar y verificar

### Fase 3: Pruebas
- [ ] Migrar/adapter tests de ZIO a munit
- [ ] Ejecutar suite completa
- [ ] Corrección de errores

### Fase 4: Commit y Reporte
- [ ] Commits por módulo
- [ ] Generar `MIGRATION_REPORT.md`

---

## 6. Referencias

- Skill Kyo: `/home/openclaw/.openclaw/workspace/skills/kyo-effect-system`
- Módulos clave: `core-effects.md`, `side-effects-async.md`, `dependency-injection.md`, `integrations.md`
- Repositorio: https://github.com/getkyo/kyo
