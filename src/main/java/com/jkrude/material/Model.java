package com.jkrude.material;

import java.util.ArrayList;
import java.util.List;

public class Model {

  private static Model instance;

  public static Model getInstance() {
    if (instance == null) {
      instance = new Model();
    }
    return instance;
  }

  private List<Camt> camtList;
  private Profile profile;


  private Model(List<Camt> camtList) {
    if (camtList == null) {
      throw new NullPointerException();
    }
    this.camtList = camtList;
    this.profile = new Profile();
  }

  private Model() {
    this.camtList = new ArrayList<>();
    this.profile = new Profile();
  }

  public List<Camt> getCamtList() {
    return camtList;
  }

  public Profile getProfile() {
    return profile;
  }

  public void setProfile(Profile profile) {
    this.profile = profile;
  }

}
