package com.jkrude.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import com.jkrude.material.AlertBox;
import com.jkrude.material.Model;

public abstract class AbstractController {
    static protected Model model = new Model();

    protected void goTo(String pathToFxml, ActionEvent actionEvent){
        Stage stageTheEventSourceNodeBelongs = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(pathToFxml));
            stageTheEventSourceNodeBelongs.setScene(new Scene(loader.load()));
        } catch (IOException | IllegalStateException e) {
            AlertBox.display("Fatal Error", "Unable to go to page!\n Could not load fxml file.");
            e.printStackTrace();
        }

    }
}
