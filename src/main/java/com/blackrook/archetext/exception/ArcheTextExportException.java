/*******************************************************************************
 * Copyright (c) 2016-2020 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
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
