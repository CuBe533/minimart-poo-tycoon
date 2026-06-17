package com.minimart.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML private VBox         slotEstanteria1;
    @FXML private VBox         slotEstanteria2;
    @FXML private VBox         slotEstanteria3;
    @FXML private VBox         slotEstanteria4;
    @FXML private VBox         slotEstanteria5;

    @FXML private ImageView    imgEstanteria1;
    @FXML private ImageView    imgEstanteria2;
    @FXML private ImageView    imgEstanteria3;
    @FXML private ImageView    imgEstanteria4;
    @FXML private ImageView    imgEstanteria5;

    @FXML private ProgressBar  stockBar1;
    @FXML private ProgressBar  stockBar2;
    @FXML private ProgressBar  stockBar3;
    @FXML private ProgressBar  stockBar4;
    @FXML private ProgressBar  stockBar5;

    @FXML private Label        labelTipo1;
    @FXML private Label        labelTipo2;
    @FXML private Label        labelTipo3;
    @FXML private Label        labelTipo4;
    @FXML private Label        labelTipo5;

    @FXML private VBox         slotCajero1;
    @FXML private VBox         slotCajero2;
    @FXML private VBox         slotCajero3;

    @FXML private ImageView    imgCajero1;
    @FXML private ImageView    imgCajero2;
    @FXML private ImageView    imgCajero3;

    @FXML private ProgressBar  atenderBar1;
    @FXML private ProgressBar  atenderBar2;
    @FXML private ProgressBar  atenderBar3;

    @FXML private ImageView    comprador1;
    @FXML private ImageView    comprador2;
    @FXML private ImageView    comprador3;
    @FXML private ImageView    comprador4;

    @FXML private Button       btnUpgrade1;
    @FXML private Button       btnUpgrade2;
    @FXML private Button       btnUpgrade3;

    @FXML private Label        labelDinero;
    @FXML private Label        labelReputacion;
    @FXML private Label        labelDia;
    @FXML private Button       btnAvanzarDia;

    @FXML
    public void initialize() {
        System.out.println("[MainController] initialize() — Sprint 2: UI cargada correctamente.");
    }
}
