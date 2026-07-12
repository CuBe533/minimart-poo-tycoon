package com.minimart.dao;

import com.minimart.model.Cajero;
import com.minimart.model.Estanteria;
import com.minimart.model.Tienda;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TiendaDAO implements DAO<Tienda> {

    private final Connection    conexion;
    private final EstanteriaDAO estanteriaDAO;
    private final CajeroDAO     cajeroDAO;

    public TiendaDAO() {
        this.conexion      = ConexionBD.getInstance().getConnection();
        this.estanteriaDAO = new EstanteriaDAO();
        this.cajeroDAO     = new CajeroDAO();
    }

    @Override
    public List<Tienda> findAll() {
        List<Tienda> lista = new ArrayList<>();
        String sql = "SELECT id, nombre_tienda, dinero_actual, dia_actual FROM tienda";
        try (Statement stmt = conexion.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("[TiendaDAO.findAll] " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public Optional<Tienda> findById(int id) {
        String sql = "SELECT id, nombre_tienda, dinero_actual, dia_actual FROM tienda WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearFila(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("[TiendaDAO.findById] " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void save(Tienda tienda) {
        String sql = "INSERT INTO tienda (nombre_tienda, dinero_actual, dia_actual) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tienda.getNombreTienda());
            ps.setDouble(2, tienda.getDineroActual());
            ps.setInt(3, tienda.getDiaActual());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    tienda.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("[TiendaDAO.save] " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Tienda tienda) {
        String sql = "UPDATE tienda SET nombre_tienda = ?, dinero_actual = ?, dia_actual = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, tienda.getNombreTienda());
            ps.setDouble(2, tienda.getDineroActual());
            ps.setInt(3, tienda.getDiaActual());
            ps.setInt(4, tienda.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[TiendaDAO.update] " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM tienda WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[TiendaDAO.delete] " + e.getMessage(), e);
        }
    }

    public Tienda cargarPartidaCompleta(int tiendaId) {
        Tienda tienda = findById(tiendaId).orElseThrow(() ->
            new RuntimeException(
                "[TiendaDAO.cargarPartidaCompleta] No existe tienda con id=" + tiendaId +
                ". Verificar que initDB() se ejecutó correctamente."
            )
        );
        tienda.setEstanterias(estanteriaDAO.findByTiendaId(tiendaId));
        tienda.setCajeros(cajeroDAO.findByTiendaId(tiendaId));
        return tienda;
    }

    private Tienda mapearFila(ResultSet rs) throws SQLException {
        return new Tienda(
            rs.getInt("id"),
            rs.getString("nombre_tienda"),
            rs.getDouble("dinero_actual"),
            rs.getInt("dia_actual")
        );
    }
}