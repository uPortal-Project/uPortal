package org.jasig.portal;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import com.objectspace.xml.*;
import org.jasig.portal.layout.*;

/**
 * Methods to accompany dispatch.jsp
 * @author Ken Weiner
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
      org.jasig.portal.layout.IChannel channel = (org.jasig.portal.layout.IChannel) session.getAttribute (sDispatchChannel);

      if (channel == null)
      {
        int iTab = Integer.parseInt (req.getParameter ("tab"));
        int iCol = Integer.parseInt (req.getParameter ("column"));
        int iChan = Integer.parseInt (req.getParameter ("channel"));
        channel = layoutBean.getChannel (req, iTab, iCol, iChan);
        session.setAttribute (sDispatchChannel, channel);
      }
      
      ch = layoutBean.getChannelInstance (channel);
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
    return ch;
  }
 
  /**
   * This method should be called by a channel when it wants
   * to return to layout.jsp
   * @param the servlet request object
   */
  public void finish (HttpServletRequest req, HttpServletResponse res)
  {
    try
    {
      HttpSession session = req.getSession (false);
      session.removeAttribute (sDispatchChannel);
      res.sendRedirect ("layout.jsp");
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
  }      
}