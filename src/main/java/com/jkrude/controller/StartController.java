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
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.transform.Shear;
import javafx.stage.Stage;

public class StartController extends AbstractController {

  public Button goToPieChartBtn;
  public Button goToMonthOverviewBtn;
  public Button loadFileBtn;


  @FXML
  private void handleButtonAction(ActionEvent event) {
    System.out.println("You clicked me!");

    Stage stageTheEventSourceNodeBelongs = (Stage) ((Node) event.getSource()).getScene()
        .getWindow();

    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("ignoreListScene.fxml"));

      stageTheEventSourceNodeBelongs.setScene(new Scene(loader.load()));
    } catch (IOException e) {
      e.printStackTrace();
    }
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

    super.goTo("pieChartScene.fxml", actionEvent);
  }

  public void goToMonthOverview(ActionEvent actionEvent) {
    super.goTo("lineChartScene.fxml", actionEvent);

  }
}
