module MoneyWatch {

    requires javafx.controls;
    requires javafx.fxml;
  requires json.simple;
  opens com.jkrude.controller to javafx.fxml;
    opens com.jkrude.material to javafx.base;
    opens com.jkrude.material.UI to javafx.base, javafx.fxml;
    exports com.jkrude.main;
}