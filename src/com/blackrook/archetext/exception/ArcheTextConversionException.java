package com.blackrook.archetext.exception;

/**
 * Exception thrown on bad ArcheText parse.
 * @author Matthew Tropiano
 */
public class ArcheTextConversionException extends RuntimeException
{
	private static final long serialVersionUID = -3172139012517260171L;

	public ArcheTextConversionException()
	{
		super("An exception happened on ArcheText value conversion.");
	}
	
	public ArcheTextConversionException(String message)
	{
		super(message);
	}
	
	public ArcheTextConversionException(Throwable t)
	{
		super(t);
	}

	public ArcheTextConversionException(String message, Throwable t)
	{
		super(message, t);
	}
	
}
