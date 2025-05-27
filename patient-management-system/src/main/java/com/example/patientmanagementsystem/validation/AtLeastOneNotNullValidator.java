package com.example.patientmanagementsystem.validation;

import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AtLeastOneNotNullValidator implements ConstraintValidator<AtLeastOneNotNull, Object> {

    private String[] fields;

    @Override
    public void initialize(AtLeastOneNotNull constraintAnnotation) {
        this.fields = constraintAnnotation.fields();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Or false, depending on how you want to handle overall null DTO
        }
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(value);
        for (String fieldName : fields) {
            Object fieldValue = beanWrapper.getPropertyValue(fieldName);
            if (fieldValue != null) {
                // Also check for empty strings if that's a concern for some fields
                // if (fieldValue instanceof String && ((String) fieldValue).isEmpty()) {
                //     continue;
                // }
                return true;
            }
        }
        // If loop completes, no non-null field was found
        // Customize the error message to include which fields were checked, if desired.
        // context.disableDefaultConstraintViolation();
        // context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
        // .addPropertyNode("object").addConstraintViolation(); // Associates error with the object itself
        return false;
    }
} 