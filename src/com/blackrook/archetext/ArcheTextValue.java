package com.blackrook.archetext;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Set;

import com.blackrook.commons.AbstractMap;
import com.blackrook.commons.AbstractSet;
import com.blackrook.commons.ObjectPair;
import com.blackrook.commons.Reflect;
import com.blackrook.commons.hash.Hash;
import com.blackrook.commons.list.List;

/**
 * The values stored in an {@link ArcheTextObject}.
 * @author Matthew Tropiano
 */
public final class ArcheTextValue
{
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
		OBJECT;
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
		hashCode = type.hashCode() ^ (value != null ? value.hashCode() : 0);
	}

	/**
	 * Creates a new value.
	 * Assigns a SET combinator.
	 * Converts primitives and boxed primitives, strings, arrays, and {@link AbstractSet}s, and {@link AbstractMap}s, and objects.
	 * @param object the input object.
	 * @return an {@link ArcheTextValue} to use in {@link ArcheTextObject}.
	 */
	public static <T> ArcheTextValue create(T object)
	{
		return create(Combinator.SET, object);
	}

	/**
	 * Creates a new value. 
	 * Converts primitives and boxed primitives, strings, arrays, and {@link AbstractSet}s, and {@link AbstractMap}s, and objects.
	 * @param combinator the combinator type. 
	 * @param object the input object.
	 * @return an {@link ArcheTextValue} to use in {@link ArcheTextObject}.
	 */
	public static <T> ArcheTextValue create(Combinator combinator, T object)
	{
		if (object == null)
			return new ArcheTextValue(Type.OBJECT, object);
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
			Hash<ArcheTextValue> set = new Hash<ArcheTextValue>();
			for (Object obj : (Set<?>)object)
				set.put(create(obj));
			return new ArcheTextValue(Type.SET, set);
		}
		else if (object instanceof AbstractSet<?>)
		{
			Hash<ArcheTextValue> set = new Hash<ArcheTextValue>();
			for (Object obj : (AbstractSet<?>)object)
				set.put(create(obj));
			return new ArcheTextValue(Type.SET, set);
		}
		else if (object instanceof Map<?, ?>)
		{
			ArcheTextObject ato = new ArcheTextObject();
			for (Map.Entry<?, ?> pair : ((Map<?, ?>)object).entrySet())
				ato.setField(String.valueOf(pair.getKey()), Combinator.SET, create(Combinator.SET, pair.getValue()));
			return new ArcheTextValue(Type.OBJECT, ato);
		}
		else if (object instanceof AbstractMap<?, ?>)
		{
			ArcheTextObject ato = new ArcheTextObject();
			for (ObjectPair<?, ?> pair : ((AbstractMap<?, ?>)object))
				ato.setField(String.valueOf(pair.getKey()), Combinator.SET, create(Combinator.SET, pair.getValue()));
			return new ArcheTextValue(Type.OBJECT, ato);
		}
		else if (Reflect.isArray(object))
		{
			List<ArcheTextValue> out = new List<ArcheTextValue>();
			int alen = Array.getLength(object);
			for (int i = 0; i < alen; i++)
				out.add(create(Array.get(object, i)));
			return new ArcheTextValue(Type.LIST, out);
		}
		else if (object instanceof Iterable<?>)
		{
			List<ArcheTextValue> out = new List<ArcheTextValue>();
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
	 * Creates a deep copy of this value.
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
				Hash<ArcheTextValue> set = new Hash<ArcheTextValue>();
				for (ArcheTextValue val : (Hash<ArcheTextValue>)this.value)
					set.put(val.copy());
				return new ArcheTextValue(Type.SET, set);
			}
			case LIST:
			{
				// append
				List<ArcheTextValue> list = new List<ArcheTextValue>();
				for (ArcheTextValue val : (List<ArcheTextValue>)this.value)
					list.add(val.copy());
				return new ArcheTextValue(Type.LIST, list);
			}
			default:
			case OBJECT:
			{
				// combine
				ArcheTextObject object = new ArcheTextObject();
				object.cascade((ArcheTextObject)this.value);
				return new ArcheTextValue(Type.OBJECT, object);
			}
		}

	}
	
	/**
	 * Gets the value type.
	 */
	public Type getType()
	{
		return type;
	}

	/**
	 * Gets the value itself.
	 */
	public Object getValue()
	{
		return value;
	}
	
	/**
	 * Combines this value with another and returns the result.
	 * @param combinator the combinator to use.
	 * @param value the value to combine with. 
	 * @return the new value.
	 */
	public ArcheTextValue combineWith(Combinator combinator, ArcheTextValue value)
	{
		return combinator.combine(this, value);
	}
	
	/**
	 * Returns a new ArcheTextValue that is a promoted type
	 * from this one. No types can be promoted to {@link Type#OBJECT}.
	 * If the same type is passed in, this object is returned.
	 * @param promotionType the type to promote to.
	 * @return the new, promoted value.
	 * @throws IllegalArgumentException if the type is less than this one or
	 */
	public ArcheTextValue promoteTo(Type promotionType)
	{
		if (promotionType == Type.OBJECT)
			throw new IllegalArgumentException("Cannot promote this value to an Object.");
		else if (promotionType.ordinal() < type.ordinal())
			throw new IllegalArgumentException("Cannot promote this value to a lesser type. Current is "+type+". Promotion type is "+promotionType+".");
		else if (promotionType.ordinal() == type.ordinal())
			return this;
		else switch (type)
		{
			default:
				throw new IllegalArgumentException("This value does not have a promotable type. Current is "+type+".");
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

	boolean getBoolean()
	{
		return value != null ? ((Boolean)value) : false;  
	}
	
	long getLong()
	{
		return value != null ? ((Long)value).longValue() : 0L;  
	}
	
	double getDouble()
	{
		return value != null ? ((Double)value).doubleValue() : 0.0;  
	}
	
	String getString()
	{
		return value != null ? String.valueOf(value) : null;  
	}
	
	private ArcheTextValue promoteBooleanTo(Type promotionType)
	{
		switch (promotionType)
		{
			default:
				throw new IllegalArgumentException("Cannot promote object to "+promotionType);
			case INTEGER:
				return new ArcheTextValue(Type.INTEGER, ((Boolean)value) ? 1L : 0L);
			case FLOAT:
				return new ArcheTextValue(Type.FLOAT, ((Boolean)value) ? 1.0 : 0.0);
			case STRING:
				return new ArcheTextValue(Type.STRING, String.valueOf(value));
			case SET:
				Hash<ArcheTextValue> set = new Hash<ArcheTextValue>(1);
				set.put(create(value));
				return new ArcheTextValue(Type.SET, set);
			case LIST:
				List<ArcheTextValue> list = new List<ArcheTextValue>(1);
				list.add(create(value));
				return new ArcheTextValue(Type.LIST, list);
		}
	}

	private ArcheTextValue promoteIntegerTo(Type promotionType)
	{
		switch (promotionType)
		{
			default:
				throw new IllegalArgumentException("Cannot promote object to "+promotionType);
			case FLOAT:
				return new ArcheTextValue(Type.FLOAT, ((Number)value).doubleValue());
			case STRING:
				return new ArcheTextValue(Type.STRING, String.valueOf(value));
			case SET:
				Hash<ArcheTextValue> set = new Hash<ArcheTextValue>(1);
				set.put(create(value));
				return new ArcheTextValue(Type.SET, set);
			case LIST:
				List<ArcheTextValue> list = new List<ArcheTextValue>(1);
				list.add(create(value));
				return new ArcheTextValue(Type.LIST, list);
		}
	}

	private ArcheTextValue promoteFloatTo(Type promotionType)
	{
		switch (promotionType)
		{
			default:
				throw new IllegalArgumentException("Cannot promote object to "+promotionType);
			case STRING:
				return new ArcheTextValue(Type.STRING, String.valueOf(value));
			case SET:
				Hash<ArcheTextValue> set = new Hash<ArcheTextValue>(1);
				set.put(create(value));
				return new ArcheTextValue(Type.SET, set);
			case LIST:
				List<ArcheTextValue> list = new List<ArcheTextValue>(1);
				list.add(create(value));
				return new ArcheTextValue(Type.LIST, list);
		}
	}

	private ArcheTextValue promoteStringTo(Type promotionType)
	{
		switch (promotionType)
		{
			default:
				throw new IllegalArgumentException("Cannot promote object to "+promotionType);
			case SET:
				Hash<ArcheTextValue> set = new Hash<ArcheTextValue>(1);
				set.put(create(value));
				return new ArcheTextValue(Type.SET, set);
			case LIST:
				List<ArcheTextValue> list = new List<ArcheTextValue>(1);
				list.add(create(value));
				return new ArcheTextValue(Type.LIST, list);
		}
	}

	private ArcheTextValue promoteSetTo(Type promotionType)
	{
		switch (promotionType)
		{
			default:
				throw new IllegalArgumentException("Cannot promote object to "+promotionType);
			case LIST:
				@SuppressWarnings("unchecked")
				Hash<ArcheTextValue> set = (Hash<ArcheTextValue>)value;
				List<ArcheTextValue> list = new List<ArcheTextValue>(set.size());
				for (ArcheTextValue v : set)
					list.add(create(v));
				return new ArcheTextValue(Type.LIST, list);
		}
	}

	private ArcheTextValue promoteArrayTo(Type promotionType)
	{
		throw new IllegalArgumentException("Cannot promote object to "+promotionType);
	}
	
	@Override
	public String toString()
	{
		return "[" + type + ": " + value + "]";
	}
	
}
