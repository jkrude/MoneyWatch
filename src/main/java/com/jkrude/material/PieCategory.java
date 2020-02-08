package com.jkrude.material;

import com.jkrude.material.Camt.ListType;
import java.util.ArrayList;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PieCategory {


  private StringProperty name;


  private ListProperty<Entry> identifierList;

  public static class Entry {

    private String pattern;
    private Camt.ListType type;

    public Entry(String pattern, Camt.ListType type) {
      this.pattern = pattern;
      this.type = type;
    }

    public String getPattern() {
      return pattern;
    }

    public ListType getType() {
      return type;
    }

    public void setPattern(String pattern) {
      this.pattern = pattern;
    }

    public void setType(ListType type) {
      this.type = type;
    }
  }


  public PieCategory(StringProperty name) {
    this.name = name;
    this.identifierList = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
  }
  public PieCategory(String name){
    this.name =new SimpleStringProperty(name);
    this.identifierList = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
  }

  public PieCategory(StringProperty name, SimpleListProperty<Entry> identifierList) {
    this.name = name;
    this.identifierList = identifierList;
  }

  public StringProperty getName() {
    return name;
  }

  public ObservableList<Entry> getIdentifierList() {
    return identifierList.getValue();
  }

  public ListProperty<Entry> getIdentifierProperty() {
    return identifierList;
  }

  public void setName(StringProperty name) {
    this.name = name;
  }

  public void setIdentifierList(ListProperty<Entry> identifierList) {
    this.identifierList = identifierList;
  }
}
