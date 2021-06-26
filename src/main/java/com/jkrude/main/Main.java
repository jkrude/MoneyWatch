package com.jkrude.main;

import com.jkrude.UI.NavigationRail;
import com.jkrude.controller.CategoryEditorView;
import com.jkrude.controller.CategoryEditorViewModel;
import com.jkrude.controller.DataView;
import com.jkrude.controller.DataViewModel;
import com.jkrude.controller.Prepareable;
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
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

  public enum UsableScene {
    DATA,
    TIMELINE,
    SUNBURST,
    CATEGORY_EDITOR;

    private static Prepareable lookupControllable(UsableScene scene) {
      switch (scene) {
        case DATA:
          return vTData.getCodeBehind();
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
        case DATA:
          return vTData.getView();
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

    private static final ViewTuple<DataView, DataViewModel> vTData =
        FluentViewLoader.fxmlView(DataView.class).load();
    private static final ViewTuple<TimeLineView, TimeLineViewModel> vtTimeLine =
        FluentViewLoader.fxmlView(TimeLineView.class).load();
    private static final ViewTuple<CategoryEditorView, CategoryEditorViewModel> vtCategoryEditor =
        FluentViewLoader.fxmlView(CategoryEditorView.class).load();
    private static final ViewTuple<SunburstChartView, SunburstChartViewModel> vtSunburst =
        FluentViewLoader.fxmlView(SunburstChartView.class).load();
  }

  public static final URL persistenceFile = Main.class.getClassLoader().getResource("pers.json");
  private static final URL navigationRail = Main.class
      .getResource("/com/jkrude/UI/NavigationRail.fxml");
  private static NavigationRail navigationController;
  public static BorderPane backgroundPane;
  private Model model;
  private static Stage primaryStage;


  @Override
  public void start(Stage primaryStage) throws Exception {
    assert persistenceFile != null;
    model = Model.getInstance();
    Profile profile = new Profile();
    PersistenceManager.load(profile, persistenceFile);
    model.setProfile(profile);

    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(navigationRail);
    backgroundPane = loader.load();
    navigationController = loader.getController();

    Main.primaryStage = primaryStage;

    primaryStage.setTitle("Money Watch");
    primaryStage.setScene(new Scene(backgroundPane));
    primaryStage.setMinWidth(1280);
    primaryStage.setMinHeight(800);
    if (Model.getInstance().getTransactionContainerList().isEmpty()) {
      goTo(UsableScene.DATA);
    } else {
      goTo(UsableScene.SUNBURST);
    }
    primaryStage.show();
  }

  @Override
  public void stop() throws Exception {
    assert persistenceFile != null;
    super.stop();
    PersistenceManager.save(model.getProfile(), persistenceFile);
  }

  public static void goTo(UsableScene usableScene) {
    Prepareable prepareable = UsableScene.lookupControllable(usableScene);
    Parent parent = UsableScene.lookupParent(usableScene);
    prepareable.prepare();
    backgroundPane.setCenter(parent);
    navigationController.setCurrent(usableScene);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
