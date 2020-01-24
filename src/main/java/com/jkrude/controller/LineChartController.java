package com.jkrude.controller;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Camt;
import com.jkrude.material.Camt.DataPoint;
import com.jkrude.material.Money;
import com.jkrude.material.Utility;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
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
      List<DataPoint> l = camt.getDPsForChartData(d);
      StringBuilder msg = new StringBuilder();
      Money totalThatDay = new Money(0);
      for (DataPoint dataPoint : l){
        msg.append("From/To: ")
              .append(dataPoint.getReceiverOrPayer()).append("\n")
            .append("Usage: ")
              .append(dataPoint.getUsage()).append("\n")
            .append("Amount: ")
              .append(dataPoint.getAmount().getValue().toString()).append("\n");
        totalThatDay.add(dataPoint.getAmount());
      }
      // Add Alert with more Information
      final String textAndTotal = text + "\n" + "Total at this date:" + totalThatDay.getValue().toString();
      d.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED,
          e -> AlertBox.showAlert("Additional Information",textAndTotal ,msg.toString(), AlertType.INFORMATION));
    }
  }
}
