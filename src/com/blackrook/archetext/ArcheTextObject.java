package com.blackrook.archetext;

import com.blackrook.commons.Common;
import com.blackrook.commons.linkedlist.Stack;

/**
 * The object representation for all ArcheText objects and values.
 * ArcheText objects have types and can inherit values from one or more objects.
 * @author Matthew Tropiano
 */
public class ArcheTextObject
{
	
	
	/**
	 * ArcheText object internal types.
	 */
	public static enum Type
	{
		/** Boolean type. */
		BOOLEAN,
		/** Numeric type. */
		NUMBER,
		/** String type. */
		STRING,
		/** Array type. Stored as ArcheTextObject[]. */
		ARRAY,
		/** Set type. Stored as Hash<ArcheTextObject>. */
		SET,
		/** Object type for objects, or null. Stored as HashMap<String, ArcheTextObject>, or null. */
		OBJECT;
	}

	/** Object type. */
	private String type;
	/** Object name. */
	private String name;
	
	/** Object hierarchy parents. */
	private Stack<ArcheTextObject> parents;

	/** Object value type. */
	private Type valueType;
	/** Object internal value. */
	private Object value;

	/**
	 * Creates a new anonymous ArcheTextObject.
	 */
	public ArcheTextObject()
	{
		this(null, null);
	}

	/**
	 * Creates a new default ArcheTextObject with a type.
	 */
	public ArcheTextObject(String type)
	{
		this(type, null);
	}

	/**
	 * Creates a new ArcheTextObject with a type and name.
	 */
	public ArcheTextObject(String type, String name)
	{
		if (!Common.isEmpty(name) && Common.isEmpty(type))
			throw new IllegalArgumentException("type cannot be empty if the name is.");
		this.type = type;
		this.name = name;
	}
	
	/**
	 * Creates a new ArcheTextObject with a type and name.
	 */
	public static <T> ArcheTextObject create(T value)
	{
		// TODO: Finish.
		return null;
	}
	
	/**
	 * Returns the type name of this object.
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * Returns the declarative name of this object.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the object's parents, in order of precedence.
	 */
	public Iterable<ArcheTextObject> getParents()
	{
		return parents;
	}

	/**
	 * Returns the value type.
	 */
	public Type getValueType()
	{
		return valueType;
	}

	/**
	 * Returns the value.
	 */
	public Object getValue()
	{
		return value;
	}

	/**
	 * Returns true if this object has no type nor name.
	 */
	public boolean isAnonymous()
	{
		return type == null && name == null;
	}

	/**
	 * Returns true if this object has a type but no name.
	 */
	public boolean isDefault()
	{
		return type != null && name == null;
	}

	/**
	 * Pushes a parent onto the parent stack.
	 * @param parent the parent to push.
	 */
	public void pushParent(ArcheTextObject parent)
	{
		if (parents == null)
			parents = new Stack<ArcheTextObject>();
		parents.push(parent);
	}

	/**
	 * Removes a parent from the parent stack.
	 * @param parent the parent to remove.
	 * @return true if removed.
	 */
	public boolean removeParent(ArcheTextObject parent)
	{
		if (parents == null)
			return false;
		boolean out = parents.remove(parent);
		if (parents.isEmpty())
			parents = null;
		return out;
	}
	
	
	// TODO: Add set value and get value (and get-value-as).
	
}
