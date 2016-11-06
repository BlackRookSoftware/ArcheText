/*******************************************************************************
 * Copyright (c) 2016 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.archetext.exception;

/**
 * Exception thrown on bad ArcheText object type conversion.
 * @author Matthew Tropiano
 */
public class ArcheTextConversionException extends RuntimeException
{
	private static final long serialVersionUID = 3318234627907563160L;

	public ArcheTextConversionException()
	{
		super("An exception happened on ArcheText export.");
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
