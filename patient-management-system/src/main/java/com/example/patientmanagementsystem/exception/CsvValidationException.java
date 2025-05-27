package com.example.patientmanagementsystem.exception;

/**
 * Custom exception for CSV validation errors during parsing.
 */
public class CsvValidationException extends RuntimeException {
    public CsvValidationException(String message) {
        super(message);
    }

    public CsvValidationException(String message, Throwable cause) {
        super(message, cause);
    }
} 