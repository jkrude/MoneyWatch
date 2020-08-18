package com.jkrude.controller;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Model;
import com.jkrude.material.TransactionContainer;
import com.jkrude.material.UI.SourceChoiceDialog;
import java.util.Optional;
import javafx.scene.control.Alert.AlertType;

public abstract class DataDependingControlller extends Controller {

  protected TransactionContainer transactions;

  protected void fetchDataWithDialogs() {
    if (transactions == null) {
      if (Model.getInstance().getTransactionContainerList() == null
          || Model.getInstance().getTransactionContainerList().isEmpty()) {

        throw new IllegalStateException("Controller was called but no data is available");
      } else if (Model.getInstance().getTransactionContainerList().size() > 1) {
        forceSourceChoiceDialog();
      } else {
        transactions = Model.getInstance().getTransactionContainerList().get(0);
      }
    }
  }

  protected void forceSourceChoiceDialog() {
    Optional<TransactionContainer> result = SourceChoiceDialog
        .showAndWait(Model.getInstance().getTransactionContainerList());
    result.ifPresentOrElse(resTrans -> transactions = resTrans, this::returnAfterChoiceDialogError);
  }

  protected void returnAfterChoiceDialogError() {
    AlertBox.showAlert("Fehlender Datensatz", "Bitte wähle im Dialog einen Datensatz", "",
        AlertType.ERROR);
    forceSourceChoiceDialog();
  }

}
