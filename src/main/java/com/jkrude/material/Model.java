package com.jkrude.material;

import com.jkrude.controller.ParentController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javafx.scene.Scene;

public class Model {

  private List<Camt> camtList;
  private Stack<ScnCntrlPair> lastSceneStack;
  private ScnCntrlPair currScenePair;
  private Profile profile;

  private Map<String, ScnCntrlPair> loadedFxmlFiles;

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

  public Stack<ScnCntrlPair> getLastSceneStack() {
    return lastSceneStack;
  }

  public ScnCntrlPair getCurrScenePair() {
    return currScenePair;
  }

  public void setCurrScenePair(ScnCntrlPair currScenePair) {
    this.currScenePair = currScenePair;
  }

  public Map<String, ScnCntrlPair> getLoadedFxmlFiles() {
    return loadedFxmlFiles;
  }

  public void setLoadedFxmlFiles(Map<String, ScnCntrlPair> loadedFxmlFiles) {
    this.loadedFxmlFiles = loadedFxmlFiles;
  }

  public Profile getProfile() {
    return profile;
  }

  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  public static class ScnCntrlPair {

    private Scene scene;
    private ParentController controller;

    public ScnCntrlPair(Scene scene, ParentController controller) {
      this.scene = scene;
      this.controller = controller;
    }

    public Scene getScene() {
      return scene;
    }

    public ParentController getController() {
      return controller;
    }
  }
}
