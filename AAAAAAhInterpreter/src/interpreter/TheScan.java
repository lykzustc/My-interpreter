package interpreter;

import java.util.*;

import token.Token;
import token.TokenTag;

import java.io.*;

/*
 This class implements our lexer, or scanner, or tokenizer, or whatever.
 Basically it iterates through the source code character by character and creates
 valid tokens out of it. After the lexical analysis is done, the parser takes
 control and starts taking care of statements. 
 */
public class TheScan
{
	final int REDUNDANTEQ = 0;
	final int STRINGNOTEND = 1;
	
	// global tokenized list
	private ArrayList<Token> tokens = new ArrayList<Token>();

	// list of all our possible operators
	private ArrayList<Character> operators = new ArrayList<Character>(
			Arrays.asList('(', ')', '+', '-', '*', '%', '/',
						  '<', '>'));

	// untokenized data
	private String data; 

	public TheScan ( String text )
		throws IOException, Exception
	{
		// store a copy of the text
		this.data = text;

		// start the interpreter
		try{
			interpret();
		} catch (InterpreterException e){
			System.err.println("EXCEPTION IN TOKENIZING");
			e.printStackTrace();
		}
		
	}
	
	// this is the 'main'; it inits tokenization and executes
	// statements
	@SuppressWarnings("unused")
	private void interpret() throws Exception
	{		
		tokenize();
		
		// print the tokens
		printer();

		// call the parser and pass it the tokenized list
		Parser parser = new Parser(tokens);
	}

	// This method creates a new token based on 
	// the current operator
	private void addOperator ( char c )
	{
		// initialize the token tag with a dummy var
		TokenTag tag = TokenTag.NONE;
		
		switch ( c )
		{
			case '(':
				tag = TokenTag.LPARA;
				break;
			case ')':
				tag = TokenTag.RPARA;
				break;
			case '+':
				tag = TokenTag.PLUS;
				break;
			case '-':
				tag = TokenTag.MINUS;
				break;
			case '*':
				tag = TokenTag.MULTIPLY;
				break;
			case '/':
				tag = TokenTag.DIVIDE;
				break;
			case '%':
				tag = TokenTag.MOD;
				break;
			case '<':
				tag = TokenTag.LT;
				break;
			case '>':
				tag = TokenTag.GT;
				break;
			default:
				return;
		}
		// add it to the token list
		tokens.add(new Token(Character.toString(c), tag));
	}
	
	// this takes the local content string and tokenizes
	// it into a list, which we use later in the AST
	@SuppressWarnings("incomplete-switch")
	private void tokenize() throws InterpreterException
	{
		// initialize the tag we're working on parsing
		TokenTag tag = TokenTag.NONE;

		// the current 'token' we're working on parsing
		String current = "";

		// start scanning through all the plaintext, one
		// character a time and tokenize stuff we see/need
		for ( int i = 0; i < data.length(); ++i )
		{
			// read the character
			char c = data.charAt(i);

			
			// if it's a string (for output, say)
			if ( c == '"' )
			{
				tag = TokenTag.STRING;
			}
			// figure out what it is and set the current tag
			// if it's an operator, go figure out what it is
			else if ( operators.contains(c))
			{
				tag = TokenTag.OP;			
			}
			else if ( c == '=' ){
				tag = TokenTag.ASSIGN;
			}
			
			// if it's a digit and we're not parsing anything in particular stm
			else if ( tag == TokenTag.NONE && Character.isDigit(c))
			{
				tag = TokenTag.INT;
			}
			// if it's a letter (vars, for instances) OR we're building a variable and
			// we run across a digit (for instance, N1)
			else if ( Character.isLetter(c) ||
							(tag == TokenTag.VARIABLE && Character.isDigit(c)))
			{
				current += c;
				tag = TokenTag.VARIABLE;
			}
			//System.out.println ( "preprocessing " + c + " with tag " + tag);
			// switch on the current tag and do some parsin'
			switch ( tag )
			{				
			case OP:
				addOperator(c);
				current = "";
				tag = TokenTag.NONE;
				break;
			case ASSIGN:
				char tmpe1 = '0';
				char tmpe2 = '0';
				if (i+1 < data.length()){
					tmpe1 = data.charAt(i+1);
				}
				if (i+2 < data.length()){
					tmpe2 = data.charAt(i+2);
				}
				if (tmpe1 == '=' && tmpe2 == '='){
					handlerError(REDUNDANTEQ);
				}else{
					if (tmpe1 == '='){
						tokens.add(new Token("==", TokenTag.EQ));
						tag = TokenTag.NONE;
						i++;
					} else {
						tokens.add(new Token("=",TokenTag.ASSIGN));
						tag = TokenTag.NONE;
					}
				}				
				break;

			case INT:
					if ( Character.isDigit(c))
					{
						// consume them digits
						while ( Character.isDigit(c)) {
							current += c;
							c = data.charAt(++i);
						}
						
						// create the integer
						tokens.add(new Token (current, TokenTag.INT));
						current = "";
						tag = TokenTag.NONE;
						i--;	// rewind
					}
					else
					{
						// create the token, clear the current string
						// and reset the parsing tag
						tokens.add ( new Token (current, TokenTag.INT));
						current = "";
						tag = TokenTag.NONE;
						i--; // rewind
					}
					break;
				// handles variables
				case VARIABLE:					
					if ( current.equalsIgnoreCase("IF"))
					{
						tokens.add(new Token(current, TokenTag.IF));
						current = "";
						tag = TokenTag.NONE;
					}
					else if ( current.equalsIgnoreCase("PRINT"))
					{
						//add 'Print' token
						tokens.add(new Token(current, TokenTag.PRINT));
						current = "";
						tag = TokenTag.NONE;
					}
					
					else if ( current.equalsIgnoreCase("TRUE"))
					{
						tokens.add(new Token(current, TokenTag.TRUE));
						current = "";
						tag = TokenTag.NONE;
					}
					else if ( current.equalsIgnoreCase("FALSE"))
					{
						tokens.add(new Token(current, TokenTag.FALSE));
						current = "";
						tag = TokenTag.NONE;
					}	
					else if ( current.equalsIgnoreCase("ENDIF")){
						tokens.add(new Token(current, TokenTag.ENDIF));
						current = "";
						tag = TokenTag.NONE;
					}
					else if ( current.equalsIgnoreCase("RUN")){
						tokens.add(new Token(current, TokenTag.OVER));
						current = "";
						tag = TokenTag.NONE;
					}
					else if ( current.equalsIgnoreCase("ELSE")){
						tokens.add(new Token(current, TokenTag.ELSE));
						current = "";
						tag = TokenTag.NONE;
					}
					else
					{
						// this is for if we're reading just a variable
						char tmpc = data.charAt(++i);
						if ( Character.isLetter(tmpc) ||
							 Character.isDigit(tmpc)){
							i--;
							continue;
						}
					
						// if it's a space or something else, create the variable
						tokens.add(new Token(current, TokenTag.VARIABLE));
						current = "";
						tag = TokenTag.NONE;
						i--;
					}
					break;

				case STRING:
					// get everything up to the ending "
					try{
						while ( (c = data.charAt(++i)) != '"') 
							current += c ;
						// tokenize
						tokens.add(new Token(current, TokenTag.STRING));
						current = "";
						tag = TokenTag.NONE;	
					}
					catch (Exception e){
						handlerError(STRINGNOTEND);
					}
					break;
			}
		}
	}

	// util: prints stuff
	private void printer()
	{
		System.out.println ( "\t**FILE TOKENS: " );
		for ( Token s : tokens ){
			System.out.println ( s.getValue().toString() + " : " + s.getTag() );
		}
		System.out.println("========FILE TOKENS END==========");
	}
	
	private void handlerError(int error) throws InterpreterException{
		String[] err = {
			" REDUNDANT ===, CAN'T DISTINGUISH '==='",
			" YOU DIDN'T END A STRING"
		};
		
		throw new InterpreterException(err[error]);
	}
}
