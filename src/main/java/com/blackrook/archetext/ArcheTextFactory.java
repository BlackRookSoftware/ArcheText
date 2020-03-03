/*******************************************************************************
 * Copyright (c) 2016-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.archetext;

import com.blackrook.archetext.struct.Utils;

/**
 * Factory class for generating {@link ArcheTextObject}s.
 * @author Matthew Tropiano
 */
public final class ArcheTextFactory
{
	/**
	 * Creates a new anonymous ArcheTextObject using a POJO (Plain Ol' Java Object) or Map type.
	 * Primitives, boxed primitives, Sets, and Arrays are not acceptable.
	 * @param <T> the input object type.
	 * @param value the object value to convert.
	 * @return a new ArcheText object that represents the input object.
	 * @throws IllegalArgumentException if value is not a POJO nor map.
	 */
	public static <T> ArcheTextObject create(T value)
	{
		String name = value.getClass().getSimpleName();
		return create(Character.toLowerCase(name.charAt(0)) + name.substring(1), Utils.getIdentityFromObject(value), value);
	}

	/**
	 * Creates a new default ArcheTextObject using a POJO (Plain Ol' Java Object) or Map type.
	 * Primitives, boxed primitives, Sets, and Arrays are not acceptable.
	 * @param <T> the input object type.
	 * @param type the object type.
	 * @param value the object value to convert.
	 * @return a new ArcheText object that represents the input object.
	 * @throws IllegalArgumentException if value is not a POJO nor map.
	 */
	public static <T> ArcheTextObject create(String type, T value)
	{
		return create(type, Utils.getIdentityFromObject(value), value);
	}
	
	/**
	 * Creates a new ArcheTextObject using a POJO (Plain Ol' Java Object) or Map type.
	 * Primitives, boxed primitives, Sets, and Arrays are not acceptable.
	 * @param <T> the input object type.
	 * @param type the object type.
	 * @param name the object name.
	 * @param value the object value to convert.
	 * @return a new ArcheText object that represents the input object.
	 * @throws IllegalArgumentException if value is not a POJO nor map.
	 */
	public static <T> ArcheTextObject create(String type, String name, T value)
	{
		ArcheTextObject out = new ArcheTextObject(type, name);
		Utils.exportTo(value, out);
		return out;
	}
	
}
