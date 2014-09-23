package com.blackrook.archetext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import com.blackrook.lang.CommonLexer;
import com.blackrook.lang.CommonLexerKernel;
import com.blackrook.lang.Parser;

/**
 * 
 * @author Matthew Tropiano
 */
public final class ArcheTextReader
{
	public static final String STREAMNAME_TEXT = "[Text String]";
	
	/**
	 * Reads ArcheText objects into a new root from a starting text file.
	 * Note: Calls apply() with a new root.
	 * @param file	the file to read from.
	 * @return A new ArcheTextRoot that contains all the read object hierarchy.
	 * @throws IOException if the stream can't be read.
	 * @throws NullPointerException if f is null. 
	 */
	public ArcheTextRoot read(File file) throws IOException
	{
		return read(file.getPath(), new FileInputStream(file));
	}
	
	/**
	 * Reads ArcheText objects from a String of text into a new root.
	 * Note: Calls apply() with a new root.
	 * @param text the String to read from.
	 * @return A new ArcheTextRoot that contains all the read object hierarchy.
	 * @throws IOException if the stream can't be read.
	 * @throws NullPointerException if f is null. 
	 */
	public ArcheTextRoot read(String text) throws IOException
	{
		return read(STREAMNAME_TEXT, new StringReader(text));
	}
	
	/**
	 * Reads ArcheText objects into a new root.
	 * Note: Calls apply() with a new root.
	 * @param streamName the name of the stream.
	 * @param in the stream to read from.
	 * @return A new ArcheTextRoot that contains all the read object hierarchy.
	 * @throws IOException if the stream can't be read.
	 * @throws NullPointerException if in is null. 
	 */
	public ArcheTextRoot read(String streamName, InputStream in) throws IOException
	{
		return read(streamName, new InputStreamReader(in));
	}
	
	/**
	 * Reads ArcheText objects into a new root from a reader stream.
	 * Note: Calls apply() with a new root.
	 * @param streamName the name of the stream.
	 * @param reader the reader to read from.
	 * @return A new ArcheTextRoot that contains all the read object hierarchy.
	 * @throws IOException if the stream can't be read.
	 * @throws NullPointerException if f is null. 
	 */
	public ArcheTextRoot read(String streamName, Reader reader) throws IOException
	{
		ArcheTextRoot out = new ArcheTextRoot();
		apply(streamName, reader, out);
		return out;
	}

	/**
	 * Applies the ArcheText objects read to an already existing root.
	 * @param f	the file to read from.
	 * @param root the root to apply the objects to.
	 * @throws NullPointerException	if either object is null. 
	 */
	public void apply(File f, ArcheTextRoot root) throws IOException
	{
		apply(f.getPath(), new FileInputStream(f), root);
	}
	
	/**
	 * Applies the ArcheText objects read to an already existing root.
	 * @param text the String to read from.
	 * @param root the root to apply the objects to.
	 * @throws NullPointerException	if either object is null. 
	 */
	public void apply(String text, ArcheTextRoot root) throws IOException
	{
		apply(STREAMNAME_TEXT, new StringReader(text), root);
	}
	
	/**
	 * Applies the ArcheText objects read to an already existing root.
	 * @param streamName the name of the stream.
	 * @param in the stream to read from.
	 * @param root the root to apply the objects to.
	 * @throws NullPointerException	if either object is null. 
	 */
	public void apply(String streamName, InputStream in, ArcheTextRoot root)
	{
		apply(streamName, new InputStreamReader(in), root);
	}
	
	/**
	 * Applies the ArcheText objects read to an already existing root.
	 * @param streamName the name of the stream.
	 * @param reader the reader to read from.
	 * @param root the root to apply the objects to.
	 * @throws NullPointerException	if either object is null. 
	 */
	public void apply(String streamName, Reader reader, ArcheTextRoot root)
	{
		ATLexer lexer = new ATLexer(streamName, reader);
		ATParser parser = new ATParser(lexer);
		parser.readObjects(root);
	}
		
	/**
	 * Returns an open {@link InputStream} for a path when the parser needs a resource.
	 * By default, this attempts to open a file at the provided path.
	 * @param streamName the name of the stream.
	 * @param path the stream path.
	 * @return an open {@link InputStream} for the requested resource, or null if not found.
	 */
	public InputStream getIncludeResource(String streamName, String path) throws IOException
	{
		return new FileInputStream(new File(path));
	}
	
	/** The singular instance for the kernel. */
	private static final Kernel KERNEL_INSTANCE = new Kernel();

	/** The Lexer Kernel for the ArcheText Lexers. */
	private static class Kernel extends CommonLexerKernel
	{

		static final int TYPE_LBRACE = 0;
		static final int TYPE_RBRACE = 1;
		static final int TYPE_LPAREN = 2;
		static final int TYPE_RPAREN = 3;
		static final int TYPE_LBRACK = 4;
		static final int TYPE_RBRACK = 5;
		static final int TYPE_LANGLEBRACK = 6;
		static final int TYPE_RANGLEBRACK = 7;
		static final int TYPE_SEMICOLON = 8;
		static final int TYPE_COLON = 9;
		static final int TYPE_COMMA = 10;
		static final int TYPE_LEFTARROW = 11;

		static final int TYPE_AT = 12;
		static final int TYPE_PLUS = 13;
		static final int TYPE_MINUS = 14;
		static final int TYPE_TIMES = 15;
		static final int TYPE_DIV = 16;
		static final int TYPE_MODULO = 17;
		static final int TYPE_POWER = 18;
		static final int TYPE_LNOT = 19;
		static final int TYPE_NOT = 20;
		static final int TYPE_AND = 21;
		static final int TYPE_OR = 22;
		static final int TYPE_XOR = 23;
		static final int TYPE_LSHIFT = 24;
		static final int TYPE_RSHIFT = 25;
		static final int TYPE_RSHIFTPAD = 26;

		static final int TYPE_TRUE = 40;
		static final int TYPE_FALSE = 41;
		static final int TYPE_NULL = 42;

		static final int TYPE_COMMENT = 50;

		static final int TYPE_ASSIGNMENT_TYPE_START = 100;
		
		private Kernel()
		{
			addStringDelimiter('"', '"');
			
			addDelimiter("{", TYPE_LBRACE);
			addDelimiter("}", TYPE_RBRACE);
			addDelimiter("(", TYPE_LPAREN);
			addDelimiter(")", TYPE_RPAREN);
			addDelimiter("[", TYPE_LBRACK);
			addDelimiter("]", TYPE_RBRACK);
			addDelimiter("<", TYPE_LANGLEBRACK);
			addDelimiter(">", TYPE_RANGLEBRACK);
			addDelimiter(";", TYPE_SEMICOLON);
			addDelimiter(":", TYPE_COLON);
			addDelimiter(",", TYPE_COMMA);
			addDelimiter("<-", TYPE_LEFTARROW);

			addDelimiter("@", TYPE_AT);
			addDelimiter("+", TYPE_PLUS);
			addDelimiter("-", TYPE_MINUS);
			addDelimiter("*", TYPE_TIMES);
			addDelimiter("/", TYPE_DIV);
			addDelimiter("%", TYPE_MODULO);
			addDelimiter("^", TYPE_POWER);
			addDelimiter("~", TYPE_LNOT);
			addDelimiter("!", TYPE_NOT);
			addDelimiter("&", TYPE_AND);
			addDelimiter("|", TYPE_OR);
			addDelimiter(".", TYPE_XOR);
			addDelimiter("<<", TYPE_LSHIFT);
			addDelimiter(">>", TYPE_RSHIFT);
			addDelimiter(">>>", TYPE_RSHIFTPAD);
			
			addCaseInsensitiveKeyword("true", TYPE_TRUE);
			addCaseInsensitiveKeyword("false", TYPE_FALSE);
			addCaseInsensitiveKeyword("null", TYPE_NULL);
			
			addCommentStartDelimiter("/*", TYPE_COMMENT);
			addCommentLineDelimiter("//", TYPE_COMMENT);
			addCommentEndDelimiter("*/", TYPE_COMMENT);
			
			int i = 0;
			for (ArcheTextValue.Combinator combinator : ArcheTextValue.Combinator.values())
				addDelimiter(combinator.getAssignmentOperator(), TYPE_ASSIGNMENT_TYPE_START + (i++));
			
		}
		
	}
	
	/**
	 * The lexer for a reader context.
	 */
	private class ATLexer extends CommonLexer
	{
		private ATLexer(Reader in)
		{
			super(KERNEL_INSTANCE, in);
		}

		private ATLexer(String in)
		{
			super(KERNEL_INSTANCE, in);
		}
		
		private ATLexer(String name, Reader in)
		{
			super(KERNEL_INSTANCE, name, in);
		}

		private ATLexer(String name, String in)
		{
			super(KERNEL_INSTANCE, name, in);
		}
		
		@Override
		public InputStream getResource(String path) throws IOException
		{
			return getIncludeResource(getCurrentStreamName(), path);
		}
	}
	
	/**
	 * The parser that parses text for the ArcheText structures. 
	 */
	private class ATParser extends Parser
	{
		private ATParser(ATLexer lexer)
		{
			super(lexer);
		}
		
		/**
		 * Reads objects into the target root.
		 */
		public void readObjects(ArcheTextRoot targetRoot)
		{
			// TODO: Finish.
		}
		
		@Override
		protected String getTypeErrorText(int tokenType)
		{
			switch (tokenType)
			{
				case Kernel.TYPE_LPAREN:
					return "'('";
				case Kernel.TYPE_RPAREN:
					return "')'";
				case Kernel.TYPE_LBRACE:
					return "'{'";
				case Kernel.TYPE_RBRACE:
					return "'}'";
				case Kernel.TYPE_LBRACK:
					return "'['";
				case Kernel.TYPE_RBRACK:
					return "']'";
				case Kernel.TYPE_SEMICOLON:
					return "';'";
				case Kernel.TYPE_COLON:
					return "':'";
				case Kernel.TYPE_COMMA:
					return "','";
				default:
					return "";
			}
		}
	}
	
	
}
