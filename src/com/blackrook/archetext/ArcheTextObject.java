/*******************************************************************************
 * Copyright (c) 2016-2019 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.archetext;

import java.lang.reflect.Field;
import java.util.Iterator;

import com.blackrook.archetext.annotation.ATIgnore;
import com.blackrook.archetext.annotation.ATName;
import com.blackrook.commons.AbstractSet;
import com.blackrook.commons.ObjectPair;
import com.blackrook.commons.Reflect;
import com.blackrook.commons.ResettableIterator;
import com.blackrook.commons.TypeProfile;
import com.blackrook.commons.TypeProfile.MethodSignature;
import com.blackrook.commons.hash.Hash;
import com.blackrook.commons.hash.HashMap;
import com.blackrook.commons.linkedlist.Queue;
import com.blackrook.commons.linkedlist.Stack;
import com.blackrook.commons.util.ObjectUtils;

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
	private Queue<ArcheTextObject> parents;
	/** Object local fields. */
	private HashMap<String, AField> fields;

	/** Field */
	private class AField
	{
		private Combinator combinator;
		private ArcheTextValue value;
		
		AField(Combinator combinator, ArcheTextValue value)
		{
			this.combinator = combinator;
			this.value = value;
		}
		
		public String toString()
		{
			return combinator.getAssignmentOperator() + " " + value.toString();
		}
		
	}
	
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
		if (!ObjectUtils.isEmpty(name) && ObjectUtils.isEmpty(type))
			throw new IllegalArgumentException("type cannot be empty if the name is not empty.");
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
			parents = new Queue<ArcheTextObject>();
		parents.add(parent);
	}

	/**
	 * Adds a parent to this object.
	 * The new parent is now the lowest priority for inheritance.
	 * @param parent the parent to add.
	 */
	public void addParent(ArcheTextObject parent)
	{
		if (parents == null)
			parents = new Queue<ArcheTextObject>();
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
	public void set(String name, Object value)
	{
		set(name, Combinator.SET, value);
	}
	
	/**
	 * Sets the value of a field.
	 * @param name the name of the field to set.
	 * @param combinator the value combinator for field inheritance.
	 * @param value the value of the field.
	 */
	public void set(String name, Combinator combinator, Object value)
	{
		if (fields == null)
			fields = new HashMap<String, AField>();
		fields.put(name, new AField(combinator, ArcheTextValue.create(value)));
	}
	
	/**
	 * Clears the value of a field.
	 * @param name the name of the field to clear.
	 */
	public void clear(String name)
	{
		if (fields == null)
			return;
		fields.removeUsingKey(name);
		if (fields.isEmpty())
			fields = null;
	}
	
	/**
	 * Returns true of this object (and only the).
	 * @param name the name of the field.
	 */
	public boolean containsLocal(String name)
	{
		if (fields == null)
			return false;
		return fields.containsKey(name);
	}
	
	/**
	 * Gets the value of a local field.
	 * The field's value is taken from THIS OBJECT, not its parents.
	 * @param name the name of the field.
	 * @param outputType the output type to convert to.
	 * @return the value converted to the desired type.
	 */
	public <T> T getLocal(String name, Class<T> outputType)
	{
		if (fields == null)
			return Reflect.createForType(null, outputType);

		AField f = fields.get(name);
		if (f == null)
			return Reflect.createForType(null, outputType);
		else
			return Reflect.createForType(f.value, outputType);
	}
	
	/**
	 * Gets the native value of a local field.
	 * The field's value is taken from THIS OBJECT, not its parents.
	 * @param name the name of the field.
	 */
	AField getLocalField(String name)
	{
		if (fields == null)
			return null;
		return fields.get(name);
	}
	
	/**
	 * Gets the value of a field, searching through its lineage
	 * if it doesn't exist in this one, combining values as necessary.
	 * @param name the name of the field.
	 * @return the value, as an object.
	 */
	public Object get(String name)
	{
		return get(name, Object.class);
	}
	
	/**
	 * Gets the value of a field, searching through its lineage
	 * if it doesn't exist in this one, combining values as necessary.
	 * @param name the name of the field.
	 * @param outputType the output type to convert to.
	 * @return the value converted to the desired type.
	 */
	public <T> T get(String name, Class<T> outputType)
	{
		ArcheTextValue rv = getField(name);
		if (rv == null)
			return Reflect.createForType(null, outputType);
		else
			return rv.createForType(name, outputType); 
	}
	
	/**
	 * Sets the value of a field read in from the reader.
	 */
	void setField(String name, Combinator combinator, ArcheTextValue value)
	{
		if (fields == null)
			fields = new HashMap<String, AField>();
		fields.put(name, new AField(combinator, value));
	}

	/**
	 * Gets the final value of a field, after going through its lineage.
	 * @param name the name of the field.
	 */
	ArcheTextValue getField(String name)
	{
		Stack<AField> fields = new Stack<AField>();
		accumFields(name, this, fields);
		
		ArcheTextValue out = null;
		while (!fields.isEmpty())
		{
			AField field = fields.pop();
			out = field.value.combineWith(field.combinator, out); 
		}
		
		return out;
	}
	
	// recursively finds the correct value.
	private static void accumFields(String name, ArcheTextObject atobject, Stack<AField> values)
	{
		AField field = atobject.getLocalField(name);
		
		if (field != null)
			values.push(field);
		
		if (atobject.parents != null) for (ArcheTextObject parent : atobject.parents)
			accumFields(name, parent, values);
	}
	
	/**
	 * Adds the fields and lineage from another object to this object.
	 * NOTE: Field {@link Combinator}s from the object getting added 
	 * come into effect when setting the values in this object.
	 * @param addend the object to add to this one.
	 */
	public void cascade(ArcheTextObject addend)
	{
		if (addend.parents != null) for (ArcheTextObject parent : addend.parents)
			this.addParent(parent);
		
		Iterator<String> fieldNames = addend.fieldNameIterator();
		while (fieldNames.hasNext())
		{
			String fname = fieldNames.next();
			AField af = addend.getLocalField(fname);
			AField tf = this.getLocalField(fname);
			setField(fname, af.combinator, af.value.combineWith(af.combinator, tf != null ? tf.value : null));
		}
	}
	
	/**
	 * Flattens the hierarchy of this object, such that it has no parent references
	 * and its fields are all SET fields with the hierarchically-calculated results.
	 * This object's contents will be changed. The parents themselves are not changed.  
	 */
	public void flatten()
	{
		for (String name : getAvailableFieldNames())
			setField(name, Combinator.SET, getField(name));
		parents = null;
	}
	
	/**
	 * Returns all possible field names in this object's lineage.
	 * @return an array of every field name.
	 */
	public AbstractSet<String> getAvailableFieldNames()
	{
		Hash<String> nameSet = new Hash<String>(24);
		accumFieldNames(nameSet, this);
		return nameSet;
	}
	
	// finds all field names in the hierarchy.
	private static void accumFieldNames(AbstractSet<String> nameSet, ArcheTextObject object)
	{
		if (object.fields != null) for (ObjectPair<String, AField> pair : object.fields)
			nameSet.put(pair.getKey());
		
		if (object.parents != null) for (ArcheTextObject parent : object.parents)
			accumFieldNames(nameSet, parent);
	}
	
	/**
	 * Converts the contents of this object to a new instance of an object bean / plain ol' Java object.
	 * <p>
	 * This object is applied via the target object's public fields and setter methods.
	 * <p>
	 * For instance, if there is a member on this object called "color", its value
	 * will be applied via the public field "color" or the setter "setColor()". Public
	 * fields take precedence over setters.
	 * @param clazz the output class type.
	 * @return the new object.
	 */
	public <T extends Object> T newObject(Class<T> clazz)
	{
		T out = Reflect.create(clazz);
		applyToObject(out);
		return out;
	}

	/**
	 * Applies this object to an object bean / plain ol' Java object.
	 * <p>
	 * This object is applied via the target object's public fields and setter methods.
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
		Iterator<String> it = getAvailableFieldNames().iterator();
		while (it.hasNext())
		{
			String member = it.next();
			Field field = null; 
			MethodSignature setter = null;
			if ((field = profile.getPublicFields().get(member)) != null && !field.isAnnotationPresent(ATIgnore.class))
				Reflect.setField(object, member, get(member, field.getType()));
			else if ((setter = profile.getSetterMethods().get(member)) != null && !setter.getMethod().isAnnotationPresent(ATIgnore.class))
				Reflect.invokeBlind(setter.getMethod(), object, get(member, setter.getType()));
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
			if (parent.isDefault())
				sb.append(parent.type).append(' ');
			else
				sb.append(parent.type).append(' ').append('"').append(parent.name).append('"').append(' ');
		}

		sb.append("{ ");

		if (fields != null) for (ObjectPair<String, AField> pair : fields)
			sb.append(pair.getKey()).append(' ').append(pair.getValue()).append("; ");
		
		sb.append("}");
		return sb.toString();
	}
	
}
