package com.minimart.admin;

import com.minimart.dao.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO {

    private final Connection conexion;

    public AdminDAO() {
        this.conexion = ConexionBD.getInstance().getConnection();
    }

    public List<PartidaDTO> listarPartidas() {
        List<PartidaDTO> lista = new ArrayList<>();
        String sql = """
            SELECT t.id,
                   COALESCE(u.usuario, 'desconocido') AS usuario_nombre,
                   t.nombre_tienda,
                   t.dinero_actual,
                   t.dia_actual,
                   (SELECT COUNT(*) FROM estanterias WHERE tienda_id = t.id)    AS total_estanterias,
                   (SELECT COUNT(*) FROM cajeros     WHERE tienda_id = t.id
                                                          AND activo = 1)       AS cajeros_activos
            FROM tienda t
            LEFT JOIN usuarios u ON u.id = t.usuario_id
            ORDER BY t.dia_actual DESC, t.id ASC
        """;
        try (Statement stmt = conexion.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new PartidaDTO(
                    rs.getInt("id"),
                    rs.getString("usuario_nombre"),
                    rs.getString("nombre_tienda"),
                    rs.getDouble("dinero_actual"),
                    rs.getInt("dia_actual"),
                    rs.getInt("total_estanterias"),
                    rs.getInt("cajeros_activos")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("[AdminDAO.listarPartidas] " + e.getMessage(), e);
        }
        return lista;
    }

    public void reiniciarPartida(int tiendaId) {
        try {
            conexion.setAutoCommit(false);

            try (PreparedStatement ps = conexion.prepareStatement(
                    "DELETE FROM estanterias WHERE tienda_id = ?")) {
                ps.setInt(1, tiendaId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conexion.prepareStatement(
                    "DELETE FROM cajeros WHERE tienda_id = ?")) {
                ps.setInt(1, tiendaId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conexion.prepareStatement(
                    "UPDATE tienda SET dinero_actual = 500.0, dia_actual = 1 WHERE id = ?")) {
                ps.setInt(1, tiendaId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conexion.prepareStatement(
                    "INSERT INTO cajeros (tienda_id, nivel_mejora, tiempo_despacho, activo) " +
                    "VALUES (?, 1, 5, 1), (?, 1, 5, 0), (?, 1, 5, 0)")) {
                ps.setInt(1, tiendaId);
                ps.setInt(2, tiendaId);
                ps.setInt(3, tiendaId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conexion.prepareStatement(
                    "INSERT INTO estanterias (tienda_id, tipo_producto, stock_actual, stock_maximo, posicion_visual) " +
                    "VALUES (?, 'Snacks', 10, 10, 1)")) {
                ps.setInt(1, tiendaId);
                ps.executeUpdate();
            }

            conexion.commit();
            System.out.println("[AdminDAO] Partida id=" + tiendaId + " reiniciada a estado inicial.");

        } catch (SQLException e) {
            try {
                conexion.rollback();
                System.err.println("[AdminDAO] Rollback ejecutado en reiniciarPartida.");
            } catch (SQLException rollbackEx) {
                System.err.println("[AdminDAO] Error adicional durante rollback: " + rollbackEx.getMessage());
            }
            throw new RuntimeException("[AdminDAO.reiniciarPartida] " + e.getMessage(), e);
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("[AdminDAO] Error restaurando autoCommit: " + e.getMessage());
            }
        }
    }

    public void eliminarPartida(int tiendaId) {
        try {
            conexion.setAutoCommit(false);

            try (PreparedStatement ps = conexion.prepareStatement(
                    "DELETE FROM estanterias WHERE tienda_id = ?")) {
                ps.setInt(1, tiendaId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conexion.prepareStatement(
                    "DELETE FROM cajeros WHERE tienda_id = ?")) {
                ps.setInt(1, tiendaId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conexion.prepareStatement(
                    "DELETE FROM tienda WHERE id = ?")) {
                ps.setInt(1, tiendaId);
                ps.executeUpdate();
            }

            conexion.commit();
            System.out.println("[AdminDAO] Partida id=" + tiendaId + " eliminada permanentemente.");

        } catch (SQLException e) {
            try {
                conexion.rollback();
                System.err.println("[AdminDAO] Rollback ejecutado en eliminarPartida.");
            } catch (SQLException rollbackEx) {
                System.err.println("[AdminDAO] Error adicional durante rollback: " + rollbackEx.getMessage());
            }
            throw new RuntimeException("[AdminDAO.eliminarPartida] " + e.getMessage(), e);
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("[AdminDAO] Error restaurando autoCommit: " + e.getMessage());
            }
        }
    }
}
