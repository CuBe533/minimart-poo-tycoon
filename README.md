# Estado Actual del Proyecto — MiniMart POO Tycoon

> **Sprint completado:** 8 de 8 — Multi-tenant, Login, Panel Admin
> **Fecha:** 2026-07-21

---

## 1. Stack Tecnológico

| Componente | Versión | Rol |
|---|---|---|
| Java | 21 (LTS) | Lenguaje base, records, text blocks |
| JavaFX | 26.0.1 | UI toolkit (Controls + FXML + Media) |
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
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-media</artifactId>
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

---

## 2. Estructura Completa de Directorios

```
minimart-poo-tycoon/
├── pom.xml
├── .gitignore
├── src/main/java/com/minimart/
│   ├── module-info.java              19 líneas
│   ├── App.java                     155 líneas
│   ├── controller/
│   │   ├── Sesion.java               20 líneas
│   │   ├── LoginController.java     112 líneas
│   │   ├── RegistroController.java   84 líneas
│   │   ├── MainController.java      743 líneas
│   │   ├── GameLoopService.java     199 líneas
│   │   ├── AnimacionService.java     30 líneas
│   │   ├── PreciosConfig.java        21 líneas
│   │   ├── GameOverController.java   63 líneas
│   │   └── ResumenDiaController.java 44 líneas
│   ├── model/
│   │   ├── Tienda.java               62 líneas
│   │   ├── Estanteria.java           61 líneas
│   │   ├── Cajero.java               58 líneas
│   │   └── Cliente.java              36 líneas
│   ├── dao/
│   │   ├── DAO.java                  17 líneas
│   │   ├── ConexionBD.java          130 líneas
│   │   ├── TiendaDAO.java           171 líneas
│   │   ├── EstanteriaDAO.java       143 líneas
│   │   ├── CajeroDAO.java           149 líneas
│   │   ├── JuegoDAO.java            132 líneas
│   │   └── UsuarioDAO.java           54 líneas
│   ├── admin/
│   │   ├── AdminController.java     161 líneas
│   │   ├── AdminDAO.java            150 líneas
│   │   └── PartidaDTO.java           52 líneas
│   └── view/
│       ├── PanelesView.java           5 líneas
│       └── package-info.java          2 líneas
├── src/main/resources/com/minimart/
│   ├── login.fxml                    32 líneas
│   ├── Registro.fxml                 26 líneas
│   ├── MainWindow.fxml              190 líneas
│   ├── AdminPanel.fxml               63 líneas
│   ├── ResumenDia.fxml               49 líneas
│   ├── GameOver.fxml                 42 líneas
│   ├── styles.css                   173 líneas
│   ├── admin-styles.css             139 líneas
│   ├── audio/melody.mp3             6.4 MB
│   └── imagenes/                     22 GIFs
└── target/                           (ignorado por git)
```

**Total:** 27 archivos Java (~2,772 líneas) + 6 FXML + 2 CSS + 1 audio + 22 GIFs.

---

## 3. `module-info.java` — Sistema de Módulos JPMS

```java
module com.minimart {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.media;

    opens com.minimart to javafx.fxml;

    exports com.minimart;
    exports com.minimart.dao;
    exports com.minimart.model;

    exports com.minimart.controller;
    exports com.minimart.view;
    exports com.minimart.admin;
    opens   com.minimart.controller to javafx.fxml;
    opens   com.minimart.view       to javafx.fxml;
    opens   com.minimart.admin      to javafx.fxml;
}
```

6 paquetes exportados, 4 abiertos a `javafx.fxml` (para reflexión FXML).

---

## 4. `App.java` — Punto de Entrada (155 líneas)

### Ciclo de vida

| Método | Hilo | Responsabilidad |
|---|---|---|
| `init()` | Inicialización (no UI) | Inicializar BD (`ConexionBD.initDB()`) |
| `start(Stage)` | JavaFX Application | Cargar `login.fxml`, iniciar música de fondo |
| `stop()` | JavaFX Application | Auto-save partida en curso, parar música, cerrar BD |

### Funcionalidades

- **Pantalla de inicio:** Carga `login.fxml` como pantalla principal (no `MainWindow.fxml`)
- **Música de fondo:** `MediaPlayer` con `melody.mp3`, ciclo `INDEFINITE`, volumen 10%
- **Auto-save:** `stop()` llama `JuegoDAO.guardarEstadoCompleto(tiendaEnJuego)` si hay partida activa
- **`verificarPartidaExistente()`:** Detecta si el usuario tiene progreso (día > 1, dinero != 500, estanterías, cajeros mejorados). Si existe, ofrece continuar o empezar nueva partida via `ConfirmationAlert`
- **`abrirPanelAdmin(Stage)`:** Abre panel admin como modal para usuarios con rol `admin`
- **`setTiendaEnJuego(Tienda)`:** Registra la tienda activa para auto-save al cerrar

---

## 5. Sistema de Login y Roles

### Flujo de autenticación

```
App.start() → login.fxml
  ├─ [Ingresar]  → LoginController.handleLogin() → UsuarioDAO.validarLogin()
  │     ├─ rol=admin    → App.abrirPanelAdmin()
  │     ├─ rol=estandar → verificarPartidaExistente() → MainWindow.fxml
  │     └─ rol=invitado → verificarPartidaExistente() → MainWindow.fxml
  ├─ [Invitado]   → LoginController.handleGuest() → crear/recuperar "guest" → MainWindow.fxml
  └─ [Crear cuenta] → Registro.fxml → Login.fxml
```

### `Sesion.java` (20 líneas)

Sesión estática con campos: `rol`, `usuarioId`, `nombreUsuario`.

| Método | Descripción |
|---|---|
| `esAdmin()` | `"admin".equals(rol)` |
| `esEstandar()` | `"estandar".equals(rol)` |
| `esInvitado()` | `"invitado".equals(rol)` |

### `UsuarioDAO.java` (54 líneas)

| Método | Retorna |
|---|---|
| `existeUsuario(String)` | `boolean` |
| `registrar(String, String, String)` | `int` (ID generado) |
| `validarLogin(String, String)` | `String[]{id, rol}` o `null` |

### `LoginController.java` (112 líneas)

- Campos FXML: `txtUsuario`, `txtPassword`, `btnLogin`, `btnGuest`, `btnRegistro`, `labelError`
- **`handleLogin()`:** Valida campos no vacíos, consulta BD, establece sesión, navega al juego
- **`handleGuest()`:** Crea usuario "guest" si no existe (password `"none"`, rol `"invitado"`), inicia sesión directa
- **`irAlJuego()`:** Admin → panel admin; invitado → sin verificar partida; estándar → `verificarPartidaExistente()`

### `RegistroController.java` (84 líneas)

- ComboBox de roles: "administrador", "estandar", "invitado"
- Valida: campos no vacíos, contraseñas coinciden, rol seleccionado, usuario único
- Mapeo ComboBox → BD: "administrador" → `"admin"`, "estandar" → `"estandar"`, default → `"invitado"`

---

## 6. Panel de Administración

### `AdminController.java` (161 líneas)

- **`abrirPanel(Stage)`:** Carga `AdminPanel.fxml` como `APPLICATION_MODAL`
- **`configurarTabla()`:** Vincula `PartidaDTO` a 7 columnas (ID, Usuario, Nombre, Dia, Dinero, Estanterias, Cajeros)
- **`handleReiniciar()`:** Confirmación → `AdminDAO.reiniciarPartida(tiendaId)` → refresca tabla
- **`handleEliminar()`:** Advertencia → `AdminDAO.eliminarPartida(tiendaId)` → refresca tabla

### `AdminDAO.java` (150 líneas)

| Método | Descripción |
|---|---|
| `listarPartidas()` | JOIN `tienda` + `usuarios` → `List<PartidaDTO>`, ordenado por día DESC |
| `reiniciarPartida(int)` | Transacción: DELETE estanterías → DELETE cajeros → UPDATE tienda ($500, día 1) → INSERT semilla |
| `eliminarPartida(int)` | Transacción: DELETE estanterías → DELETE cajeros → DELETE tienda |

### `PartidaDTO.java` (52 líneas)

DTO con: `tiendaId`, `usuarioNombre`, `nombreTienda`, `dineroActual`, `diaActual`, `totalEstanterias`, `cajerosActivos`.

---

## 7. `MainController.java` — Controlador Principal (743 líneas)

### Componentes FXML

| Zona | Componentes |
|---|---|
| **Estanterías** (5 slots) | `slotEstanteria1-5` (VBox), `imgEstanteria1-5` (ImageView), `stockBar1-5` (ProgressBar), `labelTipo1-5` (Label) |
| **Cajeros** (3 slots) | `slotCajero1-3` (VBox), `imgCajero1-3` (ImageView), `atenderBar1-3` (ProgressBar) |
| **Compradores** (4 slots) | `comprador1-4` (ImageView) |
| **Upgrades** | `btnUpgrade1` (Estantería $150), `btnUpgrade2` (Reabastecer $50), `btnUpgrade3` (Cajero $200) |
| **Estadísticas** | `labelDinero`, `labelReputacion`, `labelDia`, `btnAvanzarDia` |

### Métodos principales

| Método | Descripción |
|---|---|
| `initialize()` | Carga partida, inicia game loop, asigna 22 GIFs sprites |
| `cargarPartida()` | `TiendaDAO.cargarPartidaPorUsuario(Sesion.getUsuarioId())`, binding reactivo |
| `iniciarGameLoop()` | Crea `GameLoopService(tiendaActual, this)`, arranca Timeline |
| `actualizarVistas()` | Refresca barras de atención, aplica rojo para stock crítico (<30%) |
| `handleComprarEstanteria()` | Límite 5, $150, tipo secuencial del catálogo |
| `handleReabastecer()` | ChoiceDialog, $50 |
| `handleMejorarCajero()` | Contratar inactivo ($200) o mejorar el de menor nivel ($200) |
| `handleAvanzarDia()` | Restricción para invitados, guarda estado, incrementa día, muestra `ResumenDiaController` |
| `mostrarGameOver()` | Carga `GameOver.fxml` como modal |

### Sistema visual de clientes (~130 líneas)

- Walking animation: 2s Timeline con 4 sprites de caminata alternados
- Sprite estático al llegar al cajero (4 variantes)
- Mostrador alterna entre 3 sprites de servicio
- Imágenes cargadas vía `getResourceAsStream()`

---

## 8. Game Loop — `GameLoopService.java` (199 líneas)

### Configuración

| Constante | Valor |
|---|---|
| Probabilidad de spawn | 80% por tick |
| Umbral de despacho rápido | ≤ 3 segundos → +2 reputación |
| Reputación inicial | 100.0 |

### Flujo por tick (cada 1s)

```
1. ¿Llega cliente? → 80% sí: elige producto al azar, crea Cliente,
   resta stock, asigna al cajero menos ocupado
2. Cada cajero activo con cola: decrementa segundosRestantes
3. ¿segundosRestantes ≤ 0? → cobra cliente (+dinero, +animación),
   saca de cola, inicia siguiente si hay
4. Actualizar reputación: -0.5 por estantería vacía por tick
5. ¿Game Over? → dinero < 0 O reputación ≤ 0 → pausar y mostrar pantalla
```

### Catálogo de precios

| Producto | Precio |
|---|---|
| Snacks | $4.00 |
| Bebidas | $3.00 |
| Lácteos | $7.00 |
| Dulces | $3.20 |
| Conservas | $5.00 |

---

## 9. `ConexionBD.java` — Singleton de Base de Datos (130 líneas)

- **Patrón:** Double-checked locking thread-safe
- **URL:** `jdbc:sqlite:` + `{user.home}/minimart.db`
- **PRAGMA:** `foreign_keys = ON`
- **Reconexión automática:** `getConnection()` verifica `isClosed()` y reabre si es necesario

### Tablas

#### `usuarios` (Sprint 8)

| Columna | Tipo | Default | Restricciones |
|---|---|---|---|
| id | INTEGER | — | PK AUTOINCREMENT |
| usuario | TEXT | — | NOT NULL UNIQUE |
| password | TEXT | — | NOT NULL |
| rol | TEXT | — | NOT NULL, CHECK('invitado','estandar','admin') |

#### `tienda`

| Columna | Tipo | Default | Restricciones |
|---|---|---|---|
| id | INTEGER | — | PK AUTOINCREMENT |
| usuario_id | INTEGER | NULL | FK → usuarios(id) ON DELETE SET NULL |
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

### Semilla programática

Ya no se insertan datos en `initDB()`. La semilla se crea en:
- `TiendaDAO.crearPartidaInicial(usuarioId)` — 1 estantería Snacks + 3 cajeros (1 activo a 3s, 2 inactivos a 5s)
- `JuegoDAO.resetearPartida(tiendaId)` — misma semilla para nueva partida
- `AdminDAO.reiniciarPartida(tiendaId)` — reinicio desde panel admin

---

## 10. Modelos (4 clases en `com.minimart.model`)

### `Tienda.java` (62 líneas)

| Campo | Tipo Java | Property? |
|---|---|---|
| id | `int` | No |
| usuarioId | `int` | No |
| nombreTienda | `SimpleStringProperty` | Sí |
| dineroActual | `SimpleDoubleProperty` | Sí |
| diaActual | `SimpleIntegerProperty` | Sí |
| estanterias | `List<Estanteria>` | No |
| cajeros | `List<Cajero>` | No |

### `Estanteria.java` (61 líneas)

| Campo | Tipo Java | Property? |
|---|---|---|
| id | `int` | No |
| tiendaId | `int` | No |
| tipoProducto | `String` | No |
| stockActual | `SimpleIntegerProperty` | Sí |
| stockMaximo | `int` | No |
| posicionVisual | `int` (1-5) | No |

**Helpers:** `tieneStock()`, `getPorcentajeStock()`.

### `Cajero.java` (58 líneas)

| Campo | Tipo Java | Persiste? |
|---|---|---|
| id | `int` | Sí |
| tiendaId | `int` | Sí |
| nivelMejora | `int` | Sí |
| tiempoDespacho | `int` | Sí |
| activo | `boolean` | Sí |
| colaClientes | `Queue<Cliente>` (LinkedList) | No (efímero) |
| segundosRestantes | `int` | No (efímero) |

### `Cliente.java` (36 líneas)

Cliente de sesión (no persiste en BD).

| Campo | Tipo Java |
|---|---|
| CONTADOR_SESION | `static final AtomicInteger` |
| id | `final int` |
| productoElegido | `String` |
| montoGastado | `double` |

---

## 11. DAOs (7 implementaciones en `com.minimart.dao`)

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

### Resumen de DAOs

| DAO | Métodos especiales |
|---|---|
| `TiendaDAO` | `cargarPartidaPorUsuario(usuarioId)`, `crearPartidaInicial(usuarioId)`, `findByUsuarioId(usuarioId)` |
| `EstanteriaDAO` | `findByTiendaId(tiendaId)`, `updateStock(id, nuevoStock)` |
| `CajeroDAO` | `findByTiendaId(tiendaId)`, `activar(id)`, `mejorar(id)` |
| `JuegoDAO` | `guardarEstadoCompleto(Tienda)`, `resetearPartida(tiendaId)` — transaccionales |
| `UsuarioDAO` | `existeUsuario(usuario)`, `registrar(usuario, pass, rol)`, `validarLogin(usuario, pass)` |

---

## 12. Servicios Auxiliares

### `AnimacionService.java` (30 líneas)

`animarGanancia(Label)`: FadeTransition (500ms, 2 ciclos, auto-reverse) en color verde sobre label de dinero.

### `PreciosConfig.java` (21 líneas)

Mapa estático de precios por tipo de producto (Snacks $4, Bebidas $3, Lácteos $7, Dulces $3.2, Conservas $5, default $2).

---

## 13. FXML y Estilos

### Pantallas (6 FXML)

| FXML | Tamaño | Descripción |
|---|---|---|
| `login.fxml` | 400×400 | Login con usuario/contraseña + botón Invitado + Crear cuenta |
| `Registro.fxml` | 400×400 | Registro con ComboBox de roles |
| `MainWindow.fxml` | 1024×768 | Layout principal (BorderPane: center + right) |
| `AdminPanel.fxml` | 850×520 | Panel admin con TableView + botones reiniciar/eliminar |
| `ResumenDia.fxml` | 380×320 | Modal de resumen de jornada |
| `GameOver.fxml` | 380×320 | Modal de game over con estadísticas |

### Hojas de estilo (2 CSS)

| CSS | Líneas | Propósito |
|---|---|---|
| `styles.css` | 173 | Estilos del juego principal |
| `admin-styles.css` | 139 | Tema oscuro para panel admin |

---

## 14. Assets

### Audio

- `audio/melody.mp3` (6.4 MB) — Música de fondo, ciclo infinito al 10% volumen

### Sprites GIF (22 archivos)

| Sprite | Uso |
|---|---|
| `producto_snacks.gif`, `producto_bebidas.gif`, `producto_lacteos.gif`, `producto_dulces.gif`, `producto_conservas.gif` | Imágenes de estanterías por tipo |
| `cajero.gif`, `atendiendo.gif`, `MejoraCajero.gif` | Sprites de cajeros |
| `cliente.gif` | Sprite base de cliente |
| `clienteCaminando1-4.gif` | Animación de caminata |
| `ClienteEstatico1-4.gif` | Sprites estáticos al counter |
| `Mostrador.gif`, `Mostrador (2).gif`, `Mostrador (3).gif` | Sprites de mostrador |
| `upgrade_estanteria.gif`, `upgrade_reabastecer.gif` | Iconos de botones de upgrade |

---

## 15. Resumen de Archivos (27 Java + 8 recursos)

| Paquete | Archivos | Estado |
|---|---|---|
| `controller/` | 9 archivos | Login, registro, main, game loop, animación, precios, game over, resumen día |
| `model/` | 4 archivos | Tienda, Estanteria, Cajero, Cliente |
| `dao/` | 7 archivos | ConexionBD, DAO, Tienda, Estanteria, Cajero, Juego, Usuario |
| `admin/` | 3 archivos | AdminController, AdminDAO, PartidaDTO |
| `view/` | 2 archivos | PanelesView, package-info |
| `resources/` | 8 archivos | 6 FXML, 2 CSS |
| `resources/imagenes/` | 22 GIFs | Sprites del juego |
| `resources/audio/` | 1 MP3 | Música de fondo |

---

## 16. Comandos de Build y Ejecución

| Comando | Propósito |
|---|---|
| `mvn clean compile` | Compilar (verificar errores) |
| `mvn javafx:run` | Ejecutar la aplicación con ventana |
| `mvn package` | Generar JAR |

---

## 17. Flujo General de la Aplicación

```
App.init() → BD.initDB() (crea tablas si no existen)
App.start() → login.fxml + música de fondo
  │
  ├─ [Ingresar] → valida usuario+contraseña → sesión
  │     ├─ admin → AdminPanel.fxml (modal)
  │     └─ otro  → verificarPartidaExistente() → continuar/nueva → MainWindow.fxml
  │
  ├─ [Invitado] → crea usuario "guest" → MainWindow.fxml (sin persistencia entre sesiones)
  │
  └─ [Crear cuenta] → Registro.fxml → login.fxml
       │
       MainWindow.fxml → GameLoopService (1s ticks)
         ├─ Spawn clientes (80%) → decrementa stock
         ├─ Despacho cajeros → +dinero → animación
         ├─ Reputación: -0.5/estantería vacía por tick
         ├─ Comprar/Reabastecer/Mejorar → $150/$50/$200
         ├─ Avanzar día → guardar → resumen → siguiente día
         └─ Game Over (dinero < 0 o reputación ≤ 0) → nueva partida

App.stop() → auto-save partida en curso
```

---

## 18. Criterios de Aceptación — Sprint 2-5 (completados)

| Sprint | Estado |
|---|---|
| Sprint 2 — Layout + UI | ✅ |
| Sprint 3 — Carga desde BD + Bindings | ✅ |
| Sprint 4 — Game Loop (spawn + despacho) | ✅ |
| Sprint 5 — CRUD desde botones de upgrade | ✅ |

---

## 19. Criterios de Aceptación — Sprint 6 (Persistencia)

| CA | Descripción | Estado |
|---|---|---|
| CA-01 | `mvn clean compile` → BUILD SUCCESS | ✅ |
| CA-02 | Avanzar día guarda estado completo (JuegoDAO.transaccional) | ✅ |
| CA-03 | Resetear partida crea semilla limpia (3 cajeros, 1 estantería) | ✅ |
| CA-04 | Cerrar y reabrir app conserva día, dinero, stock, mejoras | ✅ |
| CA-05 | Auto-save en `App.stop()` | ✅ |
| CA-06 | `PreciosConfig` define precios por tipo de producto | ✅ |
| CA-07 | `TiendaDAO.cargarPartidaPorUsuario()` crea partida si no existe | ✅ |

---

## 20. Criterios de Aceptación — Sprint 7 (Animaciones + Game Over)

| CA | Descripción | Estado |
|---|---|---|
| CA-01 | `mvn clean compile` → BUILD SUCCESS | ✅ |
| CA-02 | Animación de ganancia (FadeTransition verde) en label de dinero | ✅ |
| CA-03 | Game Over al dinero < 0 o reputación ≤ 0 | ✅ |
| CA-04 | Pantalla de Game Over muestra días sobrevividos y dinero máximo | ✅ |
| CA-05 | Resumen de día muestra ventas, ganancia y dinero total | ✅ |
| CA-06 | Restricción: invitados no pueden avanzar día | ✅ |
| CA-07 | Música de fondo con `javafx.media` | ✅ |
| CA-08 | 22 GIFs de sprites para estanterías, cajeros, clientes y mostrador | ✅ |
| CA-09 | Animación visual de clientes caminando y llegando al counter | ✅ |

---

## 21. Criterios de Aceptación — Sprint 8 (Multi-tenant + Login + Admin)

| CA | Descripción | Estado |
|---|---|---|
| CA-01 | `mvn clean compile` → BUILD SUCCESS | ✅ |
| CA-02 | Login con usuario+contraseña valida contra BD | ✅ |
| CA-03 | Botón "Invitado" crea usuario guest y carga juego | ✅ |
| CA-04 | Registro con ComboBox de roles (admin, estándar, invitado) | ✅ |
| CA-05 | Rol `admin` abre panel de administración | ✅ |
| CA-06 | Panel admin muestra tabla con todas las partidas (usuarios + tiendas) | ✅ |
| CA-07 | Admin puede reiniciar partida (transaccional) | ✅ |
| CA-08 | Admin puede eliminar partida (transaccional) | ✅ |
| CA-09 | Cada usuario tiene su propia tienda (`tienda.usuario_id` FK) | ✅ |
| CA-10 | `TiendaDAO.crearPartidaInicial()` crea semilla por usuario | ✅ |
| CA-11 | `module-info.java` exporta/opens `com.minimart.admin` | ✅ |
| CA-12 | `App.stop()` auto-saves la partida activa | ✅ |
| CA-13 | `ConexionBD` crea tabla `usuarios` y migración `usuario_id` en `tienda` | ✅ |

---

## 22. Arquitectura Multi-tenant

```
usuarios (1) ────→ tienda (1) ────→ estanterias (N)
                      │
                      └────────────→ cajeros (N)
```

- Cada usuario tiene 0 o 1 tienda (creada en login si no existe)
- Admin ve todas las partidas en una tabla
- Guest comparte un usuario único en la BD
- El FK `tienda.usuario_id` usa `ON DELETE SET NULL` (si se borra el usuario, la tienda queda huérfana)
