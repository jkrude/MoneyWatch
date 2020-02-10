package com.jkrude.controller;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Camt;
import com.jkrude.material.Camt.DateDataPoint;
import com.jkrude.material.Money;
import com.jkrude.material.Utility;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;

public class LineChartController extends ParentController {

  @FXML
  private LineChart<Number, Number> lineChart;
  @FXML
  private Button backButton;
  // Controller specific var:
  private boolean chartIsPopulated = false;
  private Map<Number, Date> dateLookupTable;


  public LineChartController() {
    dateLookupTable = new HashMap<>();
  }

  @Override
  protected void checkIntegrity() {
    if (!chartIsPopulated) {
      initialize();
      if (!chartIsPopulated) {
        throw new IllegalStateException("Chart could not be populated");
      }
    }
  }

  // Fetch datasource -> setup series & setupAxis & setupTooltip usw.
  @FXML
  public void initialize() {
    backButton.setOnAction(ParentController::goBack);
    if (dateLookupTable == null) {
      throw new IllegalStateException("dateLookupTable was not initialized");
    }
    // TMP Select data-source
    Camt camt = model.getCamtList().get(0);
    if (camt == null) {
      chartIsPopulated = false;
      return;
    }

    XYChart.Series<Number, Number> series = new Series<>();
    setupSeries(series, camt);

    NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
    setupAxis(xAxis, series, camt);

    for (Data<Number, Number> d : series.getData()) {
      // Add Tooltip for every data-point
      setToolTip(d);

      setClickListener(d, camt);

      setContextMenu(d.getNode());
    }
    chartIsPopulated = true;
  }

  private void setupAxis(NumberAxis xAxis, XYChart.Series<Number, Number> series,Camt camt) {
    xAxis.setAutoRanging(false);
    xAxis.setLowerBound(
        series.getData().get(0).getXValue()
            .longValue());
    xAxis.setUpperBound(
        series.getData().get(series.getData().size() - 1).getXValue()
            .longValue());
    xAxis.setTickUnit(86400000); // seconds per day

    xAxis.setTickLabelFormatter(Utility.convertFromInstant());
  }

  private void setupSeries(XYChart.Series<Number, Number> series, Camt camt) {
    series.setName("Verlaufsansicht");
    genDataFromSource(series, camt);
    if (series.getData().isEmpty()) {
      throw new IllegalStateException("No data to show in LineChart");
    }
    lineChart.getData().add(series);
  }

  private void genDataFromSource(XYChart.Series<Number, Number> series, Camt source) {
    TreeMap<Date, List<DateDataPoint>> dateMap = source.getDateMap();
    Set<Date> set = dateMap.keySet();
    Money currAmount = new Money(0);

    for (Date d : set) {
      for (DateDataPoint dateDataPoint : dateMap.get(d)) {
        currAmount.add(dateDataPoint.getAmount());
      }
      Number dateAsNumber = d.toInstant().toEpochMilli();
      dateLookupTable.put(dateAsNumber, d);
      series.getData().add(new Data<>(dateAsNumber, currAmount.getAmount()));
    }
  }

  private void setToolTip(XYChart.Data<Number, Number> data) {
    String text = Utility.convertFromInstant().toString(data.getXValue().longValue()) + "\n" +
        "Total : " + data.getYValue();
    Tooltip tlp = new Tooltip(text);
    tlp.setShowDelay(new Duration(100));
    Tooltip.install(data.getNode(), tlp);
  }

  private void setClickListener(XYChart.Data<Number, Number> data, Camt camt) {
    // Add click Listener for every day
    Date date = dateLookupTable.get(data.getXValue());
    List<DateDataPoint> dateDataPoints = camt.getDateMap().get(date);

    TableView<DateDataPoint> tableView = new TableView<>();
    tableView.setEditable(false);
    TableColumn<DateDataPoint, String> fromToColumn = new TableColumn<>("From/To");
    fromToColumn.setCellValueFactory(new PropertyValueFactory<>("otherParty"));
    TableColumn<DateDataPoint, String> usageColumn = new TableColumn<>("Usage");
    usageColumn.setCellValueFactory(new PropertyValueFactory<>("usage"));
    TableColumn<DateDataPoint, Double> amountColumn = new TableColumn<>("Amount");
    amountColumn.setCellValueFactory(new PropertyValueFactory<>("amountAsDouble"));
    tableView.getColumns().add(fromToColumn);
    tableView.getColumns().add(usageColumn);
    tableView.getColumns().add(amountColumn);

    tableView.getItems().addAll(dateDataPoints);
    /*for (DateDataPoint dateDataPoint : l) {
      String  string =
          "From/To: "
          + dateDataPoint.getReceiverOrPayer() + "\n"
          + "Usage: "
          + dateDataPoint.getUsage() + "\n"
          + "Amount: "
          + dateDataPoint.getAmount().getValue().toString() + "\n";
      tableView.getItems().add(string);
    }
    String text = Utility.convertFromInstant().toString(data.getXValue().longValue()) + "\n" +
        "Total : " + data.getYValue();
    // Add Alert with more Information
    final String textAndTotal =
        text + "\n" + "Total at this date:" + totalThatDay.getValue().toString();

     */
    data.getNode().setOnMouseClicked(
        event -> {
          if (event.getButton() == MouseButton.PRIMARY) {
            AlertBox.displayGeneric("Title", tableView, 1000, 400);
          }
        });
  }

  private void setContextMenu(Node node) {
    // Set ContextMenu
    ContextMenu contextMenu = new ContextMenu();
    MenuItem item1 = new MenuItem("Menu Item 1");
    item1.setOnAction(event -> System.out.println("Action Event"));
    // Add MenuItem to ContextMenu
    contextMenu.getItems().add(item1);

    node.setOnContextMenuRequested(
        event -> contextMenu.show(node, event.getScreenX(), event.getScreenY()));
  }
}
