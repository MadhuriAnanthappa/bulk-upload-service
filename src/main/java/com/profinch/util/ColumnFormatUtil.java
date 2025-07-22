package com.profinch.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ColumnFormatUtil {

    // Converts snake_case to camelCase
    public static String toCamelCase(String snakeCase) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : snakeCase.toLowerCase().toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    // Converts List of Maps with snake_case keys to camelCase keys
    public static List<Map<String, Object>> convertListKeysToCamelCase(List<Map<String, Object>> details) {
        List<Map<String, Object>> camelCaseList = new ArrayList<>();

        for (Map<String, Object> row : details) {
            Map<String, Object> camelCaseRow = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                camelCaseRow.put(toCamelCase(entry.getKey()), entry.getValue());
            }
            camelCaseList.add(camelCaseRow);
        }
        return camelCaseList;
    }
}
