package com.jkrude.material;

import com.jkrude.material.Camt.ListType;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("unchecked")
public class PersistenceManager {

  public static void save(Model model) {
    try {
      ObservableList<PieCategory> categories = model.getProfile().getPieCategories();
      FileWriter file = new FileWriter(
          "/home/jakob/Documents/Coding/IntelliJ-Projekte/MoneyWatch/src/main/resources/pers.json");
      JSONObject jMain = new JSONObject();
      JSONArray jCatArr = new JSONArray();
      for (PieCategory cat : categories) {
        JSONObject jCat = new JSONObject();
        jCat.put("name", cat.getName().getValue());
        JSONArray jIds = new JSONArray();
        for (Rule rule : cat.getRulesRO()) {
          for (Pair<ListType, String> entry : rule.getIdentifierPairs()) {
            JSONObject jRule = new JSONObject();
            jRule.put("key", entry.getKey().toString());
            jRule.put("value", entry.getValue());
            jIds.add(jRule);
          }
        }
        jCat.put("ids", jIds);
        jCatArr.add(jCat);
      }
      jMain.put("categories", jCatArr);
      file.write(jMain.toJSONString());
      file.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void load(Model model) {
    try {
      Reader reader = new FileReader(
          "/home/jakob/Documents/Coding/IntelliJ-Projekte/MoneyWatch/src/main/resources/pers.json");
      JSONParser parser = new JSONParser();
      JSONObject jMain = (JSONObject) parser.parse(reader);
      ArrayList<PieCategory> pieCategories = new ArrayList<>();
      JSONArray jCatArray = (JSONArray) jMain.get("categories");

      for (JSONObject jCat : (Iterable<JSONObject>) jCatArray) {
        String name = (String) jCat.get("name");
        StringProperty nameProp = new SimpleStringProperty(name);
        ListProperty<Rule> identifierList = new SimpleListProperty<>(
            FXCollections.observableArrayList());
        JSONArray jIds = (JSONArray) jCat.get("ids");
        for (JSONObject jRule : (Iterable<JSONObject>) jIds) {
          String key = (String) jRule.get("key");
          ListType listType = ListType.get(key);
          String string = (String) jRule.get("value");
          Rule rule = Rule.RuleFactory.generate(new Pair<>(listType,string),"");
          identifierList.add(rule);
        }
        PieCategory pieCategory = new PieCategory(nameProp,identifierList);
        model.getProfile().addCategory(pieCategory);
      }
    } catch (ParseException | IOException | java.text.ParseException e) {
      e.printStackTrace();
    }
  }
}
