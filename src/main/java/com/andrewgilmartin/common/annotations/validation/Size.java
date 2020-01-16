package com.andrewgilmartin.common.annotations.validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Maximum size of the data. For example, a string might need to be less than 10
 * characters.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Size {

    int value();
}
