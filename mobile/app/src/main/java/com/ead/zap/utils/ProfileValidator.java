package com.ead.zap.utils;

import java.util.regex.Pattern;

/**
 * ProfileValidator utility class for validating profile data
 * Provides validation methods for various profile fields
 */
public class ProfileValidator {
    
    // Email pattern for validation
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    
    // Phone number pattern for Sri Lankan numbers (+94 or 0)
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^(\\+94|0)[1-9][0-9]{8}$");
    
    // NIC pattern for Sri Lankan NICs (old: 9 digits + V/X, new: 12 digits)
    private static final Pattern NIC_OLD_PATTERN = 
        Pattern.compile("^[0-9]{9}[VvXx]$");
    private static final Pattern NIC_NEW_PATTERN = 
        Pattern.compile("^[0-9]{12}$");
    
    // Name pattern (letters, spaces, hyphens, apostrophes only)
    private static final Pattern NAME_PATTERN = 
        Pattern.compile("^[a-zA-Z\\s'-]{1,50}$");

    /**
     * Validate email address
     * @param email Email to validate
     * @return ValidationResult containing success status and error message
     */
    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ValidationResult(false, "Email is required");
        }
        
        String trimmedEmail = email.trim();
        
        if (trimmedEmail.length() > 254) {
            return new ValidationResult(false, "Email is too long");
        }
        
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            return new ValidationResult(false, "Please enter a valid email address");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validate phone number
     * @param phone Phone number to validate
     * @return ValidationResult containing success status and error message
     */
    public static ValidationResult validatePhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return new ValidationResult(false, "Phone number is required");
        }
        
        String cleanPhone = phone.trim().replaceAll("\\s", ""); // Remove spaces
        
        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            return new ValidationResult(false, "Please enter a valid Sri Lankan phone number (e.g., +94771234567 or 0771234567)");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validate NIC number
     * @param nic NIC to validate
     * @return ValidationResult containing success status and error message
     */
    public static ValidationResult validateNIC(String nic) {
        if (nic == null || nic.trim().isEmpty()) {
            return new ValidationResult(false, "NIC is required");
        }
        
        String trimmedNic = nic.trim().toUpperCase();
        
        if (NIC_OLD_PATTERN.matcher(trimmedNic).matches() || 
            NIC_NEW_PATTERN.matcher(trimmedNic).matches()) {
            return new ValidationResult(true, null);
        }
        
        return new ValidationResult(false, "Please enter a valid NIC number (9 digits + V/X or 12 digits)");
    }

    /**
     * Validate first name
     * @param firstName First name to validate
     * @return ValidationResult containing success status and error message
     */
    public static ValidationResult validateFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            return new ValidationResult(false, "First name is required");
        }
        
        String trimmedName = firstName.trim();
        
        if (trimmedName.length() < 2) {
            return new ValidationResult(false, "First name must be at least 2 characters");
        }
        
        if (!NAME_PATTERN.matcher(trimmedName).matches()) {
            return new ValidationResult(false, "First name can only contain letters, spaces, hyphens, and apostrophes");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validate last name
     * @param lastName Last name to validate
     * @return ValidationResult containing success status and error message
     */
    public static ValidationResult validateLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            return new ValidationResult(false, "Last name is required");
        }
        
        String trimmedName = lastName.trim();
        
        if (trimmedName.length() < 2) {
            return new ValidationResult(false, "Last name must be at least 2 characters");
        }
        
        if (!NAME_PATTERN.matcher(trimmedName).matches()) {
            return new ValidationResult(false, "Last name can only contain letters, spaces, hyphens, and apostrophes");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validate vehicle make
     * @param make Vehicle make to validate
     * @return ValidationResult containing success status and error message
     */
    public static ValidationResult validateVehicleMake(String make) {
        if (make == null || make.trim().isEmpty()) {
            return new ValidationResult(false, "Vehicle make is required");
        }
        
        String trimmedMake = make.trim();
        
        if (trimmedMake.length() < 2 || trimmedMake.length() > 30) {
            return new ValidationResult(false, "Vehicle make must be between 2 and 30 characters");
        }
        
        if (!Pattern.matches("^[a-zA-Z0-9\\s-]{2,30}$", trimmedMake)) {
            return new ValidationResult(false, "Vehicle make can only contain letters, numbers, spaces, and hyphens");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validate vehicle model
     * @param model Vehicle model to validate
     * @return ValidationResult containing success status and error message
     */
    public static ValidationResult validateVehicleModel(String model) {
        if (model == null || model.trim().isEmpty()) {
            return new ValidationResult(false, "Vehicle model is required");
        }
        
        String trimmedModel = model.trim();
        
        if (trimmedModel.length() < 1 || trimmedModel.length() > 50) {
            return new ValidationResult(false, "Vehicle model must be between 1 and 50 characters");
        }
        
        if (!Pattern.matches("^[a-zA-Z0-9\\s.-]{1,50}$", trimmedModel)) {
            return new ValidationResult(false, "Vehicle model can only contain letters, numbers, spaces, dots, and hyphens");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validate license plate
     * @param licensePlate License plate to validate
     * @return ValidationResult containing success status and error message
     */
    public static ValidationResult validateLicensePlate(String licensePlate) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            return new ValidationResult(false, "License plate is required");
        }
        
        String cleanPlate = licensePlate.trim().toUpperCase().replaceAll("\\s", "");
        
        if (cleanPlate.length() < 4 || cleanPlate.length() > 10) {
            return new ValidationResult(false, "License plate must be between 4 and 10 characters");
        }
        
        if (!Pattern.matches("^[A-Z0-9-]+$", cleanPlate)) {
            return new ValidationResult(false, "License plate can only contain letters, numbers, and hyphens");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validate vehicle year
     * @param year Vehicle year to validate
     * @return ValidationResult containing success status and error message
     */
    public static ValidationResult validateVehicleYear(int year) {
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        
        if (year < 1900) {
            return new ValidationResult(false, "Vehicle year cannot be before 1900");
        }
        
        if (year > currentYear + 1) {
            return new ValidationResult(false, "Vehicle year cannot be more than one year in the future");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * ValidationResult class to hold validation results
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;

        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return isValid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public boolean hasError() {
            return !isValid;
        }
    }
}