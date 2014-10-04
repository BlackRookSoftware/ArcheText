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
			
			switch (target.type)
			{
				default:
				{
					if (target.type.compareTo(operand.type) < 0)
						target = target.promoteTo(operand.type);
					else if (target.type.compareTo(operand.type) > 0)
						operand = operand.promoteTo(target.type);
					else
						target = target.copy();
				}
				break;
			}
			
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
					return new ArcheTextValue(Type.SET, Hash.union((Hash<ArcheTextValue>)target.value, (Hash<ArcheTextValue>)operand.value));
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
			
			switch (target.type)
			{
				default:
				{
					if (target.type.compareTo(operand.type) < 0)
						target = target.promoteTo(operand.type);
					else if (target.type.compareTo(operand.type) > 0)
						operand = operand.promoteTo(target.type);
					else
						target = target.copy();
				}
				break;
			}
			
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
					return new ArcheTextValue(Type.SET, Hash.difference((Hash<ArcheTextValue>)target.value, (Hash<ArcheTextValue>)operand.value));
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
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue target)
		{
			if (target == null)
				return operand.copy();

			operatorObjectCheck("multiplication", operand, target);

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
			}
			
			return null;
		}
	},

	DIVISION("/=")
	{
		@Override
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue target)
		{
			if (target == null)
				return operand.copy();

			operatorObjectCheck("division", operand, target);
			
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
					if (operand.getLong() == 0L)
						throw new ArcheTextConversionException("Divide by zero.");
					else
						return new ArcheTextValue(Type.INTEGER, target.getLong() / operand.getLong());
				case FLOAT:
					if (operand.getDouble() == 0.0)
						throw new ArcheTextConversionException("Divide by zero.");
					else
						return new ArcheTextValue(Type.FLOAT, target.getDouble() / operand.getDouble());
			}
			
			return null;
		}
	},
	
	MODULO("%=")
	{
		@Override
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue target)
		{
			if (target == null)
				return operand.copy();
			
			operatorObjectCheck("modulo", operand, target);

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
					if (operand.getLong() == 0L)
						throw new ArcheTextConversionException("Divide by zero.");
					else
						return new ArcheTextValue(Type.INTEGER, target.getLong() % operand.getLong());
				case FLOAT:
					if (operand.getDouble() == 0.0)
						throw new ArcheTextConversionException("Divide by zero.");
					else
						return new ArcheTextValue(Type.FLOAT, target.getDouble() % operand.getDouble());
			}
			
			return null;
		}
	},
	
	POWER("^=")
	{
		@Override
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue target)
		{
			if (target == null)
				return operand.copy();
			
			operatorObjectCheck("power", operand, target);

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
					return new ArcheTextValue(Type.INTEGER, (long)Math.pow(target.getLong(), operand.getLong()));
				case FLOAT:
					return new ArcheTextValue(Type.FLOAT, Math.pow(target.getDouble(), operand.getDouble()));
			}
			
			return null;
		}
	},
	
	BITWISEAND("&=")
	{
		@Override
		@SuppressWarnings("unchecked")
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue target)
		{
			if (target == null)
				return operand.copy();
			
			// set and set
			if (target.type == Type.SET && operand.type == Type.SET)
			{
				return new ArcheTextValue(Type.SET, Hash.intersection((Hash<ArcheTextValue>)target.value, (Hash<ArcheTextValue>)operand.value));
			}
			
			operatorObjectCheck("bitwise-and", operand, target);

			long targbits = 0L;
			long operandbits = 0L;
			
			switch (target.type)
			{
				case BOOLEAN:
					targbits = target.getBoolean() ? -1L : 0L;
					break;
				case INTEGER:
					targbits = target.getLong();
					break;
				case FLOAT:
					targbits = Double.doubleToRawLongBits(target.getDouble());
					break;
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
			}
			
			long result = targbits & operandbits;
			Type maxtype = target.type.ordinal() > operand.type.ordinal() ? target.type : operand.type;
			
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
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue target)
		{
			if (target == null)
				return operand.copy();
			
			// set and set
			if (target.type == Type.SET && operand.type == Type.SET)
			{
				return new ArcheTextValue(Type.SET, Hash.union((Hash<ArcheTextValue>)target.value, (Hash<ArcheTextValue>)operand.value));
			}
			
			operatorObjectCheck("bitwise-or", operand, target);

			long targbits = 0L;
			long operandbits = 0L;
			
			switch (target.type)
			{
				case BOOLEAN:
					targbits = target.getBoolean() ? -1L : 0L;
					break;
				case INTEGER:
					targbits = target.getLong();
					break;
				case FLOAT:
					targbits = Double.doubleToRawLongBits(target.getDouble());
					break;
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
			}
			
			long result = targbits | operandbits;
			Type maxtype = target.type.ordinal() > operand.type.ordinal() ? target.type : operand.type;
			
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
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue target)
		{
			if (target == null)
				return operand.copy();
			
			List<ArcheTextValue> list = null;
			long targbits = 0L;
			int operandValue = 0;
			
			switch (target.type)
			{
				case LIST:
				{
					list = (List<ArcheTextValue>)target.value;
					break;
				}
				
				case BOOLEAN:
					targbits = target.getBoolean() ? -1L : 0L;
					break;
				case INTEGER:
					targbits = target.getLong();
					break;
				case FLOAT:
					targbits = Double.doubleToRawLongBits(target.getDouble());
					break;
				case STRING:
					throw new ArcheTextConversionException("Can't use a left shift operator with strings.");
				case SET:
					throw new ArcheTextConversionException("Can't use a left shift operator with sets.");
				case OBJECT:
					throw new ArcheTextConversionException("Can't use a left shift operator with objects.");
			}
			
			switch (operand.type)
			{
				case INTEGER:
					operandValue = (int)operand.getLong();
					break;
				case BOOLEAN:
					throw new ArcheTextConversionException("Can't use a left shift operator with a boolean as an operand.");
				case FLOAT:
					throw new ArcheTextConversionException("Can't use a left shift operator with a float as an operand.");
				case STRING:
					throw new ArcheTextConversionException("Can't use a left shift operator with a string as an operand.");
				case SET:
					throw new ArcheTextConversionException("Can't use a left shift operator with a set as an operand.");
				case LIST:
					throw new ArcheTextConversionException("Can't use a left shift operator with a list as an operand.");
				case OBJECT:
					throw new ArcheTextConversionException("Can't use a left shift operator with a object as an operand.");
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
				Type maxtype = target.type.ordinal() > operand.type.ordinal() ? target.type : operand.type;
				
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
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue target)
		{
			if (target == null)
				return operand.copy();
			
			List<ArcheTextValue> list = null;
			long targbits = 0L;
			int operandValue = 0;
			
			switch (target.type)
			{
				case LIST:
				{
					list = (List<ArcheTextValue>)target.value;
					break;
				}
				
				case BOOLEAN:
					targbits = target.getBoolean() ? -1L : 0L;
					break;
				case INTEGER:
					targbits = target.getLong();
					break;
				case FLOAT:
					targbits = Double.doubleToRawLongBits(target.getDouble());
					break;
				case STRING:
					throw new ArcheTextConversionException("Can't use a left shift operator with strings.");
				case SET:
					throw new ArcheTextConversionException("Can't use a left shift operator with sets.");
				case OBJECT:
					throw new ArcheTextConversionException("Can't use a left shift operator with objects.");
			}
			
			switch (operand.type)
			{
				case INTEGER:
					operandValue = (int)operand.getLong();
					break;
				case BOOLEAN:
					throw new ArcheTextConversionException("Can't use a left shift operator with a boolean as an operand.");
				case FLOAT:
					throw new ArcheTextConversionException("Can't use a left shift operator with a float as an operand.");
				case STRING:
					throw new ArcheTextConversionException("Can't use a left shift operator with a string as an operand.");
				case SET:
					throw new ArcheTextConversionException("Can't use a left shift operator with a set as an operand.");
				case LIST:
					throw new ArcheTextConversionException("Can't use a left shift operator with a list as an operand.");
				case OBJECT:
					throw new ArcheTextConversionException("Can't use a left shift operator with a object as an operand.");
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
				Type maxtype = target.type.ordinal() > operand.type.ordinal() ? target.type : operand.type;
				
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
		public ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue target)
		{
			if (target == null)
				return operand.copy();
			
			List<ArcheTextValue> list = null;
			long targbits = 0L;
			int operandValue = 0;
			
			switch (target.type)
			{
				case LIST:
				{
					list = (List<ArcheTextValue>)target.value;
					break;
				}
				
				case BOOLEAN:
					targbits = target.getBoolean() ? -1L : 0L;
					break;
				case INTEGER:
					targbits = target.getLong();
					break;
				case FLOAT:
					targbits = Double.doubleToRawLongBits(target.getDouble());
					break;
				case STRING:
					throw new ArcheTextConversionException("Can't use a left shift operator with strings.");
				case SET:
					throw new ArcheTextConversionException("Can't use a left shift operator with sets.");
				case OBJECT:
					throw new ArcheTextConversionException("Can't use a left shift operator with objects.");
			}
			
			switch (operand.type)
			{
				case INTEGER:
					operandValue = (int)operand.getLong();
					break;
				case BOOLEAN:
					throw new ArcheTextConversionException("Can't use a left shift operator with a boolean as an operand.");
				case FLOAT:
					throw new ArcheTextConversionException("Can't use a left shift operator with a float as an operand.");
				case STRING:
					throw new ArcheTextConversionException("Can't use a left shift operator with a string as an operand.");
				case SET:
					throw new ArcheTextConversionException("Can't use a left shift operator with a set as an operand.");
				case LIST:
					throw new ArcheTextConversionException("Can't use a left shift operator with a list as an operand.");
				case OBJECT:
					throw new ArcheTextConversionException("Can't use a left shift operator with a object as an operand.");
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
				Type maxtype = target.type.ordinal() > operand.type.ordinal() ? target.type : operand.type;
				
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

	/** Combines two values. */
	public abstract ArcheTextValue combine(ArcheTextValue operand, ArcheTextValue target);

	private static void operatorObjectCheck(String operatorName, ArcheTextValue operand, ArcheTextValue target)
	{
		switch (target.type)
		{
			case STRING:
				throw new ArcheTextConversionException("Can't use a " + operatorName + " operator with strings.");
			case SET:
				throw new ArcheTextConversionException("Can't use a " + operatorName + " operator with sets.");
			case LIST:
				throw new ArcheTextConversionException("Can't use a " + operatorName + " operator with lists.");
			case OBJECT:
				throw new ArcheTextConversionException("Can't use a " + operatorName + " operator with objects.");
		}
		switch (operand.type)
		{
			case STRING:
				throw new ArcheTextConversionException("Can't use a " + operatorName + " operator with strings.");
			case SET:
				throw new ArcheTextConversionException("Can't use a " + operatorName + " operator with sets.");
			case LIST:
				throw new ArcheTextConversionException("Can't use a " + operatorName + " operator with lists.");
			case OBJECT:
				throw new ArcheTextConversionException("Can't use a " + operatorName + " operator with objects.");
		}
	}
	
}

