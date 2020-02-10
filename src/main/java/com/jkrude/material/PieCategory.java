package com.jkrude.material;

import com.jkrude.material.Camt.CamtEntry;
import java.util.ArrayList;
import java.util.function.Predicate;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PieCategory {

  private StringProperty name;

  private ListProperty<Predicate<CamtEntry>> identifierList;

  
  public PieCategory(StringProperty name) {
    this.name = name;
    this.identifierList = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
  }

  public PieCategory(String name) {
    this.name = new SimpleStringProperty(name);
    this.identifierList = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
  }

  public PieCategory(StringProperty name, SimpleListProperty<Predicate<CamtEntry>> identifierList) {
    this.name = name;
    this.identifierList = identifierList;
  }

  public StringProperty getName() {
    return name;
  }

  public ObservableList<Predicate<CamtEntry>> getIdentifierList() {
    return identifierList.getValue();
  }

  public ListProperty<Predicate<CamtEntry>> getIdentifierProperty() {
    return identifierList;
  }

  public void setName(StringProperty name) {
    this.name = name;
  }

  public void setIdentifierList(ListProperty<Predicate<CamtEntry>> identifierList) {
    this.identifierList = identifierList;
  }
}
