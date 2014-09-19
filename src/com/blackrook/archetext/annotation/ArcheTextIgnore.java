package com.blackrook.archetext.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.blackrook.lang.json.JSONObject;

/**
 * An annotation for telling {@link JSONObject} that this field or method
 * should not be serialized into a JSON construct of any kind.
 * @author Matthew Tropiano
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ArcheTextIgnore
{

}
