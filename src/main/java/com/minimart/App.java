package com.minimart;

import com.minimart.admin.AdminController;
import com.minimart.controller.Sesion;
import com.minimart.dao.ConexionBD;
import com.minimart.dao.JuegoDAO;
import com.minimart.dao.TiendaDAO;
import com.minimart.model.Cajero;
import com.minimart.model.Tienda;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class App extends Application {
    private MediaPlayer  mediaPlayer;
    private static Tienda tiendaEnJuego;

    @Override
    public void init() throws Exception {
        ConexionBD.getInstance().initDB();
    }

    @Override
    public void start(Stage stage) throws IOException {

        iniciarMusicaFondo();


        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/minimart/Login.fxml")
        );
        Parent raiz = loader.load();

        Scene escena = new Scene(raiz, 400, 400);
        escena.getStylesheets().add(
                getClass().getResource("/com/minimart/styles.css").toExternalForm()
        );

        stage.setTitle("MiniMart POO Tycoon");
        stage.setScene(escena);
        stage.setResizable(true);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        guardarPartidaEnCurso();
        detenerMusicaFondo();
        ConexionBD.getInstance().cerrar();
    }

    public static void setTiendaEnJuego(Tienda tienda) {
        tiendaEnJuego = tienda;
    }

    private static void guardarPartidaEnCurso() {
        if (tiendaEnJuego != null) {
            try {
                new JuegoDAO().guardarEstadoCompleto(tiendaEnJuego);
                System.out.println("[App] Partida auto-guardada al cerrar.");
            } catch (Exception e) {
                System.err.println("[App] Error auto-guardando: " + e.getMessage());
            }
        }
    }

    public static void verificarPartidaExistente() {
        try {
            TiendaDAO dao = new TiendaDAO();
            int usuarioId = Sesion.getUsuarioId();

            Tienda tienda = dao.cargarPartidaPorUsuario(usuarioId);

            boolean hayProgreso = tienda.getDiaActual() > 1
                    || tienda.getDineroActual() != 500.0
                    || !tienda.getEstanterias().isEmpty()
                    || tienda.getCajeros().stream().anyMatch(c -> c.isActivo() && c.getNivelMejora() > 1)
                    || tienda.getCajeros().stream().filter(Cajero::isActivo).count() > 1;

            if (hayProgreso) {
                Alert alerta = new Alert(
                        Alert.AlertType.CONFIRMATION,
                        "Tienes una partida guardada en el DIA " + tienda.getDiaActual() +
                                " con $" + String.format("%.2f", tienda.getDineroActual()) + ".\n\n" +
                                "Deseas continuar esa partida o comenzar una nueva?",
                        ButtonType.YES, ButtonType.NO
                );
                alerta.setTitle("MiniMart POO Tycoon");
                alerta.setHeaderText("Partida encontrada — DIA " + tienda.getDiaActual());

                ((Button) alerta.getDialogPane().lookupButton(ButtonType.YES)).setText("Continuar");
                ((Button) alerta.getDialogPane().lookupButton(ButtonType.NO)).setText("Nueva Partida");

                Optional<ButtonType> resultado = alerta.showAndWait();
                if (resultado.isPresent() && resultado.get() == ButtonType.NO) {
                    new JuegoDAO().resetearPartida(tienda.getId());
                    System.out.println("[App] Nueva partida iniciada — datos reseteados.");
                } else {
                    System.out.println("[App] Continuando partida existente — DIA " + tienda.getDiaActual());
                }
            } else {
                new JuegoDAO().resetearPartida(tienda.getId());
                System.out.println("[App] Sin progreso previo — se inicia estado limpio.");
            }
        } catch (RuntimeException ex) {
            System.err.println("[App] No se pudo verificar partida existente: " + ex.getMessage());
        }
    }

    private void iniciarMusicaFondo(){
        try{
            String ruta = getClass().getResource("/com/minimart/audio/melody.mp3").toString();

            Media media = new Media(ruta);
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            mediaPlayer.setVolume(0.10);

            mediaPlayer.play();

            System.out.println("[App] Musica de fondo iniciada");
        }catch (Exception e){
            System.out.println("[App] Error al cargar la musica"+e.getMessage());
        }
    }

    private void detenerMusicaFondo(){
        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.dispose();
            System.out.println("[App] Musica detenida");
        }
    }


    public static void main(String[] args) {
        launch(args);
    }

    public static void abrirPanelAdmin(Stage owner) {
        AdminController.abrirPanel(owner);
    }
}