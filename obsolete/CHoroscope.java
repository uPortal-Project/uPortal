package com.ibs.portal.channels;

import com.ibs.Framework.*;
import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import com.ibs.portal.layout.*;

/**
 * This is a user-defined channel for displaying horoscopes.
 * 
 * @author Ken Weiner
 */
public class CHoroscope extends com.ibs.Framework.servlet.GenericJSPBean 
                        implements com.ibs.portal.IChannel
{ 
  public String getName () {return "Horoscope";}
  public boolean isMinimizable () {return true;}
  public boolean isDetachable () {return false;}
  public boolean isRemovable () {return true;}
  public boolean isEditable () {return true;}
  
  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {
      linkTo (out, "Aquarius", "http://yahoo.com");
      linkTo (out, "Cancer", "http://datek.com");
    }
    catch (Exception e)
    {
      log (DEBUG, e);
      System.out.println ("\nERROR: \n" + e);
    }
  }
  
  public void edit (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
  }
  
  private void linkTo (JspWriter out, String sText, String sHref)
  {
    try
    {
      out.print ("<a href = \"" + sHref + "\">" + sText + "</a><br>");
    }
    catch (Exception e)
    {
    }
  }
  
}