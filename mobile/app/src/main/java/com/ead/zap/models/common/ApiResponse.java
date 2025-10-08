package com.ead.zap.models.common;

import java.util.List;

/**
 * Generic API response wrapper used by the backend
 * Wraps all API responses in a consistent format
 */
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<String> errors;

    public ApiResponse() {}

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    /**
     * Check if the API call was successful and has data
     */
    public boolean hasValidData() {
        return success && data != null;
    }

    /**
     * Get error message string from errors list
     */
    public String getErrorMessage() {
        if (errors != null && !errors.isEmpty()) {
            return String.join(", ", errors);
        }
        return message != null ? message : "Unknown error occurred";
    }
}