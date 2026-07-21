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

    public void registrar(String usuario, String password, String rol) {
        String sql = "INSERT INTO usuarios (usuario, password, rol) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, password);
            ps.setString(3, rol);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[UsuarioDAO.registrar] " + e.getMessage(), e);
        }
    }

    public String validarLogin(String usuario, String password) {
        String sql = "SELECT rol FROM usuarios WHERE usuario = ? AND password = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("rol") : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("[UsuarioDAO.validarLogin] " + e.getMessage(), e);
        }
    }
}