package com.ibs.portal.channels;

import com.ibs.Framework.*;
import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import com.ibs.portal.layout.*;

import java.net.*;


/**
 * ESPN Scoreboard.
 * 
 * @author Ken Weiner
 */
public class CScoreboard extends com.ibs.Framework.servlet.GenericJSPBean 
                         implements com.ibs.portal.IChannel
{  
  public String getName () {return "Scoreboard";}
  public boolean isMinimizable () {return true;}
  public boolean isDetachable () {return false;}
  public boolean isRemovable () {return true;}
  public boolean isEditable () {return false;}  
  
  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {
      out.println ("<center>");
      out.println ("<APPLET CODEBASE=\"http://scores-espn.sportszone.com/java\" CODE=\"starwave.sportszone.scorepost.ScorePost.class\" vspace=1 WIDTH=128 HEIGHT=182 ARCHIVE=\"ScorePost.zip\">");
      out.println ("<PARAM NAME=\"cabbase\" VALUE=\"ScorePost.cab\">");
      out.println ("<PARAM NAME=\"bgcolor\" VALUE=#336699>");
      out.println ("<PARAM NAME=\"text\" VALUE=#FFFFFF>");
      out.println ("<PARAM NAME=\"data\" VALUE=\"/cgi/scoretracker/databuffer.dll?/scoretracker/scorepost/data/all.dat\">");
      out.println ("<PARAM NAME=\"ad data\" VALUE=\"/ad/scorepost/nhl.txt\">");
      out.println ("<PARAM NAME=\"target\" VALUE=_top>");
      out.println ("<PARAM NAME=\"delay\" VALUE=3500>");
      out.println ("<PARAM NAME=\"data time\" VALUE=30000>");
      out.println ("<PARAM NAME=\"inactive data time\" VALUE=300000>");
      out.println ("</APPLET>");
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