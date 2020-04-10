package com.jkrude.material;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class Profile implements Observable {

  private List<InvalidationListener> categoriesInvalid;
  private ListProperty<PieCategory> pieCategories;

  private InvalidationListener categoryListener = new InvalidationListener() {
    @Override
    public void invalidated(Observable observable) {
      categoriesInvalid
          .forEach((InvalidationListener listener) -> listener.invalidated(Profile.this));
    }
  };


  public Profile() {
    this.pieCategories = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
    ListChangeListener<PieCategory> listChangeListener = new ListChangeListener<PieCategory>() {
      @Override
      public void onChanged(Change<? extends PieCategory> change) {
        while (change.next()) {
          change.getAddedSubList().forEach(category -> category.addListener(categoryListener));
          change.getRemoved().forEach(category -> category.removeListener(categoryListener));
          categoriesInvalid
              .forEach((InvalidationListener listener) -> listener.invalidated(Profile.this));
        }
      }
    };
    this.pieCategories.addListener(listChangeListener);
    this.categoriesInvalid = new ArrayList<>();
  }

  public Profile(ArrayList<PieCategory> pieCategories) {
    this();
    this.pieCategories.addAll(pieCategories);
    this.pieCategories.forEach(
        pieCategory -> pieCategory.addListener(categoryListener)
    );
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

  @Override
  public void addListener(InvalidationListener invalidationListener) {
    categoriesInvalid.add(invalidationListener);
  }

  @Override
  public void removeListener(InvalidationListener invalidationListener) {
    categoriesInvalid.add(invalidationListener);
  }
}
