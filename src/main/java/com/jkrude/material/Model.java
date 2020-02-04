package com.jkrude.material;

import com.jkrude.controller.AbstractController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javafx.scene.Scene;

public class Model {

  private List<Camt> camtList;
  private Stack<Scene> lastSceneStack;
  private Scene currScene;
  private Profile profile;

  private Map<String, MapValue> loadedFxmlFiles;

  public Model(List<Camt> camtList) {
    if (camtList == null) {
      throw new NullPointerException();
    }
    this.camtList = camtList;
    this.lastSceneStack = new Stack<>();
    this.loadedFxmlFiles = new HashMap<>();
    this.profile = new Profile();
  }

  public Model() {
    this.camtList = new ArrayList<>();
    this.lastSceneStack = new Stack<>();
    this.loadedFxmlFiles = new HashMap<>();
    this.profile = new Profile();
  }

  public List<Camt> getCamtList() {
    return camtList;
  }

  public Stack<Scene> getLastSceneStack() {
    return lastSceneStack;
  }

  public Scene getCurrScene() {
    return currScene;
  }

  public void setCurrScene(Scene currScene) {
    this.currScene = currScene;
  }

  public Map<String, MapValue> getLoadedFxmlFiles() {
    return loadedFxmlFiles;
  }

  public void setLoadedFxmlFiles(Map<String, MapValue> loadedFxmlFiles) {
    this.loadedFxmlFiles = loadedFxmlFiles;
  }

  public Profile getProfile() {
    return profile;
  }

  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  public static class MapValue {

    private Scene scene;
    private AbstractController controller;

    public MapValue(Scene scene, AbstractController controller) {
      this.scene = scene;
      this.controller = controller;
    }

    public Scene getScene() {
      return scene;
    }

    public AbstractController getController() {
      return controller;
    }
  }
}
