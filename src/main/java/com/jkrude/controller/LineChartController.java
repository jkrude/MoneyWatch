package com.jkrude.controller;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Camt;
import com.jkrude.material.Camt.DateDataPoint;
import com.jkrude.material.Money;
import com.jkrude.material.Utility;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;

public class LineChartController extends AbstractController {

  public LineChart<Number, Number> lineChart;
  public Button backButton;
  private boolean chartIsPopulated = false;


  @FXML
  public void initialize() {
    backButton.setOnAction(AbstractController::goBack);
    Camt camt = model.getCamtList().get(0);
    if (camt == null) {
      chartIsPopulated = false;
      return;
    }
    NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
    setupAxis(xAxis, camt);

    XYChart.Series<Number, Number> series = new XYChart.Series<>();
    setupSeries(series, camt);

    for (Data<Number, Number> d : series.getData()) {
      // Add Tooltip for every data-point
      setToolTip(d);

      setClickListener(d, camt);

      setContextMenu(d.getNode());
    }
    chartIsPopulated =true;
  }

  private void setupAxis(NumberAxis xAxis, Camt camt) {
    xAxis.setAutoRanging(false);
    xAxis.setLowerBound(
        camt.getLineChartData().get(0).getXValue()
            .longValue());
    xAxis.setUpperBound(
        camt.getLineChartData().get(camt.getLineChartData().size() - 1).getXValue()
            .longValue());
    xAxis.setTickUnit(86400000); // seconds per day

    xAxis.setTickLabelFormatter(Utility.convertFromInstant());
  }

  private void setupSeries(XYChart.Series<Number, Number> series, Camt camt) {
    series.setName("Month-Overview");
    series.getData().addAll(camt.getLineChartData());
    lineChart.getData().add(series);
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
    List<DateDataPoint> l = camt.getDataPointForDate(data.getXValue().longValue());
    Money totalThatDay = new Money(0);

    TableView<DateDataPoint> tableView = new TableView<>();
    tableView.setEditable(true);
    TableColumn<DateDataPoint, String> fromToColumn = new TableColumn<>("From/To");
    fromToColumn.setCellValueFactory(new PropertyValueFactory<>("otherParty"));
    TableColumn<DateDataPoint, String> usageColumn = new TableColumn<>("Usage");
    usageColumn.setCellValueFactory(new PropertyValueFactory<>("usage"));
    TableColumn<DateDataPoint, Double> amountColumn = new TableColumn<>("Amount");
    amountColumn.setCellValueFactory(new PropertyValueFactory<>("amountAsDouble"));
    tableView.getColumns().add(fromToColumn);
    tableView.getColumns().add(usageColumn);
    tableView.getColumns().add(amountColumn);

    tableView.getItems().addAll(l);
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


  @Override
  protected void checkIntegrity() {
    if (!chartIsPopulated) {
      initialize();
      if (!chartIsPopulated) {
        throw new IllegalStateException("Chart could not be populated");
      }
    }
  }
}
