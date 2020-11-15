package com.jkrude.material;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javafx.util.StringConverter;

public abstract class Utility {

  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
      .ofPattern("dd.MM.yy");

  public static StringConverter<Number> convertFromEpochDay() {
    return new StringConverter<Number>() {
      @Override
      public String toString(Number number) {
        return LocalDate.ofEpochDay(number.longValue())
            .format(DateTimeFormatter.ofPattern("dd-MM"));
      }

      @Override
      public Number fromString(String s) {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static <T> void setCellFactory(ChoiceDialog<T> dialog,
      Callback<ListView<T>, ListCell<T>> cellFactory) {
    // override the list cell in the dialog's combo box to show the terminal name
    @SuppressWarnings("unchecked") ComboBox<T> comboBox =
        (ComboBox<T>) ((GridPane) dialog.getDialogPane()
            .getContent()).getChildren().get(1);
    comboBox.setCellFactory(cellFactory);
  }
}
