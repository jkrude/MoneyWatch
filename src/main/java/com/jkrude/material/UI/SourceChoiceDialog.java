package com.jkrude.material.UI;

import com.jkrude.material.AlertBox;
import com.jkrude.material.TransactionContainer;
import com.jkrude.material.TransactionContainer.Transaction;
import com.jkrude.material.Utility;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;

public class SourceChoiceDialog {

  private SourceChoiceDialog() {

  }


  public static TransactionContainer showAndWait(List<TransactionContainer> choices) {

    // Map is used because it is not possible to cleanly edit the comboBox cellFactory from the dialog.
    Map<String, TransactionContainer> converterMap = new HashMap<>();
    for (TransactionContainer choice : choices) {
      TreeMap<Date, List<Transaction>> asDateMap = choice.getSourceAsDateMap();
      String firstDate = Utility.dateFormatter.format(asDateMap.firstEntry().getKey());
      String lastDate = Utility.dateFormatter.format(asDateMap.lastEntry().getKey());
      String toString = firstDate + " - " + lastDate;
      converterMap.put(toString, choice);
    }

    Set<String> keySet = converterMap.keySet();
    ChoiceDialog<String> sourceChoiceDialog = new ChoiceDialog<>(keySet.iterator().next(), keySet);
    Button btnApply = (Button) sourceChoiceDialog.getDialogPane().lookupButton(ButtonType.OK);
    // Only exit if a data-set was chosen.
    btnApply.addEventFilter(ActionEvent.ACTION, event -> {
      if (!converterMap.containsKey(sourceChoiceDialog.getSelectedItem())) {
        event.consume();
        AlertBox.showAlert("Missing dataset", "Please select a dataset", "",
            AlertType.ERROR);
      }
    });
    Optional<String> result = sourceChoiceDialog.showAndWait();
    return converterMap.get(result.orElseGet(sourceChoiceDialog::getDefaultChoice));
  }
}
