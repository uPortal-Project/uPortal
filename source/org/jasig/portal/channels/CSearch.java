package org.jasig.portal.channels;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import org.jasig.portal.layout.*;

import java.net.*;


/**
 * All in one search from 1Blink.com.
 * 
 * @author Ken Weiner
 */
public class CSearch implements org.jasig.portal.IChannel                     
{  
  public void initParams (Hashtable params) {};
  public String getName () {return "Search";}
  public boolean isMinimizable () {return true;}
  public boolean isDetachable () {return true;}
  public boolean isRemovable () {return true;}
  public boolean isEditable () {return false;}  
  
  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {
      out.println ("<center>");
      out.println ("<br><table width=115 border=0 cellspacing=0 cellpadding=0>");
      out.println ("<tr><td>");
      out.println ("<form action=\"http://www.1blink.com/search.cgi\" method=GET name=\"\">");
      out.println ("<table width=115 border=0 cellspacing=0 cellpadding=0>");
      out.println ("<tr><td>");
      out.println ("<table cellspacing=0 cellpadding=0 border=0 width=115 align=center>");
      out.println ("<tr><td colspan=2>");
      out.println ("<img src=\"http://www.1blink.com/graphics/affiltop.gif\" width=115 height=90 alt=\"1Blink : We Search the Search Engines For You!\"></td>");
      out.println ("</tr>");
      out.println ("<tr><td bgcolor=#99FFCC align=center><img src=\"http://www.1blink.com/graphics/spacer.gif\" width=113 height=1></td>");
      out.println ("</tr>");
      out.println ("<tr><td bgcolor=#99FFCC align=center valign=bottom>");
      out.println ("<input type=TEXT name=\"q\" size=8>");
      out.println ("<input type=SUBMIT value=\"Go\" name=\"submit2\"></td></tr>");
      out.println ("<tr><td bgcolor=#99FFCC align=center>");
      out.println ("<img src=\"http://www.1blink.com/graphics/spacer.gif\" width=113 height=1></td></tr>");
      out.println ("<tr><td colspan=2><img src=\"http://www.1blink.com/graphics/affilbot.gif\" alt=\"MEGA SEARCH\" width=115 height=17 border=0></td></tr>");
      out.println ("</table></td></tr></table></form></td></tr></table>");
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