package com.jkrude.controller;

import static java.time.temporal.ChronoUnit.DAYS;

import com.jkrude.material.Model;
import com.jkrude.material.PropertyFilteredList;
import com.jkrude.material.Utility;
import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.TransactionContainer;
import de.saxsys.mvvmfx.ViewModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;

public class TimeLineViewModel implements ViewModel {


  public enum TickRate {
    TICKS_PER_DAY(1, "Day"),
    TICKS_PER_WEEK(7, "Week"),
    TICKS_PER_MONTH(31, "31 Days");// ~ Approx one month (31 Days)

    private final int asDays;

    private final String string;

    TickRate(int asDays, String string) {
      this.asDays = asDays;
      this.string = string;
    }

    public int getAsDays() {
      return asDays;
    }

    @Override
    public String toString() {
      return string;
    }


  }

  private final Model globalModel;

  private final BooleanProperty invalidated;

  private final BooleanProperty chartIsVisible;
  private final ReadOnlyObjectWrapper<TimeLineViewModel.TickRate> selectedTickRate;
  private final Series<Number, Number> series;
  private final List<InvalidationListener> onSeriesChange;
  private TreeMap<LocalDate, PropertyFilteredList<ExtendedTransaction>> accumulatedDateMap;

  public TimeLineViewModel() {
    globalModel = Model.getInstance();
    invalidated = new SimpleBooleanProperty(false);
    chartIsVisible = new SimpleBooleanProperty(true);
    selectedTickRate = new ReadOnlyObjectWrapper<>(TickRate.TICKS_PER_DAY);
    series = new Series<>();
    onSeriesChange = new ArrayList<>();
    globalModel.activeDataProperty().addListener(this::onActiveDataChange);
    if (hasActiveDataSource()) {
      updateDateMap();
      populateSeries();
    } else {
      accumulatedDateMap = new TreeMap<>();
    }
  }


  public boolean possibleTickRateChange(TickRate newValue) {
    if (checkEnoughData(newValue, globalModel.getActiveData())
        && !newValue.equals(getSelectedTickRate())) {
      selectedTickRate.set(newValue);
      updateDateMap();
      clearSeries();
      populateSeries();
      return true;
    }
    return false;
  }

  public void updateIfNecessary() {
    if (invalidated.get()) {
      updateDateMap();
      clearSeries();
      populateSeries();
      invalidated.set(false);
    }
  }

  public void possibleActiveDataChange(TransactionContainer chosenData) {
    if (globalModel.getActiveData().equals(chosenData)) {
      return;
    }
    globalModel.setActiveData(chosenData);
    // Changes are made within onActiveDataChange
  }

  public boolean checkEnoughData(TickRate tickRate, TransactionContainer container) {
    return tickRate == TickRate.TICKS_PER_DAY
        || DAYS.between(
        container.getSourceAsDateMap().firstKey(), container.getSourceAsDateMap().lastKey())
        >= tickRate.getAsDays();
  }

  public void addSeriesChangeListener(InvalidationListener listener) {
    onSeriesChange.add(listener);
  }

  public void removeSeriesChangeListener(InvalidationListener listener) {
    onSeriesChange.remove(listener);
  }


  private void updateDateMap() {
    // Accumulate dates if tickRate != DAYS.
    // Transactions will be stored in filteredLists, which ignores deactivated transactions.
    assert hasActiveDataSource();
    this.accumulatedDateMap = new TreeMap<>();
    TransactionContainer transactions = globalModel.getActiveData();
    var dateMapSource = transactions.getSourceAsDateMap();
    LocalDate currDate = LocalDate.MIN;
    for (var entry : dateMapSource.entrySet()) {
      // If the last-saved-date is older than the current date → create new data-point
      if (DAYS.between(currDate, entry.getKey()) >= selectedTickRate.get().getAsDays()
          || entry.getKey().equals(dateMapSource.lastKey())) {
        var list = new PropertyFilteredList<>(
            ExtendedTransaction::isActiveProperty, entry.getValue());
        accumulatedDateMap.put(entry.getKey(), list);
        currDate = entry.getKey();
      } else {
        accumulatedDateMap.lastEntry().getValue().addAll(entry.getValue());
      }
    }
  }

  private void clearSeries() {
    series.getData().forEach(dp -> dp.YValueProperty().unbind());
    series.getData().clear();
  }

  private void populateSeries() {
    assert series.getData().isEmpty();
    // Init with neutral element for addition.
    DoubleBinding previousDP = new DoubleBinding() {
      @Override
      protected double computeValue() {
        return 0;
      }
    };
    for (var entry : accumulatedDateMap.entrySet()) {
      LocalDate currDate = entry.getKey();
      PropertyFilteredList<ExtendedTransaction> currTransactions = entry.getValue();
      Data<Number, Number> currDataPoint = new Data<>();
      // Bind Y, Set X
      DoubleBinding currBinding = previousDP // add creates new Binding.
          .add(Utility.bindToSumOfList(currTransactions.getFilteredList()));
      currDataPoint.YValueProperty().bind(currBinding);
      currDataPoint.setXValue(currDate.toEpochDay());
      series.getData().add(currDataPoint);
      previousDP = currBinding;
    }
    onSeriesChange.forEach(listener -> listener.invalidated(series.getData()));
  }

  private void onActiveDataChange(Observable observable) {
    if (!checkEnoughData(selectedTickRate.getValue(), globalModel.getActiveData())) {
      selectedTickRate.set(TickRate.TICKS_PER_DAY);
    }
    if (chartIsVisible.get()) {
      updateDateMap();
      clearSeries();
      populateSeries();
    } else {
      invalidated.set(true);
    }
  }


  /*
   * Getter
   */

  public List<TransactionContainer> getTransactionContainerList() {
    return globalModel.getTransactionContainerList();
  }

  public ReadOnlyObjectWrapper<TickRate> selectedTickRateProperty() {
    return selectedTickRate;
  }

  public TickRate getSelectedTickRate() {
    return selectedTickRate.get();
  }

  public TreeMap<LocalDate, PropertyFilteredList<ExtendedTransaction>> getAccumulatedDateMap() {
    return accumulatedDateMap;
  }

  public PropertyFilteredList<ExtendedTransaction> getTransactionsForDate(
      Data<Number, Number> dataPoint) {
    LocalDate asLocalDate = LocalDate.ofEpochDay(dataPoint.getXValue().longValue());
    return accumulatedDateMap.get(asLocalDate);
  }

  public void bindChartIsVisibleProperty(BooleanProperty chartIsVisible) {
    this.chartIsVisible.bind(chartIsVisible);
  }

  public Series<Number, Number> getSeries() {
    assert !invalidated.get();
    return this.series;
  }

  public boolean hasActiveDataSource() {
    return globalModel.getActiveData() != null;
  }


}
