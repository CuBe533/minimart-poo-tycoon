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

public class LoginController {

    @FXML private TextField     txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> comboRol;
    @FXML private Button        btnLogin;
    @FXML private Button        btnRegistro;
    @FXML private Label         labelError;

    @FXML
    public void initialize() {
        btnLogin.setOnAction(e -> handleLogin());
        btnRegistro.setOnAction(e -> irARegistro());
    }

    private void handleLogin() {
        String rolCombo = comboRol.getValue();

        if (rolCombo != null) {
            Sesion.setRol(rolCombo.equals("administrador") ? "admin" : "invitado");
            irAlJuego();
            return;
        }

        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        if (usuario.isBlank() || password.isBlank()) {
            labelError.setText("Completa usuario y contraseña, o selecciona un rol.");
            return;
        }

        String rol = new UsuarioDAO().validarLogin(usuario, password);

        if (rol == null) {
            labelError.setText("Usuario o contraseña incorrectos.");
            return;
        }
        Sesion.setRol(rol);
        irAlJuego();
    }

    private void irARegistro() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/minimart/Registro.fxml")
            );
            Parent raiz = loader.load();
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene escena = new Scene(raiz, 400, 400);
            escena.getStylesheets().add(
                    getClass().getResource("/com/minimart/styles.css").toExternalForm()
            );
            stage.setScene(escena);
        } catch (IOException ex) {
            labelError.setText("Error cargando el registro.");
        }
    }

    private void irAlJuego() {
        try {
            if (!Sesion.esInvitado()){
                com.minimart.App.verificarPartidaExistente();
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/minimart/MainWindow.fxml")
            );
            Parent raiz = loader.load();
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene escena = new Scene(raiz, 1080, 770);
            escena.getStylesheets().add(
                    getClass().getResource("/com/minimart/styles.css").toExternalForm()
            );
            stage.setScene(escena);
            stage.setResizable(true);
        } catch (IOException ex) {
            labelError.setText("Error cargando el juego.");
            ex.printStackTrace();
        }
    }
}