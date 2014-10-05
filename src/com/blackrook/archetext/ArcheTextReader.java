package com.blackrook.archetext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import com.blackrook.archetext.ArcheTextValue.Type;
import com.blackrook.archetext.exception.ArcheTextParseException;
import com.blackrook.commons.AbstractSet;
import com.blackrook.commons.AbstractVector;
import com.blackrook.commons.Common;
import com.blackrook.commons.hash.Hash;
import com.blackrook.commons.hash.HashMap;
import com.blackrook.commons.linkedlist.Stack;
import com.blackrook.commons.list.List;
import com.blackrook.lang.CommonLexer;
import com.blackrook.lang.CommonLexerKernel;
import com.blackrook.lang.Lexer;
import com.blackrook.lang.Parser;

/**
 * TODO: Support expressions, object references in objects.
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

		static final int TYPE_AT = 12;
		static final int TYPE_PLUS = 13;
		static final int TYPE_MINUS = 14;
		static final int TYPE_TIMES = 15;
		static final int TYPE_DIV = 16;
		static final int TYPE_MODULO = 17;
		static final int TYPE_POWER = 18;
		static final int TYPE_BITNOT = 19;
		static final int TYPE_NOT = 20;
		static final int TYPE_AND = 21;
		static final int TYPE_OR = 22;
		static final int TYPE_XOR = 23;
		static final int TYPE_LSHIFT = 24;
		static final int TYPE_RSHIFT = 25;
		static final int TYPE_RSHIFTPAD = 26;
		static final int TYPE_REF = 26;

		static final int TYPE_TRUE = 40;
		static final int TYPE_FALSE = 41;
		static final int TYPE_NULL = 42;

		static final int TYPE_COMMENT = 50;

		static final int TYPE_ASSIGNMENT_TYPE_START = 100;

		static HashMap<String, Combinator> ASSIGNMENTOPERATOR_MAP = new HashMap<String, Combinator>();

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
			addDelimiter(".", TYPE_REF);
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

		PLUS		(4, false),
		MINUS		(4, false),
		
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

		REFERENCE	(9, false),
		;
		
		private int precedence;
		private boolean rightAssociative;
		private Operator(int precedence, boolean rightAssociative) 
		{
			this.precedence = precedence;
			this.rightAssociative = rightAssociative;
		}

		public int getPrecedence() {return precedence;}
		public boolean isRightAssociative() {return rightAssociative;}
		
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
		/** Current root. */
		private ArcheTextRoot currentRoot;
		/** Current object type. */
		private String currentObjectType;
		/** Current object name. */
		private String currentObjectName;
		/** Current object. */
		private ArcheTextObject currentObject;
		/** Current object reference. */
		private List<ArcheTextObject> currentObjectParents;
		/** Current object reference flatten flag. */
		private boolean currentObjectParentsFlatten;

		/** Expression value stack. */
		private Stack<ArcheTextValue> valueStack;
		/** Expression operator stack. */
		private Stack<Operator> operatorStack;
		
		private ATParser(ATLexer lexer)
		{
			super(lexer);
			valueStack = new Stack<ArcheTextValue>();
			operatorStack = new Stack<Operator>();
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
			while (currentToken() != null && (noError = parseATEntries()))
			{
				targetRoot.add(currentObject);
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
		 *		<ATDeclaration> <ATParentList> <ATBody> <ATEntries>
		 *
		 * Sets: currentObject, currentObjectParents, currentObjectParentsFlatten
		 */
		private boolean parseATEntries()
		{
			currentObject = null;
			
			if (!parseATDeclaration())
				return false;
			
			currentObject = new ArcheTextObject(currentObjectType, currentObjectName);
			
			if (!parseATParentList())
				return false;
			
			for (ArcheTextObject parent : currentObjectParents)
				currentObject.addParent(parent);
			
			if (!parseATBody(currentObject))
				return false;
			
			if (currentObjectParentsFlatten)
				currentObject.flatten();

			return true;
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
			
			if (currentType(Lexer.TYPE_IDENTIFIER))
			{
				currentObjectType = currentToken().getLexeme();
				nextToken();
				
				if (!parseATStructName())
					return false;
				
				return true;
			}
			
			addErrorMessage("Expected ArcheText object type indentifier.");
			return false;
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
			
			if (currentType(Lexer.TYPE_IDENTIFIER, Lexer.TYPE_STRING, Lexer.TYPE_NUMBER))
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
			if (currentType(Lexer.TYPE_IDENTIFIER))
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
				
				if (!parseValue(combinator))
					return false;
				
				object.setField(member, combinator, valueStack.pop());
				
				if (!matchTypeStrict(Kernel.TYPE_SEMICOLON))
					return false;
					
				return parseATFieldList(object);
			}
			
			return true;
		}

		
		/*
		 *	<Value> :=
		 *		"@" <ATDeclaration>
		 *		"{" <ATFieldList> "}"
		 *		[EXPRESSION]
		 */
		private boolean parseValue(Combinator combinator)
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

				valueStack.push(new ArcheTextValue(Type.OBJECT, objectRef));
				
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

				valueStack.push(new ArcheTextValue(Type.OBJECT, object));
				
				return true;
			}
			else
				return parseExpression();
		}
		
		/*
		 * 
		 */
		private boolean parseExpression()
		{
			// was the last read token a value?
			boolean lastWasValue = false;
			
			// TODO: Finish.
			
			if (valueStack.isEmpty())
			{
				addErrorMessage("Expected expression.");
				return false;
			}

			return true;
		}
		
		// Return true if token type can be a unary operator.
		private boolean isValidLiteralType()
		{
			switch (currentToken().getType())
			{
				case Lexer.TYPE_STRING:
				case Lexer.TYPE_NUMBER:
				case Kernel.TYPE_TRUE:
				case Kernel.TYPE_FALSE:
				case Kernel.TYPE_NULL:
					return true;
				default:
					return false;
			}
		}
		
		// Return true if token type can be a unary operator.
		private boolean isValidSetType()
		{
			switch (currentToken().getType())
			{
				case Kernel.TYPE_LBRACE:
				case Kernel.TYPE_RBRACE:
				case Kernel.TYPE_LANGLEBRACK:
				case Kernel.TYPE_RANGLEBRACK:
				case Kernel.TYPE_LBRACK:
				case Kernel.TYPE_RBRACK:
				case Kernel.TYPE_LPAREN:
				case Kernel.TYPE_RPAREN:
				case Kernel.TYPE_COMMA:
				case Kernel.TYPE_SEMICOLON:
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
				case Kernel.TYPE_REF:
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
	
	/*
			if (matchType(Kernel.TYPE_LBRACK))
			{
				AbstractVector<ArcheTextValue> list = new List<ArcheTextValue>();
				if (!parseListBody(list))
					return false;
				
				if (!matchType(Kernel.TYPE_RBRACK))
				{
					addErrorMessage("Expected ',' or end of list declaration (']').");
					return false;
				}
				
				currentValue = new ArcheTextValue(Type.LIST, list);

				return true;
			}
			else if (matchType(Kernel.TYPE_LANGLEBRACK))
			{
				AbstractSet<ArcheTextValue> set = new Hash<ArcheTextValue>();
				if (!parseSetBody(set))
					return false;
				
				if (!matchType(Kernel.TYPE_RANGLEBRACK))
				{
					addErrorMessage("Expected ',' or end of set declaration ('>').");
					return false;
				}
				
				currentValue = new ArcheTextValue(Type.SET, set);

				return true;
			}
			else if (currentType(Lexer.TYPE_STRING))
			{
				currentValue = new ArcheTextValue(Type.STRING, currentToken().getLexeme());
				nextToken();
				return true;
			}
			else if (currentType(Lexer.TYPE_NUMBER))
			{
				String lexeme = currentToken().getLexeme();
				if (lexeme.startsWith("0X") || lexeme.startsWith("0x"))
					currentValue = new ArcheTextValue(Type.INTEGER, Long.parseLong(lexeme.substring(2), 16));
				else if (lexeme.contains("."))
					currentValue = new ArcheTextValue(Type.FLOAT, Double.parseDouble(lexeme));
				else
					currentValue = new ArcheTextValue(Type.INTEGER, Long.parseLong(lexeme));
				nextToken();
				return true;
			}
			else if (matchType(Kernel.TYPE_TRUE))
			{
				currentValue = new ArcheTextValue(Type.BOOLEAN, true);
				return true;
			}
			else if (matchType(Kernel.TYPE_FALSE))
			{
				currentValue = new ArcheTextValue(Type.BOOLEAN, false);
				return true;
			}
			else if (matchType(Kernel.TYPE_NULL))
			{
				currentValue = new ArcheTextValue(Type.OBJECT, null);
				return true;
			}
			else
			{
				addErrorMessage("Expected object, list, set, string, numeric value, boolean value, or null.");
				return false;
			}
	 */
}
