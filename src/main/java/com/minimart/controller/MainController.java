package com.minimart.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;



import com.minimart.dao.TiendaDAO;
import com.minimart.model.Cajero;
import com.minimart.model.Estanteria;
import com.minimart.model.Tienda;

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

    private Tienda tiendaActual;
    private GameLoopService gameLoopService;

    @FXML
    public void initialize() {
        System.out.println("[MainController] initialize() — Sprint 4: cargando partida e iniciando game loop...");
        try {
            cargarPartida();
            iniciarGameLoop();
            System.out.println("[MainController] Partida cargada, bindings y game loop listos.");
        }
        catch (RuntimeException ex){
            mostrarErrorDB(ex);
        }
    }

    private void cargarPartida(){
        TiendaDAO dao = new TiendaDAO();
        tiendaActual = dao.cargarPartidaCompleta(1);

        VBox[]        slotsEstanteria = { slotEstanteria1, slotEstanteria2, slotEstanteria3, slotEstanteria4, slotEstanteria5 };
        Label[]       labelsTipo      = { labelTipo1,      labelTipo2,      labelTipo3,      labelTipo4,      labelTipo5      };
        ProgressBar[] barrasStock     = { stockBar1,       stockBar2,       stockBar3,       stockBar4,       stockBar5       };
        VBox[]        slotsCajero     = { slotCajero1,     slotCajero2,     slotCajero3     };

        for (int i = 0; i < slotsEstanteria.length; i++){
            slotsEstanteria[i].setOpacity(0.3);
            barrasStock[i].setProgress(0.0);
            labelsTipo[i].setText("—");
        }

        for (Estanteria e: tiendaActual.getEstanterias()){
            int idx = e.getPosicionVisual() -1 ;
            if (idx < 0 || idx >= slotsEstanteria.length){
                System.out.println("[MainController] Estantería con posicionVisual invalida: "+e);
                continue;
            }
            slotsEstanteria[idx].setOpacity(1.0);
            labelsTipo[idx].setText(e.getTipoProducto());

            barrasStock[idx].progressProperty().bind(
              e.stockActualProperty().divide(e.getStockMaximo())
            );
        }

        for (int i = 0; i < tiendaActual.getCajeros().size() && i < slotsCajero.length; i++){
            Cajero c = tiendaActual.getCajeros().get(i);
            slotsCajero[i].setOpacity(c.isActivo() ? 1.0 : 0.3);
        }

        labelDinero.textProperty().bind(
                tiendaActual.dineroActualProperty().asString("$%.2f")
        );
        labelDia.textProperty().bind(
                tiendaActual.diaActualProperty().asString("DÍA: %d")
        );

        labelReputacion.setText("100");
    }

    private void iniciarGameLoop(){
        gameLoopService = new GameLoopService(tiendaActual, this);
        gameLoopService.iniciar();
        System.out.println("[MainController] Game loop iniciado — ticks cada 1s.");
    }

    public void actualizarVistas(){
        ProgressBar[] barrasAtencion = {atenderBar1, atenderBar2, atenderBar3};

        for (int i = 0; i<tiendaActual.getCajeros().size() && i < barrasAtencion.length; i++) {
            Cajero c = tiendaActual.getCajeros().get(i);
            if (c.isActivo() && c.getTamañoCola() > 0) {
                double progreso = (double) c.getSegundosRestantes() / c.getTiempoDespacho();
                barrasAtencion[i].setProgress(Math.max(0.0, Math.min(1.0, progreso)));
            }
            else {
                barrasAtencion[i].setProgress(0.0);
            }
        }
    }

    public Tienda getTiendaActual() {
        return tiendaActual;
    }

    private void mostrarErrorDB(Throwable causa){
        System.out.println("[MainController] ✗ Error cargando partida: " + causa.getMessage());
        causa.printStackTrace();

        Alert alerta = new Alert(
                Alert.AlertType.ERROR,
                "No se pudo cargar la partida desde la base de datos. \n\n"+
                   "Causa: "+  causa.getMessage() + "\n\n"+
                   "Verificar que ~/minimart.db existe y que initDB() se ejecutocorrectamente.\n"+
                        ButtonType.CLOSE
        );
        alerta.setTitle("Minimart — Error de carga");
        alerta.setHeaderText("Error al leer la base de datos");
        alerta.showAndWait();
    }
}
