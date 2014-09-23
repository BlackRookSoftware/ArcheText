package com.blackrook.archetext.exception;

/**
 * Exception thrown on bad ArcheText parse.
 * @author Matthew Tropiano
 */
public class ArcheTextParseException extends RuntimeException
{
	private static final long serialVersionUID = -3172139012517260171L;

	public ArcheTextParseException()
	{
		super("An exception happened on ArcheText export.");
	}
	
	public ArcheTextParseException(String message)
	{
		super(message);
	}
	
	public ArcheTextParseException(Throwable t)
	{
		super(t);
	}

	public ArcheTextParseException(String message, Throwable t)
	{
		super(message, t);
	}
	
}
