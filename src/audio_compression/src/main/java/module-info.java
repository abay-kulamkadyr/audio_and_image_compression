module org.qulad {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens org.qulad to javafx.fxml;
    exports org.qulad;
}