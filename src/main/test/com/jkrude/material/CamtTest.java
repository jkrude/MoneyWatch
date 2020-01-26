package com.jkrude.material;

import static org.junit.Assert.assertEquals;

import com.jkrude.material.Camt.DateDataPoint;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.junit.Test;

public class CamtTest {

  @Test
  public void testDateMapGeneration() {
    Camt camt = TestData.getCamtWithTestData();
    TreeMap<Date, List<DateDataPoint>> map = camt.getDateMap();
    Set<Entry<Date, List<DateDataPoint>>> s = map.entrySet();
    ArrayList<Entry<Date, List<DateDataPoint>>> list = new ArrayList<>(s);
    try {
        assertEquals(list.get(0).getKey(), TestData.simpleDateFormat.parse("23.09.19"));
        assertEquals(list.get(1).getKey(), TestData.simpleDateFormat.parse("24.09.19"));
        assertEquals(list.get(2).getKey(), TestData.simpleDateFormat.parse("25.09.19"));
        assertEquals(list.get(3).getKey(), TestData.simpleDateFormat.parse("26.09.19"));
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

}