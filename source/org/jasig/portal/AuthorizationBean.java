package org.jasig.portal;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import com.objectspace.xml.*;
import org.jasig.portal.layout.*;

/**
 * @author Ken Weiner
 */
public class AuthorizationBean extends GenericPortalBean
                               implements IAuthorizationBean
{
  public boolean authorize (String sUserName, String sPassword)
  {    
    RdbmServices rdbmService = new RdbmServices ();
    Connection con = null;
    boolean bAuthorized = false;
    
    try 
    {
      // Validate username and password here.  Sample:
      con = rdbmService.getConnection ();
      Statement stmt = con.createStatement();
      
      String sQuery = "SELECT PASSWORD FROM PORTAL_USERS WHERE USER_NAME='" + sUserName + "'";
      ResultSet rs = stmt.executeQuery (sQuery);
      
      if (rs.next ())
      {
        if (rs.getString ("PASSWORD").equals (sPassword))
        {
          bAuthorized = true;
        }
        else
        {
          // Password was not right  
        }
      }
      else
      {
        // User name was not found
      }
      
      stmt.close ();
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    finally
    {
      rdbmService.releaseConnection (con);
    }
    return bAuthorized;
  }      
}