package com.blackrook.archetext;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Set;

import com.blackrook.commons.AbstractMap;
import com.blackrook.commons.AbstractSet;
import com.blackrook.commons.Reflect;
import com.blackrook.commons.hash.Hash;
import com.blackrook.commons.list.List;

/**
 * The values stored in an {@link ArcheTextObject}.
 * @author Matthew Tropiano
 */
public class ArcheTextValue
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

	/**
	 * ArcheText object internal accumulation types.
	 */
	public static enum Combinator
	{
		SET
		{
			@Override
			public ArcheTextValue combine(ArcheTextValue base, ArcheTextValue operand)
			{
				return operand;
			}
		},
		
		ADD
		{
			@Override
			@SuppressWarnings("unchecked")
			public ArcheTextValue combine(ArcheTextValue base, ArcheTextValue operand)
			{
				if (base == null)
					return operand.copy();
				
				if (base.type.compareTo(operand.type) < 0)
					base = base.promoteTo(operand.type);
				else if (base.type.compareTo(operand.type) > 0)
					operand = operand.promoteTo(base.type);
				
				switch (operand.type)
				{
					case BOOLEAN:
						return new ArcheTextValue(Type.BOOLEAN, base.combinator, base.getBoolean() || operand.getBoolean());
					case INTEGER:
						return new ArcheTextValue(Type.INTEGER, base.combinator, base.getLong() + operand.getLong());
					case FLOAT:
						return new ArcheTextValue(Type.FLOAT, base.combinator, base.getDouble() + operand.getDouble());
					case STRING:
						return new ArcheTextValue(Type.STRING, base.combinator, base.getString() + operand.getString());
					case SET:
					{
						// union
						Hash<ArcheTextValue> set = new Hash<ArcheTextValue>();
						for (ArcheTextValue val : (Hash<ArcheTextValue>)base.value)
							set.put(val);
						for (ArcheTextValue val : (Hash<ArcheTextValue>)operand.value)
							set.put(val);
						return new ArcheTextValue(Type.SET, base.combinator, set);
					}
					case LIST:
					{
						// append
						List<ArcheTextValue> list = new List<ArcheTextValue>();
						for (ArcheTextValue val : (List<ArcheTextValue>)base.value)
							list.add(val);
						for (ArcheTextValue val : (List<ArcheTextValue>)operand.value)
							list.add(val);
						return new ArcheTextValue(Type.LIST, base.combinator, list);
					}
					case OBJECT:
					{
						// combine
						ArcheTextObject object = new ArcheTextObject();
						object.combine((ArcheTextObject)base.value);
						object.combine((ArcheTextObject)operand.value);
						return new ArcheTextValue(Type.LIST, base.combinator, object);
					}
				}
				
				return null;
			}
		},
		
		;
		
		/** Combines two values. */
		public abstract ArcheTextValue combine(ArcheTextValue base, ArcheTextValue operand);
		
	}

	/** Object value type. */
	private Type type;
	/** Object combine type - how to merge with parent values. */
	private Combinator combinator;
	/** Object internal value. */
	private Object value;
	
	ArcheTextValue(Type type, Combinator combine, Object value)
	{
		this.type = type;
		this.combinator = combine;
		this.value = value;
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
			return null;
		else if (object instanceof ArcheTextObject)
		{
			return new ArcheTextValue(Type.OBJECT, combinator, object);
		}
		else if (object instanceof Enum)
		{
			return new ArcheTextValue(Type.STRING, combinator, object);
		}
		else if (object instanceof Set<?>)
		{
			Hash<ArcheTextValue> set = new Hash<ArcheTextValue>();
			for (Object obj : (Set<?>)object)
				set.put(create(obj));
			return new ArcheTextValue(Type.SET, combinator, set);
		}
		else if (object instanceof AbstractSet<?>)
		{
			Hash<ArcheTextValue> set = new Hash<ArcheTextValue>();
			for (Object obj : (AbstractSet<?>)object)
				set.put(create(obj));
			return new ArcheTextValue(Type.SET, combinator, set);
		}
		else if (object instanceof Map<?, ?>)
		{
			return new ArcheTextValue(Type.OBJECT, combinator, ArcheTextFactory.create(object));
		}
		else if (object instanceof AbstractMap<?, ?>)
		{
			return new ArcheTextValue(Type.OBJECT, combinator, ArcheTextFactory.create(object));
		}
		else if (Reflect.isArray(object))
		{
			List<ArcheTextValue> out = new List<ArcheTextValue>();
			int alen = Array.getLength(object);
			for (int i = 0; i < alen; i++)
				out.add(create(Array.get(object, i)));
			return new ArcheTextValue(Type.LIST, combinator, out);
		}
		else if (object instanceof Iterable<?>)
		{
			List<ArcheTextValue> out = new List<ArcheTextValue>();
			for (Object obj : ((Iterable<?>)object))
				out.add(create(obj));
			return new ArcheTextValue(Type.LIST, combinator, out);
		}
		else if (object instanceof Boolean)
			return new ArcheTextValue(Type.BOOLEAN, combinator, object);
		else if (object instanceof Character)
			return new ArcheTextValue(Type.STRING, combinator, String.valueOf(object));
		else if (object instanceof Long || object instanceof Integer || object instanceof Short || object instanceof Byte)
			return new ArcheTextValue(Type.INTEGER, combinator, ((Number)object).longValue());
		else if (object instanceof Float || object instanceof Double)
			return new ArcheTextValue(Type.FLOAT, combinator, ((Number)object).doubleValue());
		else if (object instanceof String)
			return new ArcheTextValue(Type.STRING, combinator, object);
		else
			return new ArcheTextValue(Type.OBJECT, combinator, ArcheTextFactory.create(object));
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
		return 
			this.type == other.type 
			&& this.combinator == other.combinator
			&& this.value.equals(other.value)
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
				return new ArcheTextValue(this.type, this.combinator, getBoolean());
			case INTEGER:
				return new ArcheTextValue(this.type, this.combinator, getLong());
			case FLOAT:
				return new ArcheTextValue(this.type, this.combinator, getDouble());
			case STRING:
				return new ArcheTextValue(this.type, this.combinator, getString());
			case SET:
			{
				// union
				Hash<ArcheTextValue> set = new Hash<ArcheTextValue>();
				for (ArcheTextValue val : (Hash<ArcheTextValue>)this.value)
					set.put(val.copy());
				return new ArcheTextValue(Type.SET, this.combinator, set);
			}
			case LIST:
			{
				// append
				List<ArcheTextValue> list = new List<ArcheTextValue>();
				for (ArcheTextValue val : (List<ArcheTextValue>)this.value)
					list.add(val.copy());
				return new ArcheTextValue(Type.LIST, this.combinator, list);
			}
			default:
			case OBJECT:
			{
				// combine
				ArcheTextObject object = new ArcheTextObject();
				object.combine((ArcheTextObject)this.value);
				return new ArcheTextValue(Type.OBJECT, this.combinator, object);
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
	 * Gets the value combiner type.
	 */
	public Combinator getCombineType()
	{
		return combinator;
	}

	/**
	 * Gets the value itself.
	 */
	public Object getValue()
	{
		return value;
	}
	
	private boolean getBoolean()
	{
		return value != null ? ((Boolean)value) : false;  
	}
	
	private long getLong()
	{
		return value != null ? ((Long)value).longValue() : 0L;  
	}
	
	private double getDouble()
	{
		return value != null ? ((Double)value).doubleValue() : 0.0;  
	}
	
	private String getString()
	{
		return value != null ? String.valueOf(value) : null;  
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

	private ArcheTextValue promoteBooleanTo(Type promotionType)
	{
		switch (promotionType)
		{
			default:
				throw new IllegalArgumentException("YOU SHOULDN'T BE SEEING THIS.");
			case INTEGER:
				return new ArcheTextValue(Type.INTEGER, combinator, ((Boolean)value) ? 1L : 0L);
			case FLOAT:
				return new ArcheTextValue(Type.FLOAT, combinator, ((Boolean)value) ? 1.0 : 0.0);
			case STRING:
				return new ArcheTextValue(Type.STRING, combinator, String.valueOf(value));
			case SET:
				Hash<ArcheTextValue> set = new Hash<ArcheTextValue>(1);
				set.put(create(value));
				return new ArcheTextValue(Type.SET, combinator, set);
			case LIST:
				List<ArcheTextValue> list = new List<ArcheTextValue>(1);
				list.add(create(value));
				return new ArcheTextValue(Type.LIST, combinator, list);
		}
	}

	private ArcheTextValue promoteIntegerTo(Type promotionType)
	{
		switch (promotionType)
		{
			default:
				throw new IllegalArgumentException("YOU SHOULDN'T BE SEEING THIS.");
			case FLOAT:
				return new ArcheTextValue(Type.FLOAT, combinator, ((Number)value).doubleValue());
			case STRING:
				return new ArcheTextValue(Type.STRING, combinator, String.valueOf(value));
			case SET:
				Hash<ArcheTextValue> set = new Hash<ArcheTextValue>(1);
				set.put(create(value));
				return new ArcheTextValue(Type.SET, combinator, set);
			case LIST:
				List<ArcheTextValue> list = new List<ArcheTextValue>(1);
				list.add(create(value));
				return new ArcheTextValue(Type.LIST, combinator, list);
		}
	}

	private ArcheTextValue promoteFloatTo(Type promotionType)
	{
		switch (promotionType)
		{
			default:
				throw new IllegalArgumentException("YOU SHOULDN'T BE SEEING THIS.");
			case STRING:
				return new ArcheTextValue(Type.STRING, combinator, String.valueOf(value));
			case SET:
				Hash<ArcheTextValue> set = new Hash<ArcheTextValue>(1);
				set.put(create(value));
				return new ArcheTextValue(Type.SET, combinator, set);
			case LIST:
				List<ArcheTextValue> list = new List<ArcheTextValue>(1);
				list.add(create(value));
				return new ArcheTextValue(Type.LIST, combinator, list);
		}
	}

	private ArcheTextValue promoteStringTo(Type promotionType)
	{
		switch (promotionType)
		{
			default:
				throw new IllegalArgumentException("YOU SHOULDN'T BE SEEING THIS.");
			case SET:
				Hash<ArcheTextValue> set = new Hash<ArcheTextValue>(1);
				set.put(create(value));
				return new ArcheTextValue(Type.SET, combinator, set);
			case LIST:
				List<ArcheTextValue> list = new List<ArcheTextValue>(1);
				list.add(create(value));
				return new ArcheTextValue(Type.LIST, combinator, list);
		}
	}

	private ArcheTextValue promoteSetTo(Type promotionType)
	{
		switch (promotionType)
		{
			default:
				throw new IllegalArgumentException("YOU SHOULDN'T BE SEEING THIS.");
			case LIST:
				@SuppressWarnings("unchecked")
				Hash<ArcheTextValue> set = (Hash<ArcheTextValue>)value;
				List<ArcheTextValue> list = new List<ArcheTextValue>(set.size());
				for (ArcheTextValue v : set)
					list.add(create(v));
				return new ArcheTextValue(Type.LIST, combinator, list);
		}
	}

	private ArcheTextValue promoteArrayTo(Type promotionType)
	{
		throw new IllegalArgumentException("YOU SHOULDN'T BE SEEING THIS.");
	}
	
}
