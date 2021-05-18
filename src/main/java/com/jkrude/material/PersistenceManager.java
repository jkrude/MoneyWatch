package com.jkrude.material;

import com.jkrude.category.CategoryNode;
import com.jkrude.category.Rule;
import com.jkrude.category.Rule.RuleBuilder;
import com.jkrude.transaction.Transaction;
import com.jkrude.transaction.Transaction.TransactionField;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.scene.paint.Color;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("unchecked")
public class PersistenceManager {

  public static void save(Profile profile, URL fileUrl) {
    try {
      CategoryNode rootCategory = profile.getRootCategory();
      FileWriter fileWriter = new FileWriter(fileUrl.getFile());
      JSONObject jMain = new JSONObject();
      JSONArray jCatArr = new JSONArray();

      rootCategory.streamCollapse()
          .forEach(categoryNode -> serializeCategory(categoryNode, jCatArr));
      jMain.put("categories", jCatArr);
      fileWriter.write(jMain.toJSONString());
      fileWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void serializeCategory(CategoryNode categoryNode, JSONArray jCatArr) {
    JSONObject jCategory = new JSONObject();
    // TODO: name is primary key so duplicated should not be possible
    jCategory.put("name", categoryNode.getName());
    jCategory.put("color", categoryNode.getColor().toString());
    jCategory.put("rules", serializeRules(categoryNode.rulesRO()));
    Optional<CategoryNode> optParent = categoryNode.getParent();
    String parentAsJson = optParent.map(CategoryNode::getName).orElse("null");
    jCategory.put("parent", parentAsJson);
    jCatArr.add(jCategory);
  }

  private static JSONArray serializeRules(ReadOnlyListWrapper<Rule> leafsRO) {
    JSONArray jRules = new JSONArray();
    for (Rule rule : leafsRO) {
      JSONObject jRule = new JSONObject();
      jRule.put("note", rule.getNote().orElse(""));
      JSONArray jIds = new JSONArray();
      for (Map.Entry<TransactionField, String> entry : rule.getIdentifierPairs().entrySet()) {
        JSONObject jId = new JSONObject();
        jId.put("key", entry.getKey().toString());
        jId.put("value", entry.getValue());
        jIds.add(jId);
      }
      jRule.put("ids", jIds);
      jRules.add(jRule);
    }
    return jRules;
  }

  public static void load(Profile profile, URL fileUrl) {
    try {
      FileReader reader = new FileReader(fileUrl.getFile());
      JSONParser parser = new JSONParser();
      JSONObject jMain = (JSONObject) parser.parse(reader);

      JSONArray jCatArray = (JSONArray) jMain.get("categories");
      CategoryNode rootCategory = deserializeCategories(jCatArray);
      profile.setRootCategory(rootCategory);
    } catch (ParseException | IOException | java.text.ParseException e) {
      e.printStackTrace();
    }
  }

  private static List<Rule> deserializeRules(JSONArray jRules, CategoryNode node)
      throws java.text.ParseException {
    List<Rule> rules = new ArrayList<>();
    for (JSONObject jRule : (Iterable<JSONObject>) jRules) {
      Map<TransactionField, String> ids = new HashMap<>();
      for (JSONObject jId : (Iterable<JSONObject>) jRule.get("ids")) {
        String key = (String) jId.get("key");
        TransactionField transactionField = Transaction.TransactionField.get(key);
        String string = (String) jId.get("value");
        ids.put(transactionField, string);
      }
      String note = (String) jRule.get("note");
      note = note != null && note.isBlank() ? null : note;
      Rule rule = RuleBuilder.fromMap(ids)
          .addNote(note)
          .setParent(node)
          .build();
      rules.add(rule);
    }
    return rules;
  }

  private static CategoryNode deserializeCategories(JSONArray categories)
      throws java.text.ParseException {

    //TODO: name of a category needs to be unique
    List<Entry<String, CategoryNode>> searchingForParent = new ArrayList<>();
    Map<String, CategoryNode> nodesAsNames = new HashMap<>();

    List<CategoryNode> nodeList = new ArrayList<>();

    for (JSONObject jCategory : (Iterable<JSONObject>) categories) {
      String name = (String) jCategory.get("name");
      CategoryNode node = new CategoryNode(name);
      Object colorObj = jCategory.get("color");
      if (colorObj != null) {
        Color color = Color.valueOf((String) colorObj);
        node.setColor(color);
      }
      node.addAllRules(deserializeRules((JSONArray) jCategory.get("rules"), node));
      nodesAsNames.put(name, node);
      String parentAsString = (String) jCategory.get("parent");
      if (!parentAsString.equals("null")) {
        searchingForParent.add(new SimpleEntry<>(parentAsString, node));
      }
      nodeList.add(node);
    }
    searchingForParent.forEach(
        (entry) -> nodesAsNames.get(entry.getKey())
            .addCategory(entry.getValue())
    );
    List<CategoryNode> roots = nodeList.stream().filter(categoryNode -> !categoryNode.hasParent())
        .collect(
            Collectors.toList());
    assert roots.size() == 1;
    return roots.get(0);
  }

}
