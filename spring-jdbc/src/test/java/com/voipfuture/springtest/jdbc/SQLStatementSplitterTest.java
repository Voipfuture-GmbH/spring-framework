package com.voipfuture.springtest.jdbc;

import java.util.List;

import junit.framework.TestCase;

public class SQLStatementSplitterTest extends TestCase
{
	public void testNonEmptyTagName() {
		final String sql ="CREATE OR REPLACE FUNCTION assertdbschemaversion(expectedversion character varying) RETURNS void AS\n" + 
				"$BODY$\n" + 
				"DECLARE\n" + 
				"    currentVersion varchar(10);\n" + 
				"BEGIN\n" + 
				"    SELECT version FROM db_schema_version INTO currentVersion;\n" + 
				"    IF currentVersion <> expectedVersion THEN\n" + 
				"      RAISE EXCEPTION 'DB schema version mismatch, expected % but got %',expectedVersion,currentVersion;\n" + 
				"    END IF;\n" + 
				"END;\n" + 
				"$BODY$\n" + 
				"  LANGUAGE plpgsql VOLATILE\n" + 
				"  COST 100;";

		final List<String> stmts = parse(sql);
		for ( String s : stmts ) {
			System.out.println("GOT: >>"+s+"<<");
		}
		assertEquals(1 , stmts.size() );
		assertEquals( sql.substring( 0 , sql.length()-1 ) , stmts.get(0) );
	}
	
	public void testEmptyTagName() {
		final String sql ="CREATE OR REPLACE FUNCTION assertdbschemaversion(expectedversion character varying) RETURNS void AS\n" + 
				"$$\n" + 
				"DECLARE\n" + 
				"    currentVersion varchar(10);\n" + 
				"BEGIN\n" + 
				"    SELECT version FROM db_schema_version INTO currentVersion;\n" + 
				"    IF currentVersion <> expectedVersion THEN\n" + 
				"      RAISE EXCEPTION 'DB schema version mismatch, expected % but got %',expectedVersion,currentVersion;\n" + 
				"    END IF;\n" + 
				"END;\n" + 
				"$$\n" + 
				"  LANGUAGE plpgsql VOLATILE\n" + 
				"  COST 100;";

		final List<String> stmts = parse(sql);
		for ( String s : stmts ) {
			System.out.println("GOT: >>"+s+"<<");
		}
		assertEquals(1 , stmts.size() );
		assertEquals( sql.substring( 0 , sql.length()-1 ) , stmts.get(0) );
	}	
	
	public void testUnclosedDollarQuotes() 
	{
		final String sql ="CREATE OR REPLACE FUNCTION assertdbschemaversion(expectedversion character varying) RETURNS void AS\n" + 
				"$$\n" + 
				"DECLARE\n" + 
				"    currentVersion varchar(10);\n" + 
				"BEGIN\n" + 
				"    SELECT version FROM db_schema_version INTO currentVersion;\n" + 
				"    IF currentVersion <> expectedVersion THEN\n" + 
				"      RAISE EXCEPTION 'DB schema version mismatch, expected % but got %',expectedVersion,currentVersion;\n" + 
				"    END IF;\n" + 
				"END;"; 

		try {
			parse(sql);
			fail("Should've failed");
		} catch(ParseException e) {
			// ok
		}
	}	
	
	public void testNestedDollarQuotes() {
		final String sql ="CREATE OR REPLACE FUNCTION assertdbschemaversion(expectedversion character varying) RETURNS void AS\n" + 
				"$a$\n" + 
				"DECLARE\n" + 
				"    currentVersion varchar(10);\n" + 
				"BEGIN\n" + 
				"    $b$SELECT version FROM db_schema_version INTO currentVersion;\n" + 
				"    IF currentVersion <> expectedVersion THEN\n" + 
				"      RAISE EXCEPTION 'DB schema version mismatch, expected % but got %',expectedVersion,currentVersion;\n" + 
				"    END IF;\n" +
				"    $b$"+
				"END;\n" + 
				"$a$\n" + 
				"  LANGUAGE plpgsql VOLATILE\n" + 
				"  COST 100;";

		final List<String> stmts = parse(sql);
		for ( String s : stmts ) {
			System.out.println("GOT: >>"+s+"<<");
		}
		assertEquals(1 , stmts.size() );
		assertEquals( sql.substring( 0 , sql.length()-1 ) , stmts.get(0) );
	}	
	
	public void testMismatchedNestedDollarQuotes() {
		final String sql ="CREATE OR REPLACE FUNCTION assertdbschemaversion(expectedversion character varying) RETURNS void AS\n" + 
				"$a$\n" + 
				"DECLARE\n" + 
				"    currentVersion varchar(10);\n" + 
				"BEGIN\n" + 
				"    $b$SELECT version FROM db_schema_version INTO currentVersion;\n" + 
				"    IF currentVersion <> expectedVersion THEN\n" + 
				"      RAISE EXCEPTION 'DB schema version mismatch, expected % but got %',expectedVersion,currentVersion;\n" + 
				"    END IF;\n" +
				"    $c$"+
				"END;\n" + 
				"$a$\n" + 
				"  LANGUAGE plpgsql VOLATILE\n" + 
				"  COST 100;";

		try {
			parse(sql);
			fail("Should've failed");
		} catch(ParseException e) {
			e.printStackTrace( System.out );
			// ok
		}
	}		
	
	public void testTagInStringLiteral() {
		String sql = "INSERT INTO users (user_id, name, logon_name, password, email, locale, timezone, ui_theme)\n" + 
				"VALUES (1, 'voipfuture', 'voipfuture', '{BCRYPT}$2a$10$jm8s5qvPnFswKXiSY3fVYe7NfZBBZqhf42LPhHv.RTvgrwVvev9ui', 'systemtest@voipfuture.com', 'en_US', 'Europe/Berlin', 'vodafone');";
		
		final List<String> stmts = parse(sql);
		assertEquals(1 , stmts.size() );
		assertEquals( "INSERT INTO users (user_id, name, logon_name, password, email, locale, timezone, ui_theme)\n" + 
				"VALUES (1, 'voipfuture', 'voipfuture', '{BCRYPT}$2a$10$jm8s5qvPnFswKXiSY3fVYe7NfZBBZqhf42LPhHv.RTvgrwVvev9ui', 'systemtest@voipfuture.com', 'en_US', 'Europe/Berlin', 'vodafone')" ,stmts.get(0) );
	}
	
	private List<String> parse(String sql) {
		return new SQLStatementSplitter().parse( sql );
	}
}