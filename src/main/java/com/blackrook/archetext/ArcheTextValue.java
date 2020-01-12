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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blackrook.archetext.exception.ArcheTextConversionException;
import com.blackrook.archetext.exception.ArcheTextOperationException;
import com.blackrook.archetext.struct.Utils;

/**
 * The values stored in an {@link ArcheTextObject}.
 * @author Matthew Tropiano
 */
public final class ArcheTextValue
{
	/** Null value. */
	public static final ArcheTextValue NULL = new ArcheTextValue(Type.NULL, null);
	
	/**
	 * ArcheText value internal types.
	 */
	public static enum Type
	{
		/** Boolean type. */
		BOOLEAN,
		/** Integer type. Stored as Long. */
		INTEGER,
		/** Floating-point type. Stored as Double. */
		FLOAT,
		/** String type. */
		STRING,
		/** Set type. Stored as Hash<ArcheTextValue>. */
		SET, 
		/** List type. Stored as List<ArcheTextValue>. */
		LIST,
		/** Object type for objects, or null. Stored as HashMap<String, ArcheTextObject>, or null. */
		OBJECT,
		/** Object type for null. */
		NULL;
	}

	/** Object value type. */
	Type type;
	/** Object internal value. */
	Object value;
	
	/** Calculated hashcode. */
	private int hashCode;
	
	ArcheTextValue(Type type, Object value)
	{
		this.type = type;
		this.value = value;
		calculateHashCode();
	}
	
	// recalculates the hashcode.
	private void calculateHashCode()
	{
		hashCode = type.hashCode() ^ (!isNull() ? value.hashCode() : 0);
	}

	/**
	 * Creates a new value. 
	 * Converts primitives and boxed primitives, strings, arrays, and {@link Set}s, and {@link Map}s, and objects.
	 * @param object the input object.
	 * @return an {@link ArcheTextValue} to use in {@link ArcheTextObject}.
	 */
	public static <T> ArcheTextValue create(T object)
	{
		if (object == null)
			return ArcheTextValue.NULL;
		else if (object instanceof ArcheTextObject)
		{
			return new ArcheTextValue(Type.OBJECT, object);
		}
		else if (object instanceof Enum)
		{
			return new ArcheTextValue(Type.STRING, object);
		}
		else if (object instanceof Set<?>)
		{
			Set<ArcheTextValue> set = new HashSet<ArcheTextValue>();
			for (Object obj : (Set<?>)object)
				set.add(create(obj));
			return new ArcheTextValue(Type.SET, set);
		}
		else if (object instanceof Map<?, ?>)
		{
			ArcheTextObject ato = new ArcheTextObject();
			for (Map.Entry<?, ?> pair : ((Map<?, ?>)object).entrySet())
				ato.setField(String.valueOf(pair.getKey()), Combinator.SET, create(pair.getValue()));
			return new ArcheTextValue(Type.OBJECT, ato);
		}
		else if (Utils.isArray(object))
		{
			List<ArcheTextValue> out = new ArrayList<ArcheTextValue>();
			int alen = Array.getLength(object);
			for (int i = 0; i < alen; i++)
				out.add(create(Array.get(object, i)));
			return new ArcheTextValue(Type.LIST, out);
		}
		else if (object instanceof Iterable<?>)
		{
			List<ArcheTextValue> out = new ArrayList<ArcheTextValue>();
			for (Object obj : ((Iterable<?>)object))
				out.add(create(obj));
			return new ArcheTextValue(Type.LIST, out);
		}
		else if (object instanceof Boolean)
			return new ArcheTextValue(Type.BOOLEAN, object);
		else if (object instanceof Character)
			return new ArcheTextValue(Type.STRING, String.valueOf(object));
		else if (object instanceof Long || object instanceof Integer || object instanceof Short || object instanceof Byte)
			return new ArcheTextValue(Type.INTEGER, ((Number)object).longValue());
		else if (object instanceof Float || object instanceof Double)
			return new ArcheTextValue(Type.FLOAT, ((Number)object).doubleValue());
		else if (object instanceof String)
			return new ArcheTextValue(Type.STRING, object);
		else
			return new ArcheTextValue(Type.OBJECT, ArcheTextFactory.create(object));
	}

	/**
	 * Converts this value to another Java object type.
	 * @param memberName the name of this value, if a member of another object.
	 * @param type
	 * @return
	 */
	public <T> T createForType(String memberName, Class<T> type)
	{
		if (this.isNull())
			return Utils.createForType(null, type);
		
		Type attype = this.type;
		
		switch (attype)
		{
			default:
			case BOOLEAN:
			case FLOAT:
			case INTEGER:
			case STRING:
				return Utils.createForType(this.value, type);
			case LIST:
			{
				@SuppressWarnings("unchecked")
				List<ArcheTextValue> val = (List<ArcheTextValue>)this.value;
				
				// type is array
				if (Utils.isArray(type))
				{
					Class<?> atype = Utils.getArrayType(type);
					if (atype == null)
						throw new ArcheTextConversionException((memberName != null ? "Member "+memberName : "Value") + " cannot be converted; member is list and target is not array typed.");
					
					Object newarray = Array.newInstance(atype, val.size());
					for (int i = 0; i < val.size(); i++)
						Array.set(newarray, i, val.get(i).createForType(String.format("%s[%d]", memberName, i), atype));
						
					return type.cast(newarray);
				}
				else
					throw new ClassCastException((memberName != null ? "Member "+memberName : "Value") + " cannot be converted; member is list and target is not array typed.");
			}
			
			case SET:
			{
				@SuppressWarnings("unchecked")
				Set<ArcheTextValue> val = (Set<ArcheTextValue>)this.value;
				
				// type is array
				if (Utils.isArray(type))
				{
					Class<?> atype = Utils.getArrayType(type);
					if (atype == null)
						throw new ArcheTextConversionException((memberName != null ? "Member "+memberName : "Value") + " cannot be converted; member is set and target is not array typed.");
					
					Object newarray = Array.newInstance(atype, val.size());
					int i = 0;
					for (ArcheTextValue v : val)
					{
						Array.set(newarray, i, v.createForType(String.format("%s[%d]", memberName, i), atype));
						i++;
					}
						
					return type.cast(newarray);
				}
				else
					throw new ClassCastException((memberName != null ? "Member "+memberName : "Value") + " cannot be converted; member is set and target is not array typed.");
			}
			
			case OBJECT:
			{
				// type is array
				if (Utils.isArray(type))
				{
					Class<?> atype = Utils.getArrayType(type);
					if (atype == null)
						throw new ArcheTextConversionException((memberName != null ? "Member "+memberName : "Value") + " cannot be converted; member is set and target is not array typed.");

					Object newarray = Array.newInstance(atype, 1);
					ArcheTextObject val = (ArcheTextObject)this.value;
					Array.set(newarray, 0, val.newObject(atype));
					return type.cast(newarray);
				}
				else
				{
					ArcheTextObject val = (ArcheTextObject)this.value;
					T obj = val.newObject(type);
					return obj;
				}
				
			}
			
		}
		
	}

	@Override
	public int hashCode()
	{
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof ArcheTextValue)
			return this.equals((ArcheTextValue)obj);
		return super.equals(obj);
	}
	
	/**
	 * Equality test.
	 * @param other the other value.
	 * @return true if equal, false if not.
	 */
	public boolean equals(ArcheTextValue other)
	{
		if (other == null)
			return false;
		
		return 
			this.type == other.type 
			&& (
				this.value == null 
					? other.value == null
					: other.value != null 
						? this.value.equals(other.value)
						: false
			)
			;
	}
	
	/**
	 * @return a deep copy of this value.
	 */
	@SuppressWarnings("unchecked")
	public ArcheTextValue copy()
	{
		switch (this.type)
		{
			case BOOLEAN:
				return new ArcheTextValue(this.type, getBoolean());
			case INTEGER:
				return new ArcheTextValue(this.type, getLong());
			case FLOAT:
				return new ArcheTextValue(this.type, getDouble());
			case STRING:
				return new ArcheTextValue(this.type, getString());
			case SET:
			{
				// union
				Set<ArcheTextValue> set = new HashSet<ArcheTextValue>();
				for (ArcheTextValue val : (Set<ArcheTextValue>)this.value)
					set.add(val.copy());
				return new ArcheTextValue(Type.SET, set);
			}
			case LIST:
			{
				// append
				List<ArcheTextValue> list = new ArrayList<ArcheTextValue>();
				for (ArcheTextValue val : (List<ArcheTextValue>)this.value)
					list.add(val.copy());
				return new ArcheTextValue(Type.LIST, list);
			}
			case OBJECT:
			{
				// combine
				ArcheTextObject object = new ArcheTextObject();
				if (this.value != null)
					object.cascade((ArcheTextObject)this.value);
				return new ArcheTextValue(Type.OBJECT, object);
			}
			default:
				return new ArcheTextValue(Type.NULL, null);
		}

	}
	
	/**
	 * @return the value type.
	 */
	public Type getType()
	{
		return type;
	}

	/**
	 * @return the value itself.
	 */
	public Object getValue()
	{
		return value;
	}
	
	/**
	 * Returns if this object is null-valued.
	 * @return true if so, false if not.
	 */
	public boolean isNull()
	{
		return type == Type.NULL;
	}
	
	boolean getBoolean()
	{
		return !isNull() ? ((Boolean)value) : false;  
	}

	long getLong()
	{
		return !isNull() ? ((Long)value).longValue() : 0L;  
	}

	double getDouble()
	{
		return !isNull() ? ((Double)value).doubleValue() : 0.0;  
	}

	String getString()
	{
		return !isNull() ? String.valueOf(value) : null;  
	}

	/**
	 * Combines this value with another and returns the result.
	 * @param combinator the combinator to use.
	 * @param source the source value to combine this with. Can be null for {@link #NULL}.
	 * @return the new value.
	 */
	public ArcheTextValue combineWith(Combinator combinator, ArcheTextValue source)
	{
		return combinator.combine(this, source == null ? ArcheTextValue.NULL : source);
	}
	
	/**
	 * Returns a new ArcheTextValue that is a promoted type
	 * from this one. No types can be promoted to {@link Type#OBJECT}.
	 * If the same type is passed in, this object is returned.
	 * @param promotionType the type to promote to.
	 * @return the new, promoted value.
	 * @throws ArcheTextOperationException if the type is less than this one or not promotable.
	 */
	public ArcheTextValue promoteTo(Type promotionType)
	{
		if (promotionType == Type.NULL)
			throw new ArcheTextOperationException("Cannot promote this value to null.");
		else if (promotionType == Type.OBJECT)
			throw new ArcheTextOperationException("Cannot promote this value to an Object.");
		else if (promotionType.ordinal() < type.ordinal())
			throw new ArcheTextOperationException("Cannot promote this value to a lesser type. Current is "+type+". Promotion type is "+promotionType+".");
		else if (promotionType.ordinal() == type.ordinal())
			return this;
		else switch (type)
		{
			default:
				throw new ArcheTextOperationException("This value does not have a promotable type. Current is "+type+".");
			case BOOLEAN:
				return promoteBooleanTo(promotionType);
			case INTEGER:
				return promoteIntegerTo(promotionType);
			case FLOAT:
				return promoteFloatTo(promotionType);
			case STRING:
				return promoteStringTo(promotionType);
			case SET:
				return promoteSetTo(promotionType);
			case LIST:
				return promoteArrayTo(promotionType);
		}
	}

	/**
	 * Negates the value of this value, returning a new one.
	 * Only works for integers, floats, and strings (strings are set to lower case).
	 * @return a new value.
	 * @throws ArcheTextOperationException if the current type is not a correct type.
	 */
	public ArcheTextValue negate()
	{
		switch (type)
		{
			case INTEGER:
				return new ArcheTextValue(Type.INTEGER, -getLong());
			case FLOAT:
				return new ArcheTextValue(Type.FLOAT, -getDouble());
			case STRING:
				return new ArcheTextValue(Type.STRING, getString().toLowerCase());
			default:
				throw new ArcheTextOperationException("This value does not have a negatable type. Current is "+type+".");
		}
	}
	
	/**
	 * Returns the logical "not" value of this value, returning a new one.
	 * Only works for booleans.
	 * @return a new value.
	 * @throws ArcheTextOperationException if the current type is not a correct type.
	 */
	public ArcheTextValue not()
	{
		switch (type)
		{
			case BOOLEAN:
				return new ArcheTextValue(Type.BOOLEAN, !getBoolean());
			default:
				throw new ArcheTextOperationException("This value does not have a \"not\"-able type. Current is "+type+".");
		}
	}
	
	/**
	 * Returns the bitwise "not" value of this value, returning a new one.
	 * Only works for booleans, integers, and floats.
	 * @return a new value.
	 * @throws ArcheTextOperationException if the current type is not a correct type.
	 */
	public ArcheTextValue bitwiseNot()
	{
		switch (type)
		{
			case BOOLEAN:
				return new ArcheTextValue(Type.BOOLEAN, !getBoolean());
			case INTEGER:
				return new ArcheTextValue(Type.INTEGER, ~getLong());
			case FLOAT:
				return new ArcheTextValue(Type.FLOAT, Double.longBitsToDouble(~Double.doubleToLongBits(getDouble())));
			default:
				throw new ArcheTextOperationException("This value does not have a bitwise-not-able type. Current is "+type+".");
		}
	}
	
	/**
	 * Returns the absolute value of this value, returning a new one.
	 * Only works for integers, floats, and strings (strings are set to upper case).
	 * @return a new value.
	 * @throws ArcheTextOperationException if the current type is not a correct type.
	 */
	public ArcheTextValue absolute()
	{
		switch (type)
		{
			case INTEGER:
				return new ArcheTextValue(Type.INTEGER, Math.abs(getLong()));
			case FLOAT:
				return new ArcheTextValue(Type.FLOAT, Math.abs(getDouble()));
			case STRING:
				return new ArcheTextValue(Type.STRING, getString().toUpperCase());
			default:
				throw new ArcheTextOperationException("This value does not have a absolute-able type. Current is "+type+".");
		}
	}
	
	private ArcheTextValue promoteBooleanTo(Type promotionType)
	{
		switch (promotionType)
		{
			default:
				throw new ArcheTextOperationException("Cannot promote "+type.name()+" to "+promotionType.name());
			case INTEGER:
				return new ArcheTextValue(Type.INTEGER, ((Boolean)value) ? 1L : 0L);
			case FLOAT:
				return new ArcheTextValue(Type.FLOAT, ((Boolean)value) ? 1.0 : 0.0);
			case STRING:
				return new ArcheTextValue(Type.STRING, String.valueOf(value));
			case SET:
				Set<ArcheTextValue> set = new HashSet<ArcheTextValue>(1);
				set.add(create(value));
				return new ArcheTextValue(Type.SET, set);
			case LIST:
				List<ArcheTextValue> list = new ArrayList<ArcheTextValue>(1);
				list.add(create(value));
				return new ArcheTextValue(Type.LIST, list);
		}
	}

	private ArcheTextValue promoteIntegerTo(Type promotionType)
	{
		switch (promotionType)
		{
			default:
				throw new ArcheTextOperationException("Cannot promote "+type.name()+" to "+promotionType.name());
			case FLOAT:
				return new ArcheTextValue(Type.FLOAT, ((Number)value).doubleValue());
			case STRING:
				return new ArcheTextValue(Type.STRING, String.valueOf(value));
			case SET:
				Set<ArcheTextValue> set = new HashSet<ArcheTextValue>(1);
				set.add(create(value));
				return new ArcheTextValue(Type.SET, set);
			case LIST:
				List<ArcheTextValue> list = new ArrayList<ArcheTextValue>(1);
				list.add(create(value));
				return new ArcheTextValue(Type.LIST, list);
		}
	}

	private ArcheTextValue promoteFloatTo(Type promotionType)
	{
		switch (promotionType)
		{
			default:
				throw new ArcheTextOperationException("Cannot promote "+type.name()+" to "+promotionType.name());
			case STRING:
				return new ArcheTextValue(Type.STRING, String.valueOf(value));
			case SET:
				Set<ArcheTextValue> set = new HashSet<ArcheTextValue>(1);
				set.add(create(value));
				return new ArcheTextValue(Type.SET, set);
			case LIST:
				List<ArcheTextValue> list = new ArrayList<ArcheTextValue>(1);
				list.add(create(value));
				return new ArcheTextValue(Type.LIST, list);
		}
	}

	private ArcheTextValue promoteStringTo(Type promotionType)
	{
		switch (promotionType)
		{
			default:
				throw new ArcheTextOperationException("Cannot promote "+type.name()+" to "+promotionType.name());
			case SET:
				Set<ArcheTextValue> set = new HashSet<ArcheTextValue>(1);
				set.add(create(value));
				return new ArcheTextValue(Type.SET, set);
			case LIST:
				List<ArcheTextValue> list = new ArrayList<ArcheTextValue>(1);
				list.add(create(value));
				return new ArcheTextValue(Type.LIST, list);
		}
	}

	private ArcheTextValue promoteSetTo(Type promotionType)
	{
		switch (promotionType)
		{
			default:
				throw new ArcheTextOperationException("Cannot promote "+type.name()+" to "+promotionType.name());
			case LIST:
				@SuppressWarnings("unchecked")
				Set<ArcheTextValue> set = (Set<ArcheTextValue>)value;
				List<ArcheTextValue> list = new ArrayList<ArcheTextValue>(set.size());
				for (ArcheTextValue v : set)
					list.add(create(v));
				return new ArcheTextValue(Type.LIST, list);
		}
	}

	private ArcheTextValue promoteArrayTo(Type promotionType)
	{
		throw new ArcheTextOperationException("Cannot promote "+type.name()+" to "+promotionType.name());
	}
	
	@Override
	public String toString()
	{
		return "[" + type + ": " + value + "]";
	}
	
}
