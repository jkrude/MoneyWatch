package com.jkrude.controller;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Camt;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Scanner;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

public class StartController extends ParentController {

  public Button goToPieChartBtn;
  public Button goToMonthOverviewBtn;
  public Button loadFileBtn;

  @FXML
  public void initialize() {
    //TODO
    loadFile();
  }

  // No checks to be done
  @Override
  protected void checkIntegrity() {
  }

  public void loadFile() {
    URL resource = getClass().getClassLoader().getResource("september.CSV");
    File file;
      if (resource == null) {
          AlertBox.showAlert("Error", "Failed loading september.CSV for demo purpose","",AlertType.ERROR);
          return;
      } else {
          file = new File(resource.getFile());
      }

    Camt camt;
    try {
      Scanner sc = new Scanner(file, StandardCharsets.ISO_8859_1);
      try {
        camt = new Camt(sc);
      }catch (IllegalArgumentException e){
        AlertBox.showAlert("Error",null,e.getMessage(), AlertType.ERROR);
        sc.close();
        return;
      }catch (ParseException e){
        AlertBox.showAlert("Error", null,e.getMessage(),AlertType.ERROR);
        return;
      }
      model.getCamtList().add(camt);
      sc.close();

    } catch (IOException e) {
      AlertBox.showAlert("Error", "File couldn't be opened","",AlertType.ERROR);
    }
  }

  public void goToPieChart(ActionEvent actionEvent) {
    goTo("pieChartScene", actionEvent);
  }

  public void goToMonthOverview(ActionEvent actionEvent) {
    goTo("lineChartScene", actionEvent);
  }
}
