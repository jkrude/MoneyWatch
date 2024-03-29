package com.jkrude.controller;

import com.jkrude.material.Model;
import com.jkrude.transaction.TransactionContainer;
import de.saxsys.mvvmfx.ViewModel;
import java.util.List;
import javafx.beans.binding.BooleanBinding;

public class DataViewModel implements ViewModel {

  private final Model globalModel;

  public DataViewModel() {
    globalModel = Model.getInstance();
  }


  public BooleanBinding hasNoActiveDataProperty() {
    return globalModel.activeDataProperty().isNull();
  }

  public void addTransactionData(TransactionContainer transactionContainer) {
    globalModel.getTransactionContainerList().add(transactionContainer);
    if (globalModel.getTransactionContainerList().size() == 1) {
      globalModel.setActiveData(transactionContainer);
    }
  }

  public List<TransactionContainer> getTransactionContainer() {
    return globalModel.getTransactionContainerList();
  }
}
