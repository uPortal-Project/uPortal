package org.jasig.portal.channels;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import org.jasig.portal.layout.*;

import java.net.*;


/**
 * Displays and applet. You have to pass in the appropriate parameters.
 * 
 * @author Ken Weiner
 */
public class CApplet implements org.jasig.portal.IChannel                   
{
  private Hashtable params = null;
  
  public void initParams (Hashtable params) {this.params = params;}
  public String getName () {return (String) params.get ("name");}
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
      out.println ("<applet CODE=\"" + params.get ("code") + "\" CODEBASE=\"" + params.get ("codeBase") + "\" WIDTH=\"" + params.get ("width") + "\" HEIGHT=\"" + params.get ("height") + "\" ALIGN=top border=0 archive=\"" + params.get ("archive") + "\">");
  	  
  	  // Split "params" string into name-value pairs
  	  String sParams = (String) params.get ("params");
  	  StringTokenizer stParams = new StringTokenizer (sParams, "^");
  	   
  	  // For each "param" string, parse to get name and value 
  	  while (stParams.hasMoreTokens()) 
  	  {
  	    StringTokenizer stNameValue = new StringTokenizer (stParams.nextToken (), ">");
        out.println ("<param name=\"" + stNameValue.nextToken () + "\" value=\"" + stNameValue.nextToken () + "\">");
      }

      out.println ("</applet>");
      out.println ("</center>");
    }
    catch (Exception e)
    {
      System.out.println ("\nERROR: \n" + e);
    }
  }
  public void edit (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    // This channel is not editable
  }
}