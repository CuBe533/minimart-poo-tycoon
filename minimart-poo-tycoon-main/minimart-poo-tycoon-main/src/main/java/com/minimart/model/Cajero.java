package com.minimart.model;

import java.util.LinkedList;
import java.util.Queue;

public class Cajero {

    private int     id;
    private int     tiendaId;
    private int     nivelMejora;
    private int     tiempoDespacho;
    private boolean activo;

    private Queue<Cliente> colaClientes   = new LinkedList<>();
    private int            segundosRestantes = 0;

    public Cajero() {}

    public Cajero(int id, int tiendaId, int nivelMejora, int tiempoDespacho, boolean activo) {
        this.id             = id;
        this.tiendaId       = tiendaId;
        this.nivelMejora    = nivelMejora;
        this.tiempoDespacho = tiempoDespacho;
        this.activo         = activo;
        this.colaClientes   = new LinkedList<>();
        this.segundosRestantes = 0;
    }

    public int     getId()                { return id; }
    public void    setId(int id)          { this.id = id; }

    public int     getTiendaId()                { return tiendaId; }
    public void    setTiendaId(int tiendaId)    { this.tiendaId = tiendaId; }

    public int     getNivelMejora()                 { return nivelMejora; }
    public void    setNivelMejora(int nivel)         { this.nivelMejora = nivel; }

    public int     getTiempoDespacho()               { return tiempoDespacho; }
    public void    setTiempoDespacho(int tiempo)     { this.tiempoDespacho = tiempo; }

    public boolean isActivo()               { return activo; }
    public void    setActivo(boolean activo){ this.activo = activo; }

    public Queue<Cliente> getColaClientes()          { return colaClientes; }

    public int  getSegundosRestantes()               { return segundosRestantes; }
    public void setSegundosRestantes(int segundos)   { this.segundosRestantes = segundos; }

    public int getTamañoCola() { return colaClientes.size(); }

    public boolean estaOcupado() { return !colaClientes.isEmpty(); }

    @Override
    public String toString() {
        return String.format("Cajero{id=%d, nivel=%d, tiempo=%ds, activo=%b, cola=%d}",
            id, nivelMejora, tiempoDespacho, activo, colaClientes.size());
    }
}