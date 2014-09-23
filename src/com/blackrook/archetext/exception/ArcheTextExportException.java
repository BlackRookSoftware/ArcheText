package com.blackrook.archetext.exception;

/**
 * Exception thrown on bad ArcheText conversion.
 * @author Matthew Tropiano
 */
public class ArcheTextExportException extends RuntimeException
{
	private static final long serialVersionUID = -3172139012517260171L;

	public ArcheTextExportException()
	{
		super("An exception happened on ArcheText export.");
	}
	
	public ArcheTextExportException(String message)
	{
		super(message);
	}
	
	public ArcheTextExportException(Throwable t)
	{
		super(t);
	}

	public ArcheTextExportException(String message, Throwable t)
	{
		super(message, t);
	}
	
}
