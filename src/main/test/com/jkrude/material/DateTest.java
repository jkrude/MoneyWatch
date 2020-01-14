package com.jkrude.material;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Test;

public class DateTest {

  @Test
  public void workingWithDate(){
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yy");
    try {
      Date date1 = dateFormatter.parse("01.10.19");
      Date date2 = dateFormatter.parse("02.09.19");
      assertEquals(date1.compareTo(date2),1);

    } catch (ParseException e) {
      e.printStackTrace();
      fail();
    }

  }
}
