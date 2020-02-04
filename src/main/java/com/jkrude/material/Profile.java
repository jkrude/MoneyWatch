package com.jkrude.material;

import java.util.ArrayList;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Profile {

  private ListProperty<PieCategory> pieCategories;


  public Profile() {
    this.pieCategories = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
  }

  public Profile(ArrayList<PieCategory> pieCategories) {
    this.pieCategories = new SimpleListProperty<>(FXCollections.observableList(pieCategories));
  }


  public void addCategory(PieCategory category) {
    if (pieCategories == null) {
      throw new IllegalStateException("pieCategories was not initialized");
    }
    if (category == null) {
      throw new NullPointerException("category was null");
    }
    this.pieCategories.add(category);
  }

  public ObservableList<PieCategory> getPieCategories() {
    return pieCategories.get();
  }

  public ListProperty<PieCategory> getCategoriesProperty() {
    return pieCategories;
  }
}
