/*******************************************************************************
 * Copyright (c) 2016-2019 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.archetext;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.blackrook.archetext.struct.Utils;

/**
 * The root of an ArcheText Hierarchy.
 * This is an object that contains other objects.
 * @author Matthew Tropiano
 */
public class ArcheTextRoot
{
	
	private static final ArcheTextObject[] NO_OBJECTS = new ArcheTextObject[0];
	private static final String[] NO_TYPES = new String[0];
	
	/**
	 * An object's set of descendants - default and named. 
	 */
	private static class DescendantSet
	{
		/** Default object (no name). */
		private ArcheTextObject defaultObject;
		/** Name to object set. */
		private Map<String, ArcheTextObject> nameSet;
		
		boolean isEmpty()
		{
			return !containsDefault() && Utils.isEmpty(nameSet);
		}
		
		/** Returns true if this contains a default object. */
		boolean containsDefault()
		{
			return defaultObject != null;
		}
		
		/** Returns true if this contains a default object. Empty name is default. */
		boolean contains(String name)
		{
			if (Utils.isEmpty(name))
				return containsDefault();
			else if (nameSet == null)
				return false;
			else
				return nameSet.containsKey(name);
		}
		
		/** Adds an object. Empty name is default. */
		void add(String name, ArcheTextObject object)
		{
			if (Utils.isEmpty(name))
				defaultObject = object;
			else
			{
				if (nameSet == null)
					nameSet = new HashMap<String, ArcheTextObject>();
				nameSet.put(name, object);
			}
		}
		
		/** Removes an object and returns this reference. Empty name is default.*/
		ArcheTextObject remove(String name)
		{
			if (Utils.isEmpty(name))
			{
				ArcheTextObject out = defaultObject;
				defaultObject = null;
				return out;
			}
			else if (nameSet == null)
				return null;
			else
				return nameSet.remove(name);
		}
		
		/** Removes an object and returns this reference. Empty name is default.*/
		ArcheTextObject get(String name)
		{
			if (Utils.isEmpty(name))
				return defaultObject;
			else if (nameSet == null)
				return null;
			else
				return nameSet.get(name);
		}
		
	}

	/** Object descendants by type. */
	private HashMap<String, DescendantSet> descendants;

	/** 
	 * Creates a new root. 
	 */
	public ArcheTextRoot()
	{
		descendants = null;
	}
	
	/**
	 * Gets a default object by type.
	 * @param type object type.
	 * @return the corresponding object, or null if no default.
	 */
	public ArcheTextObject get(String type)
	{
		if (descendants == null)
			return null;
		
		DescendantSet set = descendants.get(type);
		if (set == null)
			return null;
		
		return set.get(null);
	}

	/**
	 * Gets an object by type and name. Empty name is default.
	 * @param type object type.
	 * @param name object name.
	 * @return the corresponding object, or null if not found.
	 */
	public ArcheTextObject get(String type, String name)
	{
		if (descendants == null)
			return null;
		
		DescendantSet set = descendants.get(type);
		if (set == null)
			return null;
		
		return set.get(name);
	}
	
	/**
	 * @return all types that this root contains.
	 */
	public String[] getTypes()
	{
		if (Utils.isEmpty(descendants))
			return NO_TYPES;
		
		String[] out = new String[descendants.size()];
		Iterator<String> it = descendants.keySet().iterator();
		int i = 0;
		while (it.hasNext())
			out[i++] = it.next();
		return out;
	}
	
	/**
	 * Returns all ArcheTextObjects of a particular type.
	 * @param type the type name.
	 * @return all of the relevant objects found as an array. Never null.
	 */
	public ArcheTextObject[] getAllByType(String type)
	{
		if (descendants == null)
			return NO_OBJECTS;
		
		DescendantSet set = descendants.get(type);
		if (set == null)
			return NO_OBJECTS;
		
		List<ArcheTextObject> list = new ArrayList<ArcheTextObject>(200);
		if (set.containsDefault())
			list.add(set.defaultObject);
		if (set.nameSet != null) for (Map.Entry<String, ArcheTextObject> pair : set.nameSet.entrySet())
			list.add(pair.getValue());
		
		ArcheTextObject[] out = new ArcheTextObject[list.size()];
		list.toArray(out);
		return out;
	}

	/**
	 * Returns all ArcheTextObjects of a particular type exported as POJOs.
	 * @param <T> the return type.
	 * @param type the type name.
	 * @param outputType the output class type.
	 * @return all of the relevant objects found as an array. Never null.
	 */
	public <T> T[] exportByType(String type, Class<T> outputType)
	{
		ArcheTextObject[] objects = getAllByType(type);
		
		@SuppressWarnings("unchecked")
		T[] out = (T[])Array.newInstance(outputType, objects.length);
		
		int x = 0;
		for (ArcheTextObject obj : objects)
			out[x++] = obj.newObject(outputType);
		
		return out;
	}

	/**
	 * Adds an object.
	 * @param object the object to add.
	 */
	public void add(ArcheTextObject object)
	{
		if (descendants == null)
			descendants = new HashMap<String, ArcheTextRoot.DescendantSet>();
		
		String type = object.getType();
		DescendantSet set = descendants.get(type);
		if (set == null)
			descendants.put(type, set = new DescendantSet());
		
		set.add(object.getIdentity(), object);
	}
	
	/**
	 * Removes an object.
	 * @param object 
	 * @return true if removed, false if not.
	 */
	public boolean remove(ArcheTextObject object)
	{
		if (descendants == null)
			return false;
		
		String type = object.getType();
		DescendantSet set = descendants.get(type);
		if (set == null)
			return false;
		
		String name = object.getIdentity();
		if (set.contains(name))
		{
			set.remove(name);
			if (set.isEmpty())
				descendants.remove(type);
			return true;
		}
		
		if (descendants.isEmpty())
			descendants = null;
		
		return false;
	}
	
}
