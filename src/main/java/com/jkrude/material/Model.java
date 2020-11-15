package com.jkrude.material;

import java.util.List;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class Model {

  private static Model instance;

  public static Model getInstance() {
    if (instance == null) {
      instance = new Model();
    }
    return instance;
  }

  private ListProperty<TransactionContainer> transactionContainerList;
  private Profile profile;


  private Model(List<TransactionContainer> transactionContainerList) {
    if (transactionContainerList == null) {
      throw new NullPointerException();
    }
    this.transactionContainerList = new SimpleListProperty<>(
        FXCollections.observableArrayList(transactionContainerList));
    this.profile = new Profile();
  }

  private Model() {
    this.transactionContainerList = new SimpleListProperty<>(FXCollections.observableArrayList());
    this.profile = new Profile();
  }

  public List<TransactionContainer> getTransactionContainerList() {
    return transactionContainerList.get();
  }

  public ListProperty<TransactionContainer> transactionListProperty() {
    return transactionContainerList;
  }

  public Profile getProfile() {
    return profile;
  }

  public void setProfile(Profile profile) {
    this.profile = profile;
  }

}
