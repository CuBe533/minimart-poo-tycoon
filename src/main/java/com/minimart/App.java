package com.minimart;

import com.minimart.dao.ConexionBD;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void init() throws Exception {
        ConexionBD.getInstance().initDB();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/minimart/MainWindow.fxml")
        );
        Parent raiz = loader.load();

        Scene escena = new Scene(raiz, 1024, 768);
        escena.getStylesheets().add(
            getClass().getResource("/com/minimart/styles.css").toExternalForm()
        );

        stage.setTitle("MiniMart POO Tycoon");
        stage.setScene(escena);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        ConexionBD.getInstance().cerrar();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
