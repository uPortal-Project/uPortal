package org.jasig.portal;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.sql.*;

/**
 * Provides database access
 * @author Ken Weiner
 * @version $Revision$
 */
public class RdbmServices extends GenericPortalBean
{
  private static boolean bPropsLoaded = false;
  private static String sJdbcDriver = null;
  private static String sJdbcUrl = null;
  private static String sJdbcUser = null;
  private static String sJdbcPassword = null;
      
  /**
   * Constructor which loades JDBC parameters from property file
   * upon first invocation
   */
  public RdbmServices ()
  {    
    try 
    {    
      if (!bPropsLoaded)
      {
        File jdbcPropsFile = new File (getPortalBaseDir () + "properties" + File.separator + "rdbm.properties");
        Properties jdbcProps = new Properties ();
        jdbcProps.load (new FileInputStream (jdbcPropsFile));
        
        sJdbcDriver = jdbcProps.getProperty ("jdbcDriver");
        sJdbcUrl = jdbcProps.getProperty ("jdbcUrl");
        sJdbcUser = jdbcProps.getProperty ("jdbcUser");
        sJdbcPassword = jdbcProps.getProperty ("jdbcPassword");
        
        bPropsLoaded = true;
      }
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }    
  }    
  
  /**
   * Gets a database connection
   * @return a database Connection object
   */
	public static Connection getConnection()
	{
		Connection conn = null;

		try 
		{
			Class.forName (sJdbcDriver);
			conn = DriverManager.getConnection (sJdbcUrl, sJdbcUser, sJdbcPassword);
		}
		catch ( Exception e )
		{
      Logger.log (Logger.ERROR, e);
		}

		return conn;
	}  
	
  /**
   * Releases database connection
   * @param a database Connection object
   */
	public static void releaseConnection (Connection con)
	{
		try
		{
      if (con != null)
		    con.close();
		}
		catch ( Exception e )
		{
      Logger.log (Logger.ERROR, e);
		}
	}
}