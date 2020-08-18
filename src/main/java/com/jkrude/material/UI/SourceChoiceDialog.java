package com.jkrude.material.UI;

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
import javafx.scene.control.ChoiceDialog;

public class SourceChoiceDialog {

  private SourceChoiceDialog() {

  }


  public static Optional<TransactionContainer> showAndWait(List<TransactionContainer> choices) {

    // It is not possible to cleanly edit the comboBox cellFactory from the dialog
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
    Optional<String> result = sourceChoiceDialog.showAndWait();
    return result.map(converterMap::get);
  }

}
