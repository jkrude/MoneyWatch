package com.jkrude.material;

import java.util.ArrayList;
import java.util.List;

public class Model {

  private static Model instance;

  public static Model getInstance() {
    if (instance == null) {
      instance = new Model();
    }
    return instance;
  }

  private List<TransactionContainer> transactionContainerList;
  private Profile profile;


  private Model(List<TransactionContainer> transactionContainerList) {
    if (transactionContainerList == null) {
      throw new NullPointerException();
    }
    this.transactionContainerList = transactionContainerList;
    this.profile = new Profile();
  }

  private Model() {
    this.transactionContainerList = new ArrayList<>();
    this.profile = new Profile();
  }

  public List<TransactionContainer> getTransactionContainerList() {
    return transactionContainerList;
  }

  public Profile getProfile() {
    return profile;
  }

  public void setProfile(Profile profile) {
    this.profile = profile;
  }

}
