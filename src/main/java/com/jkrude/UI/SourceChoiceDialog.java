package com.jkrude.UI;

import com.jkrude.material.Utility;
import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.TransactionContainer;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.util.StringConverter;

public class SourceChoiceDialog {

  private SourceChoiceDialog() {
  }

  public static TransactionContainer showAndWait(List<TransactionContainer> choices) {

    if (choices.isEmpty()) {
      throw new IllegalArgumentException("At least one option required");
    }
    // Map is used because it is not possible to cleanly edit the comboBox cellFactory from the dialog.
    Map<String, TransactionContainer> converterMap = new HashMap<>();
    for (TransactionContainer choice : choices) {
      converterMap.put(convertToString(choice), choice);
    }
    Optional<TransactionContainer> choice = new JFXChoiceDialog.Builder<TransactionContainer>()
        .setOptions(FXCollections.observableList(choices))
        .setDefaultChoice(choices.get(0))
        .setStringConverter(new StringConverter<>() {
          @Override
          public String toString(TransactionContainer transactionContainer) {
            return convertToString(transactionContainer);
          }

          @Override
          public TransactionContainer fromString(String string) {
            return converterMap.get(string);
          }
        })
        .setHeader("Choose a dataset")
        .showAndWait();
    return choice.orElseGet(() -> choices.get(0));
  }

  private static String convertToString(TransactionContainer container) {
    if (container == null) {
      return "";
    }
    TreeMap<LocalDate, List<ExtendedTransaction>> asDateMap = container.getSourceAsDateMap();
    String firstDate = Utility.DATE_TIME_FORMATTER.format(asDateMap.firstEntry().getKey());
    String lastDate = Utility.DATE_TIME_FORMATTER.format(asDateMap.lastEntry().getKey());
    return firstDate + " - " + lastDate;
  }

}
