package org.jasig.portal;

import java.sql.*;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.SqlTransaction;
/**
 * @author: Dan Ellentuck
 * @version $Revision$
 */
public class ReferenceSequenceGenerator implements ISequenceGenerator {

	// Constant strings for SEQUENCE table:
	private static String SEQUENCE_TABLE = "UP_SEQUENCE";
	private static String NAME_COLUMN = "SEQUENCE_NAME";
	private static String VALUE_COLUMN = "SEQUENCE_VALUE";

	// SQL strings for SEQUENCE crud:
	private static String DEFAULT_COUNTER_NAME = "DEFAULT";
	private static String selectSequenceSql;
	private static String updateSequenceSql;
	private static String updateSequenceWhereSql;
/**
 * ReferenceOIDGenerator constructor comment.
 */
public ReferenceSequenceGenerator() {
	super();
}
/**
 * @param tableName java.lang.String
 * @exception java.lang.Exception
 */
public synchronized void createCounter (String tableName) throws Exception
{
	Connection con = null;
	Statement stmt = null;

	try
	{
		con = RDBMServices.getConnection();
		stmt = con.createStatement();
		String sInsert = getCreateCounterSql(tableName);
		stmt.executeUpdate(sInsert);
	}
	catch ( SQLException sqle )
	{
		LogService.instance().log
			(LogService.ERROR, "ReferenceSequenceGenerator::createCounter(): " + sqle.getMessage());
		throw sqle;
	}
	finally
	{
		if ( stmt != null )
			{ stmt.close(); }
		RDBMServices.releaseConnection(con);
	}
}
/**
 * @param table java.lang.String
 * @return java.lang.String
 */
private String getCreateCounterSql(String table)
{
	StringBuffer buff = new StringBuffer(100);
	buff.append("INSERT INTO ");
	buff.append(SEQUENCE_TABLE);
	buff.append(" (");
	buff.append(NAME_COLUMN);
	buff.append(", ");
	buff.append(VALUE_COLUMN);
	buff.append(") VALUES (");
	buff.append("'");
	buff.append(table);
	buff.append("'");
	buff.append(",");
	buff.append("0");
	buff.append(")");
	return buff.toString();
}
/**
 * @return java.lang.String
 * @exception java.lang.Exception
 */
public String getNext() throws Exception {
	return getNext(DEFAULT_COUNTER_NAME);
}
/**
 * @param table String
 * @return java.lang.String
 * @exception java.lang.Exception
 */
public String getNext(String table) throws Exception {
	return getNextInt(table) + "";
}
/**
 * @return int
 * @exception java.lang.Exception
 */
public int getNextInt() throws Exception {
	return getNextInt(DEFAULT_COUNTER_NAME);
}
/**
 * @return int
 * @param tableName java.lang.String
 * @exception java.lang.Exception
 */
public synchronized int getNextInt(String tableName) throws Exception
{
	int id = 1;
	String sQuery = getSelectSequenceSql(tableName);
	Connection con = null;
	Statement stmt = null;
	ResultSet rs = null;

	try
	{
		con = RDBMServices.getConnection();
		stmt = con.createStatement();

		SqlTransaction.begin(con);

		rs = stmt.executeQuery(sQuery);

		if ( rs.next() )
			{ id = rs.getInt(VALUE_COLUMN) + 1; }
		else
			{ createCounter(tableName); }
		String sInsert = getUpdateSequenceSql(id, tableName);
		stmt.executeUpdate(sInsert);

		SqlTransaction.commit(con);

		return id;
	}

	catch ( SQLException sqle )
	{
		SqlTransaction.rollback(con);
		LogService.instance().log
			(LogService.ERROR, "ReferenceSequenceGenerator::incrementCounter(): " + sqle.getMessage());
		throw sqle;
	}

	finally {
		if ( rs != null )
			{ rs.close(); }
		if ( stmt != null )
			{ stmt.close(); }
		RDBMServices.releaseConnection(con);
    }
}
/**
 * @return java.lang.String
 */
private String getSelectSequenceSql()
{
	if ( selectSequenceSql == null )
	{
		StringBuffer buff = new StringBuffer(100);
		buff.append("SELECT ");
		buff.append(VALUE_COLUMN);
		buff.append(" FROM ");
		buff.append(SEQUENCE_TABLE);
		buff.append(" WHERE ");
		buff.append(NAME_COLUMN);
		buff.append(" = ");
		selectSequenceSql = buff.toString();
	}
	return selectSequenceSql;
}
/**
 * @param table java.lang.String
 * @return java.lang.String
 */
private String getSelectSequenceSql(String table)
{
	StringBuffer buff = new StringBuffer(getSelectSequenceSql());
	buff.append("'");
	buff.append(table);
	buff.append("'");
	return buff.toString();
}
/**
 * @return java.lang.String
 */
private String getUpdateSequenceSql()
{
	if ( updateSequenceSql == null )
	{
		StringBuffer buff = new StringBuffer(100);
		buff.append("UPDATE ");
		buff.append(SEQUENCE_TABLE);
		buff.append(" SET ");
		buff.append(VALUE_COLUMN);
		buff.append(" = ");
		updateSequenceSql = buff.toString();
	}
	return updateSequenceSql;
}
/**
 * @return java.lang.String
 */
private String getUpdateSequenceSql(int id, String table)
{
	StringBuffer buff = new StringBuffer(getUpdateSequenceSql());
	buff.append(id);
	buff.append(getUpdateSequenceWhereSql());
	buff.append("'");
	buff.append(table);
	buff.append("'");
	return buff.toString();
}
/**
 * @return java.lang.String
 */
private String getUpdateSequenceWhereSql()
{
	if ( updateSequenceWhereSql == null )
	{
		StringBuffer buff = new StringBuffer(100);
		buff.append(" WHERE ");
		buff.append(NAME_COLUMN);
		buff.append(" = ");
		updateSequenceWhereSql = buff.toString();
	}
	return updateSequenceWhereSql;
}
/**
 * @param tableName java.lang.String
 * @param newValue int
 * @exception java.lang.Exception
 */
public synchronized void setCounter (String tableName, int value) throws Exception
{

	Connection con = null;
	Statement stmt = null;
	String sUpdate = getUpdateSequenceSql(value, tableName);

	try
	{
		con = RDBMServices.getConnection();
		stmt = con.createStatement();
		stmt.executeUpdate(sUpdate);
	}

	catch ( SQLException sqle )
	{
		LogService.instance().log
			(LogService.ERROR, "ReferenceSequenceGenerator::setCounter(): " + sqle.getMessage());
		throw sqle;
	}

    finally
    {
	    if ( stmt != null )
	    	{ stmt.close(); }
	    RDBMServices.releaseConnection(con);
    }
}
}
