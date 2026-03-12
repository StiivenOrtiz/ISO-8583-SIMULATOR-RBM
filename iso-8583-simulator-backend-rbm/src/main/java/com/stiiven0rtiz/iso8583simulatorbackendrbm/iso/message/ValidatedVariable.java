package com.stiiven0rtiz.iso8583simulatorbackendrbm.iso.message;

// imports
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * ValidatedVariable.java
 *
 * This class represents a variable that has been validated, along with its validation status and an optional error message.
 *
 * @param <T> The type of the variable.
 * @version 1.0
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ValidatedVariable<T> {
    /**
     * The variable that has been validated.
     */
    private T value;
    /**
     * The validation status of the variable.
     */
    private boolean isValid;
    /**
     * An optional error message if the variable is not valid.
     */
    private String errorMessage;

    /**
     * Constructor for the ValidatedVariable class.
     *
     * @param value        The variable that has been validated.
     */
    ValidatedVariable(T value) {
        this.value = value;
    }

    /**
     * Constructor for the ValidatedVariable class with validation status.
     *
     * @param value     The variable that has been validated.
     * @param isValid   The validation status of the variable.
     */
    public ValidatedVariable(T value, boolean isValid) {
        this.value = value;
        this.isValid = isValid;
    }
}
