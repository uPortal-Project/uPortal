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
 * Displays an applet. To pass in paramaters to the applet, give the
 * channel parameters whose keys start with the string "APPLET."
 * For example, the key/value pair
 *    APPLET.data=foo
 * as a channel parameter is given to the applet as
 *    data=foo
 *
 * @author Ken Weiner
 * @author Shawn Bayern
 * @version $Revision$
 */
public class CApplet implements org.jasig.portal.IChannel
{
  private ChannelConfig chConfig = null;

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
	out.println ("<center>");
	out.println (
	    "<applet CODE=\"" + chConfig.get ("code")
	    + "\" CODEBASE=\"" + chConfig.get ("codeBase") 
	    + "\" WIDTH=\"" + chConfig.get ("width") 
	    + "\" HEIGHT=\"" + chConfig.get ("height") 
	    + "\" ALIGN=top border=0 archive=\"" + chConfig.get ("archive")
	    + "\">"
	    );
	
	// Take all parameters whose names start with "APPLET." and pass them
	// to the applet (after stripping "APPLET.")
	Enumeration allKeys = chConfig.keys();
	while (allKeys.hasMoreElements()) {
	    String p = (String) allKeys.nextElement();
	    if (p.startsWith("APPLET.")) {
		String name = p.substring(7);          // skip "APPLET."
		String value = (String) chConfig.get(p);
		out.println(
		    "   <param name=\"" + name + "\" value=\"" + value + "\">"
		    );
	    }
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
