package com.jkrude.controller;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Camt;
import com.jkrude.material.PieCategory;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class PieChartController extends ParentController {

  private boolean populatedChart = false;
  private boolean dirtyFlag = false; //Marks if chart is up to date with model

  @FXML
  private Button categoryButton;
  @FXML
  private PieChart pieChart;
  @FXML
  private Button backButton;

  @FXML
  public void initialize() {
    backButton.setOnAction(ParentController::goBack);
    model.getProfile().getPieCategories().addListener(
        (ListChangeListener<PieCategory>) change -> {
          dirtyFlag = true;
          if (change.next() && !change.getAddedSubList().isEmpty()) {
            change.getAddedSubList().forEach(
                item -> item.getIdentifierList().addListener(
                    (ListChangeListener<? super PieCategory.Entry>) change1 -> dirtyFlag = true
                )
            );
          }
        }
    );
    model.getProfile().getPieCategories().forEach(
        pieCategory -> {
          pieCategory.getIdentifierList().addListener(
              (ListChangeListener<? super PieCategory.Entry>) change -> dirtyFlag = true
          );
          pieCategory.getName().addListener(
              (observableValue, s, t1) -> dirtyFlag = true);
        }
    );
  }

  private void setupChart() {
    try {
      pieChart.getData().clear();
      ObservableList<Data> data = model.getCamtList().get(0)
          .getPieChartData(model.getProfile().getPieCategories());
      // Colors are only displayed for positive values
      data.forEach(d -> d.setPieValue(Math.abs(d.getPieValue())));
      pieChart.getData().addAll(data);
      setupToolTip(pieChart.getData());
      populatedChart = true;
      dirtyFlag = false;
    } catch (NullPointerException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
      AlertBox.showAlert("Fatal Error", "Internal Error", "", AlertType.ERROR);
    }
  }

  private void setupToolTip(final ObservableList<PieChart.Data> chartData){
    for (final PieChart.Data data : chartData) {
      String displayingText = String.valueOf(data.getPieValue()) + '€';
      Tooltip tlp = new Tooltip(displayingText);
      tlp.setShowDelay(new Duration(100));
      Tooltip.install(data.getNode(),tlp);
    }

  }

  public void goToCategories(ActionEvent event) {
    ParentController.goTo("categoryEditor", event);
  }
}
