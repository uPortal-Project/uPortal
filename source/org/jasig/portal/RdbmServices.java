package org.jasig.portal;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.net.*;
import com.objectspace.xml.*;
import org.jasig.portal.layout.*;

/**
 * Provides database access
 * @author Ken Weiner
 * @version %I%, %G%
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
        File jdbcPropsFile = new File (getPortalBaseDir () + "properties\\rdbm.properties");
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
      e.printStackTrace ();
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
			e.printStackTrace();
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
			e.printStackTrace();
		}
	}
}