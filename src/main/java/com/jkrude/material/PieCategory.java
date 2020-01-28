package com.jkrude.material;

import com.jkrude.material.Camt.ListType;
import java.util.HashSet;

public class PieCategory {


  private String name;


  private HashSet<PieCategory.Entry> identifierList;

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


  public PieCategory(String name) {
    this.name = name;
    this.identifierList = new HashSet<>();
  }

  public PieCategory(String name, HashSet<PieCategory.Entry> identifierMap) {
    this.name = name;
    this.identifierList = identifierMap;
  }


  public String getName() {
    return name;
  }

  public HashSet<PieCategory.Entry> getIdentifierList() {
    return identifierList;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setIdentifierList(
      HashSet<PieCategory.Entry> identifierList) {
    this.identifierList = identifierList;
  }

}
