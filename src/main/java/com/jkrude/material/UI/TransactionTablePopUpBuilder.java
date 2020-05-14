package com.jkrude.material.UI;

import com.jkrude.material.Camt.Transaction;
import java.net.URL;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class TransactionTablePopUpBuilder {

  private static final URL fxmlResource = TransactionTablePopUpBuilder.class
      .getResource("/PopUp/camtEntryAsTable.fxml");

  public static TransactionTableController build(final ObservableList<Transaction> chartData) {
    FXMLLoader loader = new FXMLLoader();
    Stage stage = PopUp.setupStage(loader, fxmlResource);
    TransactionTableController controller = loader.getController();
    controller.stage = stage;
    controller.loader = loader;
    controller.closeBtn.setOnAction(event -> controller.stage.close());
    controller.table.getItems().addAll(chartData);
    return controller;
  }


}
