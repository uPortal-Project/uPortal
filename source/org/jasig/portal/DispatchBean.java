package org.jasig.portal;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.net.URLEncoder;
import org.jasig.portal.layout.*;

/**
 * Methods to accompany dispatch.jsp
 * @author Ken Weiner
 * @version $Revision$
 */
public class DispatchBean extends GenericPortalBean
{    
  private static final String sDispatchChannel = "dispatchChannel";
    
  /**
   * Returns the current channel and stores it in the session
   * until DispatchBean.finish () is called by the channel
   * @param the servlet request object
   * @return the channel to call
   */
  public IChannel getChannel (HttpServletRequest req)
  {
    org.jasig.portal.IChannel ch = null;
    
    try
    {
      HttpSession session = req.getSession (false);
      ILayoutBean layoutBean = (ILayoutBean) session.getAttribute ("layoutBean");
      ch = (org.jasig.portal.IChannel) session.getAttribute (sDispatchChannel);

      if (ch == null)
      {
        String sChannelID = req.getParameter ("channelID");
        
        if (sChannelID != null)
        {
          ch = layoutBean.getChannelInstance (sChannelID);
          session.setAttribute (sDispatchChannel, ch);
        }
        else
          Logger.log (Logger.ERROR, "To dispatch properly, a channel ID must be included in the query string in the form: \"...&channelID=...\"");
      }
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    return ch;
  }
 
  /**
   * This method should be called by a channel when it wants
   * to return to layout.jsp
   * @param the servlet request object
   */
  public static void finish (HttpServletRequest req, HttpServletResponse res)
  {
    try
    {
      HttpSession session = req.getSession (false);
      session.removeAttribute (sDispatchChannel);
      res.sendRedirect ("layout.jsp");
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }    
  
  /**
   * Builds a url used to send control back to a particular
   * method of a channel
   * @param the name of the channel's method to call
   * @param the channel config object
   * @return a url used to call a method in a channel
   */
  public static String buildURL (String sMethodName, ChannelConfig chConfig)
  {
    try
    {
      return buildURL (sMethodName, chConfig.getChannelID ());
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    return null;
  } 
  
  /**
   * Builds a url used to send control back to a particular
   * method of a channel
   * @param the name of the channel's method to call
   * @param the channel's ID
   * @return a url used to call a method in a channel
   */
  public static String buildURL (String sMethodName, String sChannelID)
  {
    try
    {
      StringBuffer sbURL = new StringBuffer ("dispatch.jsp");
      sbURL.append ("?method=");
      sbURL.append (URLEncoder.encode (sMethodName));
      sbURL.append ("&channelID=");
      sbURL.append (URLEncoder.encode (sChannelID));
      return sbURL.toString ();
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    return null;
  }   
  
}