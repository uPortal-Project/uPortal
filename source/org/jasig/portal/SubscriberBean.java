/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

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
import org.jasig.portal.security.IPerson;

/**
 * Provides methods associated with subscribing to a channel.
 * This includes preview, listing all available channels
 * and placement on a users page.
 * @author John Laker
 * @version $Revision$
 */
public class SubscriberBean extends GenericPortalBean {

    RdbmServices rdbmService = new RdbmServices ();
    Connection con = null;
    Hashtable registry = null;
    org.jasig.portal.IChannel ch = null;
    org.jasig.portal.layout.IChannel channel = null;
    LayoutBean layoutbean = new LayoutBean();

    public SubscriberBean() {}


    /**
     * Retrieves a handle to the channel xml
     * @param the servlet request object
     * @return handle to the channel xml
     */
    public IXml getChannelXml (HttpServletRequest req) {
        IXml channelXml = null;
        int id  = Integer.parseInt(req.getParameter("chan_id"));

        try {
            con = rdbmService.getConnection ();
            Statement stmt = con.createStatement();

            String sQuery = "SELECT CHAN_ID, TITLE, CHANNEL_XML FROM PORTAL_CHANNELS WHERE CHAN_ID=" + id;
            Logger.log (Logger.DEBUG, sQuery);
            debug(sQuery);
            ResultSet rs = stmt.executeQuery (sQuery);

            if (rs.next ()) {
                String sChannelXml = rs.getString ("CHANNEL_XML");

                String xmlFilePackage = "org.jasig.portal.layout";
                channelXml = Xml.openDocument (xmlFilePackage, new StringReader (sChannelXml));
            }

            stmt.close ();

            return channelXml;
        } catch (Exception e) {
            Logger.log (Logger.ERROR, e);
        } finally {
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
    public org.jasig.portal.layout.IChannel getChannel(HttpServletRequest req) {
        IXml channelXml = getChannelXml(req);
        channel = (org.jasig.portal.layout.IChannel)channelXml.getRoot();

        List instanceIDs = new ArrayList();
        HttpSession session = req.getSession (false);
        ILayoutBean layoutBean = (ILayoutBean)session.getAttribute("layoutBean");
        IXml layoutXml = layoutBean.getLayoutXml(layoutBean.getUserName(req));
        ILayout layout = (ILayout)layoutXml.getRoot ();
        ITab[] tabs = layout.getTabs ();

        for (int iTab = 0; iTab < tabs.length; iTab++) {
            IColumn[] columns = tabs[iTab].getColumns ();

            for (int iCol = 0; iCol < columns.length; iCol++) {
                org.jasig.portal.layout.IChannel[] channels = columns[iCol].getChannels ();

                for (int iChan = 0; iChan < channels.length; iChan++) {
                    String sInstanceID = channels[iChan].getInstanceIDAttribute ();
                    Integer id = new Integer (sInstanceID.substring (1));
                    instanceIDs.add (id);
                }
            }
        }

        Collections.sort(instanceIDs);
        int iHighest = -1;
        if (instanceIDs.size() > 0) {
            iHighest = ((Integer)instanceIDs.get (instanceIDs.size () - 1)).intValue ();
        }
        String sInstanceID = "c" + (iHighest + 1);
        channel.setInstanceIDAttribute(sInstanceID);
        channel.setMinimizedAttribute("false");
        channel.setGlobalChannelIDAttribute(req.getParameter("chan_id"));

        // Remove from channel cache
        layoutBean.removeChannelInstance(sInstanceID);

        return channel;
    }

    /**
     * Method for setting channel properties.
     * This should reduce database queries.
     * @param the servlet request object
     */
    public void setChannel(HttpServletRequest req) {
        ch = layoutbean.getChannelInstance(getChannel(req));
    }

    /**
     * Method for getting Channel Name
     * @param the servlet request object
     * @return the channel name
     */
    public String getChannelName() {
        return ch.getName();
    }

    /**
     * displays a preview of the channel
     * for the user to see before subscribing
     * @param the servlet request object
     */
    public void previewChannel(HttpServletRequest req, HttpServletResponse res, JspWriter out) {
        try {
            //ch = layoutbean.getChannelInstance(getChannel(req));

            out.println ("<table border=0 cellpadding=1 cellspacing=4 width=100%>");
            out.println ("  <tr>");
            out.print ("    <td bgcolor=cccccc>");

            // Channel heading
            IXml layoutXml = layoutbean.getLayoutXml (layoutbean.getUserName (req));
            ILayout layout = (ILayout) layoutXml.getRoot ();

            out.println ("      <table border=0 cellpadding=0 cellspacing=0 width=100% bgcolor=" + layout.getAttribute ("channelHeadingColor") + ">");
            out.println ("        <tr>");
            out.println ("          <td>");
            out.println ("            <span class=\"PortalText\"><b>&nbsp;" + ch.getName() + "</b></span>");
            out.println ("          </td>");
            out.println ("          <td nowrap valign=\"middle\" align=right>");
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
        } catch (Exception e) {
	    // If the Exception is due to a "Broken pipe" during XML rendering, then don't print the stack trace
	    if (e.getMessage().indexOf("Broken pipe") == -1) {
            	Logger.log (Logger.ERROR, e);
	    } else {
            	Logger.log (Logger.DEBUG, "EXCEPTION: java.io.IOException (Broken pipe) has occurred" );
	    }
        }
    }

    /**
     * Retreives all channels and classifications
     * and returns in the form of a Hashtable
     * @return a hastable of channels
     */
    public void setRegistry () {
        String sQuery = "SELECT CL.NAME, CH.CHAN_ID, TITLE "+
                        "FROM PORTAL_CLASS CL, PORTAL_CHANNELS CH, PORTAL_CHAN_CLASS CHCL " +
                        "WHERE CH.CHAN_ID=CHCL.CHAN_ID AND CHCL.CLASS_ID=CL.CLASS_ID " +
                        "ORDER BY CL.NAME, CH.TITLE";

        ResultSet rs = null;
        Statement stmt = null;
        String [] chan = null;

        try {
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
                } else {
                    debug("adding:"+cat+" : "+chan[0]+" : "+chan[1]);
                    ((Vector)registry.get(cat)).addElement(chan);
                }
            }
        } catch (Exception e) {
            Logger.log (Logger.ERROR, e);
        } finally {
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
    public void getAllChannels (HttpServletRequest req, HttpServletResponse res, JspWriter out) {
        try {
            AuthorizationBean authorizationBean = new AuthorizationBean();

            HttpSession session = req.getSession (false);
            ILayoutBean layoutBean = (ILayoutBean) session.getAttribute("layoutBean");
            String sUserName = layoutBean.getUserName(req);
            IPerson person = layoutBean.getPerson (req);

            setRegistry();

            if (registry==null) {
                debug("registry is null!!");
            }

            Enumeration e = registry.keys();
            int chanID = -1;
            String channelList;
            boolean canSubscribe;

            // Go through each catagory
            while(e.hasMoreElements()) {
                // Make sure the buffer is clear
                StringWriter w = new StringWriter();

                // Do not display the catagory if the user cannot
                //  subscribe to any channels under it
                channelList = new String();
                canSubscribe = false;

                String cat = (String)e.nextElement();
                Vector v = (Vector)registry.get(cat);

                // Output the catagory name
                w.write("<span class=\"PortalText\"><b>" + cat + "</b><br></span>\n");

                Enumeration enum = v.elements();

                // Output each channel within the catagory
                while(enum.hasMoreElements()) {
                    String [] chan = new String[2];

                    chan = (String[])enum.nextElement();

                    // Convert the channel ID to a integer
                    try {
                        chanID = Integer.parseInt(chan[0]);
                    } catch(Exception inf) {
                        // Don't fail completely if the channel ID is not right!
                        chanID = -1;
                    }

                    // Only show the channel to the user if they are authorized to subscribe
                    if( authorizationBean.canUserSubscribe(person, chanID) ) {
                        canSubscribe = true;

                        w.write("<span class=\"PortalText\">\n");
                        w.write("<a href=\"personalizeLayout.jsp?action=addChannel&column=0&chan_id=" + chan[0] + "\">");
                        w.write("<IMG SRC=\"images/add.gif\" WIDTH=\"20\" HEIGHT=\"13\" HSPACE=\"0\" BORDER=\"0\" ALT=\"Add Channel\"></a>&nbsp;");
                        w.write("<a href=\"previewChannel.jsp?chan_id=" + chan[0] + "\">");
                        w.write("<IMG SRC=\"images/preview.gif\" WIDTH=\"16\" HEIGHT=\"13\" HSPACE=\"0\" BORDER=\"0\" ALT=\"Preview Channel\"></a>");
                        w.write("&nbsp;<span class=\"PortalText\">" + chan[1] + "</span><br>\n");
                        w.write("</span>\n");
                    }
                }

                if(canSubscribe) {
                    out.println(w.toString());

                    if(e.hasMoreElements()) {
                        out.write("<br>");
                    }
                }
            }
        } catch (Exception e) {
            Logger.log (Logger.ERROR, e);
        }
    }

    /**
     * Retrieves all available channels
     * @param the servlet request object
     * @param the servlet response object
     * @param the JspWriter object
     */
    public void getChannels (HttpServletRequest req, HttpServletResponse res, JspWriter out) {
        ResultSet rs = null;
        Statement stmt = null;

        try {
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
        } catch (Exception e) {
            Logger.log (Logger.ERROR, e);
        } finally {
            rdbmService.releaseConnection (con);
        }
    }

}

