package com.jkrude.material;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import javafx.util.StringConverter;

public abstract class Utility {

  public static StringConverter<Number> convertFromInstant() {
    return new StringConverter<Number>() {
      @Override
      public String toString(Number number) {
        Instant instant = Instant.ofEpochMilli(number.longValue());
        SimpleDateFormat sDF = new SimpleDateFormat("dd.MM");
        Date date = Date.from(instant);
        return sDF.format(date);
      }

      @Override
      public Number fromString(String s) {
        throw new UnsupportedOperationException();
      }
    };
  }
}
