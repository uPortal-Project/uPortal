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
 * @author Ken Weiner
 * @version $Revision$
 *
 * This channel how works in conjunction with the "authentication.jsp"
 * -ADN
 */
public class CAuthorization implements org.jasig.portal.IChannel                             
{  
  private static Vector params = null;

  public void init (ChannelConfig chConfig) {};
  public String getName () {return "Sign in";}
  public boolean isMinimizable () {return false;}
  public boolean isDetachable () {return false;}
  public boolean isRemovable () {return false;}
  public boolean isEditable () {return false;}  
  public boolean hasHelp () {return true;}  
  
  public int getDefaultDetachWidth () {return 0;}
  public int getDefaultDetachHeight () {return 0;}
  
  public Vector getParameters()
  {
    return params;
  }
  
  /**
   * Called when channel should output its contents
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
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
      Logger.log (Logger.ERROR, e);
    }
  }
  
  /**
   * Called when user clicks this channel's edit button
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void edit (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    // This channel is not editable
  }
  
  /**
   * Called when user clicks this channel's help button
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void help (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try
    {
      out.println ("<p>Please sign in with the following user name and password:");
      out.println ("<p><p>");
      out.println ("<table border=0 cellspacing=5 cellpadding=5>");
      out.println ("  <tr><th align=right>User name:</th><td><tt>demo</tt></td></tr>");
      out.println ("  <tr><th align=right>Password:</th><td><tt>demo</tt></td></tr>");
      out.println ("</table>");

      out.println ("<form action=\"layout.jsp\" method=post>");
      out.println ("<input type=submit name=submit value=\"Try Again\">");
      out.println ("</form>");
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    
  }
  
  /**
   * Called by this channels render method.  Outputs an html form prompting
   * for user name and password.
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  protected void doDisplaySignIn (HttpServletRequest req, HttpServletResponse res, JspWriter out) throws IOException
  {
    HttpSession session = req.getSession (false);
    String sUserName = (String) session.getAttribute ("userName");
    
    if (sUserName != null && sUserName.equals ("guest"))
      doDisplayFailedLogonMsg (req, res, out);
      
    out.println ("<p>");
    out.println ("<form action=\"authentication.jsp\" method=post>");
    out.println ("<table align=center border=0 width=100%>");
    out.println ("  <tr>");
    out.println ("    <td>User Name: </td>");
    out.println ("    <td><input name=userName type=text size=15 value=\"\"></td>");
    out.println ("  </tr>");
    out.println ("  <tr>");
    out.println ("    <td>Password: </td>");
    out.println ("    <td><input name=password type=password size=15 value=\"\"></td>");
    out.println ("  </tr>");
    out.println ("</table>");
      
    out.println ("<center>");  
    out.println ("<input name=remember type=checkbox>Remember my user name");
    out.println ("<p><input name=signIn type=submit value=\"Sign in\">");
    out.println ("</center>");  
    out.println ("<input name=authorizationAction type=hidden value=\"signIn\"><br>");
    out.println ("</form>");
  }
  
  /**
   * Called when this channel is redisplayed after an incorrect username/password
   * is submitted by the user
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  protected void doDisplayFailedLogonMsg (HttpServletRequest req, HttpServletResponse res, JspWriter out) throws IOException
  {
    out.print ("<font color=red>");
    out.print ("Invalid user name or password!<br>");
    out.print ("Please try again.");
    out.print ("</font>");
  }
}
