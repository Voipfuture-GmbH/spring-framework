package com.voipfuture.springtest.jdbc;

import java.util.ArrayList;
import java.util.List;

public class Lexer 
{
	private final List<Token> tokens = new ArrayList<Token>();
	
	private final StringBuilder buffer = new StringBuilder();
	private final Scanner scanner;
	
	private String whitespace;
	
	public Lexer(Scanner s) {
		this.scanner = s;
	}
	
	public boolean eof() 
	{
		parse();
		return tokens.get(0).isEOF();
	}
	
	public int offset() {
		parse();
		return tokens.get(0).offset;
	}
	
	public Token peek() {
		parse();
		return tokens.get(0);
	}
	
	public void pushBack(Token tok) {
		this.tokens.add(0,tok);
	}
	
	public Token next() {
		parse();
		final Token result= tokens.remove(0);
		return result;
	}	
	
	private void token(Token.TokenType type, String value, int offset) {
		this.tokens.add(new Token(type,value,offset,whitespace));
		whitespace = null;
	}
	
	private static boolean isWhitespace(char c) {
		return c == ' ' || c == '\t';
	}
	
	private void parse() 
	{
		whitespace = null;
		
		if ( ! tokens.isEmpty() ) {
			return;
		}

		final int offset = scanner.offset();
		buffer.setLength( 0 );
		
		while ( ! scanner.eof() && isWhitespace( scanner.peek() ) ) 
		{
			char c = scanner.next();
			buffer.append( c );
		}
		
		if ( buffer.length() > 0 ) 
		{
			whitespace = buffer.toString();
			buffer.setLength( 0 );			
		}
		
		if ( scanner.eof() ) 
		{
			token( Token.TokenType.EOF , "" , scanner.offset() );
			return;
		}		
		
		while ( ! scanner.eof() ) 
		{
			char c = scanner.peek();
			if ( isWhitespace( c ) ) 
			{
				break;
			}
			c = scanner.next();
			switch( c ) 
			{
				case 13:
					if ( ! scanner.eof() && scanner.peek() == '\n' ) {
						parseBuffer(offset);
						token( Token.TokenType.EOL , "\r\n" , scanner.offset()-1 );
						return;
					}
					break;
				case 10:
					parseBuffer(offset);
					token( Token.TokenType.EOL , "\n" , scanner.offset()-1 );
					return;			
				case '$':
					parseBuffer(offset);
					token( Token.TokenType.DOLLAR , "$" , scanner.offset()-1 );
					return;			
				case ';':
					parseBuffer(offset);
					token( Token.TokenType.SEMICOLON, ";" , scanner.offset()-1 );
					return;				
				case '\'':
					parseBuffer(offset);
					token( Token.TokenType.SINGLE_QUOTE, "'" , scanner.offset()-1 );
					return;					
				case '/':
					parseBuffer(offset);
					token( Token.TokenType.SLASH , "/" , scanner.offset()-1 );
					return;
				case '*':
					parseBuffer(offset);
					token( Token.TokenType.STAR, "*" , scanner.offset()-1 );
					return;		
				case '-':
					parseBuffer(offset);
					token( Token.TokenType.DASH, "-" , scanner.offset()-1 );
					return;						
			}
			buffer.append( c );
		}
		
		parseBuffer(offset);
		
		if ( scanner.eof() ) 
		{
			token( Token.TokenType.EOF , "" , scanner.offset() );
			return;
		}		
	}
	
	private void parseBuffer(int offset) 
	{
		if ( buffer.length() > 0 ) {
			token(Token.TokenType.TEXT , buffer.toString() , offset );
		}
	}
}
