package com.jkrude.controller;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Camt;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
          AlertBox.display("Error", "Failed loading september.CSV for demo purpose");
          return;
      } else {
          file = new File(resource.getFile());
      }

    Camt camt;
    try {
      Scanner sc = new Scanner(file, StandardCharsets.ISO_8859_1);
      camt = new Camt(sc);
      model.getCamtList().add(camt);
      sc.close();

    } catch (IOException e) {
      AlertBox.display("Error", "File couldn't be opened");
    }
  }

  public void goToPieChart(ActionEvent actionEvent) {

    super.goTo("pieChartScene.fxml", actionEvent);
  }

  public void goToMonthOverview(ActionEvent actionEvent) {
    super.goTo("lineChartScene.fxml", actionEvent);

  }
}
