package com.blackrook.archetext;

import java.lang.reflect.Field;
import java.util.Iterator;

import com.blackrook.archetext.ArcheTextValue.Combinator;
import com.blackrook.archetext.annotation.ATIgnore;
import com.blackrook.archetext.annotation.ATName;
import com.blackrook.commons.Common;
import com.blackrook.commons.ObjectPair;
import com.blackrook.commons.Reflect;
import com.blackrook.commons.ResettableIterator;
import com.blackrook.commons.TypeProfile;
import com.blackrook.commons.TypeProfile.MethodSignature;
import com.blackrook.commons.hash.HashMap;
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
	/** Object local fields. */
	private HashMap<String, ArcheTextValue> fields;

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
	 * Sets the value of a field.
	 * Equivalent to <code>setField(name, Combinator.SET, value)</code>.
	 * @param name the name of the field to set.
	 * @param value the value of the field.
	 */
	public void setField(String name, Object value)
	{
		setField(name, Combinator.SET, value);
	}
	
	/**
	 * Sets the value of a field.
	 * @param name the name of the field to set.
	 * @param combinator the value combinator for field inheritance.
	 * @param value the value of the field.
	 */
	public void setField(String name, Combinator combinator, Object value)
	{
		if (fields == null)
			fields = new HashMap<String, ArcheTextValue>();
		fields.put(name, ArcheTextValue.create(combinator, value));
	}
	
	/**
	 * Sets the value of a field.
	 */
	private void setField(String name, ArcheTextValue value)
	{
		if (fields == null)
			fields = new HashMap<String, ArcheTextValue>();
		fields.put(name, value);
	}
	
	/**
	 * Clears the value of a field.
	 * @param name the name of the field to clear.
	 */
	public void clearField(String name)
	{
		if (fields == null)
			return;
		fields.removeUsingKey(name);
		if (fields.isEmpty())
			fields = null;
	}
	
	/**
	 * Gets the value of a local field.
	 * The field's value is taken from THIS OBJECT, not its parents.
	 * @param name the name of the field.
	 * @return the value converted to the desired type.
	 */
	public <T> T getLocalField(String name, Class<T> outputType)
	{
		if (fields == null)
			return Reflect.createForType(null, outputType);

		ArcheTextValue value = fields.get(name);
		if (value == null)
			return Reflect.createForType(null, outputType);
		else
			return Reflect.createForType(value.getValue(), outputType);
	}
	
	/**
	 * Gets the native value of a local field.
	 * The field's value is taken from THIS OBJECT, not its parents.
	 * @param name the name of the field.
	 */
	protected ArcheTextValue getLocalValue(String name)
	{
		if (fields == null)
			return null;
		return fields.get(name);
	}
	
	/**
	 * Gets the value of a local field.
	 * The field's value is taken from THIS OBJECT, not its parents.
	 * @param name the name of the field.
	 * @return the value converted to the desired type.
	 */
	public <T> T getField(String name, Class<T> outputType)
	{
		return Reflect.createForType(recurseValue(name, this).getValue(), outputType); 
	}
	
	// recursively finds the correct value.
	private static ArcheTextValue recurseValue(String name, ArcheTextObject atobject)
	{
		ArcheTextValue out = atobject.getLocalValue(name);
		if (out != null && out.getCombinator() == Combinator.SET)
			return out.copy();
		
		for (ArcheTextObject parent : atobject.parents)
			out = out != null ? out.combineWith(recurseValue(name, parent)) : recurseValue(name, parent);
		
		return out;
	}
	
	/**
	 * Adds the fields and lineage from another object to this object.
	 * NOTE: Field {@link Combinator}s from the object getting added 
	 * come into effect when setting the values in this object.
	 * @param addend the object to add to this one.
	 */
	public void cascade(ArcheTextObject addend)
	{
		for (ArcheTextObject parent : addend.parents)
			this.parents.add(parent);
		
		Iterator<String> fieldNames = addend.fieldNameIterator();
		while (fieldNames.hasNext())
		{
			String fname = fieldNames.next();
			setField(fname, addend.getLocalValue(fname).combineWith(getLocalValue(fname)));
		}
	}
	
	/**
	 * Applies this object to an object bean / plain ol' Java object, or Array.
	 * <p>
	 * This JSON object is applied via the target object's public fields
	 * and setter methods, if an object.
	 * <p>
	 * For instance, if there is a member on this object called "color", its value
	 * will be applied via the public field "color" or the setter "setColor()". Public
	 * fields take precedence over setters.
	 * @param object the target object.
	 * @return the applied object itself.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Object> T applyToObject(T object)
	{
		TypeProfile<T> profile = TypeProfile.getTypeProfile((Class<T>)object.getClass());
		Iterator<String> it = fieldNameIterator();
		while (it.hasNext())
		{
			String member = it.next();
			Field field = null; 
			MethodSignature setter = null;
			if ((field = profile.getPublicFields().get(member)) != null && !field.isAnnotationPresent(ATIgnore.class))
				Reflect.setField(object, member, getField(member, field.getType()));
			else if ((setter = profile.getSetterMethods().get(member)) != null && !setter.getMethod().isAnnotationPresent(ATIgnore.class))
				Reflect.invokeBlind(setter.getMethod(), object, getField(member, setter.getType()));
		}
		
		for (Field f : profile.getAnnotatedPublicFields(ATName.class))
			Reflect.setField(object, f.getName(), Reflect.createForType(getName(), String.class));
		for (MethodSignature setter : profile.getAnnotatedSetters(ATName.class))
			Reflect.invokeBlind(setter.getMethod(), object, Reflect.createForType(getName(), String.class));			
		
		return object;
	}
	
	/**
	 * Returns a field name iterator for this object (only local fields).
	 */
	public ResettableIterator<String> fieldNameIterator()
	{
		return fields.keyIterator();
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		if (isAnonymous())
			sb.append("[ANONYMOUS] ");
		else if (isDefault())
			sb.append(type).append(' ');
		else
			sb.append(type).append(' ').append('"').append(name).append('"').append(' ');
		
		if (parents != null) for (ArcheTextObject parent : parents)
		{
			sb.append(": ");
			if (isDefault())
				sb.append(parent.type).append(' ');
			else
				sb.append(parent.type).append(' ').append('"').append(parent.name).append('"').append(' ');
		}

		sb.append("{ ");

		if (fields != null) for (ObjectPair<String, ArcheTextValue> pair : fields)
			sb.append(pair.getKey()).append(' ').append(pair.getValue()).append("; ");
		
		sb.append("}");
		return sb.toString();
	}
	
}
