package com.minimart.dao;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionBD {

    private static volatile ConexionBD instancia;

    private ConexionBD() {
        abrirConexion();
    }

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

    private Connection conexion;

    private static final String DB_PATH =
            Paths.get(System.getProperty("user.home"), "minimart.db").toString();

    private static final String JDBC_URL = "jdbc:sqlite:" + DB_PATH;

    private void abrirConexion() {
        try {
            conexion = DriverManager.getConnection(JDBC_URL);
            try (Statement stmt = conexion.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            System.out.println("[ConexionBD] Conectado a: " + DB_PATH);
        } catch (SQLException e) {
            throw new RuntimeException(
                    "[ConexionBD] No se pudo conectar a la BD SQLite: " + e.getMessage(), e
            );
        }
    }

    public Connection getConnection() {
        try {
            if (conexion == null || conexion.isClosed()) {
                abrirConexion();
            }
        } catch (SQLException e) {
            throw new RuntimeException("[ConexionBD] Error verificando conexión: " + e.getMessage(), e);
        }
        return conexion;
    }

    public void initDB() {
        try (Statement stmt = getConnection().createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tienda (
                    id              INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre_tienda   TEXT    NOT NULL DEFAULT 'Mi MiniMart',
                    dinero_actual   REAL    NOT NULL DEFAULT 500.0,
                    dia_actual      INTEGER NOT NULL DEFAULT 1
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS estanterias (
                    id               INTEGER PRIMARY KEY AUTOINCREMENT,
                    tienda_id        INTEGER NOT NULL,
                    tipo_producto    TEXT    NOT NULL,
                    stock_actual     INTEGER NOT NULL DEFAULT 10,
                    stock_maximo     INTEGER NOT NULL DEFAULT 10,
                    posicion_visual  INTEGER NOT NULL CHECK(posicion_visual BETWEEN 1 AND 5),
                    FOREIGN KEY (tienda_id) REFERENCES tienda(id) ON DELETE CASCADE,
                    UNIQUE(tienda_id, posicion_visual)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS cajeros (
                    id               INTEGER PRIMARY KEY AUTOINCREMENT,
                    tienda_id        INTEGER NOT NULL,
                    nivel_mejora     INTEGER NOT NULL DEFAULT 1,
                    tiempo_despacho  INTEGER NOT NULL DEFAULT 5,
                    activo           INTEGER NOT NULL DEFAULT 0 CHECK(activo IN (0,1)),
                    FOREIGN KEY (tienda_id) REFERENCES tienda(id) ON DELETE CASCADE
                )
            """);

            stmt.execute("""
                INSERT OR IGNORE INTO tienda (id, nombre_tienda, dinero_actual, dia_actual)
                VALUES (1, 'Mi MiniMart', 500.0, 1)
            """);

            stmt.execute("""
                INSERT INTO cajeros (tienda_id, nivel_mejora, tiempo_despacho, activo)
                SELECT 1, 1, 3, 1
                WHERE NOT EXISTS (
                    SELECT 1 FROM cajeros WHERE tienda_id = 1
                )
                UNION ALL
                SELECT 1, 1, 5, 0
                WHERE NOT EXISTS (
                    SELECT 1 FROM cajeros WHERE tienda_id = 1
                )
                UNION ALL
                SELECT 1, 1, 5, 0
                WHERE NOT EXISTS (
                    SELECT 1 FROM cajeros WHERE tienda_id = 1
                )
            """);

            stmt.execute("""
                INSERT INTO estanterias (tienda_id, tipo_producto, stock_actual, stock_maximo, posicion_visual)
                SELECT 1, 'Snacks', 10, 10, 1
                WHERE NOT EXISTS (
                    SELECT 1 FROM estanterias WHERE tienda_id = 1 AND posicion_visual = 1
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id        INTEGER PRIMARY KEY AUTOINCREMENT,
                    usuario   TEXT NOT NULL UNIQUE,
                    password  TEXT NOT NULL,
                    rol       TEXT NOT NULL CHECK(rol IN ('invitado','usuario','admin'))
                )
            """);


            System.out.println("[ConexionBD] Esquema inicializado correctamente.");

        } catch (SQLException e) {
            throw new RuntimeException(
                    "[ConexionBD] Error inicializando el esquema: " + e.getMessage(), e
            );
        }
    }

    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("[ConexionBD] Conexión cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("[ConexionBD] Error cerrando la conexión: " + e.getMessage());
        }
    }
}