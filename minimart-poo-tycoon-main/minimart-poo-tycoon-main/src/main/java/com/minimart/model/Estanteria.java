package com.minimart.model;

import javafx.beans.property.SimpleIntegerProperty;

public class Estanteria {

    private int id;
    private int tiendaId;
    private String tipoProducto;

    private final SimpleIntegerProperty stockActual = new SimpleIntegerProperty(0);

    private int stockMaximo;
    private int posicionVisual;

    public Estanteria() {}

    public Estanteria(int id, int tiendaId, String tipoProducto,
                      int stockActual, int stockMaximo, int posicionVisual) {
        this.id             = id;
        this.tiendaId       = tiendaId;
        this.tipoProducto   = tipoProducto;
        this.stockActual.set(stockActual);
        this.stockMaximo    = stockMaximo;
        this.posicionVisual = posicionVisual;
    }

    public int    getId()              { return id; }
    public void   setId(int id)        { this.id = id; }

    public int    getTiendaId()              { return tiendaId; }
    public void   setTiendaId(int tiendaId)  { this.tiendaId = tiendaId; }

    public String getTipoProducto()                    { return tipoProducto; }
    public void   setTipoProducto(String tipoProducto) { this.tipoProducto = tipoProducto; }

    public int    getStockActual()                           { return stockActual.get(); }
    public void   setStockActual(int stock)                  { this.stockActual.set(stock); }
    public SimpleIntegerProperty stockActualProperty()       { return stockActual; }

    public int    getStockMaximo()               { return stockMaximo; }
    public void   setStockMaximo(int stockMaximo){ this.stockMaximo = stockMaximo; }

    public int    getPosicionVisual()                { return posicionVisual; }
    public void   setPosicionVisual(int posicion)    { this.posicionVisual = posicion; }

    public boolean tieneStock() {
        return getStockActual() > 0;
    }

    public double getPorcentajeStock() {
        if (stockMaximo == 0) return 0.0;
        return (double) getStockActual() / stockMaximo;
    }

    @Override
    public String toString() {
        return String.format("Estanteria{id=%d, tipo='%s', stock=%d/%d, pos=%d}",
            id, tipoProducto, getStockActual(), stockMaximo, posicionVisual);
    }
}