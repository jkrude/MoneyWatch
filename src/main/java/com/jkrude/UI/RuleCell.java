package com.jkrude.UI;

import com.jkrude.category.Rule;
import com.jkrude.transaction.Transaction.TransactionField;
import java.util.Map.Entry;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

public class RuleCell extends ListCell<Rule> {

  @Override
  protected void updateItem(Rule rule, boolean isEmpty) {
    super.updateItem(rule, isEmpty);
    if (isEmpty) {
      setGraphic(null);
      setContextMenu(null);
    } else {
      FlowPane ruleHolder = new FlowPane();
      ruleHolder.getStyleClass().clear();
      ruleHolder.getStyleClass().add("rule-holder-pane");
      for (Entry<TransactionField, String> entry : rule.getIdentifierPairs().entrySet()) {
        Label keyLabel = new Label(entry.getKey().toString());
        keyLabel.getStyleClass().clear();
        keyLabel.getStyleClass().add("rule-key-label");
        Label valueLabel = new Label(entry.getValue());
        valueLabel.getStyleClass().clear();
        valueLabel.getStyleClass().add("rule-value-label");
        HBox pairBox = new HBox(keyLabel, valueLabel);
        ruleHolder.getChildren().add(pairBox);
      }
      setGraphic(ruleHolder);
    }
  }
}

