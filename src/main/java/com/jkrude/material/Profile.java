package com.jkrude.material;

import com.jkrude.category.CategoryNode;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

public class Profile implements Observable {

  private List<InvalidationListener> categoriesInvalid;
  private CategoryNode rootCategory;


  public Profile(CategoryNode categoryNode) {
    this.rootCategory = categoryNode;
    InvalidationListener categoryListener = (observable) ->
        categoriesInvalid
            .forEach((InvalidationListener listener) -> listener.invalidated(Profile.this));
    this.rootCategory.addListener(categoryListener);
    this.categoriesInvalid = new ArrayList<>();
  }

  public Profile() {
    this(new CategoryNode("DefaultNegativeRoot"));
  }

  public CategoryNode getRootCategory() {
    return rootCategory;
  }

  public void setRootCategory(CategoryNode rootCategory) {
    this.rootCategory = rootCategory;
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
