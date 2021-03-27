package com.jkrude.material;

import com.jkrude.transaction.TransactionContainer;
import java.util.List;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

public class Model {

  private static Model instance;

  public static Model getInstance() {
    if (instance == null) {
      instance = new Model();
    }
    return instance;
  }

  private final ListProperty<TransactionContainer> transactionContainerList;
  private final ObjectProperty<TransactionContainer> activeDataProperty;
  private Profile profile;


  private Model(List<TransactionContainer> transactionContainerList) {
    assert transactionContainerList != null;
    this.transactionContainerList = new SimpleListProperty<>(
        FXCollections.observableArrayList(transactionContainerList));
    activeDataProperty = new SimpleObjectProperty<>();
    if (!transactionContainerList.isEmpty()) {
      activeDataProperty.set(transactionContainerList.get(0));
    }
    this.profile = new Profile();
  }

  private Model() {
    this.transactionContainerList = new SimpleListProperty<>(FXCollections.observableArrayList());
    this.activeDataProperty = new SimpleObjectProperty<>();
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

  public TransactionContainer getActiveData() {
    return activeDataProperty.get();
  }

  public ObjectProperty<TransactionContainer> activeDataProperty() {
    return activeDataProperty;
  }

  public void setActiveData(TransactionContainer activeDataProperty) {
    this.activeDataProperty.set(activeDataProperty);
    if (!this.transactionContainerList.contains(activeDataProperty)) {
      this.transactionContainerList.add(activeDataProperty);
    }
  }
}
