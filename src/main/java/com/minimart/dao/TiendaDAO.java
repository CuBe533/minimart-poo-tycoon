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
        String sql = "SELECT id, usuario_id, nombre_tienda, dinero_actual, dia_actual FROM tienda";
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
        String sql = "SELECT id, usuario_id, nombre_tienda, dinero_actual, dia_actual FROM tienda WHERE id = ?";
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

    public Optional<Tienda> findByUsuarioId(int usuarioId) {
        String sql = "SELECT id, usuario_id, nombre_tienda, dinero_actual, dia_actual FROM tienda WHERE usuario_id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearFila(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("[TiendaDAO.findByUsuarioId] " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Tienda cargarPartidaCompleta(int tiendaId) {
        Tienda tienda = findById(tiendaId).orElseThrow(() ->
                new RuntimeException(
                        "[TiendaDAO.cargarPartidaCompleta] No existe tienda con id=" + tiendaId +
                                ". Verificar que initDB() se ejecuto correctamente."
                )
        );
        tienda.setEstanterias(estanteriaDAO.findByTiendaId(tiendaId));
        tienda.setCajeros(cajeroDAO.findByTiendaId(tiendaId));
        return tienda;
    }

    public Tienda cargarPartidaPorUsuario(int usuarioId) {
        Tienda tienda = findByUsuarioId(usuarioId).orElse(null);
        if (tienda == null) {
            tienda = crearPartidaInicial(usuarioId);
        }
        tienda.setEstanterias(estanteriaDAO.findByTiendaId(tienda.getId()));
        tienda.setCajeros(cajeroDAO.findByTiendaId(tienda.getId()));
        return tienda;
    }

    public Tienda crearPartidaInicial(int usuarioId) {
        Tienda tienda = new Tienda(0, usuarioId, "Mi MiniMart", 500.0, 1);
        save(tienda);

        try (PreparedStatement ps = conexion.prepareStatement(
                "INSERT INTO cajeros (tienda_id, nivel_mejora, tiempo_despacho, activo) VALUES (?, 1, 3, 1), (?, 1, 5, 0), (?, 1, 5, 0)")) {
            ps.setInt(1, tienda.getId());
            ps.setInt(2, tienda.getId());
            ps.setInt(3, tienda.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[TiendaDAO.crearPartidaInicial] " + e.getMessage(), e);
        }

        try (PreparedStatement ps = conexion.prepareStatement(
                "INSERT INTO estanterias (tienda_id, tipo_producto, stock_actual, stock_maximo, posicion_visual) VALUES (?, 'Snacks', 10, 10, 1)")) {
            ps.setInt(1, tienda.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[TiendaDAO.crearPartidaInicial] " + e.getMessage(), e);
        }

        System.out.println("[TiendaDAO] Partida inicial creada para usuario_id=" + usuarioId);
        return tienda;
    }

    @Override
    public void save(Tienda tienda) {
        String sql = "INSERT INTO tienda (usuario_id, nombre_tienda, dinero_actual, dia_actual) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tienda.getUsuarioId());
            ps.setString(2, tienda.getNombreTienda());
            ps.setDouble(3, tienda.getDineroActual());
            ps.setInt(4, tienda.getDiaActual());
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

    private Tienda mapearFila(ResultSet rs) throws SQLException {
        return new Tienda(
                rs.getInt("id"),
                rs.getInt("usuario_id"),
                rs.getString("nombre_tienda"),
                rs.getDouble("dinero_actual"),
                rs.getInt("dia_actual")
        );
    }
}
