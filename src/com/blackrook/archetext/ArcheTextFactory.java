package com.blackrook.archetext;

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
	 * Creates a new ArcheTextObject using a POJO (Plain Ol' Java Object) or Map type.
	 * Primitives, boxed primitives, Sets, and Arrays are not acceptable.
	 * @param type the object type.
	 * @param name the object name.
	 * @param value the object value to convert.
	 * @throws IllegalArgumentException if value is not a POJO nor map.
	 */
	public static <T> ArcheTextObject create(String type, String name, T value)
	{
		// TODO: Finish.
		return null;
	}
	
}
