package org.jasig.portal;

import javax.servlet.http.*;
import java.text.*;

/**
 * Provides methods useful for the portal.  Later on, it may be necessary
 * to create an org.jasig.portal.util package
 * and several utilities classes.
 * @author Ken Weiner
 * @version $Revision$
 */
public class UtilitiesBean extends GenericPortalBean
{  
  /**
   * Prevents an html page from being cached by the browser
   * @param the servlet response object
   */
  public static void preventPageCaching (HttpServletResponse res)
  {    
    try 
    {    
      res.setHeader("pragma", "no-cache");
      res.setHeader( "Cache-Control","no-cache" );
      res.setHeader( "Cache-Control","no-store" );
      res.setDateHeader( "Expires", 0 );    
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }    
  } 
  
  /**
   * Gets the current date/time
   * @return a formatted date and time string
   */
  public static String getDate ()
  {    
    try 
    {         
      // Format the current time.
      SimpleDateFormat formatter = new SimpleDateFormat ("EEEE, MMM d, yyyy 'at' hh:mm a");
      java.util.Date currentTime = new java.util.Date();
      return formatter.format(currentTime);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    
    return "&nbsp;";
  }
}