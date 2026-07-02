package com.minimart.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import com.minimart.dao.CajeroDAO;
import com.minimart.dao.EstanteriaDAO;
import com.minimart.dao.TiendaDAO;

import com.minimart.model.Cajero;
import com.minimart.model.Estanteria;
import com.minimart.model.Tienda;
import com.minimart.model.Cliente;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private static final String[] TIPOS_PRODUCTO = {
            "Snacks", "Bebidas", "Lácteos", "Dulces", "Conservas"
    };

    private static final double COSTO_ESTANTERIA  = 150.0;
    private static final double COSTO_REABASTECER =  50.0;
    private static final double COSTO_CAJERO      = 200.0;

    @FXML
    public void initialize() {
        System.out.println("[MainController] initialize() — Sprint 5: cargando partida, game loop y handlers...");
        try {
            cargarPartida();
            iniciarGameLoop();
            configurarHandlers();
            System.out.println("[MainController] Partida cargada, game loop y handlers listos.");
        }
        catch (RuntimeException ex){
            mostrarErrorDB(ex);
        }
    }

    private void configurarHandlers() {
        btnUpgrade1.setOnAction(e -> handleComprarEstanteria());
        btnUpgrade2.setOnAction(e -> handleReabastecer());
        btnUpgrade3.setOnAction(e -> handleMejorarCajero());
        actualizarEstadoBotones();
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

    private void handleComprarEstanteria() {
        if (tiendaActual.getEstanterias().size() >= 5) {
            mostrarInfo("Límite alcanzado", "Ya tienes el máximo de 5 estanterías.");
            return;
        }

        if (!tieneFondos(COSTO_ESTANTERIA)) return;

        int nuevaPosicion = tiendaActual.getEstanterias().size() + 1;
        String tipo = TIPOS_PRODUCTO[tiendaActual.getEstanterias().size()];

        Estanteria estanteria = new Estanteria(0, 1, tipo, 10, 10, nuevaPosicion);

        new EstanteriaDAO().save(estanteria);
        tiendaActual.getEstanterias().add(estanteria);
        descontarDinero(COSTO_ESTANTERIA);

        // Actualizar el slot visual correspondiente
        VBox[] slots = { slotEstanteria1, slotEstanteria2, slotEstanteria3, slotEstanteria4, slotEstanteria5 };
        Label[] labels = { labelTipo1, labelTipo2, labelTipo3, labelTipo4, labelTipo5 };
        ProgressBar[] barras = { stockBar1, stockBar2, stockBar3, stockBar4, stockBar5 };

        int idx = nuevaPosicion - 1;
        slots[idx].setOpacity(1.0);
        labels[idx].setText(tipo);
        barras[idx].progressProperty().bind(
                estanteria.stockActualProperty().divide(estanteria.getStockMaximo())
        );

        actualizarEstadoBotones();
        System.out.println("[MainController] Estantería comprada: " + estanteria);
    }

    private void handleReabastecer() {
        List<Estanteria> rellenables = tiendaActual.getEstanterias().stream()
                .filter(e -> e.getStockActual() < e.getStockMaximo())
                .collect(Collectors.toList());

        if (rellenables.isEmpty()) {
            mostrarInfo("Sin stock bajo", "Todas las estanterías están llenas.");
            return;
        }

        ChoiceDialog<String> dialogo = new ChoiceDialog<>(
                rellenables.get(0).getTipoProducto(),
                rellenables.stream().map(Estanteria::getTipoProducto).collect(Collectors.toList())
        );
        dialogo.setTitle("Reabastecer Estantería");
        dialogo.setHeaderText("Elige la estantería a reabastecer");
        dialogo.setContentText("Costo: $50.00");

        Optional<String> resultado = dialogo.showAndWait();
        if (resultado.isEmpty()) return;

        if (!tieneFondos(COSTO_REABASTECER)) return;

        Estanteria elegida = rellenables.stream()
                .filter(e -> e.getTipoProducto().equals(resultado.get()))
                .findFirst()
                .orElse(null);
        if (elegida == null) return;

        elegida.setStockActual(elegida.getStockMaximo());
        new EstanteriaDAO().update(elegida);
        descontarDinero(COSTO_REABASTECER);

        System.out.println("[MainController] Estantería reabastecida: " + elegida.getTipoProducto());
    }

    private void handleMejorarCajero() {
        List<Cajero> inactivos = tiendaActual.getCajeros().stream()
                .filter(c -> !c.isActivo())
                .collect(Collectors.toList());

        if (!inactivos.isEmpty()) {
            // Contratar el primer cajero inactivo
            if (!tieneFondos(COSTO_CAJERO)) return;

            Cajero cajero = inactivos.get(0);
            cajero.setActivo(true);
            new CajeroDAO().update(cajero);
            descontarDinero(COSTO_CAJERO);

            // Actualizar slot visual
            VBox[] slots = { slotCajero1, slotCajero2, slotCajero3 };
            int idx = tiendaActual.getCajeros().indexOf(cajero);
            if (idx >= 0 && idx < slots.length) {
                slots[idx].setOpacity(1.0);
            }

            System.out.println("[MainController] Cajero contratado: " + cajero);
        } else {
            // Mejorar el cajero activo con menor nivel
            Cajero peor = tiendaActual.getCajeros().stream()
                    .filter(Cajero::isActivo)
                    .min((a, b) -> Integer.compare(a.getNivelMejora(), b.getNivelMejora()))
                    .orElse(null);

            if (peor == null) {
                mostrarInfo("Sin cajeros", "No hay cajeros disponibles para mejorar.");
                return;
            }

            if (!tieneFondos(COSTO_CAJERO)) return;

            peor.setNivelMejora(peor.getNivelMejora() + 1);
            int nuevoTiempo = Math.max(1, peor.getTiempoDespacho() - 2);
            peor.setTiempoDespacho(nuevoTiempo);
            new CajeroDAO().update(peor);
            descontarDinero(COSTO_CAJERO);

            System.out.println("[MainController] Cajero mejorado: " + peor);
        }

        actualizarEstadoBotones();
    }

    private void actualizarEstadoBotones() {
        btnUpgrade1.setDisable(tiendaActual.getEstanterias().size() >= 5);
    }

    private boolean tieneFondos(double costo) {
        if (tiendaActual.getDineroActual() < costo) {
            Alert alerta = new Alert(
                    Alert.AlertType.WARNING,
                    "Necesitas $" + String.format("%.2f", costo) +
                            " para esta operación.\n\nDinero actual: $" +
                            String.format("%.2f", tiendaActual.getDineroActual()),
                    ButtonType.CLOSE
            );
            alerta.setTitle("MiniMart — Fondos insuficientes");
            alerta.setHeaderText("No tienes suficiente dinero");
            alerta.showAndWait();
            return false;
        }
        return true;
    }

    private void descontarDinero(double monto) {
        double nuevo = Math.round((tiendaActual.getDineroActual() - monto) * 100.0) / 100.0;
        tiendaActual.setDineroActual(nuevo);
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.CLOSE);
        alerta.setTitle("MiniMart — " + titulo);
        alerta.setHeaderText(titulo);
        alerta.showAndWait();
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
                   "La ventana se mostrará con datos por defecto.",
                   ButtonType.CLOSE
        );
        alerta.setTitle("Minimart — Error de carga");
        alerta.setHeaderText("Error al leer la base de datos");
        alerta.showAndWait();
    }
}
