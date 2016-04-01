/*******************************************************************************
 * Copyright (c) 2016 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.archetext;

import com.blackrook.archetext.ArcheTextValue.Type;
import com.blackrook.archetext.exception.ArcheTextOperationException;
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
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue source)
		{
			return operand != null ? operand.copy() : null;
		}

	},
	
	ADD("+=")
	{
		@Override
		@SuppressWarnings("unchecked")
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue source)
		{
			if (source.isNull())
				return new ArcheTextValue(Type.NULL, null);

			switch (source.type)
			{
				default:
				{
					if (source.type.compareTo(operand.type) < 0)
						source = source.promoteTo(operand.type);
					else if (source.type.compareTo(operand.type) > 0)
						operand = operand.promoteTo(source.type);
					else
						source = source.copy();
				}
				break;
			}
			
			switch (operand.type)
			{
				case NULL:
					return new ArcheTextValue(Type.NULL, null);
				case BOOLEAN:
					return new ArcheTextValue(Type.BOOLEAN, source.getBoolean() || operand.getBoolean());
				case INTEGER:
					return new ArcheTextValue(Type.INTEGER, source.getLong() + operand.getLong());
				case FLOAT:
					return new ArcheTextValue(Type.FLOAT, source.getDouble() + operand.getDouble());
				case STRING:
					return new ArcheTextValue(Type.STRING, source.getString() + operand.getString());
				case SET:
					return new ArcheTextValue(Type.SET, Hash.union((Hash<ArcheTextValue>)source.value, (Hash<ArcheTextValue>)operand.value));
				case LIST:
				{
					// append
					List<ArcheTextValue> list = new List<ArcheTextValue>();
					for (ArcheTextValue val : (List<ArcheTextValue>)source.value)
						list.add(val);
					for (ArcheTextValue val : (List<ArcheTextValue>)operand.value)
						list.add(val);
					return new ArcheTextValue(Type.LIST, list);
				}
				case OBJECT:
				{
					// combine
					ArcheTextObject object = new ArcheTextObject();
					object.cascade((ArcheTextObject)source.value);
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
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue source)
		{
			if (source.isNull())
				return new ArcheTextValue(Type.NULL, null);

			switch (source.type)
			{
				default:
				{
					if (source.type.compareTo(operand.type) < 0)
						source = source.promoteTo(operand.type);
					else if (source.type.compareTo(operand.type) > 0)
						operand = operand.promoteTo(source.type);
					else
						source = source.copy();
				}
				break;
			}
			
			switch (operand.type)
			{
				case NULL:
					return new ArcheTextValue(Type.NULL, null);
				case BOOLEAN:
					return new ArcheTextValue(Type.BOOLEAN, source.getBoolean() || operand.getBoolean());
				case INTEGER:
					return new ArcheTextValue(Type.INTEGER, source.getLong() - operand.getLong());
				case FLOAT:
					return new ArcheTextValue(Type.FLOAT, source.getDouble() - operand.getDouble());
				case STRING:
					return new ArcheTextValue(Type.STRING, source.getString().replace(operand.getString(), ""));
				case SET:
					return new ArcheTextValue(Type.SET, Hash.difference((Hash<ArcheTextValue>)source.value, (Hash<ArcheTextValue>)operand.value));
				case LIST:
				{
					// remove
					List<ArcheTextValue> list = new List<ArcheTextValue>();
					for (ArcheTextValue val : (List<ArcheTextValue>)source.value)
						list.add(val);
					for (ArcheTextValue val : (List<ArcheTextValue>)operand.value)
						list.remove(val);
					return new ArcheTextValue(Type.LIST, list);
				}
				case OBJECT:
				{
					throw new ArcheTextOperationException("Can't use a subtraction operator with objects.");
				}
			}
			
			return null;
		}
	},

	MULTIPLY("*=")
	{
		@Override
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue source)
		{
			if (source.isNull())
				return new ArcheTextValue(Type.NULL, null);

			operatorObjectCheck("multiplication", operand, source);

			if (source.type.compareTo(operand.type) < 0)
				source = source.promoteTo(operand.type);
			else if (source.type.compareTo(operand.type) > 0)
				operand = operand.promoteTo(source.type);
			else
				source = source.copy();
			
			switch (operand.type)
			{
				case NULL:
					return new ArcheTextValue(Type.NULL, null);
				case BOOLEAN:
					return new ArcheTextValue(Type.BOOLEAN, source.getBoolean() && operand.getBoolean());
				case INTEGER:
					return new ArcheTextValue(Type.INTEGER, source.getLong() * operand.getLong());
				case FLOAT:
					return new ArcheTextValue(Type.FLOAT, source.getDouble() * operand.getDouble());
				default:
					// fall out.
			}
			
			return null;
		}
	},

	DIVISION("/=")
	{
		@Override
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue source)
		{
			if (source.isNull())
				return new ArcheTextValue(Type.NULL, null);

			operatorObjectCheck("division", operand, source);
			
			if (source.type.compareTo(operand.type) < 0)
				source = source.promoteTo(operand.type);
			else if (source.type.compareTo(operand.type) > 0)
				operand = operand.promoteTo(source.type);
			else
				source = source.copy();
			
			switch (operand.type)
			{
				case BOOLEAN:
					return new ArcheTextValue(Type.BOOLEAN, source.getBoolean() && operand.getBoolean());
				case INTEGER:
					if (operand.getLong() == 0L)
						throw new ArcheTextOperationException("Divide by zero.");
					else
						return new ArcheTextValue(Type.INTEGER, source.getLong() / operand.getLong());
				case FLOAT:
					if (operand.getDouble() == 0.0)
						throw new ArcheTextOperationException("Divide by zero.");
					else
						return new ArcheTextValue(Type.FLOAT, source.getDouble() / operand.getDouble());
				case NULL:
					return new ArcheTextValue(Type.NULL, null);
				default:
					// fall out.
			}
			
			return null;
		}
	},
	
	MODULO("%=")
	{
		@Override
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue source)
		{
			if (source.isNull())
				return new ArcheTextValue(Type.NULL, null);

			operatorObjectCheck("modulo", operand, source);

			if (source.type.compareTo(operand.type) < 0)
				source = source.promoteTo(operand.type);
			else if (source.type.compareTo(operand.type) > 0)
				operand = operand.promoteTo(source.type);
			else
				source = source.copy();
			
			switch (operand.type)
			{
				case BOOLEAN:
					return new ArcheTextValue(Type.BOOLEAN, source.getBoolean() || operand.getBoolean());
				case INTEGER:
					if (operand.getLong() == 0L)
						throw new ArcheTextOperationException("Divide by zero.");
					else
						return new ArcheTextValue(Type.INTEGER, source.getLong() % operand.getLong());
				case FLOAT:
					if (operand.getDouble() == 0.0)
						throw new ArcheTextOperationException("Divide by zero.");
					else
						return new ArcheTextValue(Type.FLOAT, source.getDouble() % operand.getDouble());
				case NULL:
					return new ArcheTextValue(Type.NULL, null);
				default:
					// fall out.
			}
			
			return null;
		}
	},
	
	POWER("'=")
	{
		@Override
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue source)
		{
			if (source.isNull())
				return new ArcheTextValue(Type.NULL, null);

			operatorObjectCheck("power", operand, source);

			if (source.type.compareTo(operand.type) < 0)
				source = source.promoteTo(operand.type);
			else if (source.type.compareTo(operand.type) > 0)
				operand = operand.promoteTo(source.type);
			else
				source = source.copy();
			
			switch (operand.type)
			{
				case BOOLEAN:
					return new ArcheTextValue(Type.BOOLEAN, source.getBoolean() && operand.getBoolean());
				case INTEGER:
					return new ArcheTextValue(Type.INTEGER, (long)Math.pow(source.getLong(), operand.getLong()));
				case FLOAT:
					return new ArcheTextValue(Type.FLOAT, Math.pow(source.getDouble(), operand.getDouble()));
				case NULL:
					return new ArcheTextValue(Type.NULL, null);
				default:
					// fall out.
			}
			
			return null;
		}
	},
	
	BITWISEAND("&=")
	{
		@Override
		@SuppressWarnings("unchecked")
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue source)
		{
			if (source.isNull())
				return new ArcheTextValue(Type.NULL, null);

			// set and set
			if (source.type == Type.SET && operand.type == Type.SET)
			{
				return new ArcheTextValue(Type.SET, Hash.intersection((Hash<ArcheTextValue>)source.value, (Hash<ArcheTextValue>)operand.value));
			}
			
			operatorObjectCheck("bitwise-and", operand, source);

			long targbits = 0L;
			long operandbits = 0L;
			
			switch (source.type)
			{
				case BOOLEAN:
					targbits = source.getBoolean() ? -1L : 0L;
					break;
				case INTEGER:
					targbits = source.getLong();
					break;
				case FLOAT:
					targbits = Double.doubleToRawLongBits(source.getDouble());
					break;
				case NULL:
					return new ArcheTextValue(Type.NULL, null);
				default:
					// fall out.
			}
			
			switch (operand.type)
			{
				case BOOLEAN:
					operandbits = operand.getBoolean() ? -1L : 0L;
					break;
				case INTEGER:
					operandbits = operand.getLong();
					break;
				case FLOAT:
					operandbits = Double.doubleToRawLongBits(operand.getDouble());
					break;
				case NULL:
					return new ArcheTextValue(Type.NULL, null);
				default:
					// fall out.
			}
			
			long result = targbits & operandbits;
			Type maxtype = source.type.ordinal() > operand.type.ordinal() ? source.type : operand.type;
			
			switch (maxtype)
			{
				case BOOLEAN:
					return new ArcheTextValue(Type.BOOLEAN, result != 0L);
				default:
				case INTEGER:
					return new ArcheTextValue(Type.INTEGER, result);
				case FLOAT:
					return new ArcheTextValue(Type.FLOAT, Double.longBitsToDouble(result));
			}
		}
	},
	
	BITWISEOR("|=")
	{
		@Override
		@SuppressWarnings("unchecked")
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue source)
		{
			if (source.isNull())
				return new ArcheTextValue(Type.NULL, null);

			// set and set
			if (source.type == Type.SET && operand.type == Type.SET)
			{
				return new ArcheTextValue(Type.SET, Hash.union((Hash<ArcheTextValue>)source.value, (Hash<ArcheTextValue>)operand.value));
			}
			
			operatorObjectCheck("bitwise-or", operand, source);

			long targbits = 0L;
			long operandbits = 0L;
			
			switch (source.type)
			{
				case BOOLEAN:
					targbits = source.getBoolean() ? -1L : 0L;
					break;
				case INTEGER:
					targbits = source.getLong();
					break;
				case FLOAT:
					targbits = Double.doubleToRawLongBits(source.getDouble());
					break;
				case NULL:
					return new ArcheTextValue(Type.NULL, null);
				default:
					// fall out.
			}
			
			switch (operand.type)
			{
				case BOOLEAN:
					operandbits = operand.getBoolean() ? -1L : 0L;
					break;
				case INTEGER:
					operandbits = operand.getLong();
					break;
				case FLOAT:
					operandbits = Double.doubleToRawLongBits(operand.getDouble());
					break;
				case NULL:
					return new ArcheTextValue(Type.NULL, null);
				default:
					// fall out.
			}
			
			long result = targbits | operandbits;
			Type maxtype = source.type.ordinal() > operand.type.ordinal() ? source.type : operand.type;
			
			switch (maxtype)
			{
				case BOOLEAN:
					return new ArcheTextValue(Type.BOOLEAN, result != 0L);
				default:
				case INTEGER:
					return new ArcheTextValue(Type.INTEGER, result);
				case FLOAT:
					return new ArcheTextValue(Type.FLOAT, Double.longBitsToDouble(result));
			}
		}
	},
	
	BITWISEXOR("^=")
	{
		@Override
		@SuppressWarnings("unchecked")
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue source)
		{
			if (source.isNull())
				return new ArcheTextValue(Type.NULL, null);

			// set and set
			if (source.type == Type.SET && operand.type == Type.SET)
			{
				return new ArcheTextValue(Type.SET, Hash.xor((Hash<ArcheTextValue>)source.value, (Hash<ArcheTextValue>)operand.value));
			}
			
			operatorObjectCheck("bitwise-xor", operand, source);

			long targbits = 0L;
			long operandbits = 0L;
			
			switch (source.type)
			{
				case BOOLEAN:
					targbits = source.getBoolean() ? -1L : 0L;
					break;
				case INTEGER:
					targbits = source.getLong();
					break;
				case FLOAT:
					targbits = Double.doubleToRawLongBits(source.getDouble());
					break;
				case NULL:
					return new ArcheTextValue(Type.NULL, null);
				default:
					// fall out.
			}
			
			switch (operand.type)
			{
				case BOOLEAN:
					operandbits = operand.getBoolean() ? -1L : 0L;
					break;
				case INTEGER:
					operandbits = operand.getLong();
					break;
				case FLOAT:
					operandbits = Double.doubleToRawLongBits(operand.getDouble());
					break;
				case NULL:
					return new ArcheTextValue(Type.NULL, null);
				default:
					// fall out.
			}
			
			long result = targbits ^ operandbits;
			Type maxtype = source.type.ordinal() > operand.type.ordinal() ? source.type : operand.type;
			
			switch (maxtype)
			{
				case BOOLEAN:
					return new ArcheTextValue(Type.BOOLEAN, result != 0L);
				default:
				case INTEGER:
					return new ArcheTextValue(Type.INTEGER, result);
				case FLOAT:
					return new ArcheTextValue(Type.FLOAT, Double.longBitsToDouble(result));
			}
		}
	},
	
	LEFTSHIFT("<<=")
	{
		@Override
		@SuppressWarnings("unchecked")
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue source)
		{
			if (source.isNull())
				return new ArcheTextValue(Type.NULL, null);

			List<ArcheTextValue> list = null;
			long targbits = 0L;
			int operandValue = 0;
			
			switch (source.type)
			{
				case LIST:
				{
					list = (List<ArcheTextValue>)source.value;
					break;
				}
				
				case BOOLEAN:
					targbits = source.getBoolean() ? -1L : 0L;
					break;
				case INTEGER:
					targbits = source.getLong();
					break;
				case FLOAT:
					targbits = Double.doubleToRawLongBits(source.getDouble());
					break;
				case STRING:
					throw new ArcheTextOperationException("Can't use a left shift operator with strings.");
				case SET:
					throw new ArcheTextOperationException("Can't use a left shift operator with sets.");
				case OBJECT:
					throw new ArcheTextOperationException("Can't use a left shift operator with objects.");
				case NULL:
					return new ArcheTextValue(Type.NULL, null);
			}
			
			switch (operand.type)
			{
				case INTEGER:
					operandValue = (int)operand.getLong();
					break;
				case BOOLEAN:
					throw new ArcheTextOperationException("Can't use a left shift operator with a boolean as an operand.");
				case FLOAT:
					throw new ArcheTextOperationException("Can't use a left shift operator with a float as an operand.");
				case STRING:
					throw new ArcheTextOperationException("Can't use a left shift operator with a string as an operand.");
				case SET:
					throw new ArcheTextOperationException("Can't use a left shift operator with a set as an operand.");
				case LIST:
					throw new ArcheTextOperationException("Can't use a left shift operator with a list as an operand.");
				case OBJECT:
					throw new ArcheTextOperationException("Can't use a left shift operator with a object as an operand.");
				case NULL:
					throw new ArcheTextOperationException("Can't use a left shift operator with a null object as an operand.");
			}
			
			if (list != null)
			{
				int amount = Math.min(Math.abs(operandValue), list.size());
				int total = list.size() - amount;
				List<ArcheTextValue> out = new List<ArcheTextValue>(total); 

				if (amount < 0)
				{
					for (int i = 0; i < total; i++)
						out.add(list.getByIndex(i));
				}
				else
				{
					for (int i = amount; i < total + amount; i++)
						out.add(list.getByIndex(i));
				}
					
				return new ArcheTextValue(Type.LIST, out);
			}
			else
			{
				long result = targbits << operandValue;
				Type maxtype = source.type.ordinal() > operand.type.ordinal() ? source.type : operand.type;
				
				switch (maxtype)
				{
					case BOOLEAN:
						return new ArcheTextValue(Type.BOOLEAN, result != 0L);
					default:
					case INTEGER:
						return new ArcheTextValue(Type.INTEGER, result);
					case FLOAT:
						return new ArcheTextValue(Type.FLOAT, Double.longBitsToDouble(result));
				}
				
			}
			
		}
	},
	
	RIGHTSHIFT(">>=")
	{
		@Override
		@SuppressWarnings("unchecked")
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue source)
		{
			if (source == null)
				return operand.copy();
			if (source.isNull())
				return new ArcheTextValue(Type.NULL, null);

			List<ArcheTextValue> list = null;
			long targbits = 0L;
			int operandValue = 0;
			
			switch (source.type)
			{
				case LIST:
				{
					list = (List<ArcheTextValue>)source.value;
					break;
				}
				
				case BOOLEAN:
					targbits = source.getBoolean() ? -1L : 0L;
					break;
				case INTEGER:
					targbits = source.getLong();
					break;
				case FLOAT:
					targbits = Double.doubleToRawLongBits(source.getDouble());
					break;
				case STRING:
					throw new ArcheTextOperationException("Can't use a right shift operator with strings.");
				case SET:
					throw new ArcheTextOperationException("Can't use a right shift operator with sets.");
				case OBJECT:
					throw new ArcheTextOperationException("Can't use a right shift operator with objects.");
				case NULL:
					return new ArcheTextValue(Type.NULL, null);
			}
			
			switch (operand.type)
			{
				case INTEGER:
					operandValue = (int)operand.getLong();
					break;
				case BOOLEAN:
					throw new ArcheTextOperationException("Can't use a right shift operator with a boolean as an operand.");
				case FLOAT:
					throw new ArcheTextOperationException("Can't use a right shift operator with a float as an operand.");
				case STRING:
					throw new ArcheTextOperationException("Can't use a right shift operator with a string as an operand.");
				case SET:
					throw new ArcheTextOperationException("Can't use a right shift operator with a set as an operand.");
				case LIST:
					throw new ArcheTextOperationException("Can't use a right shift operator with a list as an operand.");
				case OBJECT:
					throw new ArcheTextOperationException("Can't use a right shift operator with a object as an operand.");
				case NULL:
					throw new ArcheTextOperationException("Can't use a right shift operator with a null object as an operand.");
			}
			
			if (list != null)
			{
				int amount = Math.min(Math.abs(operandValue), list.size());
				int total = list.size() - amount;
				List<ArcheTextValue> out = new List<ArcheTextValue>(total); 

				if (amount < 0)
				{
					for (int i = amount; i < total + amount; i++)
						out.add(list.getByIndex(i));
				}
				else
				{
					for (int i = 0; i < total; i++)
						out.add(list.getByIndex(i));
				}
					
				return new ArcheTextValue(Type.LIST, out);
			}
			else
			{
				long result = targbits >> operandValue;
				Type maxtype = source.type.ordinal() > operand.type.ordinal() ? source.type : operand.type;
				
				switch (maxtype)
				{
					case BOOLEAN:
						return new ArcheTextValue(Type.BOOLEAN, result != 0L);
					default:
					case INTEGER:
						return new ArcheTextValue(Type.INTEGER, result);
					case FLOAT:
						return new ArcheTextValue(Type.FLOAT, Double.longBitsToDouble(result));
				}
				
			}
			
		}
	},
	
	RIGHTPADDINGSHIFT(">>>=")
	{
		@Override
		@SuppressWarnings("unchecked")
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue source)
		{
			if (source == null)
				return operand.copy();
			if (source.isNull())
				return new ArcheTextValue(Type.NULL, null);

			List<ArcheTextValue> list = null;
			long targbits = 0L;
			int operandValue = 0;
			
			switch (source.type)
			{
				case LIST:
				{
					list = (List<ArcheTextValue>)source.value;
					break;
				}
				
				case BOOLEAN:
					targbits = source.getBoolean() ? -1L : 0L;
					break;
				case INTEGER:
					targbits = source.getLong();
					break;
				case FLOAT:
					targbits = Double.doubleToRawLongBits(source.getDouble());
					break;
				case STRING:
					throw new ArcheTextOperationException("Can't use a right padded shift operator with strings.");
				case SET:
					throw new ArcheTextOperationException("Can't use a right padded shift operator with sets.");
				case OBJECT:
					throw new ArcheTextOperationException("Can't use a right padded shift operator with objects.");
				case NULL:
					return new ArcheTextValue(Type.NULL, null);
			}
			
			switch (operand.type)
			{
				case INTEGER:
					operandValue = (int)operand.getLong();
					break;
				case BOOLEAN:
					throw new ArcheTextOperationException("Can't use a right padded shift operator with a boolean as an operand.");
				case FLOAT:
					throw new ArcheTextOperationException("Can't use a right padded shift operator with a float as an operand.");
				case STRING:
					throw new ArcheTextOperationException("Can't use a right padded shift operator with a string as an operand.");
				case SET:
					throw new ArcheTextOperationException("Can't use a right padded shift operator with a set as an operand.");
				case LIST:
					throw new ArcheTextOperationException("Can't use a right padded shift operator with a list as an operand.");
				case OBJECT:
					throw new ArcheTextOperationException("Can't use a right padded shift operator with a object as an operand.");
				case NULL:
					throw new ArcheTextOperationException("Can't use a right padded shift operator with a null object as an operand.");
			}
			
			if (list != null)
			{
				int amount = Math.min(Math.abs(operandValue), list.size());
				int total = list.size() - amount;
				List<ArcheTextValue> out = new List<ArcheTextValue>(total); 

				if (amount < 0)
				{
					for (int i = amount; i < total + amount; i++)
						out.add(list.getByIndex(i));
				}
				else
				{
					for (int i = 0; i < total; i++)
						out.add(list.getByIndex(i));
				}
					
				return new ArcheTextValue(Type.LIST, out);
			}
			else
			{
				long result = targbits >>> operandValue;
				Type maxtype = source.type.ordinal() > operand.type.ordinal() ? source.type : operand.type;
				
				switch (maxtype)
				{
					case BOOLEAN:
						return new ArcheTextValue(Type.BOOLEAN, result != 0L);
					default:
					case INTEGER:
						return new ArcheTextValue(Type.INTEGER, result);
					case FLOAT:
						return new ArcheTextValue(Type.FLOAT, Double.longBitsToDouble(result));
				}
				
			}
			
		}
	},
	
	;
	
	/** Assignment operator itself. */
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

	/** 
	 * Combines two values.
	 * @param operand the incoming value.
	 * @param source the source value that this is combining with.
	 * @return the resultant value.
	 * @throws NullPointerException if source is null. If source is supposed to be null, use {@link ArcheTextValue#NULL}.
	 */
	public abstract ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue source);

	private static void operatorObjectCheck(String operatorName, ArcheTextValue operand, ArcheTextValue source)
	{
		switch (source.type)
		{
			case STRING:
				throw new ArcheTextOperationException("Can't use a " + operatorName + " operator with strings.");
			case SET:
				throw new ArcheTextOperationException("Can't use a " + operatorName + " operator with sets.");
			case LIST:
				throw new ArcheTextOperationException("Can't use a " + operatorName + " operator with lists.");
			case OBJECT:
				throw new ArcheTextOperationException("Can't use a " + operatorName + " operator with objects.");
			default:
				// fall out.
		}
		switch (operand.type)
		{
			case STRING:
				throw new ArcheTextOperationException("Can't use a " + operatorName + " operator with strings.");
			case SET:
				throw new ArcheTextOperationException("Can't use a " + operatorName + " operator with sets.");
			case LIST:
				throw new ArcheTextOperationException("Can't use a " + operatorName + " operator with lists.");
			case OBJECT:
				throw new ArcheTextOperationException("Can't use a " + operatorName + " operator with objects.");
			default:
				// fall out.
		}
	}
	
}

