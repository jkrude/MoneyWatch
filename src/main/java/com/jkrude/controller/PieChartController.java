package com.jkrude.controller;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Camt.CamtEntry;
import com.jkrude.material.Camt.DateDataPoint;
import com.jkrude.material.Money;
import com.jkrude.material.PieCategory;
import com.jkrude.material.Rule;
import com.jkrude.material.UI.TableStageController;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

public class PieChartController extends ParentController {

  private boolean populatedChart = false;
  private boolean dirtyFlag = false; //Marks if chart is up to date with model
  private Map<String, List<CamtEntry>> chartDataMap;

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
    chartDataMap = new HashMap<>();
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
    Pair<ObservableList<Data>, Map<String, List<CamtEntry>>> result = genDataFromSource(source, categories);
    if(result.getValue() != null && !result.getValue().isEmpty()){
      AlertBox.showAlert("Ignorierte gefundene Überweisungen","Manche Überweisungen werden nicht in der Grafik genutzt","Im Diagramm werden nur die Ausgaben betrachtet. Einige Regeln konnten allerdings auch auf Eingaben angewendet werden",
          AlertType.WARNING);
    }
    pieChart.getData().addAll(result.getKey());
    setupToolTip(pieChart.getData());
    setupPopUp(pieChart.getData());
    populatedChart = true;
    dirtyFlag = false;
  }

  private Pair<ObservableList<Data>,Map<String, List<CamtEntry>>> genDataFromSource(ObservableList<CamtEntry> source,
      ObservableList<PieCategory> categories) {

    Map<String, List<CamtEntry>> ignoredPositiveEntries = new HashMap<>();
    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
    HashMap<StringProperty, Money> categoryHashMap = new HashMap<>();
    chartDataMap.clear();
    categories.forEach(pieCategory -> {
      categoryHashMap.put(pieCategory.getName(), new Money(0));
      chartDataMap.put(pieCategory.getName().get(), new ArrayList<>());
    });
    for (CamtEntry camtEntry : source) {
      for (PieCategory category : categories) {
        for (Rule rule : category.getIdentifierList()) {
          if (rule.getPredicate().test(camtEntry)) {
            if (camtEntry.getDataPoint().getAmount().getAmount().compareTo(BigDecimal.ZERO) < 0) {
              categoryHashMap.get(category.getName()).add(camtEntry.getDataPoint().getAmount());
              chartDataMap.get(category.getName().get()).add(camtEntry);
            } else {
              if(ignoredPositiveEntries.containsKey(category.getName().get())){
                ignoredPositiveEntries.get(category.getName().get()).add(camtEntry);
              }else{
                List<CamtEntry> list = new ArrayList<>();
                list.add(camtEntry);
                ignoredPositiveEntries.put(category.getName().get(),list);
              }
            }
          }
        }
      }
    }

    // Colors are only displayed for positive values
    categoryHashMap.forEach(
        (key, value) -> pieChartData
            .add(new Data(key.get(), Math.abs(value.getAmount().doubleValue()))));
    return new Pair<>(pieChartData,ignoredPositiveEntries);
  }

  private void setupToolTip(final ObservableList<PieChart.Data> chartData) {
    for (final PieChart.Data data : chartData) {
      String displayingText = String.valueOf(data.getPieValue()) + '€';
      Tooltip tlp = new Tooltip(displayingText);
      tlp.setShowDelay(new Duration(100));
      Tooltip.install(data.getNode(), tlp);
    }
  }

  private void setupPopUp(final ObservableList<PieChart.Data> chartData){
    for (final PieChart.Data data: chartData){
      try{
        URL resource = getClass().getClassLoader().getResource("table.fxml");
        if (resource != null) {
          FXMLLoader loader = new FXMLLoader(resource);
          Pane pane = loader.load();
          TableStageController controller = loader.getController();
          controller.setItems(chartDataMap.get(data.getName()));
          Stage stage = new Stage();
          Scene scene = new Scene(pane);
          stage.setScene(scene);
          data.getNode().setOnMouseClicked(
              event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                  stage.showAndWait();
                }
              });
        }else{
          AlertBox.showAlert("Fatal Error","","Internal Error", AlertType.ERROR);
        }
      } catch (IOException e) {
        AlertBox.showAlert("Fatal Error","","Internal Error", AlertType.ERROR);
        e.printStackTrace();
      }
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
