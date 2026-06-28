# Estado Actual del Proyecto — MiniMart POO Tycoon

> **Sprint completado:** 4 de 7 — Game Loop: Lógica en Memoria
> **Fecha:** 2026-06-28

---

## 1. Stack Tecnológico

| Componente | Versión | Rol |
|---|---|---|
| Java | 21 (LTS) | Lenguaje base, records, text blocks |
| JavaFX | 26.0.1 | UI toolkit (Controls + FXML) |
| SQLite JDBC | 3.45.1.0 | Driver xerial para BD embebida |
| Maven | 3.9+ (plugin compiler 3.11.0) | Build y dependencias |
| javafx-maven-plugin | 0.0.8 | Ejecución con `mvn javafx:run` |
| Plataforma JavaFX | `win` (classifier) | Artefactos nativos para Windows |

### Dependencias en `pom.xml`

```xml
<properties>
    <javafx.version>26.0.1</javafx.version>
    <javafx.platform>win</javafx.platform>
</properties>

<dependencies>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>${javafx.version}</version>
        <classifier>${javafx.platform}</classifier>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>${javafx.version}</version>
        <classifier>${javafx.platform}</classifier>
    </dependency>
    <dependency>
        <groupId>org.xerial</groupId>
        <artifactId>sqlite-jdbc</artifactId>
        <version>3.45.1.0</version>
    </dependency>
</dependencies>
```

Nota: `javafx-maven-plugin` configura `mainClass` como `com.minimart/com.minimart.App` (formato módulo/clase, obligatorio con `module-info.java`).

---

## 2. Estructura Completa de Directorios

```
ProyectoTycoon/
├── pom.xml
├── .gitignore
├── .opencode/
│   ├── skills/
│   │   ├── SKILL.md                     ← Sprint 0
│   │   └── SKILL_sprint1.md             ← Sprint 1
│   └── specs/
│       ├── SPEC.md                      ← Sprint 0
│       └── SPEC_sprint1.md              ← Sprint 1
├── src/
│   └── main/
│       ├── java/com/minimart/
│       │   ├── module-info.java
│       │   ├── App.java
│       │   ├── controller/
│       │   │   ├── MainController.java
│       │   │   └── GameLoopService.java        ← Sprint 4
│       │   ├── model/
│       │   │   ├── Tienda.java
│       │   │   ├── Estanteria.java
│       │   │   ├── Cajero.java
│       │   │   └── Cliente.java
│       │   ├── dao/
│       │   │   ├── ConexionBD.java
│       │   │   ├── DAO.java
│       │   │   ├── TiendaDAO.java
│       │   │   ├── EstanteriaDAO.java
│       │   │   └── CajeroDAO.java
│       │   └── view/
│       │       ├── PanelesView.java
│       │       └── package-info.java
│       └── resources/com/minimart/
│           ├── MainWindow.fxml
│           └── styles.css
└── target/                              ← Build output (ignorado por git)
```

---

## 3. `module-info.java` — Sistema de Módulos JPMS

```java
module com.minimart {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.minimart to javafx.fxml;

    exports com.minimart;
    exports com.minimart.dao;
    exports com.minimart.model;

    exports com.minimart.controller;
    exports com.minimart.view;
    opens   com.minimart.controller to javafx.fxml;
    opens   com.minimart.view       to javafx.fxml;
}
```

Las 4 líneas de `controller` y `view` se agregaron al crear los primeros archivos Java en esos paquetes (Sprint 2).

---

## 4. `App.java` — Punto de Entrada

### Ciclo de vida

| Método | Hilo | Responsabilidad |
|---|---|---|
| `init()` | Inicialización (no UI) | Inicializar BD |
| `start()` | JavaFX Application | Cargar FXML, mostrar escena con CSS |
| `stop()` | JavaFX Application | Cerrar conexión BD |

### Contenido actual (45 líneas)

```java
package com.minimart;

import com.minimart.dao.ConexionBD;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void init() throws Exception {
        ConexionBD.getInstance().initDB();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/minimart/MainWindow.fxml")
        );
        Parent raiz = loader.load();
        Scene escena = new Scene(raiz, 1024, 768);
        escena.getStylesheets().add(
            getClass().getResource("/com/minimart/styles.css").toExternalForm()
        );
        stage.setTitle("MiniMart POO Tycoon");
        stage.setScene(escena);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        ConexionBD.getInstance().cerrar();
    }

    public static void main(String[] args) { launch(args); }
}
```

- El bloque temporal `verificarCapaDatos()` fue eliminado (como estaba previsto).
- Ahora carga `MainWindow.fxml` mediante `FXMLLoader` y aplica `styles.css`.

---

## 5. `MainController.java` — Controlador Principal (Sprints 2 + 3 + 4)

~170 líneas con inyección FXML + lógica de carga, bindings y game loop:

**Estanterías (5 slots):**
- `slotEstanteria1-5` (VBox), `imgEstanteria1-5` (ImageView)
- `stockBar1-5` (ProgressBar), `labelTipo1-5` (Label)

**Cajeros (3 slots):**
- `slotCajero1-3` (VBox), `imgCajero1-3` (ImageView)
- `atenderBar1-3` (ProgressBar)

**Compradores (4 slots):**
- `comprador1-4` (ImageView)

**Upgrades:**
- `btnUpgrade1` (Comprar Estantería), `btnUpgrade2` (Reabastecer), `btnUpgrade3` (Mejorar Cajero)

**Estadísticas:**
- `labelDinero`, `labelReputacion`, `labelDia`, `btnAvanzarDia`

**Lógica Sprint 3:**
- `initialize()` llama `cargarPartida()` dentro de `try/catch` con manejo de error vía `Alert`
- `cargarPartida()` instancia `TiendaDAO`, obtiene `tiendaActual = dao.cargarPartidaCompleta(1)`
- Arreglos indexados (`slotsEstanteria[]`, `labelsTipo[]`, `barrasStock[]`, `slotsCajero[]`) para mapeo por `posicionVisual - 1`
- Binding reactivo: `labelDinero` ↔ `dineroActualProperty()`, `labelDia` ↔ `diaActualProperty()`
- Binding reactivo: `stockBar` ↔ `stockActualProperty().divide(stockMaximo)` por cada estantería
- Opacidad de slots según datos reales (`isActivo()` en cajeros, existencia en estanterías)
**Lógica Sprint 4:**
- `initialize()` ahora también llama `iniciarGameLoop()` después de `cargarPartida()`
- `iniciarGameLoop()` crea un `GameLoopService(tiendaActual, this)` y arranca el Timeline
- `actualizarVistas()` público refresca las barras de atención de cajeros en cada tick del game loop
- `getTiendaActual()` expone el modelo para Sprint 5+

---

## 6. `MainWindow.fxml` — Layout Principal (Sprint 2)

220 líneas con estructura `BorderPane`:

| Zona | Componente | Contenido |
|---|---|---|
| **Center** | VBox | Franja estanterías (HBox, 5 slots) + Franja cajeros/compradores (HBox, 3 cajeros + 4 compradores) |
| **Right** | VBox (200px fijo) | 3 tarjetas de upgrade (Comprar, Reabastecer, Mejorar) + Estadísticas (Reputación, Dinero, Día) + Botón Avanzar Día |

- Slots usan imágenes placeholder de placehold.co.
- Slots de estanterías 2-5 y cajeros 2-3 empiezan con opacidad reducida (0.3) indicando que están bloqueados.

---

## 7. `styles.css` — Hoja de Estilos (Sprint 2)

164 líneas con clases para todos los componentes visuales:

| Clase | Propósito |
|---|---|
| `.ventana-principal` | Fondo #2B2B2B |
| `.franja-estanterias` | Fondo #90EE90 (verde claro) |
| `.franja-cajeros` | Fondo #FFB347 (naranja) |
| `.slot-estanteria` | Fondo translúcido, hover más claro |
| `.slot-cajero` | Fondo translúcido |
| `.barra-stock` | Barra verde (#1D9E75) |
| `.barra-atencion` | Barra ámbar (#E2A01A) |
| `.panel-derecho` | Fondo #808080 (gris) |
| `.titulo-panel` | Texto #FFD700 (dorado) |
| `.tarjeta-upgrade` | Borde dorado, fondo oscuro |
| `.seccion-stats` | Fondo oscuro translúcido |

---

## 8. `ConexionBD.java` — Singleton de Base de Datos (sin cambios desde Sprint 1)

### Patrón: Double-checked locking thread-safe

- **URL:** `jdbc:sqlite:` + `{user.home}/minimart.db`
- **PRAGMA:** `foreign_keys = ON`
- **Reconexión automática:** `getConnection()` verifica `isClosed()` y reabre si es necesario

### Tablas

#### `tienda`

| Columna | Tipo | Default | Restricciones |
|---|---|---|---|
| id | INTEGER | — | PK AUTOINCREMENT |
| nombre_tienda | TEXT | 'Mi MiniMart' | NOT NULL |
| dinero_actual | REAL | 500.0 | NOT NULL |
| dia_actual | INTEGER | 1 | NOT NULL |

#### `estanterias`

| Columna | Tipo | Default | Restricciones |
|---|---|---|---|
| id | INTEGER | — | PK AUTOINCREMENT |
| tienda_id | INTEGER | — | NOT NULL, FK → tienda(id) ON DELETE CASCADE |
| tipo_producto | TEXT | — | NOT NULL |
| stock_actual | INTEGER | 10 | NOT NULL |
| stock_maximo | INTEGER | 10 | NOT NULL |
| posicion_visual | INTEGER | — | NOT NULL, CHECK(1-5), UNIQUE(tienda_id, posicion_visual) |

#### `cajeros`

| Columna | Tipo | Default | Restricciones |
|---|---|---|---|
| id | INTEGER | — | PK AUTOINCREMENT |
| tienda_id | INTEGER | — | NOT NULL, FK → tienda(id) ON DELETE CASCADE |
| nivel_mejora | INTEGER | 1 | NOT NULL |
| tiempo_despacho | INTEGER | 5 | NOT NULL |
| activo | INTEGER | 0 | NOT NULL, CHECK(0,1) |

### Datos Semilla

```sql
INSERT OR IGNORE INTO tienda (id, nombre_tienda, dinero_actual, dia_actual)
VALUES (1, 'Mi MiniMart', 500.0, 1);

INSERT INTO cajeros (tienda_id, nivel_mejora, tiempo_despacho, activo)
SELECT 1, 1, 5, 1 WHERE NOT EXISTS (SELECT 1 FROM cajeros WHERE tienda_id = 1);

INSERT INTO estanterias (tienda_id, tipo_producto, stock_actual, stock_maximo, posicion_visual)
SELECT 1, 'Snacks', 10, 10, 1 WHERE NOT EXISTS (SELECT 1 FROM estanterias WHERE tienda_id = 1 AND posicion_visual = 1);
```

**Idempotencia garantizada.**

---

## 9. Modelos (4 clases en `com.minimart.model`)

### 9.1 `Tienda.java`

| Campo | Tipo Java | Property? |
|---|---|---|
| id | `int` | No |
| nombreTienda | `SimpleStringProperty` | Sí → `nombreTiendaProperty()` |
| dineroActual | `SimpleDoubleProperty` | Sí → `dineroActualProperty()` |
| diaActual | `SimpleIntegerProperty` | Sí → `diaActualProperty()` |
| estanterias | `List<Estanteria>` | No |
| cajeros | `List<Cajero>` | No |

### 9.2 `Estanteria.java`

| Campo | Tipo Java | Property? |
|---|---|---|
| id | `int` | No |
| tiendaId | `int` | No |
| tipoProducto | `String` | No |
| stockActual | `SimpleIntegerProperty` | Sí → `stockActualProperty()` |
| stockMaximo | `int` | No |
| posicionVisual | `int` (1-5) | No |

**Helpers:** `tieneStock()`, `getPorcentajeStock()`.

### 9.3 `Cajero.java`

| Campo | Tipo Java | Persiste? |
|---|---|---|
| id | `int` | Sí |
| tiendaId | `int` | Sí |
| nivelMejora | `int` | Sí |
| tiempoDespacho | `int` | Sí |
| activo | `boolean` | Sí |
| colaClientes | `Queue<Cliente>` (LinkedList) | No (efímero) |
| segundosRestantes | `int` | No (efímero) |

**Helpers:** `getTamañoCola()`, `estaOcupado()`.

### 9.4 `Cliente.java`

Cliente de sesión (no persiste en BD).

| Campo | Tipo Java |
|---|---|
| CONTADOR_SESION | `static final AtomicInteger` |
| id | `final int` |
| productoElegido | `String` |
| montoGastado | `double` |

---

## 10. DAOs (3 implementaciones en `com.minimart.dao`)

### `DAO<T>` — Interfaz Genérica

```java
public interface DAO<T> {
    List<T> findAll();
    Optional<T> findById(int id);
    void save(T entity);
    void update(T entity);
    void delete(int id);
}
```

### `EstanteriaDAO`

CRUD + `findByTiendaId()` + `updateStock()`.

### `CajeroDAO`

CRUD + `findByTiendaId()` + `activar()` + `mejorar()`.

### `TiendaDAO`

CRUD + `cargarPartidaCompleta(int tiendaId)` — punto de entrada del juego.

---

## 11. Paquetes

| Paquete | Archivos | Estado |
|---|---|---|
| `controller/` | `MainController.java`, `GameLoopService.java` | Sprint 4 completado (game loop con Timeline de 1s) |
| `view/` | `PanelesView.java`, `package-info.java` | Sprint 2 (esqueleto) |
| `resources/` | `MainWindow.fxml`, `styles.css` | Sprint 2 completado |

---

## 12. Comandos de Build y Ejecución

| Comando | Propósito |
|---|---|
| `mvn clean compile` | Compilar (verificar errores) |
| `mvn javafx:run` | Ejecutar la aplicación con ventana |
| `mvn package` | Generar JAR |

---

## 13. Criterios de Aceptación — Sprint 2

| CA | Descripción | Estado |
|---|---|---|
| CA-01 | `mvn clean compile` → BUILD SUCCESS | ✅ |
| CA-02 | Ventana con layout BorderPane (center + right) | ✅ |
| CA-03 | 5 slots de estantería con ProgressBar + Label | ✅ |
| CA-04 | 3 slots de cajero con ProgressBar de atención | ✅ |
| CA-05 | 4 slots de compradores (opacos, sin lógica) | ✅ |
| CA-06 | Panel derecho con 3 tarjetas de upgrade | ✅ |
| CA-07 | Sección de estadísticas (dinero, reputación, día) | ✅ |
| CA-08 | Botón "Avanzar Día" | ✅ |
| CA-09 | `module-info.java` exporta controller y view | ✅ |
| CA-10 | Bloque temporal de verificación eliminado de App.java | ✅ |
| CA-11 | CSS con estilos para todos los componentes | ✅ |
| CA-12 | Controller con inyección FXML de todos los elementos | ✅ |

---

## 14. Criterios de Aceptación — Sprint 3

| CA | Descripción | Estado |
|---|---|---|
| CA-01 | `mvn clean compile` → BUILD SUCCESS | ✅ |
| CA-02 | Ventana carga con datos reales desde BD (labelTipo1 muestra "Snacks") | ✅ |
| CA-03 | Output en consola: "Partida cargada y bindings establecidos." sin errores | ✅ |
| CA-04 | Bindings reactivos: `labelDinero` ↔ `dineroActualProperty()` | ✅ |
| CA-05 | `labelTipo1` muestra `"Snacks"` desde la BD | ✅ |
| CA-06 | `stockBar1-5` bindeados via `progressProperty().bind()` (sin setProgress directo) | ✅ |
| CA-07 | `labelDinero` y `labelDia` bindeados sin `setText()` directo | ✅ |
| CA-08 | Manejo de error de BD con `Alert(ERROR)` si falla `cargarPartidaCompleta()` | ✅ |
| CA-09 | `getTiendaActual()` expone el estado del modelo para Sprint 4+ | ✅ |
| CA-10 | Solo `MainController.java` modificado (sin cambios en FXML, CSS ni DAOs) | ✅ |

---

## 15. Criterios de Aceptación — Sprint 4

| CA | Descripción | Estado |
|---|---|---|
| CA-01 | `mvn clean compile` → BUILD SUCCESS | ✅ |
| CA-02 | Game loop arranca sin errores en consola ("Game loop iniciado — ticks cada 1s.") | ✅ |
| CA-03 | Spawn de clientes visible: `atenderBar1` se mueve (cuenta regresiva 5s) | ✅ |
| CA-04 | Stock decrementa al spawnear (stockBar1 se acorta al llegar clientes) | ✅ |
| CA-05 | Dinero aumenta al despachar (labelDinero se actualiza por binding reactivo) | ✅ |
| CA-06 | `actualizarVistas()` público implementado en MainController | ✅ |
| CA-07 | `GameLoopService.java` creado en `com.minimart.controller` | ✅ |
| CA-08 | `GameLoopService` tiene métodos `iniciar()`, `pausar()`, `reanudar()` | ✅ |
| CA-09 | Timeline usa `Duration.seconds(1)` y `cycleCount = INDEFINITE` | ✅ |
| CA-10 | Solo `GameLoopService.java` (nuevo) y `MainController.java` (modificado) | ✅ |

---

## 16. Arquitectura del Game Loop (Sprint 4)

### `GameLoopService.java`

| Componente | Descripción |
|---|---|
| `iniciar()` | Crea Timeline con KeyFrame `Duration.seconds(1)` y `cycleCount=INDEFINITE`, llama `play()` |
| `pausar()` | Pausa el Timeline sin reiniciarlo |
| `reanudar()` | Reanuda el Timeline desde donde se pausó |
| `tick()` | Orchestrador: `spawnCliente()` → `procesarDespacho()` → `controller.actualizarVistas()` |
| `spawnCliente()` | 30% probabilidad, filtra estanterías con stock, elige al azar, decrementa stock, asigna a cajero |
| `asignarCliente()` | Busca cajero activo con menor cola (`Collections.min`), agrega cliente, resetea cuenta regresiva |
| `procesarDespacho()` | Decrementa `segundosRestantes`, al llegar a 0 suma dinero y pasa al siguiente cliente |

### Flujo por tick (cada 1s)

```
1. ¿Llega cliente? → 30% sí: elige producto, crea Cliente, resta stock, asigna al cajero menos ocupado
2. Cada cajero activo con cola: decrementa segundosRestantes
3. ¿segundosRestantes <= 0? → cobra cliente, lo saca de cola, inicia siguiente si hay
4. actualizarVistas(): refresca atenderBar1..3 con progreso
```

---

## 17. Preparación para Sprint 5

### Lo que Sprint 5 podrá hacer

- Presionar `btnUpgrade1` (Comprar Estantería): nueva estantería aparece con binding de stock ya activo
- Presionar `btnUpgrade2` (Reabastecer): stock se restaura a `stockMaximo`, ProgressBar se actualiza sola
- Presionar `btnUpgrade3` (Mejorar Cajero): `tiempoDespacho` se reduce, el game loop se acelera
- Presionar "Avanzar Día" (Sprint 6) para persistir el estado actual del game loop

### Lo que NO incluye Sprint 4

| Funcionalidad | Sprint |
|---|---|
| Handlers de botones upgrade/contratar | Sprint 5 |
| Persistencia (JuegoDAO, transacciones) | Sprint 6 |
| Pantalla de resumen de jornada | Sprint 6 |
| Animación de dinero, stock crítico, reputación, Game Over | Sprint 7 |
| Tests unitarios | Fuera de scope |

---

## 18. Resumen de Archivos (15 archivos .java + 2 recursos)

```
src/main/java/com/minimart/
├── App.java                   45 líneas  — Punto de entrada (FXMLLoader)
├── module-info.java           16 líneas  — Declaración de módulo JPMS
├── controller/
│   ├── MainController.java   170 líneas  — Controlador principal (inyección FXML + bindings + game loop)
│   └── GameLoopService.java  145 líneas  — Game loop con Timeline de 1s (spawn, despacho, UI)
├── model/
│   ├── Tienda.java            49 líneas  — Estado global de partida (3 Properties)
│   ├── Estanteria.java        46 líneas  — Estantería con stock (1 Property)
│   ├── Cajero.java            42 líneas  — Cajero con cola en memoria
│   └── Cliente.java           28 líneas  — Cliente de sesión (no persistente)
├── dao/
│   ├── ConexionBD.java       136 líneas  — Singleton BD + initDB + esquema
│   ├── DAO.java                9 líneas  — Interfaz genérica CRUD
│   ├── TiendaDAO.java          96 líneas — CRUD + cargarPartidaCompleta()
│   ├── EstanteriaDAO.java     107 líneas — CRUD + findByTiendaId() + updateStock()
│   └── CajeroDAO.java         106 líneas — CRUD + findByTiendaId() + activar() + mejorar()
└── view/
    ├── PanelesView.java         5 líneas  — Clase utilitaria
    └── package-info.java        5 líneas  — Javadoc del paquete

src/main/resources/com/minimart/
├── MainWindow.fxml           220 líneas  — Layout BorderPane principal
└── styles.css                164 líneas  — Hoja de estilos completa
```

**Total:** ~954 líneas de código Java + 384 líneas de recursos.
