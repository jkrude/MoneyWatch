package com.jkrude.controller;

import com.jkrude.main.Main;
import com.jkrude.main.Main.UsableScene;
import com.jkrude.material.AlertBox;
import com.jkrude.material.Camt;
import com.jkrude.material.Model;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Scanner;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class StartController extends Controller {

  public Button goToPieChartBtn;
  public Button goToMonthOverviewBtn;
  public Button loadFileBtn;

  @FXML
  public void initialize() {
    //TODO
    //loadFile();
  }

  // No checks to be done
  @Override
  public void prepare() {
  }

  private File fileBrowser() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("CSV import");
    fileChooser.getExtensionFilters().add(
        new ExtensionFilter("CSV", "*.CSV"));
    fileChooser.setInitialDirectory(new File(
        "/home/jakob/Documents/Coding/IntelliJ-Projekte/MoneyWatch/src/main/resources/"));
    return fileChooser
        .showOpenDialog(new Stage());
  }

  public void loadFile() {
    File file = fileBrowser();
    if (file == null) {
      // No file selected. Dialog was canceled.
      return;
    } else if (!file.canRead()) {
      AlertBox
          .showAlert("Error", "Fehler beim Import.",
              "Datei konnte nicht geöffnet werden.", AlertType.ERROR);
    }
    Camt camt;
    try {
      Scanner sc = new Scanner(file, StandardCharsets.ISO_8859_1);
      try {
        camt = new Camt(sc);
      } catch (IllegalArgumentException e) {
        AlertBox.showAlert("Error", null, e.getMessage(), AlertType.ERROR);
        sc.close();
        return;
      } catch (ParseException e) {
        AlertBox.showAlert("Error", null, e.getMessage(), AlertType.ERROR);
        return;
      }
      Model.getInstance().getCamtList().add(camt);
      sc.close();

    } catch (IOException e) {
      AlertBox.showAlert("Error", "File couldn't be opened", "", AlertType.ERROR);
    }
  }

  public void goToPieChart(ActionEvent actionEvent) {
    if (Model.getInstance().getCamtList() == null || Model.getInstance().getCamtList().isEmpty()) {
      AlertBox.showAlert("Daten benötigt!", "Noch keine CSV Dateien geladen.",
          "Wähle im Hauptmenü: Open File",
          AlertType.ERROR);
    } else {
      Main.goTo(UsableScene.PIECHART);
    }
  }

  public void goToMonthOverview(ActionEvent actionEvent) {
    if (Model.getInstance().getCamtList() == null || Model.getInstance().getCamtList().isEmpty()) {
      AlertBox.showAlert("Daten benötigt!", "Noch keine CSV Dateien geladen.",
          "Wähle im Hauptmenü: Open File",
          AlertType.ERROR);
    } else {
      Main.goTo(UsableScene.TIMELINE);
    }
  }
}
