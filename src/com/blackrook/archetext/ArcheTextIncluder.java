package com.blackrook.archetext;

import java.io.IOException;
import java.io.InputStream;

/**
 * An interface that allows the user to resolve a resource by path when the
 * ArcheTextReader parses it.
 * @author Matthew Tropiano
 */
public interface ArcheTextIncluder
{

	/**
	 * Returns an open {@link InputStream} for a path when the parser needs a resource.
	 * By default, this attempts to open a file at the provided path.
	 * @param streamName the name of the stream.
	 * @param path the stream path.
	 * @return an open {@link InputStream} for the requested resource, or null if not found.
	 */
	public InputStream getIncludeResource(String streamName, String path) throws IOException;
	
}
