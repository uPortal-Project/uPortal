package org.jasig.portal.channels;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import org.jasig.portal.layout.*;

import java.net.*;


/**
 * Weather.
 * 
 * @author Ken Weiner
 */
public class CWeather implements org.jasig.portal.IChannel 
{  
  private Hashtable params = null;
  
  public void initParams (Hashtable params) {this.params = params;}
  public String getName () {return "Weather";}
  public boolean isMinimizable () {return true;}
  public boolean isDetachable () {return true;}
  public boolean isRemovable () {return true;}
  public boolean isEditable () {return false;}  
  public boolean hasHelp () {return false;}  
  
  public int getDefaultDetachWidth () {return 250;}
  public int getDefaultDetachHeight () {return 250;}
  
  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {
      // Change station parameter to reflect local area.  
      // Station guide at http://www.abcnews.go.com/local/
      String sState = (String) params.get ("state");
      String sCity = ((String) params.get ("city")).replace (' ', '_');
      
      out.println ("<center>");
      out.println ("<a href=\"http://www.wunderground.com/US/" + sState + "/" + sCity + ".html\">");
      out.println ("<img src=\"http://banners.wunderground.com/banner/infoboxtr/US/" + sState + "/" + sCity + ".gif\" alt=\"Click for " + sCity.replace ('_', ' ') + ", " + sState + " Forecast\" height=108 width=144 border=0></a>");
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
  
  public void save (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    // This channel is not editable
  }
}