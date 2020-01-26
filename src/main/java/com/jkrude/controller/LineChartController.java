package com.jkrude.controller;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Camt;
import com.jkrude.material.Camt.DateDataPoint;
import com.jkrude.material.Money;
import com.jkrude.material.Utility;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;

public class LineChartController extends AbstractController implements Initializable {

  public LineChart<Number, Number> lineChart;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Camt camt = model.getCamtList().get(0);
    NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
    xAxis.setAutoRanging(false);
    xAxis.setLowerBound(camt.getLineChartData().get(0).getXValue().longValue());
    xAxis.setUpperBound(
        camt.getLineChartData().get(camt.getLineChartData().size() - 1).getXValue()
            .longValue());
    xAxis.setTickUnit(86400000);
    xAxis.setTickLabelFormatter(Utility.convertFromInstant());

    XYChart.Series<Number, Number> series = new XYChart.Series<>();
    series.setName("Month-Overview");
    series.getData().addAll(camt.getLineChartData());
    lineChart.getData().add(series);

    // Add Tooltip for every data-point
    for (Data<Number, Number> d : series.getData()) {
      String text = Utility.convertFromInstant().toString(d.getXValue().longValue()) + "\n" +
          "Total : " + d.getYValue();
      Tooltip tlp = new Tooltip(text);
      tlp.setShowDelay(new Duration(100));
      Tooltip.install(d.getNode(), tlp);
      List<DateDataPoint> l = camt.getDPsForChartData(d);
      StringBuilder msg = new StringBuilder();
      Money totalThatDay = new Money(0);

      ContextMenu contextMenu = new ContextMenu();
      MenuItem item1 = new MenuItem("Menu Item 1");
      item1.setOnAction(new EventHandler<ActionEvent>() {

        @Override
        public void handle(ActionEvent event) {
          System.out.println("Action Event");
        }
      });


      // Add MenuItem to ContextMenu
      contextMenu.getItems().add(item1);

      for (DateDataPoint dateDataPoint : l){
        msg.append("From/To: ")
              .append(dateDataPoint.getReceiverOrPayer()).append("\n")
            .append("Usage: ")
              .append(dateDataPoint.getUsage()).append("\n")
            .append("Amount: ")
              .append(dateDataPoint.getAmount().getValue().toString()).append("\n");
        totalThatDay.add(dateDataPoint.getAmount());
      }
      // Add Alert with more Information
      final String textAndTotal = text + "\n" + "Total at this date:" + totalThatDay.getValue().toString();
      d.getNode().setOnMouseClicked(
          event -> {
            if(event.getButton() == MouseButton.PRIMARY) {
              AlertBox.showAlert("Additional Information", textAndTotal, msg.toString(),
                  AlertType.INFORMATION);
            }});



      d.getNode().setOnContextMenuRequested(
          event -> contextMenu.show(d.getNode(), event.getScreenX(), event.getScreenY()));
    }
  }
}
