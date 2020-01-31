module MoneyWatch {

    requires javafx.controls;
    requires javafx.fxml;
    opens com.jkrude.controller to javafx.fxml;
    opens com.jkrude.material to javafx.base;
    exports com.jkrude.main;
}