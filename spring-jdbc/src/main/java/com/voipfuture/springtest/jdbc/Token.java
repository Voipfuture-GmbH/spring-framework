package com.voipfuture.springtest.jdbc;

import java.util.List;

public class Token 
{
	public enum TokenType 
	{
		TEXT, SLASH, 
		STAR, 
		DOLLAR, 
		DASH, 
		SEMICOLON,
		EOL, 
		EOF,
		SINGLE_QUOTE;
	}
	
	public final TokenType type;
	public final String value;
	public final int offset;
	public final String whitespace;
	
	public Token(TokenType type, String value, int offset,String whitespace) 
	{
		if ( type == null ) {
			throw new IllegalArgumentException("Type must not be NULL");
		}
		if ( value == null ) {
			throw new IllegalArgumentException("value must not be NULL");
		}	
		if ( offset < 0) {
			throw new IllegalArgumentException("offset must be >= 0");
		}		
		this.type = type;
		this.value = value;
		this.offset = offset;
		this.whitespace = whitespace;
	}
	
	public static boolean hasSameContent(List<Token> l1,List<Token> l2) 
	{
		if ( l1.size() != l2.size() ) {
			return false;
		}
		for ( int i = 0 , len = l1.size() ; i < len ; i++ ) {
			if ( ! l1.get(i).hasSameContent( l2.get(i) ) ) {
				return false;
			}
		}
		return true;
	}
	
	public boolean hasSameContent(Token other) 
	{
		return this.type == other.type && this.value.equals( other.value );
	}
	
	public boolean is(TokenType tt) {
		return tt.equals( this.type );
	}
	
	public boolean isEOL() {
		return is(TokenType.EOL);
	}
	
	public boolean isEOF() {
		return is(TokenType.EOF);
	}

	public boolean isEOFOrEOL() {
		return isEOL() || isEOF();
	}

	@Override
	public String toString() {
		return "Token [type=" + type + ", value=" + value + ", offset=" + offset + ", whitespace=" + whitespace + "]";
	}	
}
