package com.blackrook.archetext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.blackrook.archetext.annotation.ArcheTextIgnore;
import com.blackrook.commons.ObjectPair;
import com.blackrook.commons.Reflect;
import com.blackrook.commons.TypeProfile;
import com.blackrook.commons.TypeProfile.MethodSignature;

/**
 * Factory class for generating {@link ArcheTextObject}s.
 * @author Matthew Tropiano
 */
public final class ArcheTextFactory
{
	/**
	 * Creates a new anonymous ArcheTextObject using a POJO (Plain Ol' Java Object) or Map type.
	 * Primitives, boxed primitives, Sets, and Arrays are not acceptable.
	 * @param value the object value to convert.
	 * @throws IllegalArgumentException if value is not a POJO nor map.
	 */
	public static <T> ArcheTextObject create(T value)
	{
		return create(null, null, value);
	}

	/**
	 * Creates a new default ArcheTextObject using a POJO (Plain Ol' Java Object) or Map type.
	 * Primitives, boxed primitives, Sets, and Arrays are not acceptable.
	 * @param type the object type.
	 * @param value the object value to convert.
	 * @throws IllegalArgumentException if value is not a POJO nor map.
	 */
	public static <T> ArcheTextObject create(String type, T value)
	{
		return create(type, null, value);
	}
	
	/**
	 * Creates a new ArcheTextObject using a POJO (Plain Ol' Java Object) or Map type.
	 * Primitives, boxed primitives, Sets, and Arrays are not acceptable.
	 * @param type the object type.
	 * @param name the object name.
	 * @param value the object value to convert.
	 * @throws IllegalArgumentException if value is not a POJO nor map.
	 */
	public static <T> ArcheTextObject create(String type, String name, T value)
	{
		ArcheTextObject out = new ArcheTextObject(type, name);
		exportTo(value, out);
		return out;
	}
	
	/**
	 * Exports the values of an object to an ArcheTextObject.
	 * @param object the object to export.
	 * @param atext the destination structure.
	 */
	private static <T> void exportTo(T object, ArcheTextObject atext)
	{
		@SuppressWarnings("unchecked")
		TypeProfile<T> typeProfile = TypeProfile.getTypeProfile((Class<T>)object.getClass());
		
		for (ObjectPair<String, MethodSignature> getter : typeProfile.getGetterMethods())
		{
			Method method = getter.getValue().getMethod();
			if (method.getAnnotation(ArcheTextIgnore.class) == null)
				atext.setField(getter.getKey(), Reflect.invokeBlind(method, object));
		}
		
		for (ObjectPair<String, Field> pubfield : typeProfile.getPublicFields())
		{
			Field field = pubfield.getValue();
			if (field.getAnnotation(ArcheTextIgnore.class) == null)
				atext.setField(pubfield.getKey(), Reflect.getFieldValue(field, object));
		}
		
	}
	
	
}
