package com.jkrude.main;

import com.jkrude.controller.CategoryEditorView;
import com.jkrude.controller.CategoryEditorViewModel;
import com.jkrude.controller.Prepareable;
import com.jkrude.controller.StartView;
import com.jkrude.controller.StartViewModel;
import com.jkrude.controller.SunburstChartView;
import com.jkrude.controller.SunburstChartViewModel;
import com.jkrude.controller.TimeLineView;
import com.jkrude.controller.TimeLineViewModel;
import com.jkrude.material.Model;
import com.jkrude.material.PersistenceManager;
import com.jkrude.material.Profile;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import java.net.URL;
import java.util.Stack;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

  public enum UsableScene {
    START,
    TIMELINE,
    SUNBURST,
    CATEGORY_EDITOR;

    private static Prepareable lookupControllable(UsableScene scene) {
      switch (scene) {
        case START:
          return vTStart.getCodeBehind();
        case TIMELINE:
          return vtTimeLine.getCodeBehind();
        case SUNBURST:
          return vtSunburst.getCodeBehind();
        case CATEGORY_EDITOR:
          return vtCategoryEditor.getCodeBehind();

        default:
          throw new IllegalStateException("Unexpected value: " + scene);
      }
    }

    private static Parent lookupParent(UsableScene scene) {
      switch (scene) {
        case START:
          return vTStart.getView();
        case TIMELINE:
          return vtTimeLine.getView();
        case SUNBURST:
          return vtSunburst.getView();
        case CATEGORY_EDITOR:
          return vtCategoryEditor.getView();

        default:
          throw new IllegalStateException("Unexpected value: " + scene);
      }
    }

    private static final ViewTuple<StartView, StartViewModel> vTStart =
        FluentViewLoader.fxmlView(StartView.class).load();
    private static final ViewTuple<TimeLineView, TimeLineViewModel> vtTimeLine =
        FluentViewLoader.fxmlView(TimeLineView.class).load();
    private static final ViewTuple<CategoryEditorView, CategoryEditorViewModel> vtCategoryEditor =
        FluentViewLoader.fxmlView(CategoryEditorView.class).load();
    private static final ViewTuple<SunburstChartView, SunburstChartViewModel> vtSunburst =
        FluentViewLoader.fxmlView(SunburstChartView.class).load();
  }

  public static final URL persistenceFile = Main.class.getClassLoader().getResource("pers.json");
  private Model model;
  private static Stack<UsableScene> callStack;
  private static Stage primaryStage;
  private static UsableScene current;


  @Override
  public void start(Stage primaryStage) throws Exception {
    assert persistenceFile != null;
    model = Model.getInstance();
    Profile profile = new Profile();
    PersistenceManager.load(profile, persistenceFile);
    model.setProfile(profile);
    callStack = new Stack<>();

    Main.primaryStage = primaryStage;

    primaryStage.setTitle("Money Watch");
    primaryStage.setScene(new Scene(new Pane()));
    goTo(UsableScene.START, primaryStage);
    primaryStage.show();
  }

  @Override
  public void stop() throws Exception {
    assert persistenceFile != null;
    super.stop();
    PersistenceManager.save(model.getProfile(), persistenceFile);
  }

  public static void goTo(UsableScene usableScene, Stage stage) {
    Prepareable prepareable = UsableScene.lookupControllable(usableScene);
    Parent parent = UsableScene.lookupParent(usableScene);
    prepareable.prepare();
    if (current != null) { // First call -> startScene
      callStack.push(Main.current);
    }
    Main.current = usableScene;
    stage.getScene().setRoot(parent);

  }

  public static void goTo(UsableScene scene) {
    goTo(scene, primaryStage);
  }

  public static void goBack(Stage stage) {
    if (callStack.isEmpty()) {
      goTo(UsableScene.START, primaryStage);
      return;
    }
    UsableScene previousScene = callStack.pop();
    Prepareable prepareable = UsableScene.lookupControllable(previousScene);
    prepareable.prepare();
    Main.current = previousScene;
    stage.getScene().setRoot(UsableScene.lookupParent(previousScene));
  }

  public static void goBack() {
    goBack(primaryStage);
  }


  public static void main(String[] args) {
    launch(args);
  }
}
