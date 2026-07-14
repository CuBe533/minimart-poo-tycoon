package com.minimart;

import com.minimart.dao.ConexionBD;
import com.minimart.dao.JuegoDAO;
import com.minimart.dao.TiendaDAO;
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

    @Override
    public void init() throws Exception {
        ConexionBD.getInstance().initDB();
    }

    @Override
    public void start(Stage stage) throws IOException {

        iniciarMusicaFondo();

        verificarPartidaExistente();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/minimart/MainWindow.fxml")
        );
        Parent raiz = loader.load();

        Scene escena = new Scene(raiz, 1080, 770);
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
        detenerMusicaFondo();
        ConexionBD.getInstance().cerrar();
    }

    private void verificarPartidaExistente() {
        try {
            TiendaDAO dao = new TiendaDAO();
            Tienda tienda = dao.cargarPartidaCompleta(1);

            if (tienda.getDiaActual() > 1) {
                Alert alerta = new Alert(
                        Alert.AlertType.CONFIRMATION,
                        "Tienes una partida guardada en el DÍA " + tienda.getDiaActual() +
                                " con $" + String.format("%.2f", tienda.getDineroActual()) + ".\n\n" +
                                "¿Deseas continuar esa partida o comenzar una nueva?",
                        ButtonType.YES, ButtonType.NO
                );
                alerta.setTitle("MiniMart POO Tycoon");
                alerta.setHeaderText("Partida encontrada — DÍA " + tienda.getDiaActual());

                ((Button) alerta.getDialogPane().lookupButton(ButtonType.YES)).setText("Continuar");
                ((Button) alerta.getDialogPane().lookupButton(ButtonType.NO)).setText("Nueva Partida");

                Optional<ButtonType> resultado = alerta.showAndWait();
                if (resultado.isPresent() && resultado.get() == ButtonType.NO) {
                    new JuegoDAO().resetearPartida();
                    System.out.println("[App] Nueva partida iniciada — datos reseteados.");
                } else {
                    System.out.println("[App] Continuando partida existente — DÍA " + tienda.getDiaActual());
                }
            } else {

                new JuegoDAO().resetearPartida();
                System.out.println("[App] Día 1 sin avanzar — se reinicia el estado.");
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
}