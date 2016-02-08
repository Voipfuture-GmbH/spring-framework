package com.voipfuture.springtest.jdbc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SQLStatementSplitter 
{
	private final StringBuilder buffer = new StringBuilder();
	private final List<String> statements = new ArrayList<String>();
	private Lexer lexer;

	public List<String> parse(String input) throws ParseException
	{
		this.lexer = new Lexer( new Scanner( input  ) );
		while ( ! lexer.eof() ) {
			parseStatement();
		}
		return statements;
	}
	
	private boolean peek(Token.TokenType t) {
		Token tok = lexer.peek();
		return tok.is(t);
	}	

	private Token peek() {
		Token tok = lexer.peek();
		return tok;
	}

	private Token next() {
		Token tok = lexer.next();
		return tok;
	}

	private boolean skipString() 
	{
		if ( peek(Token.TokenType.SINGLE_QUOTE ) ) 
		{
			appendToBuffer( next() );
			while ( ! lexer.eof() ) 
			{
				final Token tok = next();
				appendToBuffer( tok );
				if ( tok.is( Token.TokenType.SINGLE_QUOTE ) ) 
				{
					break;
				}
			}
			return true;
		}
		return false;
	}

	private void parseStatement() 
	{
		buffer.setLength(0);		

		while ( ! lexer.eof() ) 
		{
			if ( skipString() || skipSingleLineComment() || skipMultiLineComment() ) {
				continue;
			} 

			if ( parseDollarQuotedString() ) 
			{
				// advance to next semicolon
				while ( ! lexer.eof() && ! peek(Token.TokenType.SEMICOLON) ) 
				{
					appendToBuffer( next() );
				}
				parseBuffer();
				continue;
			}

			final Token tok = next();
			if ( tok.is(Token.TokenType.SEMICOLON ) ) 
			{
				parseBuffer();	
				continue;
			}
			appendToBuffer(tok);
		}

		parseBuffer();
	}

	private boolean parseDollarQuotedString() 
	{
		final Token tok = peek();
		if ( tok.is(Token.TokenType.DOLLAR) ) 
		{
			final Stack<List<Token>> quoteStack = new Stack<List<Token>>();

			next(); // consume leading '$'

			// gather tag tokens
			final List<Token> startTag = new ArrayList<Token>();
			while ( ! lexer.eof() && ! peek(Token.TokenType.DOLLAR ) && ! peek().isEOL() ) {
				startTag.add( next() );
			}
			if ( lexer.eof() || peek().isEOL() ) {
				throw new ParseException("Unterminated dollar-quoted string starts here ",tok.offset);
			}
			quoteStack.push( startTag );

			appendToBuffer( tok );
			for ( Token t : startTag ) {
				appendToBuffer( t );
			}
			appendToBuffer( next() ); // consume trailing '$'


			while ( ! lexer.eof() && ! quoteStack.isEmpty() ) 
			{
				if ( peek().is( Token.TokenType.DOLLAR ) ) 
				{
					final List<Token> currentTag = new ArrayList<Token>();
					final Token tok2 = next();
					while ( ! lexer.eof() && ! peek(Token.TokenType.DOLLAR ) && ! peek().isEOL() ) {
						currentTag.add( next() );
					}			
					if ( lexer.eof() || peek().isEOL() ) {
						throw new ParseException("Unterminated dollar-quoted string starts here ",tok2.offset);							
					}
					appendToBuffer( tok2 );

					for ( Token t : currentTag ) {
						appendToBuffer(t);
					}
					appendToBuffer( next() ); // trailing '$'

					if ( Token.hasSameContent( quoteStack.peek() , currentTag ) ) 
					{
						quoteStack.pop();
					} else { // new depth level
						quoteStack.push( currentTag );
					}
				}
				else 
				{
					appendToBuffer( next() );
				}
			}
			if ( ! quoteStack.isEmpty() ) 
			{
				StringBuilder expected = new StringBuilder("$");
				for ( Token t : quoteStack.peek() ) {
					expected.append( t.value );
				}
				expected.append("$");
				throw new ParseException("Mismatched dollar-quoted string, still looking for "+expected,lexer.offset());
			}
			return true;
		}
		return false;
	}

	private void appendToBuffer(Token tok) {
		if ( tok.whitespace != null ) {
			buffer.append( tok.whitespace );
		}
		buffer.append(tok.value);
	}

	private void parseBuffer() 
	{
		if ( buffer.length() > 0 ) 
		{			
			final String trimmed = buffer.toString().trim();
			if ( trimmed.length() > 0 ) 
			{
				statements.add( trimmed.trim() );
			}
			buffer.setLength(0);
		}		
	}

	private boolean skipMultiLineComment() 
	{
		Token tok = next();		
		if ( tok.is(Token.TokenType.SLASH ) && peek(Token.TokenType.STAR ) ) 
		{
			next();
			// strip multi-line comment
			while( true ) {
				tok = next();
				if ( tok.isEOF() ) 
				{
					throw new ParseException("Unterminated multi-line comment",lexer.offset());
				}
				if ( tok.is(Token.TokenType.STAR) && peek(Token.TokenType.SLASH) ) 
				{
					next();
					break;
				}
			}
			return true;
		}
		lexer.pushBack( tok );
		return false;
	}

	private boolean skipSingleLineComment() 
	{
		Token tok = next();

		if ( tok.is(Token.TokenType.DASH ) && peek(Token.TokenType.DASH ) ) {
			next();
			while ( true ) {
				tok = peek();
				if ( tok.isEOFOrEOL() ) {
					break;
				}
				next();
			}
			return true;
		} 
		lexer.pushBack( tok );
		return false;
	}

	public static String parseFile(File file) throws IOException, FileNotFoundException {
		return parseFile( new FileReader(file) );
	}

	public static String parseFile(Reader inputReader) throws IOException, FileNotFoundException 
	{
		final StringBuilder buffer = new StringBuilder();
		String line;
		BufferedReader reader = null;
		try 
		{
			reader = new BufferedReader( inputReader );
			while ( ( line = reader.readLine()) != null ) {
				buffer.append( line ).append("\n");
			}
		} finally {
			try { inputReader.close(); } catch(Exception e2) { /* nop */ }
		}
		return buffer.toString();
	}	
}
