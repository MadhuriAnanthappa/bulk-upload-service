package com.profinch.exception;

public class TotalRecordsMismatchException extends RuntimeException {
    public TotalRecordsMismatchException(int expected, int actual) {
        super("totalRecords (" + expected + ") does not match actual records (" + actual + ")");
    }
}