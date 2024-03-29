package com.jkrude.material;

import com.jkrude.transaction.ExtendedTransaction;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
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

  public static LocalDate parse(String stringDate) {
    LocalDate localDate;
    try {
      localDate = LocalDate.parse(stringDate, DATE_TIME_FORMATTER);
    } catch (DateTimeParseException exception) {
      // Could fail too
      localDate = LocalDate.parse(stringDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }
    return localDate;
  }

  public static StringConverter<Number> convertFromEpochDay() {
    return new StringConverter<>() {
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

  public static DoubleProperty bindToSumOfList(ObservableList<ExtendedTransaction> list) {
    // DoubleProperty that reflects the sum of a list of transactions.
    DoubleProperty d = new SimpleDoubleProperty(Money.mapSum(list).getRawAmount().doubleValue());
    list.addListener(new ListChangeListener<>() {
      @Override
      public void onChanged(Change<? extends ExtendedTransaction> change) {
        while (change.next()) {
          if (change.wasRemoved()) {
            var removed = change.getRemoved();
            d.set(d.get() - Money.mapSum(removed).getRawAmount().doubleValue());
          }
          if (change.wasAdded()) {
            d.set(d.get() + Money.mapSum(change.getAddedSubList()).getRawAmount().doubleValue());
          }
        }
      }
    });
    return d;
  }

  public static DoubleProperty bindToSumOfSet(ObservableSet<ExtendedTransaction> set) {
    // DoubleProperty that reflects the sum of a list of transactions.
    DoubleProperty d = new SimpleDoubleProperty(Money.mapSum(set).getRawAmount().doubleValue());
    set.addListener(new SetChangeListener<>() {
      @Override
      public void onChanged(Change<? extends ExtendedTransaction> change) {

        if (change.wasRemoved()) {
          ExtendedTransaction removed = change.getElementRemoved();
          d.set(
              d.get() - removed.getBaseTransaction().getMoneyAmount().getRawAmount().doubleValue());
        }
        if (change.wasAdded()) {
          d.set(d.get() + change.getElementAdded().getBaseTransaction().getMoneyAmount()
              .getRawAmount().doubleValue());
        }
      }

    });
    return d;
  }

  public static <T> ReadOnlyListWrapper<T> bindList2Set(ObservableSet<T> set) {
    ListProperty<T> listProperty = new SimpleListProperty<>(FXCollections.observableArrayList(set));
    set.addListener(new SetChangeListener<>() {
      @Override
      public void onChanged(Change<? extends T> change) {
        if (change.wasAdded()) {
          listProperty.add(change.getElementAdded());
        }
        if (change.wasRemoved()) {
          listProperty.remove(change.getElementRemoved());
        }
      }
    });
    return new ReadOnlyListWrapper<>(listProperty);
  }


  public static DoubleBinding asBinding(DoubleProperty property) {
    return new DoubleBinding() {
      {
        super.bind(property);
      }

      @Override
      public void dispose() {
        super.unbind(property);
      }

      @Override
      protected double computeValue() {
        return property.get();
      }

      @Override
      public ObservableList<?> getDependencies() {
        return FXCollections.singletonObservableList(property);
      }
    };
  }

}
