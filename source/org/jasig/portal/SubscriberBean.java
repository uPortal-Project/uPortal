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

  private static Object dummyObject = new Object (); // For syncronizing code
  RdbmServices rdbmService = new RdbmServices ();
  Connection con = null;

  public SubscriberBean() {
  }


  /**
   * Retrieves a handle to the channel xml
   * @param the servlet request object
   * @param the channel id
   * @return handle to the channel xml
   */
  public IXml getChannelXml (HttpServletRequest req)
  {
    IXml channelXml = null;
    int id  = Integer.parseInt(req.getParameter("chan_id"));

    try
    {
      synchronized (dummyObject)
      {
        con = rdbmService.getConnection ();
        Statement stmt = con.createStatement();

        String sQuery = "SELECT ID, TITLE, CHANNEL_XML FROM PORTAL_CHANNELS WHERE ID=" + id ;
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
   * Retrieves all available channels
   * @param the servlet request object
   * @return ResultSet
   */
  public ResultSet getChannels (HttpServletRequest req)
  {
    ResultSet rs = null;
    Statement stmt = null;

    try
    {
      synchronized (dummyObject)
      {
        con = rdbmService.getConnection ();
        stmt = con.createStatement();

        String sQuery = "SELECT ID, TITLE FROM PORTAL_CHANNELS" ;
        Logger.log (Logger.DEBUG, sQuery);
        debug(sQuery);

        rs = stmt.executeQuery (sQuery);
      }

      return rs;
      
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    return null;
  }

  /**
   * method for closing subscribe database connection
   */
  public void close()
  {
   rdbmService.releaseConnection(con);
  }
}