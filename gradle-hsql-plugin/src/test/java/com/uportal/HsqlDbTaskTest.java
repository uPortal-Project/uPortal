package com.uportal;

import org.hsqldb.persist.HsqlProperties;
import org.junit.Before;
import org.junit.Test;

public class HsqlDbTaskTest {
	HsqlDb db;
	@Before
	public void setup(){
		db = new HsqlDb();
	}
	@Test
	public void testStartDb() {

		System.out.println("Starting Database");
		HsqlProperties p = new HsqlProperties();
		p.setProperty("server.database.0", "file:./uPortal");
		p.setProperty("server.dbname.0", "uPortal");
		p.setProperty("server.port", "8887");
		db.startDb(p);
	}

	@Test
	public void testStopdb() {
		System.out.println("Starting Database");
		HsqlProperties p = new HsqlProperties();
		p.setProperty("server.database.0", "file:./uPortal");
		p.setProperty("server.dbname.0", "uPortal");
		p.setProperty("server.port", "8887");
		db.startDb(p);
		db.stopDb(p);
	}

}
