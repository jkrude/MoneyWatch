package com.jkrude.main;

import com.jkrude.controller.Controller;
import com.jkrude.material.Model;
import com.jkrude.material.PersistenceManager;
import com.jkrude.material.Profile;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

public class Main extends Application {

  public enum UsableScene {
    START(Main.class
        .getResource("/MainScene/start.fxml")),
    TIMELINE(Main.class
        .getResource("/MainScene/lineChart.fxml")),
    //PIECHART(Main.class
    //    .getResource("/MainScene/pieChart.fxml")),
    CATEGORY_EDITOR(Main.class
        .getResource("/MainScene/hierarchicalCategoryEditor.fxml"));

    private URL resourceURL;

    UsableScene(URL resourceURL) {
      this.resourceURL = resourceURL;
    }

    public URL getResourceURL() {
      return resourceURL;
    }
  }


  public static final URL persistenceFile = Main.class.getClassLoader().getResource("pers.json");
  private Model model;
  private static Map<UsableScene, Pair<Controller, Scene>> loadedFxmlFiles;
  private static Stack<Pair<Controller, Scene>> callStack;
  private static Stage primaryStage;
  private static Pair<Controller, Scene> current;


  @Override
  public void start(Stage primaryStage) throws Exception {
    model = Model.getInstance();
    Profile profile = new Profile();
    PersistenceManager.load(profile, persistenceFile);
    model.setProfile(profile);
    loadedFxmlFiles = new HashMap<>();
    callStack = new Stack<>();

    Main.primaryStage = primaryStage;

    for (UsableScene usableScene : UsableScene.values()) {
      try {
        FXMLLoader fxmlLoader = new FXMLLoader(usableScene.getResourceURL());
        Scene scene = new Scene(fxmlLoader.load());
        Controller controller = fxmlLoader.getController();
        loadedFxmlFiles.put(usableScene, new Pair<>(controller, scene));

      } catch (IOException e) {
        System.err.println("A FXML resource could not be loaded!");
        e.printStackTrace();
      }
    }
    primaryStage.setTitle("Money Watch");
    goTo(UsableScene.START, primaryStage);
    primaryStage.show();
  }

  @Override
  public void stop() throws Exception {
    super.stop();
    PersistenceManager.save(model.getProfile(), persistenceFile);
  }

  public static void goTo(UsableScene scene, Stage stage) {
    if (!loadedFxmlFiles.containsKey(scene)) {
      throw new IllegalStateException("Scene was not yet loaded");
    }
    Pair<Controller, Scene> next = loadedFxmlFiles.get(scene);
    next.getKey().prepare();
    if (current != null) { // First call -> startScene
      callStack.push(Main.current);
    }
    Main.current = next;
    stage.setScene(next.getValue());
  }

  public static void goTo(UsableScene scene) {
    goTo(scene, primaryStage);
  }

  public static void goBack(Stage stage) {
    if (callStack.isEmpty()) {
      // TODO: What to do?
      throw new IllegalStateException();
    }
    Pair<Controller, Scene> previous = callStack.pop();
    previous.getKey().prepare();
    Main.current = previous;
    stage.setScene(previous.getValue());
  }

  public static void goBack() {
    goBack(primaryStage);
  }


  public static void main(String[] args) {
    launch(args);
  }
}
