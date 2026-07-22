package com.minimart.controller;

import com.minimart.model.Cajero;
import com.minimart.model.Cliente;
import com.minimart.model.Estanteria;
import com.minimart.model.Tienda;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GameLoopService {
    private final Tienda           tienda;
    private final MainController   controller;
    private final AnimacionService animacionService = new AnimacionService();
    private       Timeline         timeline;

    private static final double PROBABILIDAD_SPAWN = 0.80;
    private static final Random RNG = new Random();

    private int    ventasDelDia    = 0;
    private double dineroGanadoDia = 0.0;
    private int    clientesSpawnedHoy = 0;

    private double reputacion = 100.0;
    private static final int UMBRAL_DESPACHO_RAPIDO = 3;

    private double dineroMaximoAlcanzado;
    private boolean juegoTerminado = false;

    public GameLoopService(Tienda tienda, MainController controller){
        this.tienda = tienda;
        this.controller = controller;
        this.dineroMaximoAlcanzado = tienda.getDineroActual();
    }

    public void iniciar(){
        if (timeline != null && timeline.getStatus() == Animation.Status.RUNNING){
            return;
        }
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> tick())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void pausar(){
        if (timeline != null){
            timeline.pause();
        }
    }

    public void reanudar(){
        if (timeline != null && !juegoTerminado){
            timeline.play();
        }
    }

    private void tick(){
        if (juegoTerminado) return;

        spawnCliente();
        procesarDespacho();
        actualizarReputacion();
        controller.actualizarVistas();

        if ((tienda.getDineroActual() < 0 || reputacion <= 0) && !juegoTerminado) {
            juegoTerminado = true;
            pausar();
            controller.mostrarGameOver(tienda.getDiaActual(), dineroMaximoAlcanzado);
        }
    }

    private int getLimiteClientes() {
        return 10 + (tienda.getDiaActual() - 1) * 5;
    }

    private void spawnCliente(){
        if (clientesSpawnedHoy >= getLimiteClientes()) {
            return;
        }

        if (RNG.nextDouble() >= PROBABILIDAD_SPAWN){
            return;
        }

        boolean hayCajeroActivo = tienda.getCajeros().stream().anyMatch(Cajero::isActivo);
        if (!hayCajeroActivo) {
            return;
        }

        List<Estanteria> conStock = tienda.getEstanterias().stream()
                .filter(Estanteria::tieneStock)
                .collect(Collectors.toList());

        if (conStock.isEmpty()){
            return;
        }

        Estanteria elegida = conStock.get(RNG.nextInt(conStock.size()));

        double precio = PreciosConfig.getPrecio(elegida.getTipoProducto());
        Cliente cliente = new Cliente(elegida.getTipoProducto(), precio);

        elegida.setStockActual(elegida.getStockActual() - 1);
        clientesSpawnedHoy++;
        asignarCliente(cliente);
    }

    private void asignarCliente(Cliente cliente){
        List<Cajero> activos = tienda.getCajeros().stream()
                .filter(Cajero::isActivo)
                .collect(Collectors.toList());

        if (activos.isEmpty()){
            return;
        }

        Cajero elegido = Collections.min(activos, Comparator.comparingInt(Cajero::getTamañoCola));

        elegido.getColaClientes().add(cliente);

        if (elegido.getColaClientes().size() == 1){
            elegido.setSegundosRestantes(elegido.getTiempoDespacho());
            controller.notificarClienteEnCamino();
        }
    }

    private void procesarDespacho(){
        for (Cajero c : tienda.getCajeros()) {
            if (!c.isActivo() || c.getColaClientes().isEmpty()) {
                continue;
            }
            c.setSegundosRestantes(c.getSegundosRestantes() - 1);

            if (c.getSegundosRestantes() <= 0) {
                Cliente atendido = c.getColaClientes().poll();
                if (atendido != null) {
                    double nuevoDinero = tienda.getDineroActual() + atendido.getMontoGastado();
                    nuevoDinero = Math.round(nuevoDinero * 100.0) / 100.0;
                    tienda.setDineroActual(nuevoDinero);

                    if (nuevoDinero > dineroMaximoAlcanzado) {
                        dineroMaximoAlcanzado = nuevoDinero;
                    }

                    ventasDelDia++;
                    dineroGanadoDia = Math.round((dineroGanadoDia + atendido.getMontoGastado()) * 100.0) / 100.0;

                    animacionService.animarGanancia(controller.getLabelDinero());

                    if (c.getTiempoDespacho() <= UMBRAL_DESPACHO_RAPIDO) {
                        reputacion = Math.min(100.0, reputacion + 2.0);
                    }
                }

                if (!c.getColaClientes().isEmpty()) {
                    c.setSegundosRestantes(c.getTiempoDespacho());
                    controller.notificarSiguienteCliente();
                }
                else {
                    c.setSegundosRestantes(0);
                    controller.ocultarCliente();
                }
            }
        }
    }

    private void actualizarReputacion() {
        long estanteriasVacias = tienda.getEstanterias().stream()
                .filter(e -> !e.tieneStock())
                .count();

        if (estanteriasVacias > 0) {
            reputacion -= 0.5 * estanteriasVacias;
        }

        reputacion = Math.max(0.0, Math.min(100.0, reputacion));

        controller.actualizarLabelReputacion(reputacion);
    }

    public int getVentasDelDia() {
        return ventasDelDia;
    }

    public double getDineroGanadoDia() {
        return dineroGanadoDia;
    }

    public void reiniciarContadoresDia() {
        ventasDelDia = 0;
        dineroGanadoDia = 0.0;
        clientesSpawnedHoy = 0;
    }

    public void limpiarClienteEnCurso(){
        for(Cajero c: tienda.getCajeros()){
            c.getColaClientes().clear();
            c.setSegundosRestantes(0);
        }
    }

    public double getReputacion() {
        return reputacion;
    }
}