package com.jkrude.material;

import java.util.HashMap;

public class PieCategory {
  

  private String name;


  private HashMap<String, Camt.ListType> identifierMap;


  public PieCategory(String name) {
    this.name = name;
    this.identifierMap = new HashMap<>();
  }

  public PieCategory(String name, HashMap<String, Camt.ListType> identifierMap) {
    this.name = name;
    this.identifierMap = identifierMap;
  }


  public String getName() {
    return name;
  }

  public HashMap<String, Camt.ListType> getIdentifierMap() {
    return identifierMap;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setIdentifierMap(
      HashMap<String, Camt.ListType> identifierMap) {
    this.identifierMap = identifierMap;
  }

}
