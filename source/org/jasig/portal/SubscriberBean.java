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
 * @version $Revision$
 */
public class SubscriberBean extends GenericPortalBean{

  private static Object dummyObject = new Object (); // For syncronizing code

  public SubscriberBean() {
  }


  /**
   * Retrieves a handle to the channel xml
   * @param the servlet request object
   * @param the channel id
   * @return handle to the layout xml
   */
  public IXml getChannelXml (HttpServletRequest req)
  {
    RdbmServices rdbmService = new RdbmServices ();
    Connection con = null;
    IXml channelXml = null;
    int id  = Integer.parseInt(req.getParameter("chan_id"));

    try
    {
      synchronized (dummyObject)
      {
        con = rdbmService.getConnection ();
        Statement stmt = con.createStatement();

        String sQuery = "SELECT ID, TITLE, CHANNEL_XML FROM CHANNELS WHERE ID=" + id ;
        Logger.log (Logger.DEBUG, sQuery);

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
      }

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
   * Creates a channel instance and
   * sends it to LayoutBean where it is added
   * to user's layout.xml
   * @param the servlet request object
   */
   public void addChannel(HttpServletRequest req)
   {
    IXml channelXml = getChannelXml (req);
    org.jasig.portal.layout.IChannel channel = (org.jasig.portal.layout.IChannel) channelXml.getRoot ();
    LayoutBean layout = new LayoutBean();
    layout.addChannel(req, channel);
   }

  /**
   * Retrieves all available channels
   * @param the servlet request object
   * @return ResultSet
   */
  public ResultSet getChannels (HttpServletRequest req)
  {
    RdbmServices rdbmService = new RdbmServices ();
    Connection con = null;
    ResultSet rs = null;
    Statement stmt = null;

    try
    {
      synchronized (dummyObject)
      {
        con = rdbmService.getConnection ();
        stmt = con.createStatement();

        String sQuery = "SELECT ID, TITLE FROM CHANNELS" ;
        Logger.log (Logger.DEBUG, sQuery);

        rs = stmt.executeQuery (sQuery);
      }

      return rs;
      
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    finally
    {
      try {stmt.close();} catch(Exception e) {}
      rdbmService.releaseConnection (con);
    }
    return null;
  }
}