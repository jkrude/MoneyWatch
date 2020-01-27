package com.jkrude.material;

import java.util.HashMap;
import java.util.HashSet;

public class PieCategory {

  public enum IdentifierType{
    IBAN,
    USAGE,
    OTHER_PARTY
  }

  private String name;


  private HashMap<String,IdentifierType> identifierMap;


  public PieCategory(String name){
    this.name = name;
    this.identifierMap = new HashMap<>();
  }
  public PieCategory(String name, HashMap<String, IdentifierType> identifierMap){
    this.name = name;
    this.identifierMap = identifierMap;
  }


  public String getName() {
    return name;
  }

  public HashMap<String, IdentifierType> getIdentifierMap() {
    return identifierMap;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setIdentifierMap(
      HashMap<String, IdentifierType> identifierMap) {
    this.identifierMap = identifierMap;
  }

}
