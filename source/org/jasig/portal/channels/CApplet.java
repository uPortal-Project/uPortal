package org.jasig.portal.channels;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import org.jasig.portal.*;
import org.jasig.portal.layout.*;

import java.net.*;


/**
 * Displays and applet. You have to pass in the appropriate parameters.
 * Applet parameters are currently passed in as a concatenated string with
 * format: name1>value1^name2>value2^name3>value3 ...
 * It is possible that '>' and '^' are characters that may actually appear
 * in applet parameters.  If this is true, this method will have to be
 * rewritten.
 * 
 * @author Ken Weiner
 * @version $Revision$
 */
public class CApplet implements org.jasig.portal.IChannel                   
{
  private Hashtable chConfig = null;
  
  public void init (ChannelConfig chConfig) {this.chConfig = chConfig;}
  public String getName () {return (String) chConfig.get ("name");}
  public boolean isMinimizable () {return true;}
  public boolean isDetachable () {return true;}
  public boolean isRemovable () {return true;}
  public boolean isEditable () {return false;}  
  public boolean hasHelp () {return false;}  
  
  public int getDefaultDetachWidth () {return 250;}
  public int getDefaultDetachHeight () {return 350;}
  
  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {
      // Change station parameter to reflect local area.  
      // Station guide at http://www.abcnews.go.com/local/
      out.println ("<center>");
      out.println ("<applet CODE=\"" + chConfig.get ("code") + "\" CODEBASE=\"" + chConfig.get ("codeBase") + "\" WIDTH=\"" + chConfig.get ("width") + "\" HEIGHT=\"" + chConfig.get ("height") + "\" ALIGN=top border=0 archive=\"" + chConfig.get ("archive") + "\">");
  	  
  	  // Split "params" string into name-value pairs
  	  String sParams = (String) chConfig.get ("params");
  	  StringTokenizer stParams = new StringTokenizer (sParams, "^");
  	   
  	  // For each "param" string, parse to get name and value
  	  String sParamName, sParamValue;
  	  
  	  while (stParams.hasMoreTokens()) 
  	  {
  	    sParamName="";
  	    sParamValue="";
  	    StringTokenizer stNameValue = new StringTokenizer (stParams.nextToken (), ">");
  	    
  	    if (stNameValue.hasMoreTokens ())
  	      sParamName = stNameValue.nextToken ();
  	    
  	    if (stNameValue.hasMoreTokens ())
  	      sParamValue = stNameValue.nextToken ();
        
        out.println ("<param name=\"" + sParamName + "\" value=\"" + sParamValue + "\">");
      }

      out.println ("</applet>");
      out.println ("</center>");
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }
  public void edit (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    // This channel is not editable
  }
  
  public void help (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    // This channel has no help
  }
}