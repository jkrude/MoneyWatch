package com.jkrude.UI;


import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Cell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

public class ToolTippedTableCell<S, T> extends TableCell<S, T> {

  public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forTableColumn() {
    return forTableColumn(new DefaultStringConverter());
  }

  public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(final StringConverter<T> converter) {
    return list -> new ToolTippedTableCell<>(converter);
  }

  private static <T> String getItemText(Cell<T> cell, StringConverter<T> converter) {
    return converter == null ? cell.getItem() == null ? "" : cell.getItem()
        .toString() : converter.toString(cell.getItem());
  }

  private void updateItem(final Cell<T> cell, final StringConverter<T> converter) {

    if (cell.isEmpty()) {
      cell.setText(null);
      cell.setTooltip(null);

    } else {
      cell.setText(getItemText(cell, converter));

      //Add text as tooltip so that user can read text without editing it.
      Tooltip tooltip = new Tooltip(getItemText(cell, converter));
      tooltip.setWrapText(true);
      tooltip.maxWidthProperty().bind(Bindings.max(55, cell.widthProperty()));
      cell.setTooltip(tooltip);

    }
  }

  private final ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<>(this, "converter");

  /**
   * The easiest way to get this working is to call this class's static forTableColumn() method:
   * <code>
   * someColumn.setCellFactory(ToolTippedTableCell.forTableColumn());
   * </code>
   */
  public ToolTippedTableCell() {
    this(null);
  }

  public ToolTippedTableCell(StringConverter<T> converter) {
    this.getStyleClass().add("tooltipped-table-cell");
    setConverter(converter);
  }

  public final ObjectProperty<StringConverter<T>> converterProperty() {
    return converter;
  }

  public final void setConverter(StringConverter<T> value) {
    converterProperty().set(value);
  }

  public final StringConverter<T> getConverter() {
    return converterProperty().get();
  }


  @Override
  public void updateItem(T item, boolean empty) {
    super.updateItem(item, empty);
    updateItem(this, getConverter());
  }
}
