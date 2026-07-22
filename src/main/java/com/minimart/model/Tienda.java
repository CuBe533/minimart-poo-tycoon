package com.minimart.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

public class Tienda {

    private int id;
    private int usuarioId;

    private final SimpleStringProperty nombreTienda   = new SimpleStringProperty("");
    private final SimpleDoubleProperty  dineroActual  = new SimpleDoubleProperty(0.0);
    private final SimpleIntegerProperty diaActual     = new SimpleIntegerProperty(1);

    private List<Estanteria> estanterias = new ArrayList<>();
    private List<Cajero>     cajeros     = new ArrayList<>();

    public Tienda() {}

    public Tienda(int id, int usuarioId, String nombreTienda, double dineroActual, int diaActual) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nombreTienda.set(nombreTienda);
        this.dineroActual.set(dineroActual);
        this.diaActual.set(diaActual);
    }

    public int getId()          { return id; }
    public void setId(int id)   { this.id = id; }

    public int  getUsuarioId()              { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getNombreTienda()                        { return nombreTienda.get(); }
    public void   setNombreTienda(String nombre)           { this.nombreTienda.set(nombre); }
    public SimpleStringProperty nombreTiendaProperty()     { return nombreTienda; }

    public double getDineroActual()                        { return dineroActual.get(); }
    public void   setDineroActual(double dinero)           { this.dineroActual.set(dinero); }
    public SimpleDoubleProperty dineroActualProperty()     { return dineroActual; }

    public int  getDiaActual()                             { return diaActual.get(); }
    public void setDiaActual(int dia)                      { this.diaActual.set(dia); }
    public SimpleIntegerProperty diaActualProperty()       { return diaActual; }

    public List<Estanteria> getEstanterias()                       { return estanterias; }
    public void setEstanterias(List<Estanteria> estanterias)       { this.estanterias = estanterias; }

    public List<Cajero> getCajeros()                 { return cajeros; }
    public void setCajeros(List<Cajero> cajeros)     { this.cajeros = cajeros; }

    @Override
    public String toString() {
        return String.format("Tienda{id=%d, nombre='%s', dinero=%.2f, dia=%d, estanterias=%d, cajeros=%d}",
                id, getNombreTienda(), getDineroActual(), getDiaActual(),
                estanterias.size(), cajeros.size());
    }
}