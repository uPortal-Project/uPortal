package org.jasig.portal.utils;

import java.sql.Connection;
import java.sql.SQLException;

import org.jasig.portal.RDBMServices;
import org.jasig.portal.services.LogService;

/**
 * This type is a place to centralize the portal's sql transaction code.
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class SqlTransaction {
/**
 * SqlTransaction constructor comment.
 */
public SqlTransaction() {
	super();
}
/**
 * @param conn java.sql.Connection
 * @exception java.sql.SQLException
 */
public static void begin(Connection conn) throws java.sql.SQLException
{
	try
	{
          RDBMServices.setAutoCommit(conn, false);
	}
	catch (SQLException sqle)
	{
		LogService.log(LogService.ERROR, sqle);
		throw sqle;
	}
}
/**
 * @param conn java.sql.Connection
 * @exception java.sql.SQLException
 */
public static void commit(Connection conn) throws java.sql.SQLException
{
	try
	{
			RDBMServices.commit(conn);
			RDBMServices.setAutoCommit(conn, true);

	}
	catch (SQLException sqle)
	{
		LogService.log(LogService.ERROR, sqle);
		throw sqle;
	}
}
/**
 *
 */
protected static void logNoTransactionWarning()
{
	String msg = "You are running the portal on a database that does not support transactions.  " +
				 "This is not a supported production environment for uPortal.  " +
				 "Sooner or later, your database will become corrupt.";
	LogService.log(LogService.WARN, msg);
}
/**
 * @param conn java.sql.Connection
 * @exception java.sql.SQLException
 */
public static void rollback(Connection conn) throws java.sql.SQLException
{
	try
	{
			RDBMServices.rollback(conn);
			RDBMServices.setAutoCommit(conn, true);
	}
	catch (SQLException sqle)
	{
		LogService.log(LogService.ERROR, sqle);
		throw sqle;
	}
}
/**
 * @param conn java.sql.Connection
 * @param newValue boolean
 * @exception java.sql.SQLException
 */
public static void setAutoCommit(Connection conn, boolean newValue) throws java.sql.SQLException
{
	try
	{
          RDBMServices.setAutoCommit(conn, newValue);
	}
	catch (SQLException sqle)
	{
		LogService.log(LogService.ERROR, sqle);
		throw sqle;
	}
}
}
