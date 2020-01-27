package com.jkrude.material;

import java.util.HashSet;

public class Profile {

  private HashSet<PieCategory> pieCategories;

  public Profile(HashSet<PieCategory> pieCategories) {
    this.pieCategories = pieCategories;
  }


  public void addCategory(PieCategory category){
    if(pieCategories == null){
      throw new IllegalStateException("pieCategories was not initialized");
    }
    if(category == null){
      throw new NullPointerException("category was null");
    }

    this.pieCategories.add(category);
  }

  public HashSet<PieCategory> getPieCategories() {
    return pieCategories;
  }

  public void setPieCategories(HashSet<PieCategory> pieCategories) {
    this.pieCategories = pieCategories;
  }
}
