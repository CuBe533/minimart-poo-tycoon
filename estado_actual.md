# Estado Actual del Proyecto — MiniMart POO Tycoon

> **Sprint completado:** 1 de 7 — Capa de Datos (Modelos + DAOs)
> **Fecha:** 2026-06-11

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
├── AGENTS.md
├── estado_actual.md                     ← ESTE ARCHIVO
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
│       │   ├── controller/              ← VACÍO (Sprint 2)
│       │   └── view/                    ← VACÍO (Sprint 2)
│       └── resources/com/minimart/
│           └── .gitkeep
└── target/                              ← Build output (ignorado por git)
```

---

## 3. `module-info.java` — Sistema de Módulos JPMS

### Contenido actual

```java
module com.minimart {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.minimart to javafx.fxml;

    exports com.minimart;
    exports com.minimart.dao;
    exports com.minimart.model;
}
```

### Líneas faltantes (se agregan en Sprint 2)

```java
exports com.minimart.controller;
exports com.minimart.view;
opens   com.minimart.controller to javafx.fxml;
opens   com.minimart.view       to javafx.fxml;
```

### Explicación de la omisión

La SPEC_sprint1.md (sección 3) indica agregar estas 4 líneas para `controller` y `view`. Sin embargo, el compilador JPMS de Java 21 **rechaza** exportar o abrir paquetes vacíos con el error:

```
package is empty or does not exist: com.minimart.controller
package is empty or does not exist: com.minimart.view
```

La afirmación de la SPEC de que *"exportar un paquete vacío es válido en JPMS"* **no es correcta** en la práctica con `javac 21`. Los paquetes `controller/` y `view/` contienen solo un `.gitkeep`, no archivos `.java`, por lo que el compilador no reconoce ningún símbolo en esos paquetes.

**Acción requerida en Sprint 2:** Tan pronto como se cree el primer archivo Java dentro de `controller/` (ej. `MainController.java`) y `view/` (ej. `MainView.java` o se copien recursos FXML), se deben **agregar las 4 líneas faltantes**. La ausencia actual de estas líneas no afecta el funcionamiento del Sprint 1 porque ninguna clase referencia esos paquetes.

---

## 4. `App.java` — Punto de Entrada

### Ciclo de vida

| Método | Hilo | Responsabilidad |
|---|---|---|
| `init()` | Inicialización (no UI) | Inicializar BD + verificación temporal Sprint 1 |
| `start()` | JavaFX Application | Mostrar ventana placeholder |
| `stop()` | JavaFX Application | Cerrar conexión BD |

### Contenido actual (84 líneas)

```java
package com.minimart;

import com.minimart.dao.ConexionBD;
import com.minimart.dao.TiendaDAO;
import com.minimart.model.Tienda;
import javafx.application.Application;
// ...

public class App extends Application {

    @Override
    public void init() throws Exception {
        ConexionBD.getInstance().initDB();
        verificarCapaDatos(); // TODO: eliminar antes de Sprint 2
    }

    @Override
    public void start(Stage stage) {
        // Placeholder visual — se reemplaza en Sprint 2
        Label titulo = new Label("MiniMart POO Tycoon");
        Label subtitulo = new Label("Sprint 0 completado — Base de datos inicializada ✓");
        Label ruta = new Label("BD: " + Paths.get(user.home, "minimart.db"));
        VBox contenido = new VBox(12, titulo, subtitulo, ruta);
        Scene scene = new Scene(new StackPane(contenido), 1024, 768);
        stage.setTitle("MiniMart POO Tycoon");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        ConexionBD.getInstance().cerrar();
    }

    // ── BLOQUE TEMPORAL SPRINT 1 (ELIMINAR EN SPRINT 2) ──────
    private void verificarCapaDatos() {
        System.out.println("══ VERIFICACIÓN SPRINT 1 ══════════════════════════════");
        try {
            TiendaDAO dao = new TiendaDAO();
            Tienda tienda = dao.cargarPartidaCompleta(1);
            System.out.println("Tienda:       " + tienda);
            System.out.printf("Estanterías:  %d encontrada(s)%n", tienda.getEstanterias().size());
            tienda.getEstanterias().forEach(e -> System.out.println("  " + e));
            System.out.printf("Cajeros:      %d encontrado(s)%n", tienda.getCajeros().size());
            tienda.getCajeros().forEach(c -> System.out.println("  " + c));
            // Verificar Properties
            double original = tienda.getDineroActual();
            tienda.setDineroActual(999.99);
            assert tienda.dineroActualProperty().get() == 999.99;
            tienda.setDineroActual(original);
            tienda.getEstanterias().get(0).setStockActual(3);
            assert tienda.getEstanterias().get(0).stockActualProperty().get() == 3;
            System.out.println("✓ cargarPartidaCompleta() OK");
            System.out.println("✓ JavaFX Properties funcionales");
        } catch (Exception ex) {
            System.err.println("✗ ERROR: " + ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println("═══════════════════════════════════════════════════════");
    }
    // ── FIN BLOQUE TEMPORAL ──────────────────────────────────

    public static void main(String[] args) { launch(args); }
}
```

### Nota sobre el typo corregido

La SPEC_sprint1.md contenía un error tipográfico intencional en la línea:
```java
Tienda.getEstanterias().get(0).setStockActual(3);  // INCORRECTO
```
Se corrigió a:
```java
tienda.getEstanterias().get(0).setStockActual(3);   // CORRECTO
```

---

## 5. `ConexionBD.java` — Singleton de Base de Datos (Sprint 0, sin cambios)

### Patrón: Double-checked locking thread-safe

```java
public class ConexionBD {
    private static volatile ConexionBD instancia;

    private ConexionBD() { abrirConexion(); }

    public static ConexionBD getInstance() {
        if (instancia == null) {
            synchronized (ConexionBD.class) {
                if (instancia == null) {
                    instancia = new ConexionBD();
                }
            }
        }
        return instancia;
    }
}
```

### Conexión

- **URL:** `jdbc:sqlite:` + `{user.home}/minimart.db`
- **PRAGMA:** `foreign_keys = ON` (activado al conectar)
- **Reconexión automática:** `getConnection()` verifica `isClosed()` y reabre si es necesario

### Esquema de Base de Datos (creado por `initDB()`)

#### Tabla: `tienda`

| Columna | Tipo | Default | Restricciones |
|---|---|---|---|
| id | INTEGER | — | PK AUTOINCREMENT |
| nombre_tienda | TEXT | 'Mi MiniMart' | NOT NULL |
| dinero_actual | REAL | 500.0 | NOT NULL |
| dia_actual | INTEGER | 1 | NOT NULL |

#### Tabla: `estanterias`

| Columna | Tipo | Default | Restricciones |
|---|---|---|---|
| id | INTEGER | — | PK AUTOINCREMENT |
| tienda_id | INTEGER | — | NOT NULL, FK → tienda(id) ON DELETE CASCADE |
| tipo_producto | TEXT | — | NOT NULL |
| stock_actual | INTEGER | 10 | NOT NULL |
| stock_maximo | INTEGER | 10 | NOT NULL |
| posicion_visual | INTEGER | — | NOT NULL, CHECK(1-5), UNIQUE(tienda_id, posicion_visual) |

#### Tabla: `cajeros`

| Columna | Tipo | Default | Restricciones |
|---|---|---|---|
| id | INTEGER | — | PK AUTOINCREMENT |
| tienda_id | INTEGER | — | NOT NULL, FK → tienda(id) ON DELETE CASCADE |
| nivel_mejora | INTEGER | 1 | NOT NULL |
| tiempo_despacho | INTEGER | 5 | NOT NULL |
| activo | INTEGER | 0 | NOT NULL, CHECK(0,1) |

### Datos Semilla (insertados con `INSERT OR IGNORE` / `WHERE NOT EXISTS`)

```sql
-- Tienda inicial
INSERT OR IGNORE INTO tienda (id, nombre_tienda, dinero_actual, dia_actual)
VALUES (1, 'Mi MiniMart', 500.0, 1);

-- Primer cajero activo
INSERT INTO cajeros (tienda_id, nivel_mejora, tiempo_despacho, activo)
SELECT 1, 1, 5, 1 WHERE NOT EXISTS (SELECT 1 FROM cajeros WHERE tienda_id = 1);

-- Primera estantería
INSERT INTO estanterias (tienda_id, tipo_producto, stock_actual, stock_maximo, posicion_visual)
SELECT 1, 'Snacks', 10, 10, 1 WHERE NOT EXISTS (SELECT 1 FROM estanterias WHERE tienda_id = 1 AND posicion_visual = 1);
```

**Idempotencia garantizada:** Ejecutar `initDB()` múltiples veces no duplica datos.

---

## 6. Modelos (4 clases en `com.minimart.model`)

### 6.1 `Tienda.java`

Representa el estado global de la partida del jugador.

| Campo | Tipo Java | Tipo BD | Property? |
|---|---|---|---|
| id | `int` | INTEGER PK | No |
| nombreTienda | `SimpleStringProperty` | TEXT | Sí → `nombreTiendaProperty()` |
| dineroActual | `SimpleDoubleProperty` | REAL | Sí → `dineroActualProperty()` |
| diaActual | `SimpleIntegerProperty` | INTEGER | Sí → `diaActualProperty()` |
| estanterias | `List<Estanteria>` | — (memoria) | No |
| cajeros | `List<Cajero>` | — (memoria) | No |

**Constructores:**
- `Tienda()` — vacío (para DAO)
- `Tienda(int id, String nombre, double dinero, int dia)` — completo

**Properties expuestas:**
```java
public SimpleStringProperty  nombreTiendaProperty();
public SimpleDoubleProperty  dineroActualProperty();
public SimpleIntegerProperty diaActualProperty();
```

**Uso futuro (Sprint 3):** Binding reactivo con Labels y ProgressBars.

### 6.2 `Estanteria.java`

Estantería con stock de un tipo de producto.

| Campo | Tipo Java | Tipo BD | Property? |
|---|---|---|---|
| id | `int` | INTEGER PK | No |
| tiendaId | `int` | INTEGER FK | No |
| tipoProducto | `String` | TEXT | No |
| stockActual | `SimpleIntegerProperty` | INTEGER | Sí → `stockActualProperty()` |
| stockMaximo | `int` | INTEGER | No |
| posicionVisual | `int` | INTEGER (1-5) | No |

**Property:**
```java
public SimpleIntegerProperty stockActualProperty();
```

**Helpers de negocio:**
```java
public boolean tieneStock();              // stockActual > 0
public double  getPorcentajeStock();      // rango [0.0, 1.0], guard si stockMaximo == 0
```

**Uso futuro (Sprint 3):**
```java
progressBar.progressProperty().bind(
    estanteria.stockActualProperty().divide(estanteria.getStockMaximo())
);
```

### 6.3 `Cajero.java`

Cajero de la tienda con cola de clientes en memoria.

| Campo | Tipo Java | Tipo BD | Persiste? |
|---|---|---|---|
| id | `int` | INTEGER PK | Sí |
| tiendaId | `int` | INTEGER FK | Sí |
| nivelMejora | `int` | INTEGER | Sí |
| tiempoDespacho | `int` | INTEGER | Sí |
| activo | `boolean` | INTEGER (0/1) | Sí |
| colaClientes | `Queue<Cliente>` (LinkedList) | — | **No** (efímero) |
| segundosRestantes | `int` | — | **No** (efímero) |

**Helpers:**
```java
public int     getTamañoCola();    // colaClientes.size()
public boolean estaOcupado();      // !colaClientes.isEmpty()
```

Los campos efímeros se reinician en el constructor completo con `new LinkedList<>()` y `segundosRestantes = 0`.

### 6.4 `Cliente.java`

Cliente que visita la tienda. **No persiste en BD** — existe solo en memoria durante la sesión.

| Campo | Tipo Java | Descripción |
|---|---|---|
| CONTADOR_SESION | `static final AtomicInteger` | Contador global compartido |
| id | `final int` | Asignado en constructor, **sin setter** |
| productoElegido | `String` | Tipo de producto que compra |
| montoGastado | `double` | Precio calculado al hacer spawn |

**Constructores:**
- `Cliente()` — asigna id automático
- `Cliente(String producto, double monto)` — completo

El contador se resetea al reiniciar la aplicación (comportamiento deseado para el juego).

---

## 7. Interfaz DAO y DAOs (3 implementaciones en `com.minimart.dao`)

### 7.1 `DAO<T>` — Interfaz Genérica

```java
public interface DAO<T> {
    List<T> findAll();
    Optional<T> findById(int id);
    void save(T entity);           // post: entity.id = generated key
    void update(T entity);         // pre: entity.getId() > 0
    void delete(int id);           // ON DELETE CASCADE
}
```

### 7.2 `EstanteriaDAO implements DAO<Estanteria>`

**Columnas:** `id, tienda_id, tipo_producto, stock_actual, stock_maximo, posicion_visual`

**Métodos CRUD:** findAll, findById, save, update, delete.

**Métodos específicos:**
```java
public List<Estanteria> findByTiendaId(int tiendaId);
    // SELECT ... FROM estanterias WHERE tienda_id = ? ORDER BY posicion_visual
    // Usado por TiendaDAO.cargarPartidaCompleta()

public void updateStock(int id, int nuevoStock);
    // UPDATE estanterias SET stock_actual = ? WHERE id = ?
    // Optimización: solo actualiza stock, no toda la fila
    // Usado en Sprint 5 (reabastecer estantería)
```

### 7.3 `CajeroDAO implements DAO<Cajero>`

**Columnas:** `id, tienda_id, nivel_mejora, tiempo_despacho, activo`

**Métodos CRUD:** findAll, findById, save, update, delete.

**Métodos específicos:**
```java
public List<Cajero> findByTiendaId(int tiendaId);
    // Usado por TiendaDAO.cargarPartidaCompleta()

public void activar(int id);
    // UPDATE cajeros SET activo = 1 WHERE id = ?
    // Contratar cajero inactivo (Sprint 5)

public void mejorar(int id);
    // UPDATE cajeros SET nivel_mejora + 1, tiempo_despacho = MAX(1, tiempo_despacho - 2)
    // Upgrade con mínimo de 1s en SQL (Sprint 5)
```

**Mapeo boolean:** `rs.getInt("activo") == 1` (SQLite no tiene boolean nativo).

### 7.4 `TiendaDAO implements DAO<Tienda>`

**Columnas:** `id, nombre_tienda, dinero_actual, dia_actual`

**Dependencias internas:** Constructor instancia `EstanteriaDAO` y `CajeroDAO`.

**Métodos CRUD:** findAll, findById, save, update, delete.

**Método principal del dominio:**
```java
public Tienda cargarPartidaCompleta(int tiendaId);
    // 1. findById(tiendaId) → orElseThrow con mensaje descriptivo
    // 2. estanteriaDAO.findByTiendaId(tiendaId) → tienda.setEstanterias(...)
    // 3. cajeroDAO.findByTiendaId(tiendaId) → tienda.setCajeros(...)
    // 4. return tienda (con listas populadas)
    //
    // Punto de entrada del juego al arrancar (Sprint 3+):
    //   Tienda tienda = new TiendaDAO().cargarPartidaCompleta(1);
```

### Patrón común en los 3 DAOs

- **Constructor:** `this.conexion = ConexionBD.getInstance().getConnection();`
- **save():** usa `PreparedStatement` con `Statement.RETURN_GENERATED_KEYS`, recupera el ID autogenerado con `ps.getGeneratedKeys()`
- **Columnas:** referenciadas por nombre (`rs.getInt("id")`), no por índice
- **Errores:** SQLException envuelta en RuntimeException con prefijo `[Clase.metodo]`
- **Text blocks:** SQL multi-línea con `"""..."""`

---

## 8. Paquetes Vacíos (preparados para Sprint 2)

| Paquete | Ruta | Contenido actual | Sprint de llenado |
|---|---|---|---|
| `controller/` | `src/main/java/com/minimart/controller/` | `.gitkeep` | Sprint 2 |
| `view/` | `src/main/java/com/minimart/view/` | `.gitkeep` | Sprint 2 |

Ambos paquetes existen físicamente pero no tienen archivos `.java`. El `.gitkeep` es necesario para que Git preserve los directorios vacíos.

---

## 9. Base de Datos en Tiempo de Ejecución

- **Ubicación:** `C:\Users\{usuario}\minimart.db` (Windows) / `~/minimart.db` (Linux/Mac)
- **Tamaño típico:** ~8 KB con datos semilla
- **Engine:** SQLite 3 (embebido, sin servidor)
- **FK habilitadas:** `PRAGMA foreign_keys = ON` al conectar

### Verificación con SQLite CLI

```sql
-- Listar tablas
SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;
-- Resultado: cajeros | estanterias | tienda

-- Ver datos semilla
SELECT * FROM tienda;
-- 1|Mi MiniMart|500.0|1

SELECT * FROM cajeros;
-- 1|1|1|5|1

SELECT * FROM estanterias;
-- 1|1|Snacks|10|10|1
```

---

## 10. Comandos de Build y Ejecución

| Comando | Propósito |
|---|---|
| `mvn clean compile` | Compilar (verificar errores) |
| `mvn javafx:run` | Ejecutar la aplicación con ventana |
| `mvn -DskipTests package` | Generar JAR (sin tests) |

---

## 11. Criterios de Aceptación — Sprint 1 (Verificados)

| CA | Descripción | Resultado | Evidencia |
|---|---|---|---|
| CA-01 | `mvn clean compile` → BUILD SUCCESS | ✅ | Compilación sin errores ni warnings |
| CA-02 | Ventana sin cambios visuales respecto a Sprint 0 | ✅ | Mismo placeholder de 1024×768 |
| CA-03 | Output de verificación en consola | ✅ | Tienda, 1 estantería, 1 cajero, Properties OK |
| CA-04 | Estructura de paquetes correcta | ✅ | 11 archivos .java en las ubicaciones correctas |
| CA-05 | `implements DAO<Tipo>` en 3 DAOs | ✅ | CajeroDAO, EstanteriaDAO, TiendaDAO |
| CA-06 | Properties declaradas (Tienda: 3, Estanteria: 1) | ✅ | SimpleStringProperty, SimpleDoubleProperty, SimpleIntegerProperty |
| CA-07 | module-info exporta paquetes nuevos | ✅ | model exportado (controller/view pendientes) |
| CA-08 | `// TODO: eliminar antes de Sprint 2` presente | ✅ | Línea 19 de App.java |

---

## 12. Preparación para Sprint 2

### Tareas de migración al iniciar Sprint 2

1. **Eliminar bloque temporal en `App.java`:**
   - Remover la línea `verificarCapaDatos(); // TODO: eliminar antes de Sprint 2` en `init()`
   - Eliminar el método completo `verificarCapaDatos()` (líneas 51-79)

2. **Agregar líneas faltantes en `module-info.java`:**
   ```java
   exports com.minimart.controller;
   exports com.minimart.view;
   opens   com.minimart.controller to javafx.fxml;
   opens   com.minimart.view       to javafx.fxml;
   ```
   Esto debe hacerse **después** de crear al menos un archivo en cada paquete.

3. **Reemplazar `start()`:** El placeholder visual se sustituirá por la carga de un archivo FXML con `FXMLLoader`.

### Lo que Sprint 2 podrá hacer (dependencias desde Sprint 1)

- `TiendaDAO dao = new TiendaDAO()` desde cualquier Controller
- `tienda.dineroActualProperty()` para binding con Labels
- `estanteria.stockActualProperty()` para binding con ProgressBars
- `tienda.getEstanterias()` para renderizar slots de estanterías
- `tienda.getCajeros()` filtrado por `isActivo()` para renderizar slots de cajeros
- `cajero.getColaClientes()` (poblada por game loop en Sprint 4)

### Lo que NO incluye Sprint 1 (responsabilidad de sprints posteriores)

| Funcionalidad | Sprint |
|---|---|
| Archivos FXML y CSS | Sprint 2 |
| Controllers (MainController, etc.) | Sprint 2 |
| Game loop (timer, ticks, clientes) | Sprint 4 |
| Botones de upgrade/contratar | Sprint 5 |
| Guardado masivo (JuegoDAO) | Sprint 6 |
| Pantalla de derrota/victoria | Sprint 7 |
| Tests unitarios | Fuera de scope |

---

## 13. Resumen de Archivos (11 archivos .java)

```
src/main/java/com/minimart/
├── App.java                   84 líneas  — Punto de entrada + verificación temporal
├── module-info.java           11 líneas  — Declaración de módulo JPMS
├── model/
│   ├── Tienda.java            49 líneas  — Estado global de partida (3 Properties)
│   ├── Estanteria.java        46 líneas  — Estantería con stock (1 Property)
│   ├── Cajero.java            42 líneas  — Cajero con cola en memoria
│   └── Cliente.java           28 líneas  — Cliente de sesión (no persistente)
└── dao/
    ├── ConexionBD.java       136 líneas  — Singleton BD + initDB + esquema
    ├── DAO.java                9 líneas  — Interfaz genérica CRUD
    ├── TiendaDAO.java          96 líneas — CRUD + cargarPartidaCompleta()
    ├── EstanteriaDAO.java     107 líneas — CRUD + findByTiendaId() + updateStock()
    └── CajeroDAO.java         106 líneas — CRUD + findByTiendaId() + activar() + mejorar()
```

**Total:** ~714 líneas de código Java.
