package com.blackrook.archetext.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to signify that this field/get-setter's value should be
 * the name of the structure. Field type must be String or vague equivalent, like
 * char[].
 * @author Matthew Tropiano
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ATName
{

}