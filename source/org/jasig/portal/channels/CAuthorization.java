package org.jasig.portal.channels;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import org.jasig.portal.*;

import java.net.*;


/**
 * Authorization channel.  This channel works in conjunction with
 * authorization.jsp
 * 
 * @author Ken Weiner
 */
public class CAuthorization implements org.jasig.portal.IChannel                             
{  
  public void initParams (Hashtable params) {};
  public String getName () {return "Sign in";}
  public boolean isMinimizable () {return false;}
  public boolean isDetachable () {return false;}
  public boolean isRemovable () {return false;}
  public boolean isEditable () {return false;}  
  
  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {
      String sAction = req.getParameter ("authorizationAction");
      
      if (sAction == null)
        doDisplaySignIn (req, res, out);
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
  
  protected void doDisplaySignIn (HttpServletRequest req, HttpServletResponse res, JspWriter out) throws IOException
  {
    HttpSession session = req.getSession (false);
    String sLogonStatus = (String) session.getAttribute ("logonStatus");
    
    if (sLogonStatus != null && sLogonStatus.equals ("true"))
      doDisplayFailedLogonMsg (req, res, out);
      
    out.println ("<p>");
    out.println ("<form action=\"authorization.jsp\" method=post>");
    out.println ("<table align=center border=0 width=100%>");
    out.println ("  <tr>");
    out.println ("    <td><font face=\"Helvetica,Arial,sans-serif\" size=-1>User Name: </font></td>");
    out.println ("    <td><input name=userName type=text size=15 value=\"\"></td>");
    out.println ("  </tr>");
    out.println ("  <tr>");
    out.println ("    <td><font face=\"Helvetica,Arial,sans-serif\" size=-1>Password: </font></td>");
    out.println ("    <td><input name=password type=password size=15 value=\"\"></td>");
    out.println ("  </tr>");
    out.println ("</table>");
      
    out.println ("<center>");  
    out.println ("<input name=remember type=checkbox><font face=\"Helvetica,Arial,sans-serif\" size=-1>Remember my user name</font>");
    out.println ("<p><input name=signIn type=submit value=\"Sign in\">");
    out.println ("</center>");  
    out.println ("<input name=authorizationAction type=hidden value=\"signIn\"><br>");
    out.println ("</form>");
  }
  
  protected void doDisplayFailedLogonMsg (HttpServletRequest req, HttpServletResponse res, JspWriter out) throws IOException
  {
    out.print ("<font face=\"Helvetica,Arial,sans-serif\" color=red size=-1>");
    out.print ("Invalid user name or password!");
    out.print ("Please try again.");
    out.print ("</font>");
  }
}