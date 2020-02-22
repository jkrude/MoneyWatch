package com.jkrude.controller;

import com.jkrude.material.Camt.CamtEntry;
import com.jkrude.material.Money;
import com.jkrude.material.PieCategory;
import com.jkrude.material.Rule;
import java.math.BigDecimal;
import java.util.HashMap;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
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

  @Override
  protected void checkIntegrity() {
    if (dirtyFlag || !populatedChart) {
      setupChart();
    }
  }

  @FXML
  public void initialize() {
    backButton.setOnAction(ParentController::goBack);

    // Setup change-listener for data-invalidation
    model.getProfile().getPieCategories().addListener(
        (ListChangeListener<PieCategory>) change -> {
          dirtyFlag = true;
          if (change.next() && !change.getAddedSubList().isEmpty()) {
            change.getAddedSubList().forEach(
                item -> item.getIdentifierList().addListener(
                    (ListChangeListener<? super Rule>) change1 -> dirtyFlag = true
                )
            );
          }
        }
    );
    model.getProfile().getPieCategories().forEach(
        pieCategory -> {
          pieCategory.getIdentifierList().addListener(
              (ListChangeListener<? super Rule>) change -> dirtyFlag = true
          );
          pieCategory.getName().addListener(
              (observableValue, s, t1) -> dirtyFlag = true);
        }
    );
  }

  private void setupChart() {
    pieChart.getData().clear();
    ObservableList<CamtEntry> source = model.getCamtList().get(0).getSource();
    ObservableList<PieCategory> categories = model.getProfile().getPieCategories();
    pieChart.getData().addAll(genDataFromSource(source, categories));
    setupToolTip(pieChart.getData());
    populatedChart = true;
    dirtyFlag = false;
  }

  private ObservableList<PieChart.Data> genDataFromSource(ObservableList<CamtEntry> source,
      ObservableList<PieCategory> categories) {
    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
    HashMap<StringProperty, Money> categoryHashMap = new HashMap<>();
    categories.forEach(pieCategory -> categoryHashMap.put(pieCategory.getName(), new Money(0)));
    for (CamtEntry camtEntry : source) {
      for (PieCategory category : categories) {
        for (Rule rule : category.getIdentifierList()) {
          if (rule.getPredicate().test(camtEntry)) {
            if (camtEntry.getDataPoint().getAmount().getAmount().compareTo(BigDecimal.ZERO) < 0) {
              categoryHashMap.get(category.getName()).add(camtEntry.getDataPoint().getAmount());
            } else {
              //FIXME what to do with positive values?
              throw new IllegalArgumentException("Found matching positive value");
            }
          }
        }

      }
    }

    // Colors are only displayed for positive values
    categoryHashMap.forEach(
        (key, value) -> pieChartData
            .add(new Data(key.get(), Math.abs(value.getAmount().doubleValue()))));
    return pieChartData;
  }

  private void setupToolTip(final ObservableList<PieChart.Data> chartData) {
    for (final PieChart.Data data : chartData) {
      String displayingText = String.valueOf(data.getPieValue()) + '€';
      Tooltip tlp = new Tooltip(displayingText);
      tlp.setShowDelay(new Duration(100));
      Tooltip.install(data.getNode(), tlp);
    }

  }

  public void goToCategories(ActionEvent event) {
    ParentController.goTo("categoryEditor", event);
  }

  /*
  Getter
   */
  public PieChart getPieChart() {
    return pieChart;
  }
}
