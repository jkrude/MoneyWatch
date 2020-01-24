package com.jkrude.controller;

import com.jkrude.material.Camt;
import com.jkrude.material.Utility;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

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
  }
}
