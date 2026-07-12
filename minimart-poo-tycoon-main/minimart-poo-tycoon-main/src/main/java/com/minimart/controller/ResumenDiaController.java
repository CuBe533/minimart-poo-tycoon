package com.minimart.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ResumenDiaController {

    @FXML private Label  labelTituloDia;
    @FXML private Label  labelVentas;
    @FXML private Label  labelGanancia;
    @FXML private Label  labelDineroTotal;
    @FXML private Button btnSiguienteDia;

    private MainController mainController;
    private Stage stage;

    @FXML
    public void initialize() {
        btnSiguienteDia.setOnAction(e -> cerrarYReanudar());
    }


    public void setDatos(int diaQueTermino, int ventas, double ganancia, double dineroTotal,
                         MainController mainController, Stage stage) {
        this.mainController = mainController;
        this.stage = stage;

        labelTituloDia.setText("RESUMEN DEL DÍA " + diaQueTermino);
        labelVentas.setText(String.valueOf(ventas));
        labelGanancia.setText(String.format("$%.2f", ganancia));
        labelDineroTotal.setText(String.format("$%.2f", dineroTotal));
    }

    private void cerrarYReanudar() {
        if (stage != null) {
            stage.close();
        }
        if (mainController != null) {
            mainController.reanudarJuegoTrasResumen();
        }
    }
}