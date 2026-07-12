module com.minimart {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.media;

    opens com.minimart to javafx.fxml;

    exports com.minimart;
    exports com.minimart.dao;
    exports com.minimart.model;

    exports com.minimart.controller;
    exports com.minimart.view;
    opens   com.minimart.controller to javafx.fxml;
    opens   com.minimart.view       to javafx.fxml;
}
