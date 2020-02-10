package com.jkrude.material;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.jkrude.material.Camt.CamtEntry;
import com.jkrude.material.Camt.DateDataPoint;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import org.junit.Test;

public class CamtTest {

  @Test
  public void testDateMapGeneration() {
    Camt camt = TestData.getCamtWithTestData();
    TreeMap<Date, List<DateDataPoint>> map = camt.getSourceAsDateMap();
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

  @Test
  public void testCSVParser() {
    URL resource = getClass().getClassLoader().getResource("september.CSV");
    File file;
    if (resource == null) {
      fail();
    }
    file = new File(resource.getFile());

    Camt camt;
    try {
      Scanner sc = new Scanner(file, StandardCharsets.ISO_8859_1);
      try {
        camt = new Camt(sc);
        List<CamtEntry> source = camt.getSource();
        assertEquals(source.size(), 40);
        assertEquals(source.get(0).getDataPoint().getAmount().getAmount().longValue(), 800L);
        assertEquals(source.get(0).getDataPoint().getAmount(), new Money(-293.33));
      } catch (Exception e) {
        fail();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}