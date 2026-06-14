package com.minimart.dao;

import com.minimart.model.Cajero;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CajeroDAO implements DAO<Cajero> {

    private static final String COLUMNAS =
        "id, tienda_id, nivel_mejora, tiempo_despacho, activo";

    private final Connection conexion;

    public CajeroDAO() {
        this.conexion = ConexionBD.getInstance().getConnection();
    }

    @Override
    public List<Cajero> findAll() {
        List<Cajero> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM cajeros";
        try (Statement stmt = conexion.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("[CajeroDAO.findAll] " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public Optional<Cajero> findById(int id) {
        String sql = "SELECT " + COLUMNAS + " FROM cajeros WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearFila(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("[CajeroDAO.findById] " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void save(Cajero cajero) {
        String sql = """
            INSERT INTO cajeros (tienda_id, nivel_mejora, tiempo_despacho, activo)
            VALUES (?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, cajero.getTiendaId());
            ps.setInt(2, cajero.getNivelMejora());
            ps.setInt(3, cajero.getTiempoDespacho());
            ps.setInt(4, cajero.isActivo() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    cajero.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("[CajeroDAO.save] " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Cajero cajero) {
        String sql = """
            UPDATE cajeros
            SET nivel_mejora = ?, tiempo_despacho = ?, activo = ?
            WHERE id = ?
        """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, cajero.getNivelMejora());
            ps.setInt(2, cajero.getTiempoDespacho());
            ps.setInt(3, cajero.isActivo() ? 1 : 0);
            ps.setInt(4, cajero.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[CajeroDAO.update] " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM cajeros WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[CajeroDAO.delete] " + e.getMessage(), e);
        }
    }

    public List<Cajero> findByTiendaId(int tiendaId) {
        List<Cajero> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM cajeros WHERE tienda_id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, tiendaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFila(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("[CajeroDAO.findByTiendaId] " + e.getMessage(), e);
        }
        return lista;
    }

    public void activar(int id) {
        String sql = "UPDATE cajeros SET activo = 1 WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[CajeroDAO.activar] " + e.getMessage(), e);
        }
    }

    public void mejorar(int id) {
        String sql = "UPDATE cajeros SET nivel_mejora = nivel_mejora + 1, " +
                     "tiempo_despacho = MAX(1, tiempo_despacho - 2) WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[CajeroDAO.mejorar] " + e.getMessage(), e);
        }
    }

    private Cajero mapearFila(ResultSet rs) throws SQLException {
        return new Cajero(
            rs.getInt("id"),
            rs.getInt("tienda_id"),
            rs.getInt("nivel_mejora"),
            rs.getInt("tiempo_despacho"),
            rs.getInt("activo") == 1
        );
    }
}