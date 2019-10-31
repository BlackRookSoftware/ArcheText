/*******************************************************************************
 * Copyright (c) 2016-2019 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.archetext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import com.blackrook.archetext.struct.Utils;

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
	private String identity;
	
	/** Object hierarchy parents. */
	private Queue<ArcheTextObject> parents;
	/** Object local fields. */
	private Map<String, AField> fields;

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
	 * @param type the object type name.
	 */
	public ArcheTextObject(String type)
	{
		this(type, null);
	}

	/**
	 * Creates a new ArcheTextObject with a type and identity.
	 * @param type the object type name.
	 * @param identity the object's identity.
	 */
	public ArcheTextObject(String type, String identity)
	{
		if (!Utils.isEmpty(identity) && Utils.isEmpty(type))
			throw new IllegalArgumentException("type cannot be empty if the name is not empty.");
		this.type = type;
		this.identity = identity;
	}
	
	/**
	 * @return the type name of this object.
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @return the identity of this object.
	 */
	public String getIdentity()
	{
		return identity;
	}

	/**
	 * @return an Iterable of the object's parents, in order of precedence.
	 */
	public Iterable<ArcheTextObject> getParents()
	{
		return parents;
	}

	/**
	 * @return true if this object has no type nor name, false otherwise.
	 */
	public boolean isAnonymous()
	{
		return type == null && identity == null;
	}

	/**
	 * @return true if this object has a type but no name, false otherwise.
	 */
	public boolean isDefault()
	{
		return type != null && identity == null;
	}

	/**
	 * Pushes a parent onto the top of the parent stack.
	 * The new parent is now the highest priority for inheritance.
	 * @param parent the parent to push.
	 */
	public void pushParent(ArcheTextObject parent)
	{
		if (parents == null)
			parents = new LinkedList<ArcheTextObject>();
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
			parents = new LinkedList<ArcheTextObject>();
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
		fields.remove(name);
		if (fields.isEmpty())
			fields = null;
	}
	
	/**
	 * Returns true if this object (and only this object) contains a field.
	 * @param name the name of the field.
	 * @return true if so, false if not.
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
			return Utils.createForType(null, outputType);

		AField f = fields.get(name);
		if (f == null)
			return Utils.createForType(null, outputType);
		else
			return Utils.createForType(f.value, outputType);
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
			return Utils.createForType(null, outputType);
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
	public Set<String> getAvailableFieldNames()
	{
		Set<String> nameSet = new HashSet<String>(24);
		accumFieldNames(nameSet, this);
		return nameSet;
	}
	
	// finds all field names in the hierarchy.
	private static void accumFieldNames(Set<String> nameSet, ArcheTextObject object)
	{
		if (object.fields != null) for (Map.Entry<String, AField> pair : object.fields.entrySet())
			nameSet.add(pair.getKey());
		
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
		T out = Utils.create(clazz);
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
	public <T extends Object> T applyToObject(T object)
	{
		return Utils.applyToObject(this, object);
	}
	
	/**
	 * @return a field name iterator for this object (only local fields).
	 */
	public Iterator<String> fieldNameIterator()
	{
		return fields.keySet().iterator();
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
			sb.append(type).append(' ').append('"').append(identity).append('"').append(' ');
		
		if (parents != null) for (ArcheTextObject parent : parents)
		{
			sb.append(": ");
			if (parent.isDefault())
				sb.append(parent.type).append(' ');
			else
				sb.append(parent.type).append(' ').append('"').append(parent.identity).append('"').append(' ');
		}

		sb.append("{ ");

		if (fields != null) for (Map.Entry<String, AField> pair : fields.entrySet())
			sb.append(pair.getKey()).append(' ').append(pair.getValue()).append("; ");
		
		sb.append("}");
		return sb.toString();
	}
	
}
