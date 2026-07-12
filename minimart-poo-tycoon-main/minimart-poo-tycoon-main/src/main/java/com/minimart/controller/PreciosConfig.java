package com.minimart.controller;

import java.util.Map;

public final class PreciosConfig {

    private PreciosConfig() {}

    public static final Map<String, Double> PRECIOS = Map.of(
            "Snacks",    2.0,
            "Bebidas",   1.5,
            "Lácteos",   3.5,
            "Dulces",    1.8,
            "Conservas", 2.5
    );

    public static double getPrecio(String tipoProducto) {
        return PRECIOS.getOrDefault(tipoProducto, 2.0);
    }
}