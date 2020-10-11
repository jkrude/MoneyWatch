package com.jkrude.material;

import com.jkrude.category.CategoryNode;
import com.jkrude.category.Rule;
import com.jkrude.material.TransactionContainer.TransactionField;
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
import javafx.util.Pair;
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

      rootCategory.stream().forEach(categoryNode -> serializeCategory(categoryNode, jCatArr));
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
    jCategory.put("ids", serializeRules(categoryNode.leafsRO()));
    Optional<CategoryNode> optParent = categoryNode.getParent();
    String parentAsJson = optParent.map(CategoryNode::getName).orElse("null");
    jCategory.put("parent", parentAsJson);
    jCatArr.add(jCategory);
  }

  private static JSONArray serializeRules(ReadOnlyListWrapper<Rule> leafsRO) {
    JSONArray jIds = new JSONArray();
    for (Rule rule : leafsRO) {
      for (Pair<TransactionField, String> entry : rule.getIdentifierPairs()) {
        JSONObject jRule = new JSONObject();
        jRule.put("key", entry.getKey().toString());
        jRule.put("value", entry.getValue());
        jIds.add(jRule);
      }
    }
    return jIds;
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

  private static List<Rule> deserializeRules(JSONArray jIds) throws java.text.ParseException {
    List<Rule> identifierList = new ArrayList<>();
    for (JSONObject jRule : (Iterable<JSONObject>) jIds) {
      String key = (String) jRule.get("key");
      TransactionField transactionField = TransactionField.get(key);
      String string = (String) jRule.get("value");
      Rule rule = Rule.RuleFactory.generate(new Pair<>(transactionField, string), "");
      identifierList.add(rule);
    }
    return identifierList;
  }

  private static CategoryNode deserializeCategories(JSONArray categories)
      throws java.text.ParseException {

    //TODO: name of a category needs to be unique
    List<Entry<String, CategoryNode>> searchingForParent = new ArrayList<>();
    Map<String, CategoryNode> nodesAsNames = new HashMap<>();

    List<CategoryNode> nodeList = new ArrayList<>();

    for (JSONObject jCategory : (Iterable<JSONObject>) categories) {
      String name = (String) jCategory.get("name");
      List<Rule> rules = deserializeRules((JSONArray) jCategory.get("ids"));
      CategoryNode node = new CategoryNode(name, rules);
      nodesAsNames.put(name, node);
      String parentAsString = (String) jCategory.get("parent");
      if (!parentAsString.equals("null")) {
        searchingForParent.add(new SimpleEntry<>(parentAsString, node));
      }
      nodeList.add(node);
    }
    searchingForParent.forEach(
        (entry) -> nodesAsNames.get(entry.getKey())
            .addCategoryIfPossible(entry.getValue())
    );
    List<CategoryNode> roots = nodeList.stream().filter(categoryNode -> !categoryNode.hasParent())
        .collect(
            Collectors.toList());
    assert roots.size() == 1;
    return roots.get(0);
  }

}
