module org.qulad {
  requires javafx.controls;
  requires javafx.fxml;
  requires java.desktop;
  requires javafx.swing;

  opens org.qulad to
      javafx.fxml;

  exports org.qulad;
}
