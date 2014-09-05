package com.blackrook.archetext;

import com.blackrook.archetext.ArcheTextValue.Combinator;
import com.blackrook.commons.Common;
import com.blackrook.commons.linkedlist.Stack;

/**
 * The object representation for all ArcheText objects and values.
 * ArcheText objects have types and can inherit values from one or more objects.
 * @author Matthew Tropiano
 */
public class ArcheTextObject
{
	/** Object type. */
	private String type;
	/** Object name. */
	private String name;
	
	/** Object hierarchy parents. */
	private Stack<ArcheTextObject> parents;

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
	 * Creates a new anonymous ArcheTextObject using a POJO (Plain Ol' Java Object).
	 * Primitives, boxed primitives, Sets, and Arrays are not acceptable.
	 * @throws IllegalArgumentException if value is not a POJO.
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
	 * Pushes a parent onto the top of the parent stack.
	 * The new parent is now the highest priority for inheritance.
	 * @param parent the parent to push.
	 */
	public void pushParent(ArcheTextObject parent)
	{
		if (parents == null)
			parents = new Stack<ArcheTextObject>();
		parents.push(parent);
	}

	/**
	 * Adds a parent to this object.
	 * The new parent is now the lowest priority for inheritance.
	 * @param parent the parent to add.
	 */
	public void addParent(ArcheTextObject parent)
	{
		if (parents == null)
			parents = new Stack<ArcheTextObject>();
		parents.add(parent);
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
	
	/**
	 * Adds the fields and lineage from another object to this object.
	 * NOTE: Field {@link Combinator}s from the object getting added come into effect when setting
	 * the values in this object.
	 * @param addend the object to add to this one.
	 */
	public void combine(ArcheTextObject addend)
	{
		//TODO: Finish.
	}
	
	// TODO: Add set value and get value (and get-value-as).
	
}
