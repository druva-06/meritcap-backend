package com.consultancy.education.utils;

import com.consultancy.education.enums.Month;

import java.util.ArrayList;
import java.util.List;

public class FormatConverter {

    public static Integer cnvrtDurationToInteger(String duration) {
        String[] parts = duration.split("//s+");
        int durationInt;
        try {
            durationInt = Integer.parseInt(parts[0]);
        }
        catch (Exception e) {
            durationInt = 0;
        }
        return durationInt;
    }

    public static List<Month> cnvrtIntakesToList(String intakes) {
        String[] parts = intakes.split(",");
        List<Month> list = new ArrayList<>();
        for (String part : parts) {
            try{
                list.add(Month.valueOf(part.trim().toUpperCase()));
            }
            catch (Exception ignored) {
            }
        }
        return list;
    }

    public static Double cnvrtCurrencyToDouble(String currency) {
        String[] parts = currency.split("//s+");
        double currencyDouble;
        try{
            currencyDouble = Double.parseDouble(parts[1].trim().replace("$", ""));
        }
        catch (Exception e) {
            currencyDouble = 0;
        }
        return currencyDouble;
    }

    public static Double cnvrtPercentageToDouble(String percentage){
        double percentageDouble;
        try {
            percentage = percentage.replace("%", "").trim();
            percentageDouble = Double.parseDouble(percentage);
        }
        catch (Exception e) {
            percentageDouble = 0.0;
        }
        return percentageDouble;
    }
}
