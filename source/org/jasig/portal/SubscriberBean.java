package org.jasig.portal;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.net.*;
import com.objectspace.xml.*;
import org.jasig.portal.layout.*;
import org.jasig.portal.GenericPortalBean;

/**
 * Provides methods associated with subscribing to a channel.
 * This includes preview, listing all available channels
 * and placement on a users page.
 * @author John Laker
 * @version %I%, %G%
 */
public class SubscriberBean extends GenericPortalBean{

  RdbmServices rdbmService = new RdbmServices ();
  Connection con = null;

  public SubscriberBean() {
  }


  /**
   * Retrieves a handle to the channel xml
   * @param the servlet request object
   * @return handle to the channel xml
   */
  public IXml getChannelXml (HttpServletRequest req)
  {
    IXml channelXml = null;
    int id  = Integer.parseInt(req.getParameter("chan_id"));

    try
    {
        con = rdbmService.getConnection ();
        Statement stmt = con.createStatement();

        String sQuery = "SELECT CHAN_ID, TITLE, CHANNEL_XML FROM PORTAL_CHANNELS WHERE CHAN_ID=" + id ;
        Logger.log (Logger.DEBUG, sQuery);
        debug(sQuery);
        ResultSet rs = stmt.executeQuery (sQuery);

        if (rs.next ())
        {
          String sChannelXml = rs.getString ("CHANNEL_XML");

          // Tack on the full path to layout.dtd
          //int iInsertBefore = sLayoutXml.indexOf (sLayoutDtd);
          //sChannelXml = sLayoutXml.substring (0, iInsertBefore) + sPathToLayoutDtd + sLayoutXml.substring (iInsertBefore);

          String xmlFilePackage = "org.jasig.portal.layout";
          channelXml = Xml.openDocument (xmlFilePackage, new StringReader (sChannelXml));
        }
        stmt.close ();

      return channelXml;
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    finally
    {
      rdbmService.releaseConnection (con);
    }
    return null;
  }

  /**
   * Returns a channel instance
   * to be added to user's layout.xml
   * @param the servlet request object
   * @return IChannel
   */
   public org.jasig.portal.layout.IChannel getChannel(HttpServletRequest req)
   {
    IXml channelXml = getChannelXml (req);
    org.jasig.portal.layout.IChannel channel = (org.jasig.portal.layout.IChannel) channelXml.getRoot ();
    return channel;
   }

  /**
   * Method for getting Channel Name
   * @param the servlet request object
   * @return the channel name
   */
   public String getChannelName(HttpServletRequest req)
   {
     LayoutBean layoutbean = new LayoutBean();
     org.jasig.portal.IChannel ch = layoutbean.getChannelInstance(getChannel(req));
     return ch.getName();
   } 

  /**
   * displays a preview of the channel
   * for the user to see before subscribing
   * @param the servlet request object
   */
  public void previewChannel(HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
        try{
        LayoutBean layoutbean = new LayoutBean();
        org.jasig.portal.IChannel ch = layoutbean.getChannelInstance(getChannel(req));

        out.println ("<table border=0 cellpadding=1 cellspacing=4 width=100%>");
        out.println ("  <tr>");
        out.println ("    <td bgcolor=cccccc>");

        // Channel heading
        IXml layoutXml = layoutbean.getLayoutXml (req, layoutbean.getUserName (req));
        ILayout layout = (ILayout) layoutXml.getRoot ();
            
        out.println ("      <table border=0 cellpadding=0 cellspacing=0 width=100% bgcolor=" + layout.getAttribute ("channelHeadingColor") + ">");
        out.println ("        <tr>");
        out.println ("          <td>");
        out.println ("            <font face=arial color=#000000><b>&nbsp;" + ch.getName() + "</b></font>");
        out.println ("          </td>");
        out.println ("          <td nowrap valign=center align=right>");
        out.println ("            &nbsp;");
            
        // Channel control buttons
        if (ch.isMinimizable ())
        out.println ("<img border=0 width=\"18\" height=\"15\" src=\"images/minimize.gif\" alt=\"Minimize\">");

        if (ch.isDetachable ())
        out.println ("<img border=0 width=\"18\" height=\"15\" src=\"images/detach.gif\" alt=\"Detach\">");
            
        if (ch.isRemovable ())
        out.println ("<img border=0 width=\"18\" height=\"15\" src=\"images/remove.gif\" alt=\"Remove\">");

        if (ch.isEditable ())
        out.println ("<img border=0 width=\"28\" height=\"15\" src=\"images/edit.gif\" alt=\"Edit\">");

        if (ch.hasHelp ())
        out.println ("<img border=0 width=\"18\" height=\"15\" src=\"images/help.gif\" alt=\"Help\">");

            out.println ("            &nbsp;");
            out.println ("          </td>");            
            out.println ("        </tr>");
            out.println ("      </table>");

            // Channel body
            out.println ("      <table border=0 cellpadding=0 cellspacing=0 width=100%>");
            out.println ("        <tr>");
            out.println ("          <td bgcolor=#ffffff>");

            out.println ("            <table border=0 cellpadding=3 cellspacing=0 width=100% bgcolor=#ffffff>");
            out.println ("              <tr>");
            out.println ("                <td valign=top>");
                            

              // Render channel contents
              ch.render (req, res, out);

              
            out.println ("                </td>");
            out.println ("              </tr>");
            out.println ("            </table>");

            out.println ("          </td>");
            out.println ("        </tr>");
            out.println ("      </table>");

            out.println ("    </td>");
            out.println ("  </tr>");
            out.println ("</table>");
        }
        catch (Exception e){
              Logger.log (Logger.ERROR, e);
        }
  }

  /**
   * Retrieves all available channels
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void getChannels (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    ResultSet rs = null;
    Statement stmt = null;

    try
    {
        con = rdbmService.getConnection ();
        stmt = con.createStatement();

        String sQuery = "SELECT CHAN_ID, TITLE FROM PORTAL_CHANNELS" ;
        Logger.log (Logger.DEBUG, sQuery);
        debug(sQuery);

        rs = stmt.executeQuery (sQuery);

        while(rs.next()) {
        out.println("<a href=\"previewChannel.jsp?chan_id="+rs.getString("CHAN_ID")+"\">"+rs.getString("TITLE")+"</a><br>");
        }
        stmt.close();
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    finally
    {
      rdbmService.releaseConnection (con);
    }
  }

}