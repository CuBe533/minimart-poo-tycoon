package com.minimart.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Cliente {

    private static final AtomicInteger CONTADOR_SESION = new AtomicInteger(0);

    private final int id;
    private String productoElegido;
    private double montoGastado;

    public Cliente() {
        this.id = CONTADOR_SESION.incrementAndGet();
    }

    public Cliente(String productoElegido, double montoGastado) {
        this.id              = CONTADOR_SESION.incrementAndGet();
        this.productoElegido = productoElegido;
        this.montoGastado    = montoGastado;
    }

    public int getId() { return id; }

    public String getProductoElegido()                     { return productoElegido; }
    public void   setProductoElegido(String productoElegido){ this.productoElegido = productoElegido; }

    public double getMontoGastado()                { return montoGastado; }
    public void   setMontoGastado(double monto)    { this.montoGastado = monto; }

    @Override
    public String toString() {
        return String.format("Cliente{id=%d, producto='%s', monto=$%.2f}",
            id, productoElegido, montoGastado);
    }
}