package com.minimart.dao;

import java.sql.*;

public class UsuarioDAO {

    private final Connection conexion = ConexionBD.getInstance().getConnection();

    public boolean existeUsuario(String usuario) {
        String sql = "SELECT 1 FROM usuarios WHERE usuario = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("[UsuarioDAO.existeUsuario] " + e.getMessage(), e);
        }
    }

    public int registrar(String usuario, String password, String rol) {
        String sql = "INSERT INTO usuarios (usuario, password, rol) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario);
            ps.setString(2, password);
            ps.setString(3, rol);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("[UsuarioDAO.registrar] " + e.getMessage(), e);
        }
        return -1;
    }

    public String[] validarLogin(String usuario, String password) {
        String sql = "SELECT id, rol FROM usuarios WHERE usuario = ? AND password = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[] { String.valueOf(rs.getInt("id")), rs.getString("rol") };
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("[UsuarioDAO.validarLogin] " + e.getMessage(), e);
        }
        return null;
    }
}
