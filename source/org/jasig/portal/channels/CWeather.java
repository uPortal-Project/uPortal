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
 * Weather.
 * 
 * @author Ken Weiner
 * @version $Revision$
 */
public class CWeather implements org.jasig.portal.IChannel 
{  
  private ChannelConfig chConfig = null;
  private static Vector params = null;

  public void init (ChannelConfig chConfig) {this.chConfig = chConfig;}
  public String getName () {return "Weather";}
  public boolean isMinimizable () {return true;}
  public boolean isDetachable () {return true;}
  public boolean isRemovable () {return true;}
  public boolean isEditable () {return false;}  
  public boolean hasHelp () {return false;}  
  
  public int getDefaultDetachWidth () {return 250;}
  public int getDefaultDetachHeight () {return 250;}

  public Vector getParameters()
  {
    return params;
  }
  
  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {
      // Change station parameter to reflect local area.  
      // Station guide at http://www.abcnews.go.com/local/
      String sState = (String) chConfig.get ("state");
      String sCity = ((String) chConfig.get ("city")).replace (' ', '_');
      
      out.println ("<center>");
      out.println ("<a href=\"http://www.wunderground.com/US/" + sState + "/" + sCity + ".html\">");
      out.println ("<img src=\"http://banners.wunderground.com/banner/infoboxtr/US/" + sState + "/" + sCity + ".gif\" alt=\"Click for " + sCity.replace ('_', ' ') + ", " + sState + " Forecast\" height=108 width=144 border=0></a>");
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
  
  public void save (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    // This channel is not editable
  }
  
  public void help (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    // This channel has no help
  }
}