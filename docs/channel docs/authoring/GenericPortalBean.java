package org.jasig.portal;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import java.io.*;
import com.objectspace.xml.*;
import org.jasig.portal.layout.*;

/**
 * This is a base class for all the Portal beans to extend.
 * The base class functionality contains all of the reusable code.
 * 
 * @author Ken Weiner
 */
public class GenericPortalBean
{  
  private static String sPortalBaseDir = null;
  public boolean DEBUG = false;
  
  /**
   * Set the top level directory for the portal.  This makes it possible
   * to use relative paths in the application for loading properties files, etc.
   * @param sPathToPortal
   */
  public static void setPortalBaseDir (String sPathToPortal)
  {
    sPortalBaseDir = sPathToPortal;
  }
  
  /**
   * Get the top level directory for the portal.  This makes it possible
   * to use relative paths in the application for loading properties files, etc.
   */
  public static String getPortalBaseDir ()
  {
    return sPortalBaseDir;
  }

  /**
   * Just a simple debug method that prints
   * messages to System.out
   */
  public void debug(String message)
  {
    if(DEBUG) System.out.println("DEBUG: "+message);
  }
}

