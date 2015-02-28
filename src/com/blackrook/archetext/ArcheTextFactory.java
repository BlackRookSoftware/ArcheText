/*******************************************************************************
 * Copyright (c) 2015 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.archetext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.blackrook.archetext.annotation.ATIgnore;
import com.blackrook.archetext.annotation.ATName;
import com.blackrook.archetext.exception.ArcheTextExportException;
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
		String name = value.getClass().getSimpleName();
		return create(Character.toLowerCase(name.charAt(0)) + name.substring(1), getATNameFromObject(value), value);
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
		return create(type, getATNameFromObject(value), value);
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
	 * @throws ArcheTextExportException if a problem happens during export.
	 * @throws ClassCastException if a value cannot be converted.
	 */
	private static <T> void exportTo(T object, ArcheTextObject atext)
	{
		@SuppressWarnings("unchecked")
		TypeProfile<T> typeProfile = TypeProfile.getTypeProfile((Class<T>)object.getClass());
		
		for (ObjectPair<String, MethodSignature> getter : typeProfile.getGetterMethods())
		{
			Method method = getter.getValue().getMethod();
			if (method.isAnnotationPresent(ATIgnore.class) || method.isAnnotationPresent(ATName.class))
				continue;
			
			atext.set(getter.getKey(), Reflect.invokeBlind(method, object));
		}
		
		for (ObjectPair<String, Field> pubfield : typeProfile.getPublicFields())
		{
			Field field = pubfield.getValue();
			if (field.isAnnotationPresent(ATIgnore.class) || field.isAnnotationPresent(ATName.class))
				continue;

			atext.set(pubfield.getKey(), Reflect.getFieldValue(field, object));
		}
		
	}
	
	/**
	 * Get name for object.
	 */
	private static <T> String getATNameFromObject(T object)
	{
		String out = null;
		
		@SuppressWarnings("unchecked")
		TypeProfile<T> typeProfile = TypeProfile.getTypeProfile((Class<T>)object.getClass());

		for (MethodSignature ms : typeProfile.getAnnotatedGetters(ATName.class))
			out = Reflect.createForType(ms.getMethod().getName(), Reflect.invokeBlind(ms.getMethod(), object), String.class);
		for (Field f : typeProfile.getAnnotatedPublicFields(ATName.class))
			out = Reflect.createForType(f.getName(), Reflect.getFieldValue(f, object), String.class);
		
		return out;
	}
	
}
