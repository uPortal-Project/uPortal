package com.uportal;

public class HsqlPluginExtention {
	private String database;
	private String dbName;
	private String port;
	public HsqlPluginExtention(){
		
		database= "file:./uPortal;hsqldb.tx=mvcc";
		dbName = "uPortal";
		port= "8887";
	}

	@Override
	public String toString() {
		return String.format("HsqlPluginExtention [database=%s, dbName=%s, port=%s]", database, dbName, port);
	}

	public String getDatabase() {
		return database;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}

}
