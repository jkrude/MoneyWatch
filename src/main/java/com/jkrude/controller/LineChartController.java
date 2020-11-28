package com.jkrude.controller;

import static java.time.temporal.ChronoUnit.DAYS;

import com.jkrude.main.Main;
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
import java.util.Map.Entry;
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
  private ChoiceBox<TickRate> rangeSelection;

  private NumberAxis xAxis;

  Series<Number, Number> series;

  TreeMap<LocalDate, PropertyFilteredList<ExtendedTransaction>> dateMap;

  @Override
  public void prepare() {
    if (super.transactions == null) {
      super.transactions = fetchDataWithPossibleDialog();
      setupChart();
    }

  }

  @FXML
  private void initialize() {
    xAxis = (NumberAxis) lineChart.getXAxis();
    rangeSelection.getSelectionModel().select(TickRate.TICKS_PER_DAY);
    rangeSelection.getItems().addAll(TickRate.values());
    rangeSelection.getSelectionModel().selectedItemProperty().addListener(this::tickRateChange);
  }

  private void setupChart() {
    assert super.transactions != null;
    series = new Series<>();
    lineChart.getData().clear();
    lineChart.getData().add(series);
    dateMap = transformToTickRate(super.transactions.getSourceAsDateMap(), TickRate.TICKS_PER_DAY);
    populateSeriesAndBind();
    setupAxis();
  }


  private void tickRateChange(
      ObservableValue<? extends TickRate> obsVal,
      TickRate oldValue,
      TickRate newValue) {
//FIXME: Neither the axis nor the data points change
//    if (oldValue != newValue && newValue != null) {
//      this.xAxis.setTickUnit(newValue.asDays);
//      this.dateMap = transformToTickRate(transactions.getSourceAsDateMap(), newValue);
//      populateSeriesAndBind();
//    }
  }

  private void setupAxis() {
    assert xAxis != null;
    assert dateMap != null;
    assert !dateMap.isEmpty();
    assert rangeSelection != null;
    xAxis.setAutoRanging(false);
    xAxis.setLowerBound(dateMap.firstKey().toEpochDay());
    xAxis.setUpperBound(dateMap.lastKey().toEpochDay());
    xAxis.setTickUnit(TickRate.TICKS_PER_DAY.asDays);

    xAxis.setTickLabelFormatter(Utility.convertFromEpochDay());
  }

  private void populateSeriesAndBind() {
    this.series.getData().clear();
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
        .add(bindToSumOfList(extendedTransactions.getFilteredList()));
    dataPoint.YValueProperty().bind(currentDP);
    dataPoint.setXValue(localDate.toEpochDay());

    this.series.getData().add(dataPoint);
    // These methods need dataPoint::node → only call after it is added to the series
    setToolTip(dataPoint);
    setClickListener(dataPoint, extendedTransactions.getBaseList());
    return currentDP;
  }

  private void setClickListener(Data<Number, Number> dataPoint,
      ObservableList<ExtendedTransaction> baseList) {
    // Add click Listener for every day
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
    ContextMenu contextMenu = new ContextMenu();
    MenuItem ignoreTransaction = new MenuItem("Ignore/Activate transaction");
    ignoreTransaction.setOnAction(event -> row.getItem().switchActive());
    contextMenu.getItems().add(ignoreTransaction);
    return contextMenu;
  }

  private void setToolTip(Data<Number, Number> dataPoint) {
    assert dataPoint.getNode() != null; // dataPoint was added to series
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


  private DoubleProperty bindToSumOfList(FilteredList<ExtendedTransaction> list) {
    DoubleProperty d = new SimpleDoubleProperty(Money.mapSum(list).getRawAmount().doubleValue());
    list.addListener(new ListChangeListener<ExtendedTransaction>() {
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

  private TreeMap<LocalDate, PropertyFilteredList<ExtendedTransaction>> transformToTickRate(
      final TreeMap<LocalDate, List<ExtendedTransaction>> dateMap,
      final TickRate tickRate) {

    TreeMap<LocalDate, PropertyFilteredList<ExtendedTransaction>> accumulatedMap = new TreeMap<>();
    LocalDate currDate = LocalDate.MIN;
    for (Entry<LocalDate, List<ExtendedTransaction>> entry : dateMap.entrySet()) {
      if (DAYS.between(currDate, entry.getKey()) >= tickRate.asDays) {
        PropertyFilteredList<ExtendedTransaction> list = new PropertyFilteredList<>(
            ExtendedTransaction::isActiveProperty, entry.getValue());
        accumulatedMap.put(entry.getKey(), list);
      } else {
        accumulatedMap.lastEntry().getValue().addAll(entry.getValue());
      }
      currDate = entry.getKey();
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

