package com.minimart.controller;

import com.minimart.dao.JuegoDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class GameOverController {

    @FXML private Label  labelDiasSobrevividos;
    @FXML private Label  labelDineroMaximo;
    @FXML private Button btnNuevaPartida;

    private Stage stageGameOver;
    private Stage stagePrincipal;

    @FXML
    public void initialize() {
        btnNuevaPartida.setOnAction(e -> handleNuevaPartida());
    }

    public void setDatos(int diasSobrevividos, double dineroMaximo,
                         Stage stageGameOver, Stage stagePrincipal) {
        this.stageGameOver  = stageGameOver;
        this.stagePrincipal = stagePrincipal;

        labelDiasSobrevividos.setText(String.valueOf(diasSobrevividos));
        labelDineroMaximo.setText(String.format("$%.2f", dineroMaximo));
    }

    private void handleNuevaPartida() {
        try {
            new JuegoDAO().resetearPartida();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/minimart/MainWindow.fxml")
            );
            Parent raiz = loader.load();
            Scene escenaNueva = new Scene(raiz, 1024, 768);
            escenaNueva.getStylesheets().add(
                    getClass().getResource("/com/minimart/styles.css").toExternalForm()
            );

            stagePrincipal.setScene(escenaNueva);
            if (stageGameOver != null) {
                stageGameOver.close();
            }

            System.out.println("[GameOverController] Nueva partida iniciada tras Game Over.");
        } catch (IOException ex) {
            System.err.println("[GameOverController] Error recargando MainWindow.fxml: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}