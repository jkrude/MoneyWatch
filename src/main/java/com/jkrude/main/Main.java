package com.jkrude.main;

import com.jkrude.controller.ParentController;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    ParentController.init(primaryStage);
  }

  @Override
  public void stop() throws Exception {
    super.stop();
    ParentController.stop();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
