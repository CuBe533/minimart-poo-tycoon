package com.minimart.dao;

import com.minimart.model.Cajero;
import com.minimart.model.Estanteria;
import com.minimart.model.Tienda;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class JuegoDAO {

    private final Connection conexion;

    public JuegoDAO() {
        this.conexion = ConexionBD.getInstance().getConnection();
    }


    public void guardarEstadoCompleto(Tienda t) {
        try {
            conexion.setAutoCommit(false);

            String sqlTienda = "UPDATE tienda SET dinero_actual = ?, dia_actual = ? WHERE id = ?";
            try (PreparedStatement ps = conexion.prepareStatement(sqlTienda)) {
                ps.setDouble(1, t.getDineroActual());
                ps.setInt(2, t.getDiaActual());
                ps.setInt(3, t.getId());
                ps.executeUpdate();
            }

            String sqlEstanteria = "UPDATE estanterias SET stock_actual = ? WHERE id = ?";
            try (PreparedStatement ps = conexion.prepareStatement(sqlEstanteria)) {
                for (Estanteria e : t.getEstanterias()) {
                    ps.setInt(1, e.getStockActual());
                    ps.setInt(2, e.getId());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            String sqlCajero = "UPDATE cajeros SET nivel_mejora = ?, tiempo_despacho = ?, activo = ? WHERE id = ?";
            try (PreparedStatement ps = conexion.prepareStatement(sqlCajero)) {
                for (Cajero c : t.getCajeros()) {
                    ps.setInt(1, c.getNivelMejora());
                    ps.setInt(2, c.getTiempoDespacho());
                    ps.setInt(3, c.isActivo() ? 1 : 0);
                    ps.setInt(4, c.getId());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conexion.commit();
            System.out.println("[JuegoDAO] Estado completo guardado — día " + t.getDiaActual() +
                    ", dinero $" + String.format("%.2f", t.getDineroActual()));

        } catch (SQLException e) {
            try {
                conexion.rollback();
                System.err.println("[JuegoDAO] Rollback ejecutado por error en guardarEstadoCompleto.");
            } catch (SQLException rollbackEx) {
                System.err.println("[JuegoDAO] Error adicional durante rollback: " + rollbackEx.getMessage());
            }
            throw new RuntimeException("[JuegoDAO.guardarEstadoCompleto] " + e.getMessage(), e);
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("[JuegoDAO] Error restaurando autoCommit: " + e.getMessage());
            }
        }
    }


    public void resetearPartida() {
        try {
            conexion.setAutoCommit(false);

            try (PreparedStatement ps = conexion.prepareStatement(
                    "DELETE FROM estanterias WHERE tienda_id = ?")) {
                ps.setInt(1, 1);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conexion.prepareStatement(
                    "DELETE FROM cajeros WHERE tienda_id = ?")) {
                ps.setInt(1, 1);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conexion.prepareStatement(
                    "UPDATE tienda SET dinero_actual = 500.0, dia_actual = 1 WHERE id = ?")) {
                ps.setInt(1, 1);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conexion.prepareStatement(
                    "INSERT INTO cajeros (tienda_id, nivel_mejora, tiempo_despacho, activo) VALUES (?, 1, 5, 1), (?, 1, 5, 0), (?, 1, 5, 0)")) {
                ps.setInt(1, 1);
                ps.setInt(2, 1);
                ps.setInt(3, 1);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conexion.prepareStatement(
                    "INSERT INTO estanterias (tienda_id, tipo_producto, stock_actual, stock_maximo, posicion_visual) " +
                            "VALUES (?, 'Snacks', 10, 10, 1)")) {
                ps.setInt(1, 1);
                ps.executeUpdate();
            }

            conexion.commit();
            System.out.println("[JuegoDAO] Partida reseteada a estado inicial.");

        } catch (SQLException e) {
            try {
                conexion.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("[JuegoDAO] Error adicional durante rollback: " + rollbackEx.getMessage());
            }
            throw new RuntimeException("[JuegoDAO.resetearPartida] " + e.getMessage(), e);
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("[JuegoDAO] Error restaurando autoCommit: " + e.getMessage());
            }
        }
    }
}