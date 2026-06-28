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
    private final Tienda         tienda;
    private final MainController controller;
    private       Timeline       timeline;

    private static final double PROBABILIDAD_SPAWN = 0.30;
    private static final Random RNG = new Random();

    public GameLoopService(Tienda tienda, MainController controller){
        this.tienda = tienda;
        this.controller = controller;
    }

    public void iniciar(){
        if(timeline != null && timeline.getStatus() == Animation.Status.RUNNING){
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
        if (timeline != null){
            timeline.play();
        }
    }

    private void tick(){
        spawnCliente();
        procesarDespacho();
        controller.actualizarVistas();
    }

    private void spawnCliente(){
        if (RNG.nextDouble() >= PROBABILIDAD_SPAWN){
            return;
        }

        List<Estanteria> conStock = tienda.getEstanterias().stream().filter(Estanteria::tieneStock).collect(Collectors.toList());

        if(conStock.isEmpty()){
            return;
        }

        Estanteria elegida = conStock.get(RNG.nextInt(conStock.size()));

        double precio = Math.round((RNG.nextDouble() * 5 + 1)* 100.0) /100.0;
        Cliente cliente = new Cliente(elegida.getTipoProducto(), precio);

        elegida.setStockActual(elegida.getStockActual() -1 );
        asignarCliente(cliente);
    }

    private void asignarCliente(Cliente cliente){
        List<Cajero> activos = tienda.getCajeros().stream().filter(Cajero::isActivo).collect(Collectors.toList());

        if (activos.isEmpty()){
            return;
        }

        Cajero elegido = Collections.min(activos, Comparator.comparingInt(Cajero::getTamañoCola));

        elegido.getColaClientes().add(cliente);

        if (elegido.getColaClientes().size() == 1){
            elegido.setSegundosRestantes(elegido.getTiempoDespacho());
        }
    }

    private void procesarDespacho(){
        for (Cajero c : tienda.getCajeros()) {
            if (!c.isActivo() || c.getColaClientes().isEmpty()) {
                continue;
            }
            c.setSegundosRestantes(c.getSegundosRestantes() -1);

            if (c.getSegundosRestantes() <= 0) {
                Cliente atendido = c.getColaClientes().poll();
                if (atendido != null) {
                   double nuevoDinero = tienda.getDineroActual() + atendido.getMontoGastado();
                   nuevoDinero = Math.round(nuevoDinero * 100.0) / 100.0;
                   tienda.setDineroActual(nuevoDinero);
                }

                if (!c.getColaClientes().isEmpty()) {
                    c.setSegundosRestantes(c.getTiempoDespacho());
                }
                else {
                    c.setSegundosRestantes(0);
                }
            }
        }
    }
}
