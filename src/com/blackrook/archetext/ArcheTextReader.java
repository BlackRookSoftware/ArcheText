/*******************************************************************************
 * Copyright (c) 2016 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.archetext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import com.blackrook.archetext.ArcheTextValue.Type;
import com.blackrook.archetext.exception.ArcheTextOperationException;
import com.blackrook.archetext.exception.ArcheTextParseException;
import com.blackrook.commons.AbstractSet;
import com.blackrook.commons.AbstractVector;
import com.blackrook.commons.Common;
import com.blackrook.commons.hash.Hash;
import com.blackrook.commons.hash.HashMap;
import com.blackrook.commons.hash.HashedQueueMap;
import com.blackrook.commons.linkedlist.Queue;
import com.blackrook.commons.linkedlist.Stack;
import com.blackrook.commons.list.List;
import com.blackrook.lang.CommonLexer;
import com.blackrook.lang.CommonLexerKernel;
import com.blackrook.lang.Parser;

/**
 * A reader class for reading in ArcheText data and creating objects from it.
 * @author Matthew Tropiano
 */
public final class ArcheTextReader
{
	public static final String STREAMNAME_TEXT = "[Text String]";
	
	/** The singular instance for the kernel. */
	private static final Kernel KERNEL_INSTANCE = new Kernel();
	/** The singular instance for the default includer. */
	private static final DefaultIncluder DEFAULT_INCLUDER = new DefaultIncluder();
	
	/** 
	 * Default includer to use when none specified.
	 * This includer can either pull from the classpath, URIs, or files.
	 * <p>
	 * <ul>
	 * <li>Paths that start with {@code classpath:} are parsed as resource paths in the current classpath.</li>
	 * <li>
	 * 		Else, the path is interpreted as a file path, with the following search order:
	 * 		<ul>
	 * 			<li>Relative to parent of source stream.</li>
	 * 			<li>As is.</li>
	 * 		</ul>
	 * </li>
	 * </ul> 
	 */
	public static class DefaultIncluder implements ArcheTextIncluder
	{
		private static final String CLASSPATH_PREFIX = "classpath:";
		
		// cannot be instantiated outside of this class.
		private DefaultIncluder(){}
		
		@Override
		public InputStream getIncludeResource(String streamName, String path) throws IOException
		{
			if (Common.isWindows() && streamName.contains("\\")) // check for Windows paths.
				streamName = streamName.replace('\\', '/');
			
			String streamParent = null;
			int lidx = -1; 
			if ((lidx = streamName.lastIndexOf('/')) >= 0)
				streamParent = streamName.substring(0, lidx + 1);
			
			if (path.startsWith(CLASSPATH_PREFIX) || (streamParent != null && streamParent.startsWith(CLASSPATH_PREFIX)))
				return Common.openResource(((streamParent != null ? streamParent : "") + path).substring(CLASSPATH_PREFIX.length()));
			else
			{
				File f = null;
				if (streamParent != null)
				{
					f = new File(streamParent + path);
					if (f.exists())
						return new FileInputStream(f);
					else
						return new FileInputStream(new File(path));
				}
				else
				{
					return new FileInputStream(new File(path));
				}
				
			}
			
		}
	};
	
	private ArcheTextReader() {}
	
	/**
	 * Reads ArcheText objects into a new root from a starting text file.
	 * Note: Calls apply() with a new root.
	 * @param file	the file to read from.
	 * @return A new ArcheTextRoot that contains all the read object hierarchy.
	 * @throws IOException if the stream can't be read.
	 * @throws NullPointerException if f is null. 
	 */
	public static ArcheTextRoot read(File file) throws IOException
	{
		return read(file.getPath(), new FileInputStream(file), DEFAULT_INCLUDER);
	}
	
	/**
	 * Reads ArcheText objects from a String of text into a new root.
	 * Note: Calls apply() with a new root.
	 * @param text the String to read from.
	 * @return A new ArcheTextRoot that contains all the read object hierarchy.
	 * @throws IOException if the stream can't be read.
	 * @throws NullPointerException if f is null. 
	 */
	public static ArcheTextRoot read(String text) throws IOException
	{
		return read(STREAMNAME_TEXT, new StringReader(text), DEFAULT_INCLUDER);
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
	public static ArcheTextRoot read(String streamName, InputStream in) throws IOException
	{
		return read(streamName, new InputStreamReader(in), DEFAULT_INCLUDER);
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
	public static ArcheTextRoot read(String streamName, Reader reader) throws IOException
	{
		return read(streamName, reader, DEFAULT_INCLUDER);
	}

	/**
	 * Reads ArcheText objects into a new root from a starting text file.
	 * Note: Calls apply() with a new root.
	 * @param file	the file to read from.
	 * @param includer the includer to use to resolve "included" paths.
	 * @return A new ArcheTextRoot that contains all the read object hierarchy.
	 * @throws IOException if the stream can't be read.
	 * @throws NullPointerException if f is null. 
	 */
	public static ArcheTextRoot read(File file, ArcheTextIncluder includer) throws IOException
	{
		return read(file.getPath(), new FileInputStream(file), includer);
	}

	/**
	 * Reads ArcheText objects from a String of text into a new root.
	 * Note: Calls apply() with a new root.
	 * @param text the String to read from.
	 * @param includer the includer to use to resolve "included" paths.
	 * @return A new ArcheTextRoot that contains all the read object hierarchy.
	 * @throws IOException if the stream can't be read.
	 * @throws NullPointerException if f is null. 
	 */
	public static ArcheTextRoot read(String text, ArcheTextIncluder includer) throws IOException
	{
		return read(STREAMNAME_TEXT, new StringReader(text), includer);
	}

	/**
	 * Reads ArcheText objects into a new root.
	 * Note: Calls apply() with a new root.
	 * @param streamName the name of the stream.
	 * @param in the stream to read from.
	 * @param includer the includer to use to resolve "included" paths.
	 * @return A new ArcheTextRoot that contains all the read object hierarchy.
	 * @throws IOException if the stream can't be read.
	 * @throws NullPointerException if in is null. 
	 */
	public static ArcheTextRoot read(String streamName, InputStream in, ArcheTextIncluder includer) throws IOException
	{
		return read(streamName, new InputStreamReader(in), includer);
	}

	/**
	 * Reads ArcheText objects into a new root from a reader stream.
	 * Note: Calls apply() with a new root.
	 * @param streamName the name of the stream.
	 * @param reader the reader to read from.
	 * @param includer the includer to use to resolve "included" paths.
	 * @return A new ArcheTextRoot that contains all the read object hierarchy.
	 * @throws IOException if the stream can't be read.
	 * @throws NullPointerException if f is null. 
	 */
	public static ArcheTextRoot read(String streamName, Reader reader, ArcheTextIncluder includer) throws IOException
	{
		ArcheTextRoot out = new ArcheTextRoot();
		apply(streamName, reader, includer, out);
		return out;
	}

	/**
	 * Reads ArcheText objects from a classpath resource.
	 * Note: Calls apply() with a new root.
	 * @param name the resource name.
	 * @return A new ArcheTextRoot that contains all the read object hierarchy.
	 * @throws IOException if the stream can't be read.
	 * @throws NullPointerException if name is null. 
	 */
	public static ArcheTextRoot readResource(String name) throws IOException
	{
		return read("classpath:"+name, Common.openResource(name), DEFAULT_INCLUDER);
	}
	
	/**
	 * Applies the ArcheText objects read to an already existing root.
	 * @param f	the file to read from.
	 * @param root the root to apply the objects to.
	 * @throws NullPointerException	if either object is null. 
	 */
	public static void apply(File f, ArcheTextRoot root) throws IOException
	{
		apply(f.getPath(), new FileInputStream(f), root);
	}
	
	/**
	 * Applies the ArcheText objects read to an already existing root.
	 * @param text the String to read from.
	 * @param root the root to apply the objects to.
	 * @throws NullPointerException	if either object is null. 
	 */
	public static void apply(String text, ArcheTextRoot root) throws IOException
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
	public static void apply(String streamName, InputStream in, ArcheTextRoot root)
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
	public static void apply(String streamName, Reader reader, ArcheTextRoot root)
	{
		apply(streamName, reader, DEFAULT_INCLUDER, root);
	}
		
	/**
	 * Applies the ArcheText objects read to an already existing root.
	 * @param f	the file to read from.
	 * @param includer the includer to use to resolve "included" paths.
	 * @param root the root to apply the objects to.
	 * @throws NullPointerException	if either object is null. 
	 */
	public static void apply(File f, ArcheTextIncluder includer, ArcheTextRoot root) throws IOException
	{
		apply(f.getPath(), new FileInputStream(f), includer, root);
	}

	/**
	 * Applies the ArcheText objects read to an already existing root.
	 * @param streamName the name of the stream.
	 * @param in the stream to read from.
	 * @param includer the includer to use to resolve "included" paths.
	 * @param root the root to apply the objects to.
	 * @throws NullPointerException	if either object is null. 
	 */
	public static void apply(String streamName, InputStream in, ArcheTextIncluder includer, ArcheTextRoot root)
	{
		apply(streamName, new InputStreamReader(in), includer, root);
	}

	/**
	 * Applies the ArcheText objects read to an already existing root.
	 * @param text the String to read from.
	 * @param includer the includer to use to resolve "included" paths.
	 * @param root the root to apply the objects to.
	 * @throws NullPointerException	if either object is null. 
	 */
	public static void apply(String text, ArcheTextIncluder includer, ArcheTextRoot root) throws IOException
	{
		apply(STREAMNAME_TEXT, new StringReader(text), includer, root);
	}

	/**
	 * Applies the ArcheText objects read to an already existing root.
	 * @param streamName the name of the stream.
	 * @param reader the reader to read from.
	 * @param includer the includer to use to resolve "included" paths.
	 * @param root the root to apply the objects to.
	 * @throws NullPointerException	if either object is null. 
	 */
	public static void apply(String streamName, Reader reader, ArcheTextIncluder includer, ArcheTextRoot root)
	{
		ATLexer lexer = new ATLexer(streamName, reader, includer);
		ATParser parser = new ATParser(lexer);
		parser.readObjects(root);
	}

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
		static final int TYPE_DOT = 12;

		static final int TYPE_AT = 13;
		static final int TYPE_PLUS = 14;
		static final int TYPE_MINUS = 15;
		static final int TYPE_TIMES = 16;
		static final int TYPE_DIV = 17;
		static final int TYPE_MODULO = 18;
		static final int TYPE_POWER = 19;
		static final int TYPE_BITNOT = 20;
		static final int TYPE_NOT = 21;
		static final int TYPE_AND = 22;
		static final int TYPE_OR = 23;
		static final int TYPE_XOR = 24;
		static final int TYPE_LSHIFT = 25;
		static final int TYPE_RSHIFT = 26;
		static final int TYPE_RSHIFTPAD = 27;

		static final int TYPE_TRUE = 40;
		static final int TYPE_FALSE = 41;
		static final int TYPE_NULL = 42;

		static final int TYPE_COMMENT = 50;

		static final int TYPE_ASSIGNMENT_TYPE_START = 100;

		static HashMap<String, Combinator> ASSIGNMENTOPERATOR_MAP = new HashMap<String, Combinator>();

		private Kernel()
		{
			addStringDelimiter('"', '"');
			setDecimalSeparator('.');
			
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
			addDelimiter(".", TYPE_DOT);

			addDelimiter("@", TYPE_AT);
			addDelimiter("+", TYPE_PLUS);
			addDelimiter("-", TYPE_MINUS);
			addDelimiter("*", TYPE_TIMES);
			addDelimiter("/", TYPE_DIV);
			addDelimiter("%", TYPE_MODULO);
			addDelimiter("'", TYPE_POWER);
			addDelimiter("~", TYPE_BITNOT);
			addDelimiter("!", TYPE_NOT);
			addDelimiter("&", TYPE_AND);
			addDelimiter("|", TYPE_OR);
			addDelimiter("^", TYPE_XOR);
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
			for (Combinator combinator : Combinator.values())
			{
				addDelimiter(combinator.getAssignmentOperator(), TYPE_ASSIGNMENT_TYPE_START + (i++));
				ASSIGNMENTOPERATOR_MAP.put(combinator.getAssignmentOperator(), combinator);
			}
			
		}
		
	}
	
	/**
	 * All expression operator types.
	 * @author Matthew Tropiano
	 */
	private enum Operator
	{
		OR			(1, false),
		XOR			(1, false),

		AND			(2, false),

		LSHIFT		(3, false),
		RSHIFT		(3, false),
		RSHIFTPAD	(3, false),

		ADD			(4, false),
		SUBTRACT	(4, false),
		
		MULTIPLY	(5, false),
		DIVIDE		(5, false),
		MODULO		(5, false),
		
		POWER		(6, true),

		NEGATE		(7, true),
		NOT			(7, true),
		BITNOT		(7, true),
		ABSOLUTE	(7, true),

		LPAREN		(8, true),
		LLIST		(8, true),
		LSET		(8, true),

		//REFERENCE	(9, false),
		;
		
		private int precedence;
		private boolean rightAssociative;
		private Operator(int precedence, boolean rightAssociative) 
		{
			this.precedence = precedence;
			this.rightAssociative = rightAssociative;
		}

	}

	/**
	 * The lexer for a reader context.
	 */
	private static class ATLexer extends CommonLexer
	{
		private ArcheTextIncluder includer;
		
		private ATLexer(Reader in, ArcheTextIncluder includer)
		{
			super(KERNEL_INSTANCE, in);
			this.includer = includer;
		}

		private ATLexer(String in, ArcheTextIncluder includer)
		{
			super(KERNEL_INSTANCE, in);
			this.includer = includer;
		}
		
		private ATLexer(String name, Reader in, ArcheTextIncluder includer)
		{
			super(KERNEL_INSTANCE, name, in);
			this.includer = includer;
		}

		private ATLexer(String name, String in, ArcheTextIncluder includer)
		{
			super(KERNEL_INSTANCE, name, in);
			this.includer = includer;
		}
		
		@Override
		public InputStream getResource(String path) throws IOException
		{
			return includer.getIncludeResource(getCurrentStreamName(), path);
		}
	}
	
	/**
	 * The parser that parses text for the ArcheText structures. 
	 */
	private static class ATParser extends Parser
	{
		/** Set of prototypes. */
		private HashedQueueMap<String, String> prototypes;
		
		/** Current root. */
		private ArcheTextRoot currentRoot;
		/** Current object type. */
		private String currentObjectType;
		/** Current object name. */
		private String currentObjectName;
		/** Current object. */
		private ArcheTextObject currentObject;
		/** Current field list. */
		private List<String> currentFieldList;
		/** Current object reference. */
		private List<ArcheTextObject> currentObjectParents;
		/** Current object reference flatten flag. */
		private boolean currentObjectParentsFlatten;
		/** Current value from a parseValue() call. */
		private ArcheTextValue currentValue;
		
		private ATParser(ATLexer lexer)
		{
			super(lexer);
			prototypes = new HashedQueueMap<>();
		}
		
		/**
		 * Reads objects into the target root.
		 */
		void readObjects(ArcheTextRoot targetRoot)
		{
			currentRoot = targetRoot;
			
			// prime first token.
			nextToken();
			
			// keep parsing entries.
			boolean noError = true;
			try {
				while (currentToken() != null && (noError = parseATEntries(currentRoot))) ;
			} catch (ArcheTextOperationException e) {
				addErrorMessage("Error in expression: "+e.getLocalizedMessage());
				noError = false;
			}
			
			if (!noError) // awkward, I know.
			{
				String[] errors = getErrorMessages();
				if (errors.length > 0)
				{
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < errors.length; i++)
					{
						sb.append(errors[i]);
						if (i < errors.length-1)
							sb.append('\n');
					}
					throw new ArcheTextParseException(sb.toString());
				}
			}
			
		}
		
		/*
		 *	<ATEntries> :=
		 *		"." <ATPrototype>
		 *		<ATDeclaration> <ATParentList> <ATBody> <ATEntries>
		 *
		 * Sets: currentObject, currentObjectParents, currentObjectParentsFlatten
		 */
		private boolean parseATEntries(ArcheTextRoot targetRoot)
		{
			currentObject = null;
			
			if (matchType(Kernel.TYPE_DOT))
			{
				if (!parseATPrototype())
					return false;
				
				for (String field : currentFieldList)
					prototypes.enqueue(currentObjectType, field);
				
				return true;
			}
			else if (!parseATDeclaration())
				return false;
			else
			{
				currentObject = new ArcheTextObject(currentObjectType, currentObjectName);
				
				if (!parseATParentList())
					return false;
				
				for (ArcheTextObject parent : currentObjectParents)
					currentObject.addParent(parent);
				
				if (!parseATBody(currentObject))
					return false;
				
				if (currentObjectParentsFlatten)
					currentObject.flatten();

				targetRoot.add(currentObject);
				
				return true;
			}
			
		}
		
		/*
		 *	<ATDeclaration> :=
		 *		<IDENTIFIER> <ATStructName>
		 *
		 * Sets: currentObjectType, currentObjectName
		 */
		private boolean parseATDeclaration()
		{
			currentObjectType = null;
			
			if (currentType(Kernel.TYPE_IDENTIFIER))
			{
				currentObjectType = currentToken().getLexeme();
				nextToken();
				
				if (!parseATStructName())
					return false;
				
				return true;
			}
			
			addErrorMessage("Expected ArcheText object type or prototype clause start.");
			return false;
		}
		
		/*
		 *	<ATPrototype> :=
		 *		<IDENTIFIER> "(" <ATFieldNameList> ")"
		 *
		 * Sets: currentObjectType, currentFieldList
		 */
		private boolean parseATPrototype()
		{
			if (currentType(Kernel.TYPE_IDENTIFIER))
			{
				currentObjectType = currentToken().getLexeme();
				
				if (prototypes.containsKey(currentObjectType))
				{
					addErrorMessage("Prototype \""+currentObjectType+"\" already declared.");
					return false;
				}
				
				nextToken();

				if (!matchType(Kernel.TYPE_LPAREN))
				{
					addErrorMessage("Expected \"(\" for start of prototype fields.");
					return false;
				}
				
				if (!parseATFieldNameList())
					return false;
				
				if (!matchType(Kernel.TYPE_RPAREN))
				{
					addErrorMessage("Expected \")\" for end of prototype fields.");
					return false;
				}

				return true;
			}
			
			addErrorMessage("Expected Prototype type identifier after Prototype clause start.");
			return false;
		}
		
		/*
		 *	<ATFieldNameList> :=
		 *		<IDENTIFIER> <ATFieldNameListPrime>
		 *
		 * Sets: currentFieldList
		 */
		private boolean parseATFieldNameList()
		{
			if (currentFieldList == null)
				currentFieldList = new List<String>(4);
			else
				currentFieldList.clear();

			if (currentType(Kernel.TYPE_IDENTIFIER))
			{
				currentFieldList.add(currentToken().getLexeme());
				nextToken();
				
				if (!parseATFieldNameListPrime())
					return false;
				
				return true;
			}
			
			addErrorMessage("Expected field name identifier.");
			return false;
		}
		
		/*
		 *	<ATFieldNameListPrime> :=
		 *		"," <INTEGER> <ATFieldNameListPrime>
		 *		[e]
		 */
		private boolean parseATFieldNameListPrime()
		{
			if (currentType(Kernel.TYPE_COMMA))
			{
				nextToken();

				if (currentType(Kernel.TYPE_IDENTIFIER))
				{
					currentFieldList.add(currentToken().getLexeme());
					nextToken();
					
					if (!parseATFieldNameListPrime())
						return false;
					
					return true;
				}
				
				addErrorMessage("Expected field name identifier.");
				return false;
			}
			
			return true;
		}
		
		/*
		 *	<ATStructName> :=
		 *		<IDENTIFIER>
		 *		<STRING>
		 *		<INTEGER>
		 *		<FLOAT>
		 *		[e]
		 *
		 * Sets: currentObjectName
		 */
		private boolean parseATStructName()
		{
			currentObjectName = null;
			
			if (currentType(Kernel.TYPE_IDENTIFIER, Kernel.TYPE_STRING, Kernel.TYPE_NUMBER))
			{
				currentObjectName = currentToken().getLexeme();
				nextToken();
				return true;
			}
			
			return true;
		}

		
		/*
		 *	<ATParentList> :=
		 *		"<-" <ATDeclaration> <ATParentListPrime>
		 *		":" <ATDeclaration> <ATParentListPrime>
		 *		[e]
		 * Sets: currentObjectParents, currentObjectParentsFlatten
		 */
		private boolean parseATParentList()
		{
			// clear ref list.
			currentObjectParentsFlatten = false;
			if (currentObjectParents == null)
				currentObjectParents = new List<ArcheTextObject>(4);
			else
				currentObjectParents.clear();

			
			if (currentType(Kernel.TYPE_LEFTARROW, Kernel.TYPE_COLON))
			{
				// flattens hierarchy?
				currentObjectParentsFlatten = currentType(Kernel.TYPE_LEFTARROW);
				
				nextToken();
				
				if (!parseATDeclaration())
					return false;
				
				ArcheTextObject objectRef = currentRoot.get(currentObjectType, currentObjectName);
				
				if (objectRef == null)
				{
					addErrorMessage("Parent object ("+currentObjectType+(currentObjectName != null ? " \""+currentObjectName+"\"" : "")+") not declared or found.");
					return false;
				}
			
				currentObjectParents.add(objectRef);
				
				return parseATParentListPrime();
			}
			
			return true;
		}

		
		/*
		 *	<ATParentListPrime> :=
		 *		":" <ATDeclaration> <ATParentListPrime>
		 *		[e]
		 *
		 * Appends to: currentObjectParents
		 */
		private boolean parseATParentListPrime()
		{
			if (matchType(Kernel.TYPE_COLON))
			{
				if (!parseATDeclaration())
					return false;
				
				ArcheTextObject objectRef = currentRoot.get(currentObjectType, currentObjectName);
				
				if (objectRef == null)
				{
					addErrorMessage("Parent object ("+currentObjectType+(currentObjectName != null ? " \""+currentObjectName+"\"" : "")+") not declared or found.");
					return false;
				}
			
				currentObjectParents.add(objectRef);
				
				return parseATParentListPrime();
			}
			
			return true;
		}
		
		
		/*
		 *	<ATBody> :=
		 *		"{" <ATFieldList> "}"
		 *		"(" <ATPrototypeFieldList> ")"
		 *		";"
		 */
		private boolean parseATBody(ArcheTextObject object)
		{
			if (matchType(Kernel.TYPE_LBRACE))
			{
				if (!parseATFieldList(object))
					return false;
				
				if (!matchType(Kernel.TYPE_RBRACE))
				{
					addErrorMessage("Expected ',' or end of object declaration ('}').");
					return false;
				}
				return true;
			}
			else if (currentType(Kernel.TYPE_LPAREN))
			{
				// check for prototype.
				if (!prototypes.containsKey(object.getType()))
				{
					addErrorMessage("Prototyped structure has no matching prototype delcaration for type \""+object.getType()+"\".");
					return false;
				}

				nextToken();

				if (!parseATPrototypeFieldList(object, prototypes.get(object.getType())))
					return false;
				
				if (!matchType(Kernel.TYPE_RPAREN))
				{
					addErrorMessage("Expected ',' or end of prototyped object declaration (')').");
					return false;
				}
				return true;
			}
			else if (matchType(Kernel.TYPE_SEMICOLON))
			{
				return true;
			}
			else
			{
				addErrorMessage("Expected ArcheText object body or ';'.");
				return false;
			}

		}
		
		
		/*
		 *	<ATFieldList> :=
		 *		<IDENTIFIER> <AssignmentOperator> <Expression> ";" <ATFieldList>
		 *		[e]
		 */
		private boolean parseATFieldList(ArcheTextObject object)
		{
			if (currentType(Kernel.TYPE_IDENTIFIER))
			{
				String member = currentToken().getLexeme();
				nextToken();
				
				if (currentToken().getType() < Kernel.TYPE_ASSIGNMENT_TYPE_START)
				{
					addErrorMessage("Expected assignment operator.");
					return false;
				}
				
				String operator = currentToken().getLexeme();
				Combinator combinator = Kernel.ASSIGNMENTOPERATOR_MAP.get(operator);
				nextToken();
				
				if (!parseValue())
					return false;
				
				object.setField(member, combinator, currentValue);
				
				if (!matchType(Kernel.TYPE_SEMICOLON))
				{
					addErrorMessage("Expected \";\" to end statement, or an assignment operator to continue statement.");
					return false;
				}
					
				return parseATFieldList(object);
			}
			
			return true;
		}

		/*
		 *	<ATPrototypeFieldList> := <Value> ("," <Value>....)
		 */
		private boolean parseATPrototypeFieldList(ArcheTextObject object, Queue<String> fieldQueue)
		{
			Iterator<String> fieldIterator = fieldQueue.iterator();
			while (fieldIterator.hasNext())
			{
				String field = fieldIterator.next();
				
				if (!parseValue())
					return false;
				
				object.setField(field, Combinator.SET, currentValue);
				
				// If comma not matched, abandon read.
				if (fieldIterator.hasNext())
				{
					if (!matchType(Kernel.TYPE_COMMA))
						return true;
				}
				
			}
			
			return true;
		}
		
		/*
		 *	<Value> :=
		 *		"@" <ATDeclaration>
		 *		[EXPRESSION]
		 */
		private boolean parseValue()
		{
			
			if (matchType(Kernel.TYPE_AT))
			{
				if (!parseATDeclaration())
					return false;
				
				ArcheTextObject objectRef = currentRoot.get(currentObjectType, currentObjectName);
				
				if (objectRef == null)
				{
					addErrorMessage("Parent object ("+currentObjectType+(currentObjectName != null ? " \""+currentObjectName+"\"" : "")+") not declared or found.");
					return false;
				}

				currentValue = new ArcheTextValue(Type.OBJECT, objectRef);
				
				return true;
			}
			else if (matchType(Kernel.TYPE_LBRACE))
			{
				ArcheTextObject object = new ArcheTextObject();
				if (!parseATFieldList(object))
					return false;
				
				if (!matchType(Kernel.TYPE_RBRACE))
				{
					addErrorMessage("Expected ',' or end of object declaration ('}').");
					return false;
				}

				currentValue = new ArcheTextValue(Type.OBJECT, object);
				
				return true;
			}
			else
				return parseExpression();
		}
		
		/*
		 *	<ListBody> :=
		 *		<Value> <ListBodyPrime>
		 *		[e]
		 */
		private boolean parseListBody(AbstractVector<ArcheTextValue> list)
		{
			if (parseValue())
			{
				list.add(currentValue);
				return parseListBodyPrime(list);
			}
			
			return true;
		}

		
		/*
		 *	<ListBodyPrime> :=
		 *		"," <Value> <SetBodyPrime>
		 *		[e]
		 */
		private boolean parseListBodyPrime(AbstractVector<ArcheTextValue> list)
		{
			if (matchType(Kernel.TYPE_COMMA))
			{
				if (!parseValue())
					return false;
				list.add(currentValue);
				return parseListBodyPrime(list);
			}
			
			return true;
		}
		
		
		/*
		 *	<SetBody> :=
		 *		<Value> <SetBodyPrime>
		 *		[e]
		 */
		private boolean parseSetBody(AbstractSet<ArcheTextValue> set)
		{
			if (parseValue())
			{
				set.put(currentValue);
				return parseSetBodyPrime(set);
			}
			
			return true;
		}
		
		
		/*
		 *	<SetBodyPrime> :=
		 *		"," <Value> <SetBodyPrime>
		 *		[e]
		 */
		private boolean parseSetBodyPrime(AbstractSet<ArcheTextValue> set)
		{
			if (matchType(Kernel.TYPE_COMMA))
			{
				if (!parseValue())
					return false;
				set.put(currentValue);
				return parseSetBodyPrime(set);
			}
			return true;
		}
		
		
		/*
		 * Parses an infix expression.
		 * May throw an ArcheTextOperationException if some values cannot be calculated or combined. 
		 */
		private boolean parseExpression()
		{
			// Expression value stack.
			Stack<ArcheTextValue> valueStack = new Stack<ArcheTextValue>();
			// Expression operator stack.
			Stack<Operator> operatorStack = new Stack<Operator>();

			// was the last read token a value?
			boolean lastWasValue = false;
			boolean keepGoing = true;
			
			while (keepGoing)
			{
				if (currentType(Kernel.TYPE_IDENTIFIER))
				{
					if (lastWasValue)
					{
						addErrorMessage("Expression error - expected operator.");
						return false;
					}
					
					String identname = currentToken().getLexeme();
					
					ArcheTextValue val = currentObject.getField(identname);
					if (val != null)
						val = val.copy();

					valueStack.push(val);
					
					nextToken();
					lastWasValue = true;
				}
				else if (matchType(Kernel.TYPE_LBRACE))
				{
					if (lastWasValue)
					{
						addErrorMessage("Expression error - expected operator.");
						return false;
					}
					
					ArcheTextObject object = new ArcheTextObject();
					if (!parseATFieldList(object))
						return false;
					
					if (!matchType(Kernel.TYPE_RBRACE))
					{
						addErrorMessage("Expected end of object declaration ('}').");
						return false;
					}

					valueStack.push(new ArcheTextValue(Type.OBJECT, object));
					lastWasValue = true;
				}
				else if (matchType(Kernel.TYPE_LBRACK))
				{
					if (lastWasValue)
					{
						addErrorMessage("Expression error - expected operator.");
						return false;
					}
					
					List<ArcheTextValue> list = new List<ArcheTextValue>(); 
					
					if (!parseListBody(list))
						return false;
					
					if (!matchType(Kernel.TYPE_RBRACK))
					{
						addErrorMessage("Expected end of list declaration (']').");
						return false;
					}

					valueStack.push(new ArcheTextValue(Type.LIST, list));
					lastWasValue = true;
				}
				else if (matchType(Kernel.TYPE_LANGLEBRACK))
				{
					if (lastWasValue)
					{
						addErrorMessage("Expression error - expected operator.");
						return false;
					}
					
					Hash<ArcheTextValue> set = new Hash<ArcheTextValue>(); 
					
					if (!parseSetBody(set))
						return false;
					
					if (!matchType(Kernel.TYPE_RANGLEBRACK))
					{
						addErrorMessage("Expected end of set declaration ('>').");
						return false;
					}

					valueStack.push(new ArcheTextValue(Type.SET, set));
					lastWasValue = true;
				}
				else if (matchType(Kernel.TYPE_LPAREN))
				{
					if (lastWasValue)
					{
						addErrorMessage("Expression error - expected operator.");
						return false;
					}
					
					if (!parseValue())
						return false;
					
					if (!matchType(Kernel.TYPE_RPAREN))
					{
						addErrorMessage("Expected ending parenthesis (')').");
						return false;
					}

					valueStack.push(currentValue);
					lastWasValue = true;
				}
				else if (isValidLiteralType())
				{
					if (lastWasValue)
					{
						addErrorMessage("Expression error - expected operator.");
						return false;
					}
					
					tokenToValue();
					valueStack.push(currentValue);
					lastWasValue = true;
				}
				else if (lastWasValue)
				{
					if (isBinaryOperatorType())
					{
						Operator nextOperator = null;
						
						switch (currentToken().getType())
						{
							case Kernel.TYPE_PLUS:
								nextOperator = Operator.ADD;
								break;
							case Kernel.TYPE_MINUS:
								nextOperator = Operator.SUBTRACT;
								break;
							case Kernel.TYPE_TIMES:
								nextOperator = Operator.MULTIPLY;
								break;
							case Kernel.TYPE_DIV:
								nextOperator = Operator.DIVIDE;
								break;
							case Kernel.TYPE_MODULO:
								nextOperator = Operator.MODULO;
								break;
							case Kernel.TYPE_POWER:
								nextOperator = Operator.POWER;
								break;
							case Kernel.TYPE_AND:
								nextOperator = Operator.AND;
								break;
							case Kernel.TYPE_OR:
								nextOperator = Operator.OR;
								break;
							case Kernel.TYPE_XOR:
								nextOperator = Operator.XOR;
								break;
							case Kernel.TYPE_LSHIFT:
								nextOperator = Operator.LSHIFT;
								break;
							case Kernel.TYPE_RSHIFT:
								nextOperator = Operator.RSHIFT;
								break;
							case Kernel.TYPE_RSHIFTPAD:
								nextOperator = Operator.RSHIFTPAD;
								break;
							default:
								throw new ArcheTextParseException("Internal error - unexpected binary operator miss.");
						}
						
						nextToken();

						if (!operatorReduce(operatorStack, valueStack, nextOperator))
							return false;
						
						operatorStack.push(nextOperator);
						lastWasValue = false;
					}
					else // end on a value
					{
						keepGoing = false;
					}
				}
				else if (isUnaryOperatorType())
				{
					switch (currentToken().getType())
					{
						case Kernel.TYPE_MINUS:
							operatorStack.push(Operator.NEGATE);
							break;
						case Kernel.TYPE_PLUS:
							operatorStack.push(Operator.ABSOLUTE);
							break;
						case Kernel.TYPE_BITNOT:
							operatorStack.push(Operator.BITNOT);
							break;
						case Kernel.TYPE_NOT:
							operatorStack.push(Operator.NOT);
							break;
						default:
							throw new ArcheTextParseException("Internal error - unexpected unary operator miss.");
					}
					
					nextToken();
					lastWasValue = false;
				}
				else
				{
					addErrorMessage("Expression error - expected value after operator.");
					return false;
				}
				
			}
			
			// end of expression - reduce.
			while (!operatorStack.isEmpty())
			{
				if (!expressionReduce(operatorStack, valueStack))
					return false;
			}
			
			if (valueStack.isEmpty())
			{
				addErrorMessage("Expected valid expression.");
				return false;
			}

			currentValue = valueStack.pop();
			return true;
		}

		// keeps reducing until the input operator is of greater precedence.
		private boolean operatorReduce(Stack<Operator> operatorStack, Stack<ArcheTextValue> valueStack, Operator operator)
		{
			Operator top = operatorStack.peek();
			while (top != null && (top.precedence > operator.precedence || (top.precedence == operator.precedence && !operator.rightAssociative)))
			{
				if (!expressionReduce(operatorStack, valueStack))
					return false;
				top = operatorStack.peek();
			}
			
			return true;
		}
		
		// Performs an operator reduction.
		private boolean expressionReduce(Stack<Operator> operatorStack, Stack<ArcheTextValue> valueStack)
		{
			if (operatorStack.isEmpty())
				throw new ArcheTextParseException("Internal error - operator stack must have one operator in it.");

			Operator operator = operatorStack.pop();
			
			switch (operator)
			{
				case NOT:
				case NEGATE:
				case BITNOT:
				case ABSOLUTE:
				{
					if (valueStack.size() < 1)
					{
						addErrorMessage("Bad expression - operator requires at least one operand.");
						return false;
					}
					
					ArcheTextValue value = valueStack.pop();
					
					switch (operator)
					{
						case NOT:
							valueStack.push(value.not());
							break;
						case NEGATE:
							valueStack.push(value.negate());
							break;
						case BITNOT:
							valueStack.push(value.bitwiseNot());
							break;
						case ABSOLUTE:
							valueStack.push(value.absolute());
							break;
						default:
							throw new ArcheTextParseException("Internal error - unary operator state should not have been reached.");
					}
					
					return true;
				}
				
				case ADD:
				case SUBTRACT:
				case MULTIPLY:
				case DIVIDE:
				case MODULO:
				case POWER:
				case AND:
				case OR:
				case XOR:
				case LSHIFT:
				case RSHIFT:
				case RSHIFTPAD:
				{
					if (valueStack.size() < 2)
					{
						addErrorMessage("Bad expression - operator requires at least two operands.");
						return false;
					}

					ArcheTextValue operand = valueStack.pop();
					ArcheTextValue target = valueStack.pop();

					switch (operator)
					{
						case ADD:
							valueStack.push(Combinator.ADD.combine(operand, target));
							break;
						case SUBTRACT:
							valueStack.push(Combinator.SUBTRACT.combine(operand, target));
							break;
						case MULTIPLY:
							valueStack.push(Combinator.MULTIPLY.combine(operand, target));
							break;
						case DIVIDE:
							valueStack.push(Combinator.DIVISION.combine(operand, target));
							break;
						case MODULO:
							valueStack.push(Combinator.MODULO.combine(operand, target));
							break;
						case POWER:
							valueStack.push(Combinator.POWER.combine(operand, target));
							break;
						case AND:
							valueStack.push(Combinator.BITWISEAND.combine(operand, target));
							break;
						case OR:
							valueStack.push(Combinator.BITWISEOR.combine(operand, target));
							break;
						case XOR:
							valueStack.push(Combinator.BITWISEXOR.combine(operand, target));
							break;
						case LSHIFT:
							valueStack.push(Combinator.LEFTSHIFT.combine(operand, target));
							break;
						case RSHIFT:
							valueStack.push(Combinator.RIGHTSHIFT.combine(operand, target));
							break;
						case RSHIFTPAD:
							valueStack.push(Combinator.RIGHTPADDINGSHIFT.combine(operand, target));
							break;
						default:
							throw new ArcheTextParseException("Internal error - binary operator state should not have been reached.");
					}
					
					return true;
				}
				
				default:
					throw new ArcheTextParseException("Internal error - bad operator.");
				
			}
		}
		
		// Token to value.
		private boolean tokenToValue()
		{
			if (currentType(Kernel.TYPE_STRING))
			{
				currentValue = new ArcheTextValue(Type.STRING, currentToken().getLexeme());
				nextToken();
				return true;
			}
			else if (currentType(Kernel.TYPE_NUMBER))
			{
				String lexeme = currentToken().getLexeme();
				if (lexeme.startsWith("0X") || lexeme.startsWith("0x"))
					currentValue = (new ArcheTextValue(Type.INTEGER, Long.parseLong(lexeme.substring(2), 16)));
				else if (lexeme.contains("."))
					currentValue = (new ArcheTextValue(Type.FLOAT, Double.parseDouble(lexeme)));
				else
					currentValue = (new ArcheTextValue(Type.INTEGER, Long.parseLong(lexeme)));
				nextToken();
				return true;
			}
			else if (currentType(Kernel.TYPE_TRUE))
			{
				currentValue = (new ArcheTextValue(Type.BOOLEAN, true));
				nextToken();
				return true;
			}
			else if (currentType(Kernel.TYPE_FALSE))
			{
				currentValue = (new ArcheTextValue(Type.BOOLEAN, false));
				nextToken();
				return true;
			}
			else if (currentType(Kernel.TYPE_NULL))
			{
				currentValue = (new ArcheTextValue(Type.NULL, null));
				nextToken();
				return true;
			}
			else
				throw new ArcheTextParseException("Internal error - unexpected token type.");
		}
		
		// Return true if token type can be a unary operator.
		private boolean isValidLiteralType()
		{
			switch (currentToken().getType())
			{
				case Kernel.TYPE_STRING:
				case Kernel.TYPE_NUMBER:
				case Kernel.TYPE_TRUE:
				case Kernel.TYPE_FALSE:
				case Kernel.TYPE_NULL:
					return true;
				default:
					return false;
			}
		}
		
		// Return true if token type can be a unary operator.
		private boolean isUnaryOperatorType()
		{
			switch (currentToken().getType())
			{
				case Kernel.TYPE_MINUS:
				case Kernel.TYPE_PLUS:
				case Kernel.TYPE_BITNOT:
				case Kernel.TYPE_NOT:
					return true;
				default:
					return false;
			}
		}
		
		// Return true if token type can be a binary operator.
		private boolean isBinaryOperatorType()
		{
			switch (currentToken().getType())
			{
				case Kernel.TYPE_PLUS:
				case Kernel.TYPE_MINUS:
				case Kernel.TYPE_TIMES:
				case Kernel.TYPE_DIV:
				case Kernel.TYPE_MODULO:
				case Kernel.TYPE_POWER:
				case Kernel.TYPE_AND:
				case Kernel.TYPE_OR:
				case Kernel.TYPE_XOR:
				case Kernel.TYPE_LSHIFT:
				case Kernel.TYPE_RSHIFT:
				case Kernel.TYPE_RSHIFTPAD:
					return true;
				default:
					return false;
			}
		}
		
	}
	
}
