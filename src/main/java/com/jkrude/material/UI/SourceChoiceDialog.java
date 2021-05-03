package com.jkrude.material.UI;

import com.jkrude.material.AlertBox.AlertBuilder;
import com.jkrude.material.Utility;
import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.TransactionContainer;
import java.time.LocalDate;
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
      TreeMap<LocalDate, List<ExtendedTransaction>> asDateMap = choice.getSourceAsDateMap();
      String firstDate = Utility.DATE_TIME_FORMATTER.format(asDateMap.firstEntry().getKey());
      String lastDate = Utility.DATE_TIME_FORMATTER.format(asDateMap.lastEntry().getKey());
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
        AlertBuilder.alert(AlertType.ERROR)
            .setTitle("Missing dataset")
            .setHeader("Please select a dataset.")
            .buildAndShow();
      }
    });
    Optional<String> result = sourceChoiceDialog.showAndWait();
    return converterMap.get(result.orElseGet(sourceChoiceDialog::getDefaultChoice));
  }
}
