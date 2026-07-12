package com.minimart.controller;

import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.util.Duration;


public class AnimacionService {

    private static final String ESTILO_NORMAL   = "-fx-text-fill:#FFD700;";
    private static final String ESTILO_GANANCIA = "-fx-text-fill:#1D9E75; -fx-font-weight:bold;";


    public void animarGanancia(Label label) {
        if (label == null) return;

        label.setStyle(ESTILO_GANANCIA);

        FadeTransition fade = new FadeTransition(Duration.millis(500), label);
        fade.setFromValue(1.0);
        fade.setToValue(0.6);
        fade.setCycleCount(2);
        fade.setAutoReverse(true);
        fade.setOnFinished(e -> {
            label.setStyle(ESTILO_NORMAL);
            label.setOpacity(1.0);
        });
        fade.play();
    }
}