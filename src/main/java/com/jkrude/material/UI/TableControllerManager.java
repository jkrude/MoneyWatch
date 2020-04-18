package com.jkrude.material.UI;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Camt.CamtEntry;
import java.io.IOException;
import java.net.URL;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class TableControllerManager {

  public static final URL fxmlResource = TableControllerManager.class
      .getResource("/PopUp/camtEntryAsTable.fxml");

  public static void showAsTablePopUp(final ObservableList<CamtEntry> chartData) {
    if (fxmlResource != null) {
      FXMLLoader loader = new FXMLLoader(fxmlResource);
      Pane pane;
      try {
        pane = loader.load();
        CamtEntryTableController controller = loader.getController();
        controller.setItems(chartData);
        Stage stage = new Stage();
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.showAndWait();

      } catch (IOException e) {
        e.printStackTrace();
        AlertBox.showAlert("Fatal Error", "", "Internal Error", AlertType.ERROR);
      }
    }
  }
}
