package com.ibs.portal.channels;

import com.ibs.Framework.*;
import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import com.ibs.portal.layout.*;

import java.net.*;


/**
 * ABC News.
 * 
 * @author Ken Weiner
 */
public class CABCNews extends com.ibs.Framework.servlet.GenericJSPBean 
                      implements com.ibs.portal.IChannel
{  
  public String getName () {return "ABC News";}
  public boolean isMinimizable () {return true;}
  public boolean isDetachable () {return true;}
  public boolean isRemovable () {return true;}
  public boolean isEditable () {return false;}  
  
  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {
      // Change station parameter to reflect local area.  
      // Station guide at http://www.abcnews.go.com/local/
      out.println ("<center>");
      out.println ("<applet CODE=\"starwave.news.affiliate.Megaticker.class\" CODEBASE=\"http://webapp.abcnews.com/java/\" WIDTH=141 HEIGHT=409 ALIGN=top border=0 archive=\"MegaTicker.jar\">");
  	  out.println ("<param name=\"cabbase\" value=\"MegaTicker.cab\">");
      out.println ("<param name=\"station\" value=\"KABC\"><a href=\"http://www.abcnews.com\"><img SRC=\"images/botleft.jpg\" border=0 WIDTH=142 HEIGHT=233 align=top></a>");
      out.println ("</applet>");
      out.println ("</center>");
    }
    catch (Exception e)
    {
      log (DEBUG, e);
      System.out.println ("\nERROR: \n" + e);
    }
  }
  public void edit (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    // This channel is not editable
  }
}