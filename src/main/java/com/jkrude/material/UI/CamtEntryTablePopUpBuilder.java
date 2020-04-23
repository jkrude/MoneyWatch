package com.jkrude.material.UI;

import com.jkrude.material.Camt.CamtEntry;
import java.net.URL;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class CamtEntryTablePopUpBuilder {

  private static final URL fxmlResource = CamtEntryTablePopUpBuilder.class
      .getResource("/PopUp/camtEntryAsTable.fxml");

  public static CamtEntryTableController build(final ObservableList<CamtEntry> chartData) {
    FXMLLoader loader = new FXMLLoader();
    Stage stage = PopUp.setupStage(loader, fxmlResource);
    CamtEntryTableController controller = loader.getController();
    controller.stage = stage;
    controller.loader = loader;
    controller.closeBtn.setOnAction(event -> controller.stage.close());
    controller.table.getItems().addAll(chartData);
    return controller;
  }


}
