package com.minimart.dao;

import com.minimart.model.Estanteria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EstanteriaDAO implements DAO<Estanteria> {

    private static final String COLUMNAS =
        "id, tienda_id, tipo_producto, stock_actual, stock_maximo, posicion_visual";

    private final Connection conexion;

    public EstanteriaDAO() {
        this.conexion = ConexionBD.getInstance().getConnection();
    }

    @Override
    public List<Estanteria> findAll() {
        List<Estanteria> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM estanterias ORDER BY posicion_visual";
        try (Statement stmt = conexion.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("[EstanteriaDAO.findAll] " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public Optional<Estanteria> findById(int id) {
        String sql = "SELECT " + COLUMNAS + " FROM estanterias WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearFila(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("[EstanteriaDAO.findById] " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void save(Estanteria estanteria) {
        String sql = """
            INSERT INTO estanterias
                (tienda_id, tipo_producto, stock_actual, stock_maximo, posicion_visual)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,    estanteria.getTiendaId());
            ps.setString(2, estanteria.getTipoProducto());
            ps.setInt(3,    estanteria.getStockActual());
            ps.setInt(4,    estanteria.getStockMaximo());
            ps.setInt(5,    estanteria.getPosicionVisual());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    estanteria.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("[EstanteriaDAO.save] " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Estanteria estanteria) {
        String sql = """
            UPDATE estanterias
            SET tipo_producto = ?, stock_actual = ?, stock_maximo = ?, posicion_visual = ?
            WHERE id = ?
        """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, estanteria.getTipoProducto());
            ps.setInt(2,    estanteria.getStockActual());
            ps.setInt(3,    estanteria.getStockMaximo());
            ps.setInt(4,    estanteria.getPosicionVisual());
            ps.setInt(5,    estanteria.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[EstanteriaDAO.update] " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM estanterias WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[EstanteriaDAO.delete] " + e.getMessage(), e);
        }
    }

    public List<Estanteria> findByTiendaId(int tiendaId) {
        List<Estanteria> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM estanterias WHERE tienda_id = ? ORDER BY posicion_visual";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, tiendaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFila(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("[EstanteriaDAO.findByTiendaId] " + e.getMessage(), e);
        }
        return lista;
    }

    public void updateStock(int id, int nuevoStock) {
        String sql = "UPDATE estanterias SET stock_actual = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, nuevoStock);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[EstanteriaDAO.updateStock] " + e.getMessage(), e);
        }
    }

    private Estanteria mapearFila(ResultSet rs) throws SQLException {
        return new Estanteria(
            rs.getInt("id"),
            rs.getInt("tienda_id"),
            rs.getString("tipo_producto"),
            rs.getInt("stock_actual"),
            rs.getInt("stock_maximo"),
            rs.getInt("posicion_visual")
        );
    }
}