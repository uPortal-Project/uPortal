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

  RdbmServices rdbmService = new RdbmServices ();
  Connection con = null;
  Hashtable registry = null;
  org.jasig.portal.IChannel ch = null;
  org.jasig.portal.layout.IChannel channel = null;
  LayoutBean layoutbean = new LayoutBean();

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
          //int iInsertBefore = sChannelXml.indexOf (layoutbean.sLayoutDtd);
          //sChannelXml = sChannelXml.substring (0, iInsertBefore) + layoutbean.sPathToLayoutDtd + sChannelXml.substring (iInsertBefore);

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
    channel =(org.jasig.portal.layout.IChannel) channelXml.getRoot ();

    List instanceIDs = new ArrayList ();
    HttpSession session = req.getSession (false);
    ILayoutBean layoutBean = (ILayoutBean) session.getAttribute("layoutBean");
    IXml layoutXml = layoutBean.getLayoutXml (req, layoutBean.getUserName(req));
    ILayout layout = (ILayout) layoutXml.getRoot ();
    ITab[] tabs = layout.getTabs ();

    for (int iTab = 0; iTab < tabs.length; iTab++)
    {
      IColumn[] columns = tabs[iTab].getColumns ();

      for (int iCol = 0; iCol < columns.length; iCol++)
      {
        org.jasig.portal.layout.IChannel[] channels = columns[iCol].getChannels ();

        for (int iChan = 0; iChan < channels.length; iChan++)
        {
          String sInstanceID = channels[iChan].getInstanceIDAttribute ();
          Integer id = new Integer (sInstanceID.substring (1));
          instanceIDs.add (id);
        }
      }
    }

    Collections.sort (instanceIDs);
    int iHighest = ((Integer) instanceIDs.get (instanceIDs.size () - 1)).intValue ();
    String sInstanceID = "c" + (iHighest + 1);
    channel.setInstanceIDAttribute (sInstanceID);
    channel.setMinimizedAttribute("false");
    
    // Remove from channel cache
    layoutBean.removeChannelInstance (sInstanceID);
    
    return channel;
   }

  /**
   * Method for setting channel properties.
   * This should reduce database queries.
   * @param the servlet request object
   */
   public void setChannel(HttpServletRequest req)
   {
    ch = layoutbean.getChannelInstance(getChannel(req));
   }

  /**
   * Method for getting Channel Name
   * @param the servlet request object
   * @return the channel name
   */
   public String getChannelName()
   {
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
        //ch = layoutbean.getChannelInstance(getChannel(req));

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
   * Retreives all channels and classifications
   * and returns in the form of a Hashtable
   * @return a hastable of channels
   */
   public void setRegistry ()
   {
    String sQuery = "select cl.name, ch.chan_id, title "+
                   "from portal_class cl, portal_channels ch, portal_chan_class chcl "+
                   "where ch.chan_id=chcl.chan_id and chcl.class_id =cl.class_id "+
                   "order by cl.name, ch.title";

    ResultSet rs = null;
    Statement stmt = null;
    String [] chan = null;

    try
    {
       registry = new Hashtable();
       con = rdbmService.getConnection();
       stmt = con.createStatement();

       Logger.log (Logger.DEBUG, sQuery);
       debug(sQuery);

       rs = stmt.executeQuery (sQuery);

       while (rs.next()) {
             String cat = "";
             String id = "";
             Vector v = new Vector();
             chan = new String[2];

             cat = rs.getString(1);
             chan[0] = rs.getString(2);
             chan[1] = rs.getString(3);

             if(!(registry.containsKey(cat))) {
                v.addElement(chan);
                debug("adding:"+cat+" : "+chan[0]+" : "+chan[1]);
                registry.put(cat, v);
             }
             else{
                debug("adding:"+cat+" : "+chan[0]+" : "+chan[1]);
                ((Vector)registry.get(cat)).addElement(chan);
             }
       }
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    finally
    {
      debug("closing connection");
      rdbmService.releaseConnection (con);
    }
  }

  /**
   * Retrieves all available channels
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void getAllChannels (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try
    {
    setRegistry();
    if (registry==null) debug("registry is null!!");
    Enumeration e = registry.keys();

    while(e.hasMoreElements()) {
      String cat = (String)e.nextElement();
      Vector v = (Vector)registry.get(cat);

      out.println("<br><b>"+cat+"</b><br>");
      Enumeration enum = v.elements();
      while(enum.hasMoreElements()) {
        String [] chan = new String[2];
        chan = (String[])enum.nextElement();
        out.println(chan[1]+"&nbsp;"+
                    "<a href=\"personalizeLayout.jsp?action=addChannel&column=0&chan_id="+chan[0]+"\"><IMG SRC=\"images/add.gif\" WIDTH=\"20\" HEIGHT=\"13\" HSPACE=\"0\" BORDER=\"0\" ALT=\"Add Channel\"></a>&nbsp;"+
                    "<a href=\"previewChannel.jsp?chan_id="+chan[0]+"\"><IMG SRC=\"images/preview.gif\" WIDTH=\"16\" HEIGHT=\"13\" HSPACE=\"0\" BORDER=\"0\" ALT=\"Preview Channel\"></a><br>");
      }
    }
    }
    catch (Exception e)
    {
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