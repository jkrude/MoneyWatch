module MoneyWatch {

  requires javafx.controls;
  requires javafx.fxml;
  requires json.simple;
  requires charts;
  requires de.saxsys.mvvmfx;
  requires com.jfoenix;
  opens com.jkrude.controller to javafx.fxml, de.saxsys.mvvmfx;
  opens com.jkrude.material to javafx.base;
  opens com.jkrude.UI to javafx.base, javafx.fxml, de.saxsys.mvvmfx;
  exports com.jkrude.main;
}