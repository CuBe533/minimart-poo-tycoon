package com.minimart.controller;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.minimart.dao.CajeroDAO;
import com.minimart.dao.EstanteriaDAO;
import com.minimart.dao.JuegoDAO;
import com.minimart.dao.TiendaDAO;

import com.minimart.model.Cajero;
import com.minimart.model.Estanteria;
import com.minimart.model.Tienda;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

    // ─── IMAGEN DEL CAJERO EN EL PANEL DERECHO (ZONA GRIS) ───
    @FXML private ImageView    imgCajeroPanelDerecho;

    @FXML private Button       btnUpgrade1;
    @FXML private Button       btnUpgrade2;
    @FXML private Button       btnUpgrade3;

    @FXML private ImageView    imgUpgradeEstanteria;
    @FXML private ImageView    imgUpgradeReabastecer;
    @FXML private ImageView    imgUpgradeCajero;

    @FXML private Label        labelDinero;
    @FXML private Label        labelReputacion;
    @FXML private Label        labelDia;
    @FXML private Button       btnAvanzarDia;
    @FXML private ImageView imgMostradorPanel;
    @FXML private ImageView imgClientePanel;


    private Tienda tiendaActual;
    private GameLoopService gameLoopService;

    private static final String[] TIPOS_PRODUCTO = {
            "Snacks", "Bebidas", "Lácteos", "Dulces", "Conservas"
    };

    private static final double COSTO_ESTANTERIA  = 150.0;
    private static final double COSTO_REABASTECER =  50.0;
    private static final double COSTO_CAJERO      = 200.0;

    private static final double UMBRAL_STOCK_CRITICO = 0.3;
    private static final String ESTILO_STOCK_CRITICO = "-fx-accent: #E24B4A;";

    // ─── SPRITES ──────────────────────────────────────────────────────────────
    private final Map<String, Image> spritesProducto = Map.of(
            "Snacks",    cargarSprite("producto_snacks.gif"),
            "Bebidas",   cargarSprite("producto_bebidas.gif"),
            "Lácteos",   cargarSprite("producto_lacteos.gif"),
            "Dulces",    cargarSprite("producto_dulces.gif"),
            "Conservas", cargarSprite("producto_conservas.gif")
    );

    private final Image spriteCajero = cargarSprite("cajero.gif");
    private final Image spriteJugador = cargarSprite("cliente.gif");
    private final Image spriteCliente = cargarSprite("ClienteEstatico1.gif");
    private final Image spriteUpgradeEstanteria  = cargarSprite("upgrade_estanteria.gif");
    private final Image spriteUpgradeReabastecer = cargarSprite("upgrade_reabastecer.gif");
    private final Image spriteUpgradeCajero      = cargarSprite("MejoraCajero.gif");

    private final Image spriteClienteCaminando= cargarSprite("clienteCaminando1.gif");
    private final Image spriteCliienteEstatico= cargarSprite("ClienteEstatico1.gif");
    private final Image spriteMostrador1 = cargarSprite("Mostrador.gif");
    private final Image spriteMostrador2 = cargarSprite("Mostrador (2).gif");
    private final Image spriteMostrador3 = cargarSprite("Mostrador (3).gif");


    private static Image cargarSprite(String nombreArchivo) {
        String ruta = "/com/minimart/imagenes/" + nombreArchivo;
        try (java.io.InputStream in = MainController.class.getResourceAsStream(ruta)) {
            if (in == null) {
                System.err.println("[MainController] ⚠ Sprite no encontrado (se omite): " + ruta);
                return null;
            }
            return new Image(in);
        } catch (Exception ex) {
            System.err.println("[MainController] ⚠ Error cargando sprite " + ruta + ": " + ex.getMessage());
            return null;
        }
    }

    @FXML
    public void initialize() {
        System.out.println("[MainController] initialize() — Cargando partida...");
        try {
            cargarPartida();
            iniciarGameLoop();
            configurarHandlers();

            // Asignar imágenes estáticas
            asignarImagen(imgCajero1, spriteCajero);
            asignarImagen(imgCajero2, spriteCajero);
            asignarImagen(imgCajero3, spriteCajero);

            asignarImagen(imgUpgradeEstanteria, spriteUpgradeEstanteria);
            asignarImagen(imgUpgradeReabastecer, spriteUpgradeReabastecer);
            asignarImagen(imgUpgradeCajero, spriteUpgradeCajero);
            asignarImagen(imgCajeroPanelDerecho, spriteJugador);

            asignarImagen(comprador1, spriteCliente);
            asignarImagen(comprador2, spriteCliente);
            asignarImagen(comprador3, spriteCliente);
            asignarImagen(comprador4, spriteCliente);

            asignarImagen(imgMostradorPanel, cargarSprite("Mostrador.gif"));


            System.out.println("[MainController] Inicialización completa.");
        }
        catch (RuntimeException ex){
            mostrarErrorDB(ex);
        }
    }

    private static void asignarImagen(ImageView view, Image img) {
        if (view != null) view.setImage(img);
    }

    private void configurarHandlers() {
        btnUpgrade1.setOnAction(e -> handleComprarEstanteria());
        btnUpgrade2.setOnAction(e -> handleReabastecer());
        btnUpgrade3.setOnAction(e -> handleMejorarCajero());
        btnAvanzarDia.setOnAction(e -> handleAvanzarDia());
        actualizarEstadoBotones();
    }

    private void cargarPartida(){
        TiendaDAO dao = new TiendaDAO();
        tiendaActual = dao.cargarPartidaCompleta(1);

        List<Cajero> cajerosOrdenados = tiendaActual.getCajeros().stream()
                .sorted(Comparator.comparingInt(Cajero::getId))
                .collect(Collectors.toList());
        tiendaActual.setCajeros(cajerosOrdenados);

        VBox[]        slotsEstanteria    = { slotEstanteria1, slotEstanteria2, slotEstanteria3, slotEstanteria4, slotEstanteria5 };
        Label[]       labelsTipo         = { labelTipo1,      labelTipo2,      labelTipo3,      labelTipo4,      labelTipo5      };
        ProgressBar[] barrasStock        = { stockBar1,       stockBar2,       stockBar3,       stockBar4,       stockBar5       };
        ImageView[]   imagenesEstanteria = { imgEstanteria1,  imgEstanteria2,  imgEstanteria3,  imgEstanteria4,  imgEstanteria5  };
        VBox[]        slotsCajero        = { slotCajero1,     slotCajero2,     slotCajero3     };

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
            imagenesEstanteria[idx].setImage(spritesProducto.get(e.getTipoProducto()));

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
        btnAvanzarDia.textProperty().bind(
                tiendaActual.diaActualProperty().asString("DIA %d →")
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

        ProgressBar[] barrasStock = { stockBar1, stockBar2, stockBar3, stockBar4, stockBar5 };
        for (ProgressBar barra : barrasStock) {
            if (barra.getProgress() >= 0.0 && barra.getProgress() < UMBRAL_STOCK_CRITICO) {
                barra.setStyle(ESTILO_STOCK_CRITICO);
            } else {
                barra.setStyle(null);
            }
        }
    }

    public void actualizarLabelReputacion(double reputacion) {
        labelReputacion.setText(String.format("%.0f", reputacion));
    }

    public Label getLabelDinero() {
        return labelDinero;
    }

    public void mostrarGameOver(int diaActual, double dineroMaximoAlcanzado) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/minimart/GameOver.fxml")
            );
            Parent raiz = loader.load();
            GameOverController gameOverController = loader.getController();

            Stage stagePrincipal = (Stage) btnAvanzarDia.getScene().getWindow();

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle("MiniMart — Game Over");
            modal.setResizable(false);
            modal.setScene(new Scene(raiz));

            gameOverController.setDatos(
                    diaActual, dineroMaximoAlcanzado,
                    modal, stagePrincipal
            );

            modal.show();

        } catch (IOException ex) {
            System.err.println("[MainController] Error cargando GameOver.fxml: " + ex.getMessage());
            ex.printStackTrace();
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

        VBox[] slots = { slotEstanteria1, slotEstanteria2, slotEstanteria3, slotEstanteria4, slotEstanteria5 };
        Label[] labels = { labelTipo1, labelTipo2, labelTipo3, labelTipo4, labelTipo5 };
        ProgressBar[] barras = { stockBar1, stockBar2, stockBar3, stockBar4, stockBar5 };
        ImageView[] imagenes = { imgEstanteria1, imgEstanteria2, imgEstanteria3, imgEstanteria4, imgEstanteria5 };

        int idx = nuevaPosicion - 1;
        slots[idx].setOpacity(1.0);
        labels[idx].setText(tipo);
        imagenes[idx].setImage(spritesProducto.get(tipo));
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
            if (!tieneFondos(COSTO_CAJERO)) return;

            Cajero cajero = inactivos.get(0);
            cajero.setActivo(true);
            new CajeroDAO().update(cajero);
            descontarDinero(COSTO_CAJERO);

            // Buscar primer slot inactivo y encenderlo
            VBox[] slots = { slotCajero1, slotCajero2, slotCajero3 };
            boolean slotIluminado = false;
            for (int i = 0; i < slots.length; i++) {
                if (slots[i].getOpacity() == 0.3) {
                    slots[i].setOpacity(1.0);
                    System.out.println("Slot " + (i+1) + " iluminado.");
                    slotIluminado = true;
                    break;
                }
            }
            if (!slotIluminado) {
                System.out.println("No hay slots apagados.");
            }

            System.out.println("[MainController] Cajero contratado: " + cajero);
        } else {
            // Mejorar cajero existente (código actual sin cambios)
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

    private void handleAvanzarDia() {
        gameLoopService.pausar();

        int diaQueTermina = tiendaActual.getDiaActual();
        int ventas = gameLoopService.getVentasDelDia();
        double ganancia = gameLoopService.getDineroGanadoDia();

        try {
            new JuegoDAO().guardarEstadoCompleto(tiendaActual);
            tiendaActual.setDiaActual(diaQueTermina + 1);
            gameLoopService.reiniciarContadoresDia();
            mostrarResumenDia(diaQueTermina, ventas, ganancia);

        } catch (RuntimeException ex) {
            System.err.println("[MainController] Error guardando estado del día: " + ex.getMessage());
            ex.printStackTrace();
            mostrarInfo("Error de guardado",
                    "No se pudo guardar el progreso del día en la base de datos.\n" +
                            "El juego continuará, pero verifica tu conexión a la BD.");
            gameLoopService.reanudar();
        }
    }

    private void mostrarResumenDia(int diaQueTermina, int ventas, double ganancia) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/minimart/ResumenDia.fxml")
            );
            Parent raiz = loader.load();
            ResumenDiaController resumenController = loader.getController();

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle("MiniMart — Resumen del Día");
            modal.setResizable(false);
            modal.setScene(new Scene(raiz));

            resumenController.setDatos(
                    diaQueTermina, ventas, ganancia,
                    tiendaActual.getDineroActual(),
                    this, modal
            );

            modal.showAndWait();

        } catch (IOException ex) {
            System.err.println("[MainController] Error cargando ResumenDia.fxml: " + ex.getMessage());
            ex.printStackTrace();
            gameLoopService.reanudar();
        }
    }

    public void reanudarJuegoTrasResumen() {
        gameLoopService.reanudar();
        System.out.println("[MainController] Game loop reanudado tras resumen del día.");
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

    private void animarClienteLlegando(){
        if (imgClientePanel.getOpacity()>0) return;
        if (spriteClienteCaminando==null) return;

        imgClientePanel.setImage(spriteClienteCaminando);
        imgClientePanel.setOpacity(1.0);

        Timeline temporizador = new Timeline(new KeyFrame(Duration.seconds(2), e->{
            if (spriteCliienteEstatico!=null){
                imgClientePanel.setImage(spriteCliienteEstatico);
            }
        }));
        temporizador.setCycleCount(1);
        temporizador.play();
    }

    private boolean clienteEnCamino= false;
    private boolean clienteLlegado=false;




    public void notificarClienteEnCamino() {
        if (clienteEnCamino || imgClientePanel.getOpacity()>0){
            System.out.println("Cliente ya en camino, ignorando.");
            return;
        }
        System.out.println("Cliente en camino...");
        clienteEnCamino= true;
        clienteLlegado=false;

        imgClientePanel.setImage(spriteClienteCaminando);
        imgClientePanel.setOpacity(1.0);


        Timeline llegada=new Timeline(new KeyFrame(Duration.seconds(2.0), e->{
            System.out.println("Cliente llego al mostrador");
            clienteLlegado=true;
            clienteEnCamino=false;

            if (spriteCliienteEstatico !=null){
                imgClientePanel.setImage(spriteCliienteEstatico);
            }

            imgMostradorPanel.setImage(spriteMostrador2);

            Timeline atencion=new Timeline(new KeyFrame(Duration.seconds(1.5), ev->{
                System.out.println("Cambiando a mostrador 3.");
                imgMostradorPanel.setImage(spriteMostrador3);
            }));
            atencion.setCycleCount(1);
            atencion.play();
        }));
        llegada.setCycleCount(1);
        llegada.play();

    }
    public void ocultarCliente(){

        if (clienteEnCamino){
            System.out.println("Cliente aun en camino, no se oculta");
            return;
        }


        imgClientePanel.setImage(null);
        imgClientePanel.setOpacity(0);
        imgMostradorPanel.setImage(spriteMostrador1);
        clienteLlegado=false;

    }

}


