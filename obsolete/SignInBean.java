package org.jasig.portal;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.text.*;
import com.objectspace.xml.*;
import org.jasig.portal.layout.*;

/**
 * @author Ken Weiner
 */
public class SignInBean extends GenericPortalBean
{
  public void signIn (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {
      String sUserName = req.getParameter ("userName");
      String sPassword = req.getParameter ("password");
      
      // Validate username and password
      
      if (sUserName.trim ().length () > 0 /* replace with a real condition*/)
      {
        req.getSession ().putValue ("userName", sUserName);
        res.sendRedirect ("layout.jsp");
      }
      else
      {
        out.println ("<p><font face=Arial color=red>The system could not log you in.  Make sure you have entered your user name and password correctly and try again.</font>");
      }
    }
    catch (Exception e)
    {
      System.out.println ("\nERROR: \n" + e);
    }
  }      
}