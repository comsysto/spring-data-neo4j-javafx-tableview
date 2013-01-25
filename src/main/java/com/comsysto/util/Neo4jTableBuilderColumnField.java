package com.comsysto.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** @author Elisabeth Engel */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Neo4jTableBuilderColumnField {

    FieldType columnType() default FieldType.readAndWrite;

    String columnName() default "";

    int columnOrder() default 10;

    public enum FieldType  {
        readAndWrite, readOnly
    }

}
