package org.jasig.portal;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import com.objectspace.xml.*;
import org.jasig.portal.layout.*;

/**
 * Methods to accompany edit.jsp
 * @author Ken Weiner
 */
public class EditBean extends GenericPortalBean
{        
  /**
   * Returns the channel to edit and stores it in the session
   * until EditBean.finishEdit() is called by the channel
   * @param the servlet request object
   * @return the channel to adit
   */
  public IChannel getChannelToEdit (HttpServletRequest req)
  {
    org.jasig.portal.IChannel ch = null;
    
    try
    {
      HttpSession session = req.getSession (false);
      ILayoutBean layoutBean = (ILayoutBean) session.getAttribute ("layoutBean");
      org.jasig.portal.layout.IChannel channel = (org.jasig.portal.layout.IChannel) session.getAttribute ("activeChannel");

      if (channel == null)
      {
        int iTab = Integer.parseInt (req.getParameter ("tab"));
        int iCol = Integer.parseInt (req.getParameter ("column"));
        int iChan = Integer.parseInt (req.getParameter ("channel"));
        channel = layoutBean.getChannel (req, iTab, iCol, iChan);
        session.setAttribute ("activeChannel", channel);
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
   * This method should be called by a channel when it is done
   * being edited.
   * @param the servlet request object
   */
  public void finishEdit (HttpServletRequest req, HttpServletResponse res)
  {
    try
    {
      HttpSession session = req.getSession (false);
      session.removeAttribute ("activeChannel");
      res.sendRedirect ("layout.jsp");
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
  }
      
}