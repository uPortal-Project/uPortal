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
public class CMyApplications implements org.jasig.portal.IChannel                             
{  
  public void initParams (Hashtable params) {};
  public String getName () {return "My Applications";}
  public boolean isMinimizable () {return false;}
  public boolean isDetachable () {return false;}
  public boolean isRemovable () {return false;}
  public boolean isEditable () {return false;}  
  
  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {
      // Change station parameter to reflect local area.  
      // Station guide at http://www.abcnews.go.com/local/
      out.println ("<table align=center border=0 width=100%>");
      out.println ("<a href=\"\">");
      out.println ("<tr><td><a href=\"\"><font face=Arial>Student Affairs</font></a></td></tr>");
      out.println ("<tr><td><a href=\"\"><font face=Arial>Parking</font></a></td></tr>");
      out.println ("</table>");
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