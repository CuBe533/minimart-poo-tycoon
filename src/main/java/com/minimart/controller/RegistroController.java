package com.minimart.controller;

import com.minimart.dao.UsuarioDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegistroController {

    @FXML private TextField        txtUsuario;
    @FXML private PasswordField    txtPassword;
    @FXML private PasswordField    txtPassword2;
    @FXML private ComboBox<String> comboRol;
    @FXML private Button           btnRegistrar;
    @FXML private Button           btnVolver;
    @FXML private Label            labelError;

    @FXML
    public void initialize() {
        btnRegistrar.setOnAction(e -> handleRegistrar());
        btnVolver.setOnAction(e -> irALogin());
    }

    private void handleRegistrar() {
        String usuario = txtUsuario.getText();
        String pass1 = txtPassword.getText();
        String pass2 = txtPassword2.getText();
        String rolCombo = comboRol.getValue();

        if (usuario.isBlank() || pass1.isBlank()) {
            labelError.setText("Completa todos los campos.");
            return;
        }
        if (!pass1.equals(pass2)) {
            labelError.setText("Las contraseñas no coinciden.");
            return;
        }
        if (rolCombo == null) {
            labelError.setText("Selecciona un rol.");
            return;
        }

        UsuarioDAO dao = new UsuarioDAO();
        if (dao.existeUsuario(usuario)) {
            labelError.setText("Ese usuario ya existe.");
            return;
        }

        String rol;
        switch (rolCombo) {
            case "administrador" -> rol = "admin";
            case "estandar"      -> rol = "estandar";
            default              -> rol = "invitado";
        }
        dao.registrar(usuario, pass1, rol);
        irALogin();
    }

    private void irALogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/minimart/Login.fxml")
            );
            Parent raiz = loader.load();
            Stage stage = (Stage) btnRegistrar.getScene().getWindow();
            Scene escena = new Scene(raiz, 400, 400);
            escena.getStylesheets().add(
                    getClass().getResource("/com/minimart/styles.css").toExternalForm()
            );
            stage.setScene(escena);
        } catch (IOException ex) {
            labelError.setText("Error cargando el login.");
        }
    }
}