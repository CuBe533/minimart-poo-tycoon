package com.minimart.controller;

import java.util.Map;

public final class PreciosConfig {

    private PreciosConfig() {}

    public static final Map<String, Double> PRECIOS = Map.of(
            "Snacks",    4.0,
            "Bebidas",   3.0,
            "Lácteos",   7.0,
            "Dulces",    3.2,
            "Conservas", 5.0
    );


    public static double getPrecio(String tipoProducto) {
        return PRECIOS.getOrDefault(tipoProducto, 2.0);
    }
}