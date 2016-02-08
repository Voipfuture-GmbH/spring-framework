package com.voipfuture.springtest.jdbc;

public class Scanner {

	private String text;
	
	private int index;
	
	public Scanner(String text) {
		this.text = text;
	}
	
	public char next() {
		return text.charAt(index++);
	}
	
	public char peek() {
		return text.charAt(index);
	}	
	
	public boolean eof() {
		return index >= text.length();
	}	
	
	public int offset() {
		return index;
	}
}
