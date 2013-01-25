package com.comsysto.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** @author Elisabeth Engel */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Neo4jTableBuilderColumnSetMethod {

    String columnName() default "";

    int columnOrder() default 10;

}
