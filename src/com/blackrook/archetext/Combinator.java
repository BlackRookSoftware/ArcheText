package com.blackrook.archetext;

import com.blackrook.archetext.ArcheTextValue.Type;
import com.blackrook.archetext.exception.ArcheTextConversionException;
import com.blackrook.commons.hash.Hash;
import com.blackrook.commons.list.List;

/**
 * ArcheText object internal accumulation types.
 */
public enum Combinator
{
	SET("=")
	{
		@Override
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue target)
		{
			return operand != null ? operand.copy() : null;
		}
	},
	
	ADD("+=")
	{
		@Override
		@SuppressWarnings("unchecked")
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue target)
		{
			if (target == null)
				return operand.copy();
			
			if (target.type.compareTo(operand.type) < 0)
				target = target.promoteTo(operand.type);
			else if (target.type.compareTo(operand.type) > 0)
				operand = operand.promoteTo(target.type);
			else
				target = target.copy();
			
			switch (operand.type)
			{
				case BOOLEAN:
					return new ArcheTextValue(Type.BOOLEAN, target.getBoolean() || operand.getBoolean());
				case INTEGER:
					return new ArcheTextValue(Type.INTEGER, target.getLong() + operand.getLong());
				case FLOAT:
					return new ArcheTextValue(Type.FLOAT, target.getDouble() + operand.getDouble());
				case STRING:
					return new ArcheTextValue(Type.STRING, target.getString() + operand.getString());
				case SET:
				{
					// union
					Hash<ArcheTextValue> set = new Hash<ArcheTextValue>();
					for (ArcheTextValue val : (Hash<ArcheTextValue>)target.value)
						set.put(val);
					for (ArcheTextValue val : (Hash<ArcheTextValue>)operand.value)
						set.put(val);
					return new ArcheTextValue(Type.SET, set);
				}
				case LIST:
				{
					// append
					List<ArcheTextValue> list = new List<ArcheTextValue>();
					for (ArcheTextValue val : (List<ArcheTextValue>)target.value)
						list.add(val);
					for (ArcheTextValue val : (List<ArcheTextValue>)operand.value)
						list.add(val);
					return new ArcheTextValue(Type.LIST, list);
				}
				case OBJECT:
				{
					// combine
					ArcheTextObject object = new ArcheTextObject();
					object.cascade((ArcheTextObject)target.value);
					object.cascade((ArcheTextObject)operand.value);
					return new ArcheTextValue(Type.OBJECT, object);
				}
			}
			
			return null;
		}
	},
	
	SUBTRACT("-=")
	{
		@Override
		@SuppressWarnings("unchecked")
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue target)
		{
			if (target == null)
				return operand.copy();
			
			if (target.type.compareTo(operand.type) < 0)
				target = target.promoteTo(operand.type);
			else if (target.type.compareTo(operand.type) > 0)
				operand = operand.promoteTo(target.type);
			else
				target = target.copy();
			
			switch (operand.type)
			{
				case BOOLEAN:
					return new ArcheTextValue(Type.BOOLEAN, target.getBoolean() || operand.getBoolean());
				case INTEGER:
					return new ArcheTextValue(Type.INTEGER, target.getLong() - operand.getLong());
				case FLOAT:
					return new ArcheTextValue(Type.FLOAT, target.getDouble() - operand.getDouble());
				case STRING:
					return new ArcheTextValue(Type.STRING, target.getString().replace(operand.getString(), ""));
				case SET:
				{
					// difference
					Hash<ArcheTextValue> set = new Hash<ArcheTextValue>();
					for (ArcheTextValue val : (Hash<ArcheTextValue>)target.value)
					{
						if (!((Hash<ArcheTextValue>)operand.value).contains(val))
							set.put(val);
					}
					return new ArcheTextValue(Type.SET, set);
				}
				case LIST:
				{
					// remove
					List<ArcheTextValue> list = new List<ArcheTextValue>();
					for (ArcheTextValue val : (List<ArcheTextValue>)target.value)
						list.add(val);
					for (ArcheTextValue val : (List<ArcheTextValue>)operand.value)
						list.remove(val);
					return new ArcheTextValue(Type.LIST, list);
				}
				case OBJECT:
				{
					throw new ArcheTextConversionException("Can't use a subtraction operator with objects.");
				}
			}
			
			return null;
		}
	},
	
	MULTIPLY("*=")
	{
		@Override
		@SuppressWarnings("unchecked")
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue target)
		{
			if (target == null)
				return operand.copy();
			
			if (target.type.compareTo(operand.type) < 0)
				target = target.promoteTo(operand.type);
			else if (target.type.compareTo(operand.type) > 0)
				operand = operand.promoteTo(target.type);
			else
				target = target.copy();
			
			switch (operand.type)
			{
				case BOOLEAN:
					return new ArcheTextValue(Type.BOOLEAN, target.getBoolean() && operand.getBoolean());
				case INTEGER:
					return new ArcheTextValue(Type.INTEGER, target.getLong() * operand.getLong());
				case FLOAT:
					return new ArcheTextValue(Type.FLOAT, target.getDouble() * operand.getDouble());
				case STRING:
					return new ArcheTextValue(Type.STRING, target.getString().replace(operand.getString(), ""));
				case SET:
				{
					// difference
					Hash<ArcheTextValue> set = new Hash<ArcheTextValue>();
					for (ArcheTextValue val : (Hash<ArcheTextValue>)target.value)
					{
						if (!((Hash<ArcheTextValue>)operand.value).contains(val))
							set.put(val);
					}
					return new ArcheTextValue(Type.SET, set);
				}
				case LIST:
				{
					// remove
					List<ArcheTextValue> list = new List<ArcheTextValue>();
					for (ArcheTextValue val : (List<ArcheTextValue>)target.value)
						list.add(val);
					for (ArcheTextValue val : (List<ArcheTextValue>)operand.value)
						list.remove(val);
					return new ArcheTextValue(Type.LIST, list);
				}
				case OBJECT:
				{
					throw new ArcheTextConversionException("Can't use a subtraction operator with objects.");
				}
			}
			
			return null;
		}
	},

	;
	
	/** Combines two values. */
	public abstract ArcheTextValue combine(ArcheTextValue me, ArcheTextValue them);
	
	private String assignmentOperator;
	private Combinator(String assignmentOperator)
	{
		this.assignmentOperator = assignmentOperator;
	}
	
	/** Returns the assignment operator for this combinator. */
	public String getAssignmentOperator()
	{
		return assignmentOperator;
	}
	
}

