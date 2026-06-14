package com.minimart;

import com.minimart.dao.ConexionBD;
import com.minimart.dao.TiendaDAO;
import com.minimart.model.Tienda;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void init() throws Exception {
        ConexionBD.getInstance().initDB();
        verificarCapaDatos(); // TODO: eliminar antes de Sprint 2
    }

    @Override
    public void start(Stage stage) {
        Label titulo = new Label("MiniMart POO Tycoon");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label subtitulo = new Label("Sprint 0 completado — Base de datos inicializada ✓");
        subtitulo.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        Label ruta = new Label("BD: " +
            java.nio.file.Paths.get(System.getProperty("user.home"), "minimart.db"));
        ruta.setStyle("-fx-font-size: 11px; -fx-text-fill: #999; -fx-font-family: monospace;");

        VBox contenido = new VBox(12, titulo, subtitulo, ruta);
        contenido.setAlignment(Pos.CENTER);

        Scene scene = new Scene(new StackPane(contenido), 1024, 768);
        scene.getRoot().setStyle("-fx-background-color: #f5f5f5;");

        stage.setTitle("MiniMart POO Tycoon");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        ConexionBD.getInstance().cerrar();
    }

    private void verificarCapaDatos() {
        System.out.println("══ VERIFICACIÓN SPRINT 1 ══════════════════════════════");
        try {
            TiendaDAO dao    = new TiendaDAO();
            Tienda    tienda = dao.cargarPartidaCompleta(1);

            System.out.println("Tienda:       " + tienda);
            System.out.printf ("Estanterías:  %d encontrada(s)%n", tienda.getEstanterias().size());
            tienda.getEstanterias().forEach(e -> System.out.println("  " + e));
            System.out.printf ("Cajeros:      %d encontrado(s)%n", tienda.getCajeros().size());
            tienda.getCajeros().forEach(c -> System.out.println("  " + c));

            double dineroOriginal = tienda.getDineroActual();
            tienda.setDineroActual(999.99);
            assert tienda.dineroActualProperty().get() == 999.99 : "SimpleDoubleProperty roto";
            tienda.setDineroActual(dineroOriginal);

            tienda.getEstanterias().get(0).setStockActual(3);
            assert tienda.getEstanterias().get(0).stockActualProperty().get() == 3
                : "SimpleIntegerProperty roto";

            System.out.println("✓ cargarPartidaCompleta() OK");
            System.out.println("✓ JavaFX Properties funcionales");
        } catch (Exception ex) {
            System.err.println("✗ ERROR en verificación Sprint 1: " + ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println("═══════════════════════════════════════════════════════");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
