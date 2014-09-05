package com.blackrook.archetext;

import com.blackrook.commons.AbstractVector;
import com.blackrook.commons.Common;
import com.blackrook.commons.hash.HashMap;

/**
 * The root of an ArcheText Hierarchy.
 * This is an object that contains other objects.
 * @author Matthew Tropiano
 */
public class ArcheTextRoot
{
	/**
	 * An object's set of descendants - default and named. 
	 */
	private static class DescendantSet
	{
		/** Default object (no name). */
		private ArcheTextObject defaultObject;
		/** Name to object set. */
		private HashMap<String, ArcheTextObject> nameSet;
		
		boolean isEmpty()
		{
			return !containsDefault() && Common.isEmpty(nameSet);
		}
		
		/** Returns true if this contains a default object. */
		boolean containsDefault()
		{
			return defaultObject != null;
		}
		
		/** Returns true if this contains a default object. Empty name is default. */
		boolean contains(String name)
		{
			if (Common.isEmpty(name))
				return containsDefault();
			else if (nameSet == null)
				return false;
			else
				return nameSet.containsKey(name);
		}
		
		/** Adds an object. Empty name is default. */
		void add(String name, ArcheTextObject object)
		{
			if (Common.isEmpty(name))
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
			if (Common.isEmpty(name))
			{
				ArcheTextObject out = defaultObject;
				defaultObject = null;
				return out;
			}
			else if (nameSet == null)
				return null;
			else
				return nameSet.removeUsingKey(name);
		}
		
		/** Removes an object and returns this reference. Empty name is default.*/
		ArcheTextObject get(String name)
		{
			if (Common.isEmpty(name))
				return defaultObject;
			else if (nameSet == null)
				return null;
			else
				return nameSet.removeUsingKey(name);
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
	 * Adds an object. Empty name is default.
	 */
	public void add(ArcheTextObject object)
	{
		if (descendants == null)
			descendants = new HashMap<String, ArcheTextRoot.DescendantSet>();
		
		String type = object.getType();
		DescendantSet set = descendants.get(type);
		if (set == null)
			descendants.put(type, set = new DescendantSet());
		
		set.add(object.getName(), object);
	}
	
	/**
	 * Removes an object.
	 */
	public boolean remove(ArcheTextObject object)
	{
		if (descendants == null)
			return false;
		
		String type = object.getType();
		DescendantSet set = descendants.get(type);
		if (set == null)
			return false;
		
		String name = object.getName();
		if (set.contains(name))
		{
			set.remove(name);
			if (set.isEmpty())
				descendants.removeUsingKey(type);
			return true;
		}
		
		if (descendants.isEmpty())
			descendants = null;
		
		return false;
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public AbstractVector<ArcheTextObject> getAllByType(String type)
	{
		// TODO: Finish.
		return null;
	}
	
}
