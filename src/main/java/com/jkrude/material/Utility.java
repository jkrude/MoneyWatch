package com.jkrude.material;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import javafx.scene.chart.NumberAxis;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;

public abstract class Utility {


    public static long parseToCents(String input){
        input= input.replace(",",".");

        float cents = Float.parseFloat(input);
        cents *= 100;
        return (long) cents;
    }

    public static List<Integer> mapDateToInteger(List<String> strings) {
        // uses the format dd.mm.yy
        if(strings == null || strings.contains(null))
            throw new NullPointerException();

        List<Integer> res = new ArrayList<>();
        for (String string : strings){
            String[] parts = string.split("[.]");
            if(parts.length != 2)
                throw new IllegalArgumentException("corrupt date format");
            int asInt = Integer.parseInt(parts[1]);
            asInt += 100*Integer.parseInt(parts[0]);
            res.add(asInt);
        }
        return res;
    }
    public static int mapDateToInteger(String string){
        if (string == null)
            throw new NullPointerException();
        List<String> ls = new ArrayList<>();
        ls.add(string);
        List<Integer> res = mapDateToInteger(ls);
        return res.get(0);
    }
    public static StringConverter<Number> getStringConverter(){
        // uses the format dd.mm.yy
        return new StringConverter<>() {
            @Override
            public String toString(Number number) {
                if (number == null)
                    throw new NullPointerException();
                if (number.intValue() >3112)
                    throw new IllegalArgumentException();

                String nbrStr =  Integer.toString(number.intValue());
                if (nbrStr.length() == 3)
                    nbrStr = "0" + nbrStr;

                else if (nbrStr.length() != 4)
                    return nbrStr;
                return nbrStr.substring(0,2)  +"." + nbrStr.substring(2,4);
            }
            @Override
            public Number fromString(String s) {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static StringConverter<Number> convertFromInstant(){
        return new StringConverter<Number>() {
            @Override
            public String toString(Number number) {
                Instant instant = Instant.ofEpochMilli(number.longValue());
                SimpleDateFormat sDF = new SimpleDateFormat("dd.MM.yy");
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
