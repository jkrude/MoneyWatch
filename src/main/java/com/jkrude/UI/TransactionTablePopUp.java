package com.jkrude.UI;

import com.jkrude.transaction.ExtendedTransaction;
import java.net.URL;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableRow;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class TransactionTablePopUp {


  @FXML
  private AnchorPane ttv;
  @FXML
  private TransactionTableView ttvController;

  public Button closeBtn;

  // Constructor needs to be public (FXML) but should NOT be used
  public TransactionTablePopUp() {
  }

  @FXML
  private void initialize() {
  }


  public static class Builder {

    private final TransactionTablePopUp ttp;
    private final Stage stage;

    private static final URL fxmlResource = TransactionTablePopUp.class
        .getResource("/com/jkrude/UI/TransactionTablePopUpView.fxml");

    private Builder() {
      FXMLLoader loader = new FXMLLoader();
      stage = StageSetter.setupStage(loader, fxmlResource);
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.setAlwaysOnTop(true);
      stage.setTitle("Transactions"); // default title
      ttp = loader.getController();
      ttp.closeBtn.setOnAction(event -> stage.close());
    }

    public static Builder initSet(final ObservableList<ExtendedTransaction> tableData) {
      Builder b = new Builder();
      b.ttp.ttvController.itemsProperty().set(tableData);
      return b;
    }

    public static Builder initBind(
        final ObjectProperty<? extends ObservableList<ExtendedTransaction>> tableData) {
      Builder b = new Builder();
      b.ttp.ttvController.itemsProperty().bind(tableData);
      return b;
    }

    public Builder setTitle(String categoryName) {
      this.stage.setTitle(categoryName);
      return this;
    }

    public Builder setContextMenu(
        Callback<TableRow<ExtendedTransaction>, ContextMenu> menuGenerator) {
      this.ttp.ttvController.setContextMenu(menuGenerator);
      return this;
    }

    public void showAndWait() {
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.showAndWait();
    }
  }

}
