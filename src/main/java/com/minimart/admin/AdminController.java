package com.minimart.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class AdminController {

    @FXML private TableView<PartidaDTO> tablaPartidas;

    @FXML private TableColumn<PartidaDTO, Integer> colId;
    @FXML private TableColumn<PartidaDTO, String>  colUsuario;
    @FXML private TableColumn<PartidaDTO, String>  colNombre;
    @FXML private TableColumn<PartidaDTO, Integer> colDia;
    @FXML private TableColumn<PartidaDTO, Double>  colDinero;
    @FXML private TableColumn<PartidaDTO, Integer> colEstanterias;
    @FXML private TableColumn<PartidaDTO, Integer> colCajeros;

    @FXML private Label labelUsuario;

    private AdminDAO adminDAO;

    @FXML
    public void initialize() {
        adminDAO = new AdminDAO();
        configurarTabla();
        cargarPartidas();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("tiendaId"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuarioNombre"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreTienda"));
        colDia.setCellValueFactory(new PropertyValueFactory<>("diaActual"));
        colDinero.setCellValueFactory(new PropertyValueFactory<>("dineroActual"));
        colEstanterias.setCellValueFactory(new PropertyValueFactory<>("totalEstanterias"));
        colCajeros.setCellValueFactory(new PropertyValueFactory<>("cajerosActivos"));

        colDinero.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });
    }

    private void cargarPartidas() {
        tablaPartidas.setItems(FXCollections.observableArrayList(adminDAO.listarPartidas()));
    }

    @FXML
    private void handleReiniciar() {
        PartidaDTO seleccion = tablaPartidas.getSelectionModel().getSelectedItem();
        if (seleccion == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione una partida",
                    "Debe seleccionar una partida de la tabla para reiniciarla.");
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Se reiniciara la partida \"" + seleccion.getNombreTienda() + "\" " +
                        "(Dia " + seleccion.getDiaActual() + ", $" +
                        String.format("%.2f", seleccion.getDineroActual()) + ")\n\n" +
                        "Se perdera todo el progreso y se reseteara al estado inicial (Dia 1, $500.00).",
                ButtonType.YES, ButtonType.NO
        );
        confirmacion.setTitle("MiniMart Admin — Reiniciar Partida");
        confirmacion.setHeaderText("Confirmar reinicio");
        ((Button) confirmacion.getDialogPane().lookupButton(ButtonType.YES)).setText("Reiniciar");
        ((Button) confirmacion.getDialogPane().lookupButton(ButtonType.NO)).setText("Cancelar");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.YES) {
            adminDAO.reiniciarPartida(seleccion.getTiendaId());
            cargarPartidas();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Partida reiniciada",
                    "La partida \"" + seleccion.getNombreTienda() + "\" fue reiniciada correctamente.");
        }
    }

    @FXML
    private void handleEliminar() {
        PartidaDTO seleccion = tablaPartidas.getSelectionModel().getSelectedItem();
        if (seleccion == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione una partida",
                    "Debe seleccionar una partida de la tabla para eliminarla.");
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.WARNING,
                "ELIMINAR permanentemente la partida \"" + seleccion.getNombreTienda() + "\" " +
                        "(Dia " + seleccion.getDiaActual() + ", $" +
                        String.format("%.2f", seleccion.getDineroActual()) + ")?\n\n" +
                        "Esta accion NO se puede deshacer. Se eliminaran todos los datos asociados.",
                ButtonType.YES, ButtonType.NO
        );
        confirmacion.setTitle("MiniMart Admin — Eliminar Partida");
        confirmacion.setHeaderText("Confirmar eliminacion permanente");
        ((Button) confirmacion.getDialogPane().lookupButton(ButtonType.YES)).setText("Eliminar");
        ((Button) confirmacion.getDialogPane().lookupButton(ButtonType.NO)).setText("Cancelar");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.YES) {
            adminDAO.eliminarPartida(seleccion.getTiendaId());
            cargarPartidas();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Partida eliminada",
                    "La partida \"" + seleccion.getNombreTienda() + "\" fue eliminada permanentemente.");
        }
    }

    public void setNombreUsuario(String nombre) {
        if (labelUsuario != null) {
            labelUsuario.setText("Usuario: " + nombre);
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.CLOSE);
        alerta.setTitle("MiniMart Admin — " + titulo);
        alerta.setHeaderText(titulo);
        alerta.showAndWait();
    }

    public static void abrirPanel(Stage owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    AdminController.class.getResource("/com/minimart/AdminPanel.fxml")
            );
            Parent raiz = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(owner);
            stage.setTitle("MiniMart — Panel de Administracion");
            stage.setResizable(false);
            stage.setScene(new Scene(raiz));

            stage.show();

        } catch (IOException ex) {
            System.err.println("[AdminController] Error cargando AdminPanel.fxml: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
