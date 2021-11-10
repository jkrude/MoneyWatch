package com.jkrude.controller;

import com.jfoenix.controls.JFXComboBox;
import com.jkrude.UI.AlertBox.AlertBuilder;
import com.jkrude.UI.SourceChoiceDialog;
import com.jkrude.UI.TransactionTablePopUp;
import com.jkrude.controller.TimeLineViewModel.TickRate;
import com.jkrude.material.Utility;
import com.jkrude.transaction.ExtendedTransaction;
import com.jkrude.transaction.TransactionContainer;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;

public class TimeLineView implements FxmlView<TimeLineViewModel>, Initializable, Prepareable {

  @FXML private LineChart<Number, Number> lineChart;
  @FXML private JFXComboBox<TickRate> tickRateChoiceBox;
  @FXML private NumberAxis xAxis;

  private boolean tickRateManualChange;

  @InjectViewModel
  private TimeLineViewModel viewModel;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tickRateManualChange = false;
    tickRateChoiceBox.getItems().addAll(TickRate.values());
    tickRateChoiceBox.getSelectionModel().selectedItemProperty().addListener(
        this::choiceBoxEventFilter);

    tickRateManualChange = true;
    tickRateChoiceBox.setValue(viewModel.getSelectedTickRate());

    viewModel.bindChartIsVisibleProperty(lineChart.visibleProperty());
    viewModel.selectedTickRateProperty().addListener((observable ->
    {
      if (!viewModel.getSelectedTickRate().equals(tickRateChoiceBox.getValue())) {
        this.tickRateManualChange = true;
        this.tickRateChoiceBox.setValue(viewModel.getSelectedTickRate());
      }
    }));
    var series = viewModel.getSeries();
    series.setName("Change over time");
    lineChart.getData().add(series);
    if (viewModel.hasActiveDataSource()) {
      // Setup chart
      attachClickListerAndToolTip();
      setupAxis();
    }
    viewModel.addSeriesChangeListener((observable) -> onSeriesChange());
  }

  @Override
  public void prepare() {
    viewModel.updateIfNecessary();
  }

  private void setupAxis() {
    assert viewModel.getSelectedTickRate() != null;
    xAxis.setAutoRanging(false);
    xAxis.setLowerBound(viewModel.getAccumulatedDateMap().firstKey().toEpochDay());
    xAxis.setUpperBound(viewModel.getAccumulatedDateMap().lastKey().toEpochDay());
    xAxis.tickUnitProperty().set(viewModel.getSelectedTickRate().getAsDays());
    xAxis.setTickLabelFormatter(Utility.convertFromEpochDay());
  }

  private void onSeriesChange() {
    setupAxis();
    attachClickListerAndToolTip();
  }

  private void attachClickListerAndToolTip() {
    for (var dp : viewModel.getSeries().getData()) {
      setClickListener(dp);
      setToolTip(dp);
    }
  }

  private void choiceBoxEventFilter(
      ObservableValue<? extends TickRate> obsVal, TickRate oldValue, TickRate newValue) {

    if (newValue == null || tickRateManualChange) {
      tickRateManualChange = false;
      return;
    }

    if (!viewModel.possibleTickRateChange(newValue)) {
      new AlertBuilder(AlertType.INFORMATION)
          .setHeader("Option is not available")
          .setMessage("There are not enough data points in the selected range.")
          .showAndWait();
      tickRateManualChange = true;
      tickRateChoiceBox.setValue(oldValue);
    }
  }

  private void setClickListener(Data<Number, Number> dataPoint) {
    // Show transactions for this date in a pop up.
    dataPoint.getNode().setOnMouseClicked(
        event -> {
          if (event.getButton() == MouseButton.PRIMARY) {
            TransactionTablePopUp.Builder.initBind(
                new ReadOnlyObjectWrapper<>(
                    viewModel.getTransactionsForDate(dataPoint).getBaseList()))
                .setContextMenu(this::contextMenuGenerator)
                .setTitle(LocalDate.ofEpochDay(dataPoint.getXValue().longValue()).format(Utility.DATE_TIME_FORMATTER))
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

  @FXML
  private void changeDataSource() {
    TransactionContainer chosenData = SourceChoiceDialog
        .showAndWait(viewModel.getTransactionContainerList());
    // Changes are triggered by the invalidation listener.
    viewModel.possibleActiveDataChange(chosenData);
  }

}
