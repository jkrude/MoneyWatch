package com.jkrude.controller;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Camt;
import com.jkrude.material.Model;
import com.jkrude.material.UI.SourceChoiceDialog;
import java.util.Optional;
import javafx.scene.control.Alert.AlertType;

public abstract class DataDependingControlller extends Controller {

  protected Camt camtData;

  protected void fetchDataWithDialogs() {
    if (camtData == null) {
      if (Model.getInstance().getCamtList() == null
          || Model.getInstance().getCamtList().isEmpty()) {

        throw new IllegalStateException("Controller was called but no data is available");
      } else if (Model.getInstance().getCamtList().size() > 1) {
        forceSourceChoiceDialog();
      } else {
        camtData = Model.getInstance().getCamtList().get(0);
      }
    }
  }

  protected void forceSourceChoiceDialog() {
    Optional<Camt> result = SourceChoiceDialog.showAndWait(Model.getInstance().getCamtList());
    result.ifPresentOrElse(camt -> camtData = camt, this::returnAfterChoiceDialogError);
  }

  protected void returnAfterChoiceDialogError() {
    AlertBox.showAlert("Fehlender Datensatz", "Bitte wähle im Dialog einen Datensatz", "",
        AlertType.ERROR);
    forceSourceChoiceDialog();
  }

}
