package com.eris.fintrack.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnumValidatorConstraint implements ConstraintValidator<EnumValidator, String> {
    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(EnumValidator constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        boolean valid = Stream.of(enumClass.getEnumConstants())
                .map(Enum::name)
                .anyMatch(e -> e.equalsIgnoreCase(value));

        if (!valid) {
            String allowedValues = Arrays.stream(enumClass.getEnumConstants())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Invalid value. Allowed values: " + allowedValues
            ).addConstraintViolation();
        }

        return valid;
    }
}