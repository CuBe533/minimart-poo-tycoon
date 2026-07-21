# AGENTS.md — MiniMart POO Tycoon

JavaFX 26 + SQLite tycoon game. Java 21. Maven. JPMS modules.

## Build & Run

```bash
mvn clean compile        # verify compilation
mvn javafx:run           # launch the GUI app
mvn package              # produce JAR
```

No test framework exists. No CI. No linter. `mvn clean compile` is the only automated verification.

## Critical Quirks

### JPMS module-info.java

Every new package in `com.minimart.*` must be added to `module-info.java`:
- `exports com.minimart.newpackage;` — compile-time access
- `opens com.minimart.newpackage to javafx.fxml;` — only if the package contains `@FXML` fields or FXML controllers

Forgetting `opens` causes runtime `IllegalAccessException` from FXMLLoader.

### mainClass format

In `pom.xml`, `mainClass` must use `module/class` format: `com.minimart/com.minimart.App`. Using just `com.minimart.App` will fail at runtime.

### JavaFX platform classifier

Dependencies use `<classifier>${javafx.platform}</classifier>` (currently `win`). Changing platforms requires updating the `javafx.platform` property in `pom.xml`.

### SQLite database

- Path: `{user.home}/minimart.db` (outside the repo, in user home)
- Recreated/reset on every app launch via `ConexionBD.initDB()` and `App.verificarPartidaExistente()`
- `.gitignore` excludes `*.db`
- `ConexionBD` is a thread-safe singleton with auto-reconnect

### Seed data changes

If you modify seed data in `ConexionBD.initDB()`, delete `~/minimart.db` first — existing databases won't pick up DDL changes from `CREATE TABLE IF NOT EXISTS`.

## Architecture

```
src/main/java/com/minimart/
├── App.java                    # Entry point, lifecycle, music, save detection
├── module-info.java            # JPMS module declaration
├── controller/
│   ├── MainController.java     # FXML controller, all UI logic (~670 lines)
│   ├── GameLoopService.java    # 1s Timeline tick loop (spawn, dispatch, reputation)
│   ├── AnimacionService.java   # FadeTransition for money label
│   ├── PreciosConfig.java      # Static price map per product type
│   ├── GameOverController.java # Game over modal
│   └── ResumenDiaController.java # Day summary modal
├── model/
│   ├── Tienda.java             # Game state, JavaFX Properties for binding
│   ├── Estanteria.java         # Shelf with stock (SimpleIntegerProperty)
│   ├── Cajero.java             # Cashier with queue + ephemeral state
│   └── Cliente.java            # Session-only customer (not persisted)
├── dao/
│   ├── ConexionBD.java         # SQLite singleton, schema init
│   ├── DAO.java                # Generic CRUD interface
│   ├── TiendaDAO.java          # cargarPartidaCompleta()
│   ├── EstanteriaDAO.java      # CRUD + findByTiendaId
│   ├── CajeroDAO.java          # CRUD + activar/mejorar
│   └── JuegoDAO.java           # Transactional save/reset
└── view/
    ├── PanelesView.java        # Utility (minimal)
    └── package-info.java       # Javadoc

src/main/resources/com/minimart/
├── MainWindow.fxml             # Main layout (BorderPane)
├── ResumenDia.fxml             # Day summary dialog
├── GameOver.fxml               # Game over dialog
├── styles.css                  # All styling
├── audio/melody.mp3            # Background music
└── imagenes/                   # GIF sprites
```

## Conventions

- Language: code comments and UI text are in **Spanish**
- Models use JavaFX `SimpleXxxProperty` for reactive binding to FXML labels/bars
- DAOs use raw `PreparedStatement` (no ORM)
- All DB operations go through `ConexionBD.getInstance().getConnection()`
- Money values are rounded to 2 decimal places with `Math.round(x * 100.0) / 100.0`
- New DAO classes should follow the pattern: constructor takes `Connection` from singleton, implement `DAO<T>` interface

## Context Files

Sprint specs and skills are in `context/` (gitignored). `README.md` tracks sprint status but may lag behind actual code — trust the source files over README claims.
