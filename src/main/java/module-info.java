module MoneyWatch {

    requires javafx.controls;
    requires javafx.fxml;
    opens com.jkrude.controller to javafx.fxml;
    exports com.jkrude.main;
}