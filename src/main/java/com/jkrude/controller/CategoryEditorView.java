package com.jkrude.controller;

import com.jkrude.UI.ColorPickerDialog;
import com.jkrude.UI.JFXChoiceDialog;
import com.jkrude.UI.NewCategoryDialog;
import com.jkrude.UI.RuleCell;
import com.jkrude.UI.RuleDialog;
import com.jkrude.UI.RuleDialog.Builder;
import com.jkrude.UI.TextInputDialog;
import com.jkrude.category.CategoryNode;
import com.jkrude.category.Rule;
import com.jkrude.transaction.Transaction.TransactionField;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class CategoryEditorView implements FxmlView<CategoryEditorViewModel>, Initializable,
    Prepareable {

  @FXML private AnchorPane rulePane;
  @FXML private Button addRuleBtn;
  @FXML private Button editRuleBtn;
  @FXML private Button deleteRuleBtn;
  @FXML private ListView<Rule> ruleView;
  @FXML private TreeView<CategoryNode> categoryTreeView;
  @FXML private SimpleBooleanProperty invalidatedProperty;


  @InjectViewModel
  private CategoryEditorViewModel viewModel;
  private final InvalidationListener invalidator = (observable -> invalidatedProperty.set(true));


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    invalidatedProperty = new SimpleBooleanProperty(false);
    rulePane.visibleProperty().bind(categoryTreeView.getSelectionModel().selectedItemProperty()
        .isNotNull());
    ruleView.setPlaceholder(new Label("This category has no rules yet"));
    ruleView.setCellFactory(ruleListView -> new RuleCell());
    // Edit rule on double click
    ruleView.setOnMouseClicked(mouseEvent -> {
      if (mouseEvent.getClickCount() == 2) {
        replaceRule();
      }
    });
    addRuleBtn.disableProperty().bind(categoryTreeView.getSelectionModel().selectedItemProperty()
        .isNull());
    editRuleBtn.disableProperty()
        .bind(ruleView.getSelectionModel().selectedItemProperty()
            .isNull());
    deleteRuleBtn.disableProperty()
        .bind(ruleView.getSelectionModel().selectedItemProperty()
            .isNull());
    // TreeView::cellFactory: Bind text to name and set dynamic contextMenu
    categoryTreeView.setCellFactory(new Callback<>() {
      @Override
      public TreeCell<CategoryNode> call(TreeView<CategoryNode> categoryNodeTreeView) {
        return new TreeCell<>() {
          @Override
          public void updateItem(CategoryNode item, boolean isEmpty) {
            super.updateItem(item, isEmpty);
            if (isEmpty) {
              setContextMenu(null);
              textProperty().unbind();
              textProperty().set("");
              setGraphic(null);
            } else {
              setContextMenu(CategoryEditorView.getCMForCategory(this));
              textProperty().unbind();
              textProperty().bind(item.nameProperty());
              Circle c = new Circle(5);
              c.fillProperty().bind(item.colorProperty());
              setGraphic(c);
            }
          }
        };
      }
    });
    // Bin ruleView to rules of selected item.
    categoryTreeView.getSelectionModel().selectedItemProperty().addListener(
        (observed, oldValue, newValue) -> {
          if (oldValue != null) {
            //FIXME: unbind works in mysterious ways and does not unbind because it was never bound
            ruleView.itemsProperty().unbindBidirectional(oldValue.getValue().rulesRO());
          }
          if (newValue != null) {
            ruleView.itemsProperty().bindBidirectional(newValue.getValue().rulesRO());
          }
        });
    // Populate categoryTreeView.
    viewModel.setTreeViewItems(categoryTreeView, invalidator);
  }

  @Override
  public void prepare() {
    if (invalidatedProperty != null && invalidatedProperty.get()) {
      viewModel.setTreeViewItems(categoryTreeView, invalidator);
      invalidatedProperty.set(false);
      categoryTreeView.getSelectionModel().clearSelection();
    }
  }

  /*
   * Category
   */
  private static ContextMenu getCMForCategory(TreeCell<CategoryNode> cell) {
    ContextMenu cm = new ContextMenu();
    MenuItem iRename = new MenuItem("Rename");
    iRename.setOnAction(actionEvent -> newNameDialog(cell));
    MenuItem iAddChild = new MenuItem("Add subcategory");
    iAddChild.setOnAction(actionEvent -> addSubCategory(cell));
    MenuItem iColor = new MenuItem("Change color");
    iColor.setOnAction(actionEvent -> changeColor(cell.getItem()));
    // If cell != root
    if (cell.getTreeItem().getParent() != null) {
      MenuItem iMove = new MenuItem("Change parent");
      iMove.setOnAction(actionEvent -> changeParent(cell));
      MenuItem iRemove = new MenuItem("Delete");
      iRemove.setOnAction(actionEvent -> removeCategory(cell));
      cm.getItems().addAll(iMove, iRemove);
    }
    cm.getItems().addAll(iRename, iAddChild, iColor);
    return cm;
  }

  private static void changeColor(CategoryNode node) {
    var optColor = new ColorPickerDialog().showAndWait();
    optColor.ifPresent(node::setColor);
  }

  private static void newNameDialog(TreeCell<CategoryNode> cell) {
    new TextInputDialog.Builder("Choose name")
        .setOnApply(newName -> {
          if (!cell.getItem().getName().equals(newName) && !newName.isBlank()) {
            cell.getItem().nameProperty().set(newName);
          }
        })
        .setText(cell.getItem().getName())
        .showAndWait();
  }

  private static void addSubCategory(TreeCell<CategoryNode> cell) {
    Optional<CategoryNode> newCategory = NewCategoryDialog.showAndGet();
    newCategory.ifPresent(cell.getItem()::addCategory);
  }

  private static void removeCategory(TreeCell<CategoryNode> cell) {
    // Get parent TreeItem -> get parent::item(CategoryNode) -> remove cell::item(CategoryNode).
    cell.getTreeItem().getParent().getValue().removeCategory(cell.getItem());
  }

  private static void changeParent(TreeCell<CategoryNode> cell) {
    CategoryNode node = cell.getItem();
    CategoryNode parent = cell.getTreeItem().getParent().getValue();
    // Exclude node, all children and direct parent
    List<CategoryNode> optionsList = node.getRoot().streamCollapse()
        .filter(categoryNode -> categoryNode != node
            && !node.isParentOf(categoryNode)
            && categoryNode != parent)
        .collect(Collectors.toList());

    Optional<CategoryNode> optNewParent = new JFXChoiceDialog.Builder<CategoryNode>()
        .setTitle("New parent choice")
        .setOptions(FXCollections.observableArrayList(optionsList))
        .setDefaultChoice(optionsList.get(0))
        .setStringConverter(new StringConverter<>() {
          @Override
          public String toString(CategoryNode categoryNode) {
            return categoryNode.getName();
          }

          @Override
          public CategoryNode fromString(String s) {
            return null; // Not used by JFXComboBox
          }
        })
        .setHeader("Choose the new parent")
        .showAndWait();
    if (optNewParent.isPresent()) {
      optNewParent.get().addCategory(node);
      parent.removeCategory(node);
    }
  }

  /*
   * Rule
   */
  private void addRule() {
    CategoryNode node = categoryTreeView.getSelectionModel().getSelectedItem().getValue();
    assert node != null;
    new RuleDialog.Builder().showAndWait().ifPresent(node::addRule);
  }

  private void replaceRule() {
    Rule currentRule = ruleView.getSelectionModel().getSelectedItem();
    assert currentRule != null;
    CategoryNode node = currentRule.getParent().orElseThrow();
    new Builder()
        .editRule(currentRule)
        .initiallySelected(
            currentRule.getIdentifierPairs().keySet().toArray(new TransactionField[0]))
        .showAndWait()
        .ifPresent(newRule -> {
          node.removeRule(currentRule);
          node.addRule(newRule);
        });
  }

  private void removeRule() {
    assert ruleView.getSelectionModel().getSelectedItem() != null;
    assert categoryTreeView.getSelectionModel().getSelectedItem() != null;
    Rule currentRule = ruleView.getSelectionModel().getSelectedItem();
    categoryTreeView.getSelectionModel().getSelectedItem().getValue().removeRule(
        currentRule
    );
  }

  @FXML
  private void addRuleAction(ActionEvent event) {
    addRule();
  }

  @FXML
  private void editRuleAction(ActionEvent event) {
    replaceRule();
  }

  @FXML
  private void deleteRuleAction(ActionEvent event) {
    removeRule();
  }

}
