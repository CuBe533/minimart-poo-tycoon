package com.minimart.admin;

public class PartidaDTO {

    private int tiendaId;
    private String usuarioNombre;
    private String nombreTienda;
    private double dineroActual;
    private int diaActual;
    private int totalEstanterias;
    private int cajerosActivos;

    public PartidaDTO() {}

    public PartidaDTO(int tiendaId, String usuarioNombre, String nombreTienda, double dineroActual,
                      int diaActual, int totalEstanterias, int cajerosActivos) {
        this.tiendaId        = tiendaId;
        this.usuarioNombre   = usuarioNombre;
        this.nombreTienda    = nombreTienda;
        this.dineroActual    = dineroActual;
        this.diaActual       = diaActual;
        this.totalEstanterias = totalEstanterias;
        this.cajerosActivos  = cajerosActivos;
    }

    public int    getTiendaId()          { return tiendaId; }
    public void   setTiendaId(int id)    { this.tiendaId = id; }

    public String getUsuarioNombre()                  { return usuarioNombre; }
    public void   setUsuarioNombre(String nombre)     { this.usuarioNombre = nombre; }

    public String getNombreTienda()                { return nombreTienda; }
    public void   setNombreTienda(String nombre)   { this.nombreTienda = nombre; }

    public double getDineroActual()                  { return dineroActual; }
    public void   setDineroActual(double dinero)     { this.dineroActual = dinero; }

    public int    getDiaActual()             { return diaActual; }
    public void   setDiaActual(int dia)      { this.diaActual = dia; }

    public int    getTotalEstanterias()               { return totalEstanterias; }
    public void   setTotalEstanterias(int total)      { this.totalEstanterias = total; }

    public int    getCajerosActivos()                { return cajerosActivos; }
    public void   setCajerosActivos(int activos)     { this.cajerosActivos = activos; }

    @Override
    public String toString() {
        return String.format("PartidaDTO{tiendaId=%d, nombre='%s', dinero=%.2f, dia=%d, estanterias=%d, cajerosActivos=%d}",
            tiendaId, nombreTienda, dineroActual, diaActual, totalEstanterias, cajerosActivos);
    }
}
