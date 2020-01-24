package com.jkrude.main;

import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    URL resource = Main.class.getClassLoader().getResource("startScene.fxml");
    assert resource != null;
    Parent root = FXMLLoader.load(resource);
    primaryStage.setTitle("Money Watch");
    primaryStage.setScene(new Scene(root));
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
