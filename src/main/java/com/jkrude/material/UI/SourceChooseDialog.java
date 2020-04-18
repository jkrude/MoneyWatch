package com.jkrude.material.UI;

import com.jkrude.material.Camt;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;

public class SourceChooseDialog {


  @FXML
  private ListView<Camt> listView;
  @FXML
  private DialogPane dialogPane;


  private static final URL fxmlResource = RuleDialog.class.getClassLoader()
      .getResource("chooseDataDialog.fxml");

  @FXML
  private void initialize() {
    listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    listView.setCellFactory(
        camtListView -> new ListCell<>() {
          @Override
          protected void updateItem(Camt camt, boolean empty) {
            super.updateItem(camt, empty);
            if (camt != null && !empty) {
              var dateMap = camt.getSourceAsDateMap();
              setText(dateMap.firstEntry().getKey().toString() + dateMap.lastEntry().getKey()
                  .toString());
            } else {
              setText(null);
            }
          }
        }
    );
  }

  private static Stage setupStage(FXMLLoader loader) {
    if (fxmlResource != null) {
      Parent pane;
      try {
        pane = loader.load();
        Stage stage = new Stage();
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        return stage;
      } catch (IOException e) {
        // TODO
        e.printStackTrace();
        throw new IllegalStateException(e);
      }
    } else {
      throw new IllegalStateException("FXML file was null");
    }
  }

  public static void show(final Consumer<Camt> callback, List<Camt> sources) {
    FXMLLoader loader = new FXMLLoader(fxmlResource);
    Stage stage = setupStage(loader);
    SourceChooseDialog controller = loader.getController();
    controller.listView.setItems(FXCollections.observableList(sources));
    controller.listView.getSelectionModel().selectFirst();
    controller.dialogPane.lookupButton(ButtonType.APPLY).setOnMouseClicked(
        mouseEvent -> {
          callback.accept(controller.listView.getSelectionModel().getSelectedItem());
          stage.close();
        }
    );
    controller.dialogPane.lookupButton(ButtonType.CANCEL).setOnMouseClicked(
        mouseEvent -> {
          callback.accept(null);
          stage.close();
        }
    );
    controller.listView.setOnMouseClicked(
        mouseEvent -> {
          if (mouseEvent.getClickCount() == 2) {
            callback.accept(controller.listView.getSelectionModel().getSelectedItem());
            stage.close();
          }
        });

    stage.showAndWait();
  }
}
