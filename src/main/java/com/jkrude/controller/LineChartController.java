package com.jkrude.controller;

import static java.time.temporal.ChronoUnit.DAYS;

import com.jkrude.main.Main;
import com.jkrude.material.AlertBox;
import com.jkrude.material.Model;
import com.jkrude.material.Money;
import com.jkrude.material.PropertyFilteredList;
import com.jkrude.material.UI.SourceChoiceDialog;
import com.jkrude.material.UI.TransactionTablePopUp;
import com.jkrude.material.Utility;
import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.TransactionContainer;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;

public class LineChartController extends DataDependingController {

  private enum TickRate {
    TICKS_PER_DAY(1, "Day"),
    TICKS_PER_WEEK(7, "Week"),
    TICKS_PER_MONTH(31, "31 Days");// ~ Approx one month (31 Days)

    private final int asDays;

    private final String string;

    TickRate(int asDays, String string) {
      this.asDays = asDays;
      this.string = string;
    }

    private double get() {
      return asDays;
    }

    @Override
    public String toString() {
      return string;
    }


  }

  @FXML
  private LineChart<Number, Number> lineChart;

  @FXML
  private ChoiceBox<TickRate> tickRateChoiceBox;

  private NumberAxis xAxis;

  private Series<Number, Number> series;

  private TreeMap<LocalDate, PropertyFilteredList<ExtendedTransaction>> dateMap;

  // Reflects rangeSelection.getValue but only if the new choice results in reasonable data.
  private ReadOnlyObjectWrapper<TickRate> selectedTickRate;


  @Override
  public void prepare() {
    // Only extended transactions change therefore no external changes need to be monitored.
    if (super.transactions == null) {
      super.transactions = fetchDataWithPossibleDialog();
      setupChart();
    }
  }

  @FXML
  private void initialize() {
    xAxis = (NumberAxis) lineChart.getXAxis();
    lineChart.setAnimated(false);
    selectedTickRate = new ReadOnlyObjectWrapper<>(TickRate.TICKS_PER_DAY);
    selectedTickRate.addListener(this::tickRateChange);
    tickRateChoiceBox.getItems().addAll(TickRate.values());
    tickRateChoiceBox.getSelectionModel().select(selectedTickRate.get());
    tickRateChoiceBox.getSelectionModel().selectedItemProperty().addListener(
        this::choiceBoxEventFilter);
  }


  private void setupChart() {
    assert super.transactions != null;
    assert selectedTickRate != null;
    series = new Series<>();
    series.setName("Change over time");
    lineChart.getData().clear();
    lineChart.getData().add(series);
    if (selectedTickRate.get() != TickRate.TICKS_PER_DAY) {
      // triggers choiceBoxEventFilter → tickRateChange → dataMap change
      tickRateChoiceBox.setValue(TickRate.TICKS_PER_DAY);
    } else {
      dateMap = transformToTickRate(super.transactions.getSourceAsDateMap(),
          selectedTickRate.get());
      populateSeriesAndBind();
    }
    setupAxis();
  }

  private void choiceBoxEventFilter(
      ObservableValue<? extends TickRate> obsVal,
      TickRate oldValue,
      TickRate newValue) {

    assert dateMap != null;
    // Only change if new plot would have more than one datapoint.
    if (newValue != TickRate.TICKS_PER_DAY
        && DAYS.between(this.dateMap.firstKey(), this.dateMap.lastKey()) < newValue.asDays) {

      AlertBox.showAlert(
          "Not enough data",
          "There are not enough data points in the selected range.",
          "",
          AlertType.INFORMATION);
      // triggers this method → triggers tickRateChange → oldValue will be newValue → return
      tickRateChoiceBox.setValue(oldValue);
    } else {
      selectedTickRate.set(newValue);
    }
  }

  private void tickRateChange(
      ObservableValue<? extends TickRate> obsVal,
      TickRate oldValue,
      TickRate newValue) {

    if (oldValue != newValue && newValue != null) {
      if (this.series != null && !this.series.getData().isEmpty()) {
        this.series.getData()
            .forEach(d -> d.YValueProperty().unbind()); // cant clear if yValues are bound
        this.series.getData().clear();
      }

      this.xAxis.setTickUnit(newValue.asDays);
      this.dateMap = transformToTickRate(transactions.getSourceAsDateMap(), newValue);
      populateSeriesAndBind();
    }
  }

  private void setupAxis() {
    assert xAxis != null;
    assert !dateMap.isEmpty();
    assert selectedTickRate != null;
    xAxis.setAutoRanging(false);
    xAxis.setLowerBound(dateMap.firstKey().toEpochDay());
    xAxis.setUpperBound(dateMap.lastKey().toEpochDay());
    xAxis.setTickUnit(selectedTickRate.get().asDays);

    xAxis.setTickLabelFormatter(Utility.convertFromEpochDay());
  }

  private void populateSeriesAndBind() {
    assert series.getData().isEmpty();
    // Init with neutral element for addition.
    DoubleBinding previousDP = new DoubleBinding() {
      @Override
      protected double computeValue() {
        return 0;
      }
    };
    for (var entry : dateMap.entrySet()) {
      LocalDate key = entry.getKey();
      PropertyFilteredList<ExtendedTransaction> value = entry.getValue();
      previousDP = dataPointCreation(previousDP, key, value);
    }
  }

  private DoubleBinding dataPointCreation(
      DoubleBinding previousDataPoint,
      LocalDate localDate,
      PropertyFilteredList<ExtendedTransaction> extendedTransactions) {

    Data<Number, Number> dataPoint = new Data<>();
    // Bind Y, Set X
    DoubleBinding currentDP = previousDataPoint
        .add(Utility.bindToSumOfList(extendedTransactions.getFilteredList()));
    dataPoint.YValueProperty().bind(currentDP);
    dataPoint.setXValue(localDate.toEpochDay());

    this.series.getData().add(dataPoint);
    // These methods need dataPoint::node → only call after it is added to the series.
    setToolTip(dataPoint);
    setClickListener(dataPoint, extendedTransactions.getBaseList());
    return currentDP;
  }

  private void setClickListener(Data<Number, Number> dataPoint,
      ObservableList<ExtendedTransaction> baseList) {
    // Show transactions for this date in a pop up.
    dataPoint.getNode().setOnMouseClicked(
        event -> {
          if (event.getButton() == MouseButton.PRIMARY) {
            TransactionTablePopUp.Builder.initBind(new ReadOnlyObjectWrapper<>(baseList))
                .setContextMenu(this::contextMenuGenerator)
                .showAndWait();
          }
        });
  }

  private ContextMenu contextMenuGenerator(TableRow<ExtendedTransaction> row) {
    // Context menu for the table pop up.
    ContextMenu contextMenu = new ContextMenu();
    MenuItem ignoreTransaction = new MenuItem("Ignore/Activate transaction");
    ignoreTransaction.setOnAction(event -> row.getItem().switchActive());
    contextMenu.getItems().add(ignoreTransaction);
    return contextMenu;
  }

  private void setToolTip(Data<Number, Number> dataPoint) {
    assert dataPoint.getNode() != null; // dataPoint was added to series
    // Show the total amount at that date.
    // The amount is bound to the dataPoint.yValue.
    String prefix = Utility.convertFromEpochDay().toString(dataPoint.getXValue()) + "\n" +
        "Total : ";
    StringProperty property = new SimpleStringProperty();
    StringProperty other = new SimpleStringProperty();
    other.bind(Bindings.createStringBinding(
        () -> new DecimalFormat("###.##").format(dataPoint.YValueProperty().get().doubleValue()),
        dataPoint.YValueProperty()));
    property.bind(Bindings.concat(prefix).concat(other));

    Tooltip tlp = new Tooltip();
    tlp.textProperty().bind(property);
    tlp.setShowDelay(new Duration(100));
    Tooltip.install(dataPoint.getNode(), tlp);
  }

  private TreeMap<LocalDate, PropertyFilteredList<ExtendedTransaction>> transformToTickRate(
      final TreeMap<LocalDate, List<ExtendedTransaction>> dateMap,
      final TickRate tickRate) {

    // Accumulate dates if tickRate != DAYS.
    // Transactions will be stored in filteredLists, which ignores deactivated transactions.
    TreeMap<LocalDate, PropertyFilteredList<ExtendedTransaction>> accumulatedMap = new TreeMap<>();
    LocalDate currDate = LocalDate.MIN;
    for (var entry : dateMap.entrySet()) {
      // If the last-saved-date is older than the current date → create new data-point
      if (DAYS.between(currDate, entry.getKey()) >= tickRate.asDays || entry.getKey().equals(dateMap.lastKey())) {
        var list = new PropertyFilteredList<>(
            ExtendedTransaction::isActiveProperty, entry.getValue());
        accumulatedMap.put(entry.getKey(), list);
        currDate = entry.getKey();
      } else {
        accumulatedMap.lastEntry().getValue().addAll(entry.getValue());
      }
    }
    return accumulatedMap;
  }

  @FXML
  private void changeDataSource(ActionEvent event) {
    if (Model.getInstance().getTransactionContainerList().isEmpty()) {
      throw new IllegalStateException("No data to chose from");
    }
    TransactionContainer chosenData = SourceChoiceDialog
        .showAndWait(Model.getInstance().getTransactionContainerList());
    if (!this.transactions.equals(chosenData)) {
      this.transactions = chosenData;
      setupChart();
    }
  }

  @FXML
  private void goBack(ActionEvent event) {
    Main.goBack();
  }
}

