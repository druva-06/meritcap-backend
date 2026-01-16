package com.consultancy.education.exception;

import java.util.ArrayList;
import java.util.List;

public class BadRequestException extends RuntimeException {
    private List<String> errors = new ArrayList<>();

    public BadRequestException(String message) {
        super(message);
        this.errors.add(message);
    }

    public BadRequestException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
