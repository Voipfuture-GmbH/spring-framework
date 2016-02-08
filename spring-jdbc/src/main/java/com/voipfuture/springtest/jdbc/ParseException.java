package com.voipfuture.springtest.jdbc;

public class ParseException extends RuntimeException {

    public static final long serialVersionUID = 123; 

	public final int offset;

	public ParseException(String message, Throwable cause,int offset) {
		super(message+" @ offset "+offset, cause);
		this.offset = offset;
	}

	public ParseException(String message,int offset) {
		super(message+" @ offset "+offset);
		this.offset = offset;
	}
}