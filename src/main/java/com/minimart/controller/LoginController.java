package com.minimart.controller;

import com.minimart.App;
import com.minimart.dao.UsuarioDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField     txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Button        btnLogin;
    @FXML private Button        btnGuest;
    @FXML private Button        btnRegistro;
    @FXML private Label         labelError;

    @FXML
    public void initialize() {
        btnLogin.setOnAction(e -> handleLogin());
        btnGuest.setOnAction(e -> handleGuest());
        btnRegistro.setOnAction(e -> irARegistro());
    }

    private void handleLogin() {
        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        if (usuario.isBlank() || password.isBlank()) {
            labelError.setText("Completa usuario y contrasena.");
            return;
        }

        String[] datos = new UsuarioDAO().validarLogin(usuario, password);

        if (datos == null) {
            labelError.setText("Usuario o contrasena incorrectos.");
            return;
        }
        Sesion.setUsuarioId(Integer.parseInt(datos[0]));
        Sesion.setRol(datos[1]);
        Sesion.setNombreUsuario(usuario);
        irAlJuego();
    }

    private void handleGuest() {
        UsuarioDAO dao = new UsuarioDAO();
        String nombreGuest = "guest";
        if (!dao.existeUsuario(nombreGuest)) {
            dao.registrar(nombreGuest, "none", "invitado");
        }
        String[] datos = dao.validarLogin(nombreGuest, "none");
        Sesion.setUsuarioId(Integer.parseInt(datos[0]));
        Sesion.setRol("invitado");
        Sesion.setNombreUsuario(nombreGuest);
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
            Stage stage = (Stage) btnLogin.getScene().getWindow();

            if ("admin".equals(Sesion.getRol())) {
                App.abrirPanelAdmin(stage);
                return;
            }

            if (!Sesion.esInvitado()){
                App.verificarPartidaExistente();
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/minimart/MainWindow.fxml")
            );
            Parent raiz = loader.load();
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
