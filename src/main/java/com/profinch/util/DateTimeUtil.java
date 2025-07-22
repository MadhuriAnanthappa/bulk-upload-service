package com.profinch.util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateTimeUtil {

    public static Timestamp parseToTimestamp(String input) {
        try {
            return Timestamp.valueOf(LocalDateTime.parse(input));
        } catch (DateTimeParseException e) {
            try {
                return Timestamp.valueOf(LocalDate.parse(input).atStartOfDay());
            } catch (DateTimeParseException ex) {
                log.error("Invalid date format for input '{}'", input);
                throw ex;
            }
        }
    }
}
