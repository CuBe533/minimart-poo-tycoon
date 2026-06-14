module com.minimart {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.minimart to javafx.fxml;

    exports com.minimart;
    exports com.minimart.dao;
    exports com.minimart.model;
}
