/**
 *  $Author$ $Date$ $Id: LayoutBean.java,v 1.14
 *  2001/06/01 18:55:01 vjoshi Exp $ $Name$ $Revision$ Copyright (c)
 *  2000 The JA-SIG Collaborative. All rights reserved. Redistribution and use
 *  in source and binary forms, with or without modification, are permitted
 *  provided that the following conditions are met: 1. Redistributions of source
 *  code must retain the above copyright notice, this list of conditions and the
 *  following disclaimer. 2. Redistributions in binary form must reproduce the
 *  above copyright notice, this list of conditions and the following disclaimer
 *  in the documentation and/or other materials provided with the distribution.
 *  3. Redistributions of any form whatsoever must retain the following
 *  acknowledgment: "This product includes software developed by the JA-SIG
 *  Collaborative (http://www.jasig.org/)." THIS SOFTWARE IS PROVIDED BY THE
 *  JA-SIG COLLABORATIVE "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *  JA-SIG COLLABORATIVE OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
import java.lang.Byte;
import org.jasig.portal.*;
import com.objectspace.xml.*;

import org.jasig.portal.layout.*;

import org.jasig.portal.AuthorizationBean;
import org.jasig.portal.security.IRole;
import org.jasig.portal.security.IPerson;

/**
 *  Provides methods associated with displaying and modifying a user's layout.
 *  This includes changing the colors, size and positions of tabs, columns, and
 *  channels.
 *
 *@author     Ken Weiner
 *@created    June 7, 2001
 *@version    $Revision$
 */
public final class LayoutBean extends GenericPortalBean
         implements ILayoutBean, HttpSessionBindingListener {
    private boolean readOnly = false;
    // used to indicate that this layoutBean is immutable
    private LayoutXmlCache layoutCache = null;
    private ChannelCache channelCache = new ChannelCache();
    private static boolean bPropsLoaded = false;
    private static String sPathToLayoutDtd = null;
    private final static String sLayoutDtd = "layout.dtd";
    private static boolean bSingleGuestLayout = true;


    /**
     *  Default constructor
     */
    public LayoutBean() {
        try {
            if (!bPropsLoaded) {
                // load the layout properties
                File layoutPropsFile = new File(getPortalBaseDir() + "properties" + File.separator + "layout.properties");
                Properties layoutProps = new Properties();
                layoutProps.load(new FileInputStream(layoutPropsFile));
                sPathToLayoutDtd = layoutProps.getProperty("pathToLayoutDtd");

                // configure whether we are enforcing a single guest layout
                if ("no".equals(SessionManager.getConfiguration("session.login.single_guest_layout"))) {
                    bSingleGuestLayout = false;
                    Logger.log(Logger.DEBUG, "LayoutBean: guests get their own LayoutBean (DANGEROUS!)");
                }
                else {
                    Logger.log(Logger.DEBUG, "LayoutBean: guests all share a sinlge LayoutBean");
                    bSingleGuestLayout = true;
                }

                bPropsLoaded = true;
            }
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Sets the LayoutXml attribute of the LayoutBean object
     *
     *@param  sUserName  The new LayoutXml value
     *@param  layoutXml  The new LayoutXml value
     */
    public final void setLayoutXml(String sUserName, IXml layoutXml) {
        // setup the layoutCache if it isn't already
        if (layoutCache == null) {
            layoutCache = new LayoutXmlCache(sUserName);
        }

        layoutCache.setLayoutXml(sPathToLayoutDtd, sLayoutDtd, layoutXml);
    }


    /**
     *  Stores the active tab in the session
     *
     *@param  req   The new ActiveTab value
     *@param  iTab  The new ActiveTab value
     */
    public final void setActiveTab(HttpServletRequest req, int iTab) {
        try {
            HttpSession session = req.getSession(false);
            session.setAttribute("activeTab", String.valueOf(iTab));
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Saves colors. Assumes that the session object contains the following
     *  variables: "bgcolor", "fgcolor", "tabColor", "activeTabColor", and
     *  "channelHeadingColor"
     *
     *@param  req  The new Colors value
     *@param  res  The new Colors value
     *@param  out  The new Colors value
     */
    public final void setColors(HttpServletRequest req, HttpServletResponse res, JspWriter out) {
        try {
            IXml layoutXml = getLayoutXml(req, getUserName(req));
            ILayout layout = (ILayout) layoutXml.getRoot();

            layout.setAttribute("bgcolor", "#" + req.getParameter("bgColor"));
            layout.setAttribute("fgcolor", "#" + req.getParameter("fgColor"));
            layout.setAttribute("tabColor", "#" + req.getParameter("tabColor"));
            layout.setAttribute("activeTabColor", "#" + req.getParameter("activeTabColor"));
            layout.setAttribute("channelHeadingColor", "#" + req.getParameter("channelHeadingColor"));

            setLayoutXml(getUserName(req), layoutXml);
            HttpSession session = req.getSession(false);
            session.removeAttribute("layoutXml");
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Sets the default tab
     *
     *@param  req  The new DefaultTab value
     */
    public final void setDefaultTab(HttpServletRequest req) {
        try {
            String sDefaultTab = req.getParameter("tab");

            IXml layoutXml = getLayoutXml(req, getUserName(req));
            ILayout layout = (ILayout) layoutXml.getRoot();
            layout.setActiveTabAttribute(sDefaultTab);
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Changes the width of a column at the desired location
     *
     *@param  req  The new ColumnWidth value
     */
    public final void setColumnWidth(HttpServletRequest req) {
        try {
            int iTab = Integer.parseInt(req.getParameter("tab"));
            int iCol = Integer.parseInt(req.getParameter("column"));
            String sColumnWidth = req.getParameter("columnWidth");
            String sColumnWidthType = req.getParameter("columnWidthType");

            IColumn column = getColumn(req, iTab, iCol);
            column.setWidthAttribute(sColumnWidth + sColumnWidthType);
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Gets the tabs
     *
     *@param  req  Description of Parameter
     *@return      The Tabs value
     */
    public final ITab[] getTabs(HttpServletRequest req) {
        IXml layoutXml = getLayoutXml(req, getUserName(req));
        ILayout layout = (ILayout) layoutXml.getRoot();
        ITab[] tabs = layout.getTabs();
        return tabs;
    }


    /**
     *  Gets a tab
     *
     *@param  req   Description of Parameter
     *@param  iTab  Description of Parameter
     *@return       The Tab value
     */
    public final ITab getTab(HttpServletRequest req, int iTab) {
        IXml layoutXml = getLayoutXml(req, getUserName(req));
        ILayout layout = (ILayout) layoutXml.getRoot();
        ITab tab = layout.getTabAt(iTab);
        return tab;
    }


    /**
     *  Gets a column
     *
     *@param  req   Description of Parameter
     *@param  iTab  Description of Parameter
     *@param  iCol  Description of Parameter
     *@return       The Column value
     */
    public final IColumn getColumn(HttpServletRequest req, int iTab, int iCol) {
        IXml layoutXml = getLayoutXml(req, getUserName(req));
        ILayout layout = (ILayout) layoutXml.getRoot();
        ITab tab = layout.getTabAt(iTab);
        IColumn column = tab.getColumnAt(iCol);
        return column;
    }


    /**
     *  Gets a channel
     *
     *@param  req    Description of Parameter
     *@param  iTab   Description of Parameter
     *@param  iCol   Description of Parameter
     *@param  iChan  Description of Parameter
     *@return        The Channel value
     */
    public final org.jasig.portal.layout.IChannel getChannel(HttpServletRequest req, int iTab, int iCol, int iChan) {
        IXml layoutXml = getLayoutXml(req, getUserName(req));
        ILayout layout = (ILayout) layoutXml.getRoot();
        ITab tab = layout.getTabAt(iTab);
        IColumn column = tab.getColumnAt(iCol);
        org.jasig.portal.layout.IChannel channel = column.getChannelAt(iChan);
        return channel;
    }


    /**
     *  Retrieves a handle to the layout xml
     *
     *@param  req        Description of Parameter
     *@param  sUserName  Description of Parameter
     *@return            handle to the layout xml
     */
    public final IXml getLayoutXml(HttpServletRequest req, String sUserName) {
        // setup the layoutCache if it isn't done already
        if (layoutCache == null) {
            layoutCache = new LayoutXmlCache(sUserName);
        }

        // TODO:  find out if we can get rid of the HttpServletRequest parameter since it isn't used anymore

        return layoutCache.getLayoutXml(sPathToLayoutDtd, sLayoutDtd);
    }


    /**
     *  Returns the default layout for the site. This is used by different
     *  configuration portions of the system, where the user can ask to have
     *  their layout reset to the default.
     *
     *@param  req  Description of Parameter
     *@return      The DefaultLayoutXml value
     *@author      Zed A. Shaw <zed.shaw@ubc.ca>
     */
    public final IXml getDefaultLayoutXml(HttpServletRequest req) {
        // since we are getting the layout for the user "default"
        // and we don't want to replace the current user's layout,
        // we just make a local copy of the cache

        LayoutXmlCache cache = new LayoutXmlCache("default");

        // this may be wrong though, since I'm not exactly sure
        // how this function is supposed to work.  Does it replace
        // the user's current layout with the default, or does it simply
        // return a default layout for something else to use.

        return cache.getLayoutXml(sPathToLayoutDtd, sLayoutDtd);
    }



    /**
     *  Retrieves the active tab
     *
     *@param  req  Description of Parameter
     *@return      the active tab
     */
    public final int getActiveTab(HttpServletRequest req) {
        int iActiveTab = 0;

        try {
            IXml layoutXml = getLayoutXml(req, getUserName(req));
            ILayout layout = (ILayout) layoutXml.getRoot();

            HttpSession session = req.getSession(false);
            String sTabParameter = req.getParameter("tab");
            String sTabSession = (String) session.getAttribute("activeTab");

            if (sTabParameter != null) {
                iActiveTab = Integer.parseInt(sTabParameter);
            }
            else if (sTabSession != null) {
                iActiveTab = Integer.parseInt(sTabSession);
            }
            else {
                // Active tab has not yet been set. Read it from layout.xml
                iActiveTab = Integer.parseInt(layout.getAttribute("activeTab"));
            }

            // If tab is not within acceptable range, use the first tab
            if (iActiveTab >= layout.getTabCount()) {
                iActiveTab = 0;
            }

            setActiveTab(req, iActiveTab);
            return iActiveTab;
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
        return iActiveTab;
    }


    /**
     *  Gets page background color
     *
     *@param  req  Description of Parameter
     *@param  res  Description of Parameter
     *@param  out  Description of Parameter
     *@return      The BackgroundColor value
     */
    public final String getBackgroundColor(HttpServletRequest req, HttpServletResponse res, JspWriter out) {
        try {
            IXml layoutXml = getLayoutXml(req, getUserName(req));
            ILayout layout = (ILayout) layoutXml.getRoot();
            String sBgColor = layout.getAttribute("bgcolor");

            if (sBgColor != null) {
                return sBgColor;
            }
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }

        return "";
    }


    /**
     *  Gets page foreground color
     *
     *@param  req  Description of Parameter
     *@param  res  Description of Parameter
     *@param  out  Description of Parameter
     *@return      The ForegroundColor value
     */
    public final String getForegroundColor(HttpServletRequest req, HttpServletResponse res, JspWriter out) {
        try {
            IXml layoutXml = getLayoutXml(req, getUserName(req));
            ILayout layout = (ILayout) layoutXml.getRoot();
            String sFgColor = layout.getAttribute("fgcolor");

            if (sFgColor != null) {
                return sFgColor;
            }
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }

        return "";
    }


    /**
     *  Gets color of non-active tabs
     *
     *@param  req  Description of Parameter
     *@param  res  Description of Parameter
     *@param  out  Description of Parameter
     *@return      The TabColor value
     */
    public final String getTabColor(HttpServletRequest req, HttpServletResponse res, JspWriter out) {
        try {
            IXml layoutXml = getLayoutXml(req, getUserName(req));
            ILayout layout = (ILayout) layoutXml.getRoot();
            String sTabColor = layout.getAttribute("tabColor");

            if (sTabColor != null) {
                return sTabColor;
            }
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }

        return "";
    }


    /**
     *  Gets color of active tab
     *
     *@param  req  Description of Parameter
     *@param  res  Description of Parameter
     *@param  out  Description of Parameter
     *@return      The ActiveTabColor value
     */
    public final String getActiveTabColor(HttpServletRequest req, HttpServletResponse res, JspWriter out) {
        try {
            IXml layoutXml = getLayoutXml(req, getUserName(req));
            ILayout layout = (ILayout) layoutXml.getRoot();
            String sActiveTabColor = layout.getAttribute("activeTabColor");

            if (sActiveTabColor != null) {
                return sActiveTabColor;
            }
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }

        return "";
    }


    /**
     *  Gets color of channel heading background
     *
     *@param  req  Description of Parameter
     *@param  res  Description of Parameter
     *@param  out  Description of Parameter
     *@return      The ChannelHeadingColor value
     */
    public final String getChannelHeadingColor(HttpServletRequest req, HttpServletResponse res, JspWriter out) {
        try {
            IXml layoutXml = getLayoutXml(req, getUserName(req));
            ILayout layout = (ILayout) layoutXml.getRoot();
            String sChannelHeadingColor = layout.getAttribute("channelHeadingColor");

            if (sChannelHeadingColor != null) {
                return sChannelHeadingColor;
            }
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }

        return "";
    }


    /**
     *  Initializes and returns an instance of a channel, or gets it from a
     *  member hashtable if the channel has been previously initalized.
     *
     *@param  channel  object from layout XML
     *@return          portal channel object
     */
    public final org.jasig.portal.IChannel getChannelInstance(org.jasig.portal.layout.IChannel channel) {
        return channelCache.getChannel(channel);
    }


    /**
     *  Returns an instance of a channel, which comes from a member hashtable
     *  assuming it has been previously initalized.
     *
     *@param  sChannelID  Description of Parameter
     *@return             portal channel object
     */
    public final org.jasig.portal.IChannel getChannelInstance(String sChannelID) {
        org.jasig.portal.IChannel channel = null;
        // attempt to get the channel directly with the ID
        channel = channelCache.getChannel(sChannelID);

        // a null value means that the channel isn't actively cached, so
        // we must find it in the layout and use the alternative method
        if (channel == null) {
            // get the layout and convert it to a layout.ILayout object
            IXml layoutXml = layoutCache.getLayoutXml(sPathToLayoutDtd, sLayoutDtd);
            ILayout layout = (ILayout) layoutXml.getRoot();
            // get the tabs in the layout and start searching through them
            ITab[] tabs = layout.getTabs();
            found_channel :
            // this is jumped to from a deep nested for loop
            for (int curTab = 0; curTab < tabs.length; curTab++) {
                //get the columns in the layout and start searching through them
                IColumn[] columns = tabs[curTab].getColumns();
                for (int curCol = 0; curCol < columns.length; curCol++) {
                    // try to get the channel based on the ID from the list of channels
                    org.jasig.portal.layout.IChannel[] channels = columns[curCol].getChannels();
                    for (int curChan = 0; curChan < channels.length; curChan++) {
                        String sChannelInstanceID = channels[curChan].getInstanceIDAttribute();
                        if (sChannelID.equals(sChannelInstanceID)) {
                            //found it!  Time to break out of this set of loops
                            channel = channelCache.getChannel(channels[curChan]);
                            break found_channel;
                        }
                    }
                }
            }
        }

        // TODO:  Maybe instantiate a ChannelNotFound channel if channel is still null?
        return channel;
    }


    /**
     *  Get a unique identifier for the channel instance.
     *
     *@param  channel  object from layout XML
     *@return          a unique identifier for the channel instance
     */
    public final String getChannelID(org.jasig.portal.layout.IChannel channel) {
        return channelCache.getChannelID(channel);
    }


    /**
     *  Get the ID of the channel that was assigned at publish time
     *
     *@param  channel  object from layout XML
     *@return          the ID that was assigned at publish time
     */
    public final String getGlobalChannelID(org.jasig.portal.layout.IChannel channel) {
        try {
            String sGlobalChannelID = channel.getGlobalChannelIDAttribute();

            return (sGlobalChannelID);
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
            return (null);
        }
    }


    /**
     *  Gets the username from the session
     *
     *@param  req  Description of Parameter
     *@return      the username
     */
    public final String getUserName(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (String) session.getAttribute("userName");
    }


    /**
     *  Gets the Person object from the session
     *
     *@param  req  Description of Parameter
     *@return      the Person object
     */
    public final IPerson getPerson(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (IPerson) session.getAttribute("Person");
    }


    /**
     *  Writes a style tag and an html body tag with colors set according to
     *  user preferences Modified by Princeton University June 2000 (C) by Debra
     *  Rundle
     *
     *@param  req  Description of Parameter
     *@param  res  Description of Parameter
     *@param  out  Description of Parameter
     */
    public final void writeBodyStyle(HttpServletRequest req, HttpServletResponse res, JspWriter out) {
        try {
            IXml layoutXml = getLayoutXml(req, getUserName(req));
            ILayout layout = (ILayout) layoutXml.getRoot();
            String sBgColor = layout.getAttribute("bgcolor");
            String sFgColor = layout.getAttribute("fgcolor");
            String sATabColor = layout.getAttribute("activeTabColor");
            String sTabColor = layout.getAttribute("tabColor");
            String sChanColor = layout.getAttribute("channelHeadingColor");

            out.println("  <style type=\"text/css\">");
            out.println("    body      { background: " + sBgColor + "; }");
            out.println("A:link { color: " + sATabColor + "}");
            out.println("A:visited { color: " + sTabColor + "}");
            out.println("A:active { color: " + sChanColor + "}");
            out.println("    .PortalTitleText { color: " + sFgColor + "}");

            int iBgColor = Integer.parseInt(sBgColor.substring(1), 16);
            int iColorMask = Integer.parseInt("FFFFFF", 16);

            iBgColor = iBgColor ^ iColorMask;

            String sPortalTextColor = "#" + Integer.toHexString(iBgColor).toUpperCase();

            out.println("    .PortalText      { color: " + sPortalTextColor + "}");
            out.println("  </style>");

            out.println("</head>");

            /*
             * <BODY> tag is deprecated.   However Netscape does not properly implement the style tag
             * and ignores the color setting for the body.   When browser versions do handle this, you should remove
             * this redundant body tag code below.
             */
            out.println("<body bgcolor=\"" + sBgColor + "\" link=\"" + sATabColor + "\" vlink=\"" + sTabColor + "\" alink=\"" + sChanColor + "\">");
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, "LayoutBean: writeBodyStyle " + e);
        }
    }



    /**
     *  This method is used to reset the in-memory version of the layoutXml
     *  document if modifications need to be aborted. This is done by the
     *  personalizeTabs.jsp, personalizeLayout.jsp, subscribe.jsp and
     *  SubscriberBean to abort changes the user has made. WARNING: You must
     *  call getLayoutXml before you can call this method. Think about it: How
     *  can you reload something that isn't loaded yet? Everything will still
     *  work, but you'll get a ton of ERROR log messages if you don't follow the
     *  rules.
     *
     *@author    Zed A. Shaw
     */
    public final void reloadLayoutXml() {
        try {
            layoutCache.reloadLayoutXml();
        }
        catch (NullPointerException e) {
            Logger.log(Logger.ERROR, "reloadLayoutXml cache called before getLayoutXml: " + e);
        }
    }


    /**
     *  This method is used to prevent the garbage colletor from collecting the
     *  layoutXml document from the layoutCache while we are modifying the
     *  in-memory copy. It is used by personalizeLayout.jsp,
     *  personalizeTabs.jsp, subscribe.jsp, and SubscriberBean. WARNING: Do not
     *  call this before you have called getLayoutXml at least once. How can you
     *  protect a layout that isn't loaded yet? If you do, you'll get an ERROR
     *  message in the logs. This method has no effect if you set
     *  session.memory.layoutxml_dynamic_loading to "no" since the GC can't
     *  collect the layouts anyway. In this instance, the function will just
     *  return, but you should still call it in case the option is set to "yes"
     *  in the future.
     *
     *@author    Zed A. Shaw
     */
    public final void protectLayoutXml() {
        try {
            layoutCache.setClaimable(false);
        }
        catch (NullPointerException e) {
            Logger.log(Logger.ERROR, "protectLayout called before getLayoutXml: " + e);
        }
    }


    /**
     *  This method is called when you are done working with the in-memory copy
     *  of the layoutXml document and wish to let the garbage collector manage
     *  its existence. It is used by personalizeLayout.jsp, personalizeTabs.jsp,
     *  subscribe.jsp, and SubscriberBean. WARNING: Do not call this before you
     *  have called getLayoutXml at least once. If you do, you'll get an ERROR
     *  message in the logs. This method has no effect if you set
     *  session.memory.layoutxml_dynamic_loading to "no" since the GC can't
     *  collect the layouts anyway. In this instance, the function will just
     *  return, but you should still call it in case the option is set to "yes"
     *  in the future.
     *
     *@author    Zed A. Shaw
     */
    public final void releaseLayoutXml() {
        try {
            layoutCache.setClaimable(true);
        }
        catch (NullPointerException e) {
            Logger.log(Logger.ERROR, "releaseLayout called before getLayoutXml: " + e);
        }
    }


    /**
     *  Displays tabs
     *
     *@param  req  Description of Parameter
     *@param  res  Description of Parameter
     *@param  out  Description of Parameter
     */
    public final void writeTabs(HttpServletRequest req, HttpServletResponse res, JspWriter out) {
        try {
            out.println("<!-- Tabs -->");
            out.println("<table border=0 width=\"100%\" cellspacing=0 cellpadding=0>");
            out.println("<tr>");

            IXml layoutXml = getLayoutXml(req, getUserName(req));
            ILayout layout = (ILayout) layoutXml.getRoot();

            // Get Tabs
            ITab[] tabs = layout.getTabs();

            int iTab = getActiveTab(req);
            ITab activeTab = getTab(req, iTab);

            String sBgcolor = null;
            String sTabName = activeTab.getAttribute("name");
            String sActiveTab = activeTab.getAttribute("name");
            String sTabColor = layout.getAttribute("tabColor");
            String sActiveTabColor = layout.getAttribute("activeTabColor");

            for (int i = 0; i < tabs.length; i++) {
                sTabName = tabs[i].getAttribute("name");
                sBgcolor = i == iTab ? sActiveTabColor : sTabColor;

                out.println("<td bgcolor=\"" + sBgcolor + "\" align=center width=\"20%\">");
                out.println("  <table bgcolor=\"" + sBgcolor + "\" border=0 width=\"100%\" cellspacing=0 cellpadding=2>");
                out.println("    <tr align=center>");

                if (i == iTab) {
                    out.println("      <td><span class=\"PortalTitleText\">&nbsp;" + sTabName + "</span>&nbsp;</td>");
                }
                else {
                    out.println("      <td><a href=\"layout.jsp?tab=" + i + "\"><span CLASS=\"PortalTitleText\">" + sTabName + "</span></a>&nbsp;</td>");
                }

                out.println("    </tr>");
                out.println("  </table>");
                out.println("</td>");
                out.println("<td width=\"1%\">&nbsp;</td>");
            }

            // Area to the right of the tabs
            out.println("<td width=\"98%\">&nbsp;</td>");

            out.println("</tr>");

            // This is the strip beneath the tabs
            out.println("<!-- Strip beneath tabs -->");
            out.println("<tr><td width=\"100%\" colspan=\"" + (2 * tabs.length + 1) + "\">");
            out.println("  <table border=0 cellspacing=0 width=\"100%\">");
            out.println("    <tr><td bgcolor=\"" + sActiveTabColor + "\">");
            out.println("      <table border=0 cellspacing=0 cellpadding=0><tr><td height=3></td></tr></table>");
            out.println("    </td></tr>");
            out.println("  </table>");
            out.println("</td></tr>");

            out.println("</table>");
            out.println("<br>");
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }



    /**
     *  Displays channels
     *
     *@param  req  Description of Parameter
     *@param  res  Description of Parameter
     *@param  out  Description of Parameter
     */
    public final void writeChannels(HttpServletRequest req, HttpServletResponse res, JspWriter out) {
        try {
            AuthorizationBean authorizationBean = new AuthorizationBean();

            int iTab = getActiveTab(req);
            ITab activeTab = getTab(req, iTab);

            HttpSession session = req.getSession(false);

            if (activeTab != null) {
                out.println("<!-- Channels -->");
                out.println("<table border=0 cellpadding=0 cellspacing=0 width=\"100%\">");
                out.println("  <tr>");

                IColumn[] columns = activeTab.getColumns();

                for (int iCol = 0; iCol < columns.length; iCol++) {
                    out.println("    <td valign=top width=\"" + columns[iCol].getAttribute("width") + "\">");

                    // Get channels for column iCol
                    org.jasig.portal.layout.IChannel[] channels = columns[iCol].getChannels();

                    for (int iChan = 0; iChan < channels.length; iChan++) {
                        // Get the ID given to the channel when it was published
                        String sGlobalChannelID = getGlobalChannelID(channels[iChan]);

                        // Remove the channel from the user's layout if there is a channelID and the user fails authorization
                        if (!readOnly && sGlobalChannelID != null && !authorizationBean.canUserRender(getPerson(req), Integer.parseInt(sGlobalChannelID))) {
                            // Remove the channel from the user's layout
                            columns[iCol].removeChannel(channels[iChan]);

                            // Save the user's layout
                            setLayoutXml(getUserName(req), getLayoutXml(req, getUserName(req)));
                        }
                        else {
                            org.jasig.portal.IChannel ch = getChannelInstance(channels[iChan]);

                            // Check for minimized, maximized, added or removed channel
                            String sResize = req.getParameter("resize");
                            String sTab = req.getParameter("tab");
                            String sColumn = req.getParameter("column");
                            String sChannel = req.getParameter("channel");

                            if (sResize != null && !readOnly && iTab == Integer.parseInt(sTab) && iCol == Integer.parseInt(sColumn) && iChan == Integer.parseInt(sChannel)) {
                                if (sResize.equals("minimize")) {
                                    channels[iChan].setAttribute("minimized", "true");
                                }
                                else
                                        if (sResize.equals("maximize")) {
                                    channels[iChan].setAttribute("minimized", "false");
                                }
                                else
                                        if (sResize.equals("remove")) {
                                    columns[iCol].removeChannel(channels[iChan]);

                                    // Save the user's layout (readOnly is tested for in parent if statement)
                                    setLayoutXml(getUserName(req), getLayoutXml(req, getUserName(req)));

                                    continue;
                                }
                                else
                                        if (sResize.equals("hide") && channels[iChan].getAttribute("hidden") != null) {
                                    channels[iChan].setAttribute("hidden", "true");
                                }
                                else
                                        if (sResize.equals("unhide") && channels[iChan].getAttribute("hidden") != null) {
                                    channels[iChan].setAttribute("hidden", "false");
                                }
                            }

                            String hiddenAttribute = channels[iChan].getAttribute("hidden");
                            if (hiddenAttribute != null && hiddenAttribute.equals("false")) {
                                // Channel heading
                                IXml layoutXml = getLayoutXml(req, getUserName(req));
                                ILayout layout = (ILayout) layoutXml.getRoot();

                                out.println("<table border=0 cellpadding=1 cellspacing=4 width=\"100%\">");
                                out.println("  <tr>");
                                out.println("    <td bgcolor=" + layout.getAttribute("channelHeadingColor") + ">");

                                out.println("      <table border=0 cellpadding=0 cellspacing=0 width=\"100%\" bgcolor=\"" + layout.getAttribute("channelHeadingColor") + "\">");
                                out.println("        <tr>");
                                out.println("          <td>");
                                out.println("            <span CLASS=\"PortalTitleText\">&nbsp;" + ch.getName() + "</span>");
                                out.println("          </td>");
                                out.println("          <td nowrap valign=\"middle\" align=right>");
                                out.println("            &nbsp;");

                                // Channel control buttons
                                if (channels[iChan].getAttribute("minimized").equals("true")) {
                                    out.println("<a href=\"layout.jsp?tab=" + iTab + "&column=" + iCol + "&channel=" + iChan + "&resize=maximize\"><img border=0 width=\"18\" height=\"15\" src=\"images/maximize.gif\" alt=\"Maximize\"></a>");
                                }
                                else
                                        if (ch.isMinimizable()) {
                                    out.println("<a href=\"layout.jsp?tab=" + iTab + "&column=" + iCol + "&channel=" + iChan + "&resize=minimize\"><img border=0 width=\"18\" height=\"15\" src=\"images/minimize.gif\" alt=\"Minimize\"></a>");
                                }

                                if (ch.isDetachable()) {
                                    out.println("<a href=\"JavaScript:openWin(\'detach.jsp?tab=" + iTab + "&column=" + iCol + "&channel=" + iChan + "\', \'detachedWindow\', " + ch.getDefaultDetachWidth() + ", " + ch.getDefaultDetachHeight() + ")\"><img border=0 width=\"18\" height=\"15\" src=\"images/detach.gif\" alt=\"Detach\"></a>");
                                }

                                if (ch.isRemovable()) {
                                    out.println("<a href=\"layout.jsp?tab=" + iTab + "&column=" + iCol + "&channel=" + iChan + "&resize=remove\"><img border=0 width=\"18\" height=\"15\" src=\"images/remove.gif\" alt=\"Remove\"></a>");
                                }

                                if (ch.isEditable()) {
                                    out.println("<a href=\"" + DispatchBean.buildURL("edit", getChannelID(channels[iChan])) + "\"><img border=0 width=\"28\" height=\"15\" src=\"images/edit.gif\" alt=\"Edit\"></a>");
                                }

                                if (ch.hasHelp()) {
                                    out.println("<a href=\"" + DispatchBean.buildURL("help", getChannelID(channels[iChan])) + "\"><img border=0 width=\"18\" height=\"15\" src=\"images/help.gif\" alt=\"Help\"></a>");
                                }

                                out.println("            &nbsp;");
                                out.println("          </td>");
                                out.println("        </tr>");
                                out.println("      </table>");

                                // Channel body
                                out.println("      <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
                                out.println("        <tr>");
                                out.println("          <td bgcolor=\"#ffffff\">");

                                out.println("            <table border=0 cellpadding=3 cellspacing=0 width=\"100%\" bgcolor=\"#ffffff\">");
                                out.println("              <tr>");
                                out.println("                <td valign=top>");
                                out.println("                  <div class=\"PortalChannelText\">");

                                if (channels[iChan].getAttribute("minimized").equals("false")) {
                                    // Render channel contents
                                    try {
                                        ch.render(req, res, out);
                                    }
                                    catch (Throwable e) {
                                        // if we get any kind of exception from the channel, don't log it
                                        // this is just a temporary thing to test how this affects performance
                                        out.println("<p>Caught exception: " + e);
                                    }
                                }
                                else {
                                    // Channel is minimized -- don't render it
                                }

                                out.println("                  </div>");
                                out.println("                </td>");
                                out.println("              </tr>");
                                out.println("            </table>");

                                out.println("          </td>");
                                out.println("        </tr>");
                                out.println("      </table>");

                                out.println("    </td>");
                                out.println("  </tr>");
                                out.println("</table>");
                            }
                        }
                    }
                    out.println("    </td>");
                }

                out.println("  </tr>");
                out.println("</table>");
            }
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Presents a GUI for manipulating the layout of a tab.
     *
     *@param  req  Description of Parameter
     *@param  res  Description of Parameter
     *@param  out  Description of Parameter
     */
    public final void writePersonalizeLayoutPage(HttpServletRequest req, HttpServletResponse res, JspWriter out) {
        try {
            IXml layoutXml = getLayoutXml(req, getUserName(req));
            ILayout layout = (ILayout) layoutXml.getRoot();

            // Get Tabs
            ITab[] tabs = layout.getTabs();

            String sTabName = null;
            int iTab;

            // Get tab to personalize from the request if it's there,
            // otherwise use the active tab
            try {
                iTab = Integer.parseInt(req.getParameter("tab"));
            }
            catch (NumberFormatException nfe) {
                iTab = getActiveTab(req);
            }

            sTabName = tabs[iTab].getAttribute("name");

            out.println("<form name=\"tabControls\" action=\"personalizeLayout.jsp\" method=post>");
            out.println("Tab " + (iTab + 1) + ": ");

            // Rename tab
            out.println("<input type=hidden name=\"action\" value=\"renameTab\">");
            out.println("<input type=hidden name=\"tab\" value=\"" + iTab + "\">");
            out.println("<input type=text name=\"tabName\" value=\"" + sTabName + "\" onBlur=\"document.tabControls.submit()\">");

            // Set tab as default
            int iDefaultTab;

            try {
                iDefaultTab = Integer.parseInt(layout.getActiveTabAttribute());
            }
            catch (NumberFormatException ne) {
                iDefaultTab = 0;
            }
            out.println("<input type=radio name=\"defaultTab\" onClick=\"location='personalizeLayout.jsp?action=setDefaultTab&tab=" + iTab + "'\"" + (iDefaultTab == iTab ? " checked" : "") + ">Set as default");

            out.println("</form>");

            // Get the columns for this tab
            IColumn[] columns = tabs[iTab].getColumns();

            // Fill columns with channels
            out.println("<table border=0 cellpadding=3 cellspacing=3>");
            out.println("<tr bgcolor=\"#dddddd\">");

            for (int iCol = 0; iCol < columns.length; iCol++) {
                out.println("<td>");
                out.println("Column " + (iCol + 1));

                // Move column left
                if (iCol > 0) {
                    out.println("<a href=\"personalizeLayout.jsp?action=moveColumnLeft&tab=" + iTab + "&column=" + iCol + "\">");
                    out.println("<img src=\"images/left.gif\" border=0 alt=\"Move column left\"></a>");
                }

                // Remove column
                out.println("<a href=\"personalizeLayout.jsp?action=removeColumn&tab=" + iTab + "&column=" + iCol + "\">");
                out.println("<img src=\"images/remove.gif\" border=0 alt=\"Remove column\"></a>");

                // Move column right
                if (iCol < columns.length - 1) {
                    out.println("<a href=\"personalizeLayout.jsp?action=moveColumnRight&tab=" + iTab + "&column=" + iCol + "\">");
                    out.println("<img src=\"images/right.gif\" border=0 alt=\"Move column right\"></a>");
                }

                // Column width
                String sWidth = columns[iCol].getAttribute("width");
                String sDisplayWidth = sWidth;

                if (sWidth.endsWith("%")) {
                    sDisplayWidth = sWidth.substring(0, sWidth.length() - 1);
                }

                out.println("<form name=\"columnWidth" + iTab + "_" + iCol + "\" action=\"personalizeLayout.jsp\" method=post>");
                out.println("<input type=hidden name=action value=\"setColumnWidth\">");
                out.println("<input type=hidden name=tab value=\"" + iTab + "\">");
                out.println("<input type=hidden name=column value=\"" + iCol + "\">");
                out.println("Width ");
                out.println("<input type=text name=\"columnWidth\" value=\"" + sDisplayWidth + "\" size=4 onBlur=\"document.columnWidth" + iTab + "_" + iCol + ".submit()\">");
                out.println("<select name=\"columnWidthType\" onChange=\"document.columnWidth" + iTab + "_" + iCol + ".submit()\">");
                out.println("<option value=\"\"" + (sWidth.endsWith("%") ? "" : " selected") + ">Pixels</option>");
                out.println("<option value=\"%\"" + (sWidth.endsWith("%") ? " selected" : "") + ">%</option>");
                out.println("</select>");
                out.println("</form>");
                out.println("<hr noshade>");

                out.println("<table><tr>");
                out.println("<td align=center>");

                out.println("<form name=\"channels" + iTab + "_" + iCol + "\" action=\"personalizeLayout.jsp\" method=post>");

                // Move channel left
                if (iCol > 0) {
                    out.println("<a href=\"javascript:getActionAndSubmit (document.channels" + iTab + "_" + iCol + ", 'moveChannelLeft')\"><img src=\"images/left.gif\" border=0 alt=\"Move channel left\"></a>&nbsp;");
                }

                // Remove channel
                out.println("<a href=\"javascript:getActionAndSubmit (document.channels" + iTab + "_" + iCol + ", 'removeChannel')\"><img src=\"images/remove.gif\" border=0 alt=\"Remove channel\"></a>&nbsp;");

                // Move channel right
                if (iCol < columns.length - 1) {
                    out.println("<a href=\"javascript:getActionAndSubmit (document.channels" + iTab + "_" + iCol + ", 'moveChannelRight')\"><img src=\"images/right.gif\" border=0 alt=\"Move channel right\"></a>");
                }

                out.println("<br>");
                out.println("<select name=\"channel\" size=10>");

                // Get the channels for this column
                org.jasig.portal.layout.IChannel[] channels = columns[iCol].getChannels();

                // List channels for this column
                for (int iChan = 0; iChan < channels.length; iChan++) {
                    org.jasig.portal.IChannel ch = getChannelInstance(channels[iChan]);
                    out.println("<option value=\"" + iChan + "\">" + ch.getName() + "</option>");
                }

                out.println("</select>");
                out.println("</td>");
                out.println("<td>");

                // Move channel up
                out.println("<a href=\"javascript:getActionAndSubmit (document.channels" + iTab + "_" + iCol + ", 'moveChannelUp')\"><img src=\"images/up.gif\" border=0 alt=\"Move channel up\"></a><br><br>");

                // Remove channel
                out.println("<a href=\"javascript:getActionAndSubmit (document.channels" + iTab + "_" + iCol + ", 'removeChannel')\"><img src=\"images/remove.gif\" border=0 alt=\"Remove channel\"></a><br><br>");

                // Move channel down
                out.println("<a href=\"javascript:getActionAndSubmit (document.channels" + iTab + "_" + iCol + ", 'moveChannelDown')\"><img src=\"images/down.gif\" border=0 alt=\"Move channel down\"></a>");

                out.println("</td>");
                out.println("</tr></table>");
                out.println("<input type=hidden name=\"tab\" value=\"" + iTab + "\">");
                out.println("<input type=hidden name=\"column\" value=\"" + iCol + "\">");
                out.println("<input type=hidden name=\"action\" value=\"none\">");
                out.println("</form>");

                out.println("</td>");
            }

            out.println("</tr>");
            out.println("</table>");

            // Add a new column for this tab
            out.println("<form action=\"personalizeLayout.jsp\" method=post>");
            out.println("<input type=hidden name=\"tab\" value=\"" + iTab + "\">");
            out.println("<input type=hidden name=\"action\" value=\"addColumn\">");
            out.println("<input type=submit name=\"submit\" value=\"Add\">");
            out.println("new column");
            out.println("<select name=\"column\">");

            for (int iCol = 0; iCol < columns.length; iCol++) {
                out.println("<option value=" + iCol + ">before column " + (iCol + 1) + "</option>");
            }

            out.println("<option value=\"" + columns.length + "\" selected>at the end</option>");
            out.println("</select>");
            out.println("&nbsp;&nbsp;&nbsp;&nbsp;");

            // Revert to default layout xml
            out.println("[<a href=\"personalizeLayout.jsp?action=revertToDefaultLayoutXml\">Revert to default layout</a>]");

            out.println("</form>");
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Removes a channel instance from the member hashtable htChannelInstances.
     *
     *@param  sChannelID  Description of Parameter
     */
    public final void removeChannelInstance(String sChannelID) {
        try {
            channelCache.removeChannel(sChannelID);
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Adds a tab at the desired location
     *
     *@param  req  The feature to be added to the Tab attribute
     */
    public final void addTab(HttpServletRequest req) {
        try {
            int iTab = Integer.parseInt(req.getParameter("tab"));
            String sNewTabName = "New Tab";

            IXml layoutXml = getLayoutXml(req, getUserName(req));
            ILayout layout = (ILayout) layoutXml.getRoot();

            // Get a new tab and set its name
            ITab tab = Factory.newTab();
            tab.setNameAttribute(sNewTabName);

            // Get a new column and set its width
            IColumn column = Factory.newColumn();
            column.setWidthAttribute("100%");
            tab.addColumn(column);
            layout.insertTabAt(tab, iTab);
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Renames a tab at the desired location
     *
     *@param  req  Description of Parameter
     */
    public final void renameTab(HttpServletRequest req) {
        try {
            int iTab = Integer.parseInt(req.getParameter("tab"));
            String sTabNameEntered = req.getParameter("tabName");
            String sTabName = UtilitiesBean.removeSpecialChars(sTabNameEntered);

            if (sTabName == null || sTabName.length() == 0) {
                sTabName = "Blank Tab";
            }

            IXml layoutXml = getLayoutXml(req, getUserName(req));
            ILayout layout = (ILayout) layoutXml.getRoot();

            ITab tabToRename = layout.getTabAt(iTab);
            tabToRename.setNameAttribute(sTabName);
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Removes a tab at the desired location
     *
     *@param  req  Description of Parameter
     */
    public final void removeTab(HttpServletRequest req) {
        try {
            int iTab = Integer.parseInt(req.getParameter("tab"));

            IXml layoutXml = getLayoutXml(req, getUserName(req));
            ILayout layout = (ILayout) layoutXml.getRoot();

            layout.removeTabAt(iTab);
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Move the tab at the desired location down
     *
     *@param  req  Description of Parameter
     */
    public final void moveTabDown(HttpServletRequest req) {
        try {
            int iTab = Integer.parseInt(req.getParameter("tab"));

            IXml layoutXml = getLayoutXml(req, getUserName(req));
            ILayout layout = (ILayout) layoutXml.getRoot();
            ITab tabToMoveDown = layout.getTabAt(iTab);

            // Only move tab if it isn't already at the bottom (right)
            if (iTab < layout.getTabCount() - 1) {
                layout.removeTabAt(iTab);
                layout.insertTabAt(tabToMoveDown, iTab + 1);
            }
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Move the tab at the desired location up
     *
     *@param  req  Description of Parameter
     */
    public final void moveTabUp(HttpServletRequest req) {
        try {
            int iTab = Integer.parseInt(req.getParameter("tab"));

            IXml layoutXml = getLayoutXml(req, getUserName(req));
            ILayout layout = (ILayout) layoutXml.getRoot();
            ITab tabToMoveUp = layout.getTabAt(iTab);

            // Only move tab if it isn't already at the top (left)
            if (iTab > 0) {
                layout.removeTabAt(iTab);
                layout.insertTabAt(tabToMoveUp, iTab - 1);
            }
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Adds a column at the desired location
     *
     *@param  req  The feature to be added to the Column attribute
     */
    public final void addColumn(HttpServletRequest req) {
        try {
            int iTab = Integer.parseInt(req.getParameter("tab"));
            int iCol = Integer.parseInt(req.getParameter("column"));

            ITab tab = getTab(req, iTab);

            // Get a new column and set its width
            IColumn column = Factory.newColumn();
            column.setWidthAttribute("100%");
            tab.insertColumnAt(column, iCol);
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Removes a column at the desired location
     *
     *@param  req  Description of Parameter
     */
    public final void removeColumn(HttpServletRequest req) {
        try {
            int iTab = Integer.parseInt(req.getParameter("tab"));
            int iCol = Integer.parseInt(req.getParameter("column"));

            ITab tab = getTab(req, iTab);
            tab.removeColumnAt(iCol);
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Move the column at the desired location right
     *
     *@param  req  Description of Parameter
     */
    public final void moveColumnRight(HttpServletRequest req) {
        try {
            int iTab = Integer.parseInt(req.getParameter("tab"));
            int iCol = Integer.parseInt(req.getParameter("column"));

            ITab tab = getTab(req, iTab);
            IColumn colToMoveRight = getColumn(req, iTab, iCol);
            tab.removeColumnAt(iCol);
            tab.insertColumnAt(colToMoveRight, iCol + 1);
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Move the column at the desired location left
     *
     *@param  req  Description of Parameter
     */
    public final void moveColumnLeft(HttpServletRequest req) {
        try {
            int iTab = Integer.parseInt(req.getParameter("tab"));
            int iCol = Integer.parseInt(req.getParameter("column"));

            ITab tab = getTab(req, iTab);
            IColumn colToMoveLeft = getColumn(req, iTab, iCol);
            tab.removeColumnAt(iCol);
            tab.insertColumnAt(colToMoveLeft, iCol - 1);
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Minimize a channel
     *
     *@param  req  Description of Parameter
     */
    public final void minimizeChannel(HttpServletRequest req) {
        try {
            int iTab = Integer.parseInt(req.getParameter("tab"));
            int iCol = Integer.parseInt(req.getParameter("column"));
            int iChan = Integer.parseInt(req.getParameter("channel"));

            org.jasig.portal.layout.IChannel channel = getChannel(req, iTab, iCol, iChan);
            channel.setMinimizedAttribute("true");
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Maximize a channel
     *
     *@param  req  Description of Parameter
     */
    public final void maximizeChannel(HttpServletRequest req) {
        try {
            int iTab = Integer.parseInt(req.getParameter("tab"));
            int iCol = Integer.parseInt(req.getParameter("column"));
            int iChan = Integer.parseInt(req.getParameter("channel"));

            org.jasig.portal.layout.IChannel channel = getChannel(req, iTab, iCol, iChan);
            channel.setMinimizedAttribute("false");
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Removes a channel
     *
     *@param  req  Description of Parameter
     */
    public final void removeChannel(HttpServletRequest req) {
        try {
            int iTab = Integer.parseInt(req.getParameter("tab"));
            int iCol = Integer.parseInt(req.getParameter("column"));
            int iChan = Integer.parseInt(req.getParameter("channel"));

            IColumn column = getColumn(req, iTab, iCol);
            column.removeChannelAt(iChan);
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Adds a channel to layout.xml
     *
     *@param  req  The feature to be added to the Channel attribute
     */
    public final void addChannel(HttpServletRequest req) {
        HttpSession ses = req.getSession(false);
        SubscriberBean subscribe = (SubscriberBean) ses.getAttribute("subscribe");
        try {
            int iTab = getActiveTab(req);
            int iCol = Integer.parseInt(req.getParameter("column"));

            IColumn column = getColumn(req, iTab, iCol);
            if (subscribe == null) {
                subscribe = new SubscriberBean();
                subscribe.setChannel(req);
                column.addChannel(subscribe.channel);
                //column.addChannel(subscribe.getChannel(req));
            }
            else {
                column.addChannel(subscribe.channel);
                ses.removeAttribute("subscribe");
            }
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Moves a channel to the bottom of the list of the column to the left
     *
     *@param  req  Description of Parameter
     */
    public final void moveChannelLeft(HttpServletRequest req) {
        try {
            int iTab = Integer.parseInt(req.getParameter("tab"));
            int iCol = Integer.parseInt(req.getParameter("column"));
            int iChan = Integer.parseInt(req.getParameter("channel"));

            IColumn column = getColumn(req, iTab, iCol);
            IColumn columnToTheLeft = getColumn(req, iTab, iCol - 1);
            org.jasig.portal.layout.IChannel channelToMoveLeft = column.getChannelAt(iChan);

            column.removeChannelAt(iChan);
            columnToTheLeft.addChannel(channelToMoveLeft);
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Moves a channel to the bottom of the list of the column to the right
     *
     *@param  req  Description of Parameter
     */
    public final void moveChannelRight(HttpServletRequest req) {
        try {
            int iTab = Integer.parseInt(req.getParameter("tab"));
            int iCol = Integer.parseInt(req.getParameter("column"));
            int iChan = Integer.parseInt(req.getParameter("channel"));

            IColumn column = getColumn(req, iTab, iCol);
            IColumn columnToTheRight = getColumn(req, iTab, iCol + 1);
            org.jasig.portal.layout.IChannel channelToMoveRight = column.getChannelAt(iChan);

            column.removeChannelAt(iChan);
            columnToTheRight.addChannel(channelToMoveRight);
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Moves a channel up a position
     *
     *@param  req  Description of Parameter
     */
    public final void moveChannelUp(HttpServletRequest req) {
        try {
            int iTab = Integer.parseInt(req.getParameter("tab"));
            int iCol = Integer.parseInt(req.getParameter("column"));
            int iChan = Integer.parseInt(req.getParameter("channel"));

            IColumn column = getColumn(req, iTab, iCol);
            org.jasig.portal.layout.IChannel channelToMoveUp = column.getChannelAt(iChan);

            // Only move channel if it isn't already at the top
            if (iChan > 0) {
                column.removeChannelAt(iChan);
                column.insertChannelAt(channelToMoveUp, iChan - 1);
            }
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Moves a channel down a position
     *
     *@param  req  Description of Parameter
     */
    public final void moveChannelDown(HttpServletRequest req) {
        try {
            int iTab = Integer.parseInt(req.getParameter("tab"));
            int iCol = Integer.parseInt(req.getParameter("column"));
            int iChan = Integer.parseInt(req.getParameter("channel"));

            IColumn column = getColumn(req, iTab, iCol);
            org.jasig.portal.layout.IChannel channelToMoveDown = column.getChannelAt(iChan);

            // Only move channel if it isn't already at the bottom
            if (iChan < column.getChannelCount() - 1) {
                column.removeChannelAt(iChan);
                column.insertChannelAt(channelToMoveDown, iChan + 1);
            }
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Checks with the authorization system to see whether the specified user
     *  can publish a channel or not
     *
     *@param  person  Description of Parameter
     *@return         Description of the Returned Value
     */
    public final boolean canUserPublish(IPerson person) {
        try {
            AuthorizationBean authorizationBean = new AuthorizationBean();
            boolean authorized = authorizationBean.canUserPublish(person);

            return (authorized);
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
            return (false);
        }
    }



    /**
     *  Helps manage the removal of portal sessions from the SessionManager's
     *  listings.
     *
     *@param  event  Description of Parameter
     */
    public final void valueBound(HttpSessionBindingEvent event) {
        HttpSession session = event.getSession();
        String userName = (String) session.getAttribute("userName");

        // don't do anything if there is no user name
        if (userName == null) {
            return;
        }

        String sessionID = session.getId();
        String sessionType = "PORTAL";

        // tell the session manager that the user is logging in
        SessionManager.login(userName, session, sessionType);
    }


    /**
     *  Helps manage the removal of portal sessions from the SessionManager's
     *  listings.
     *
     *@param  event  Description of Parameter
     */
    public final void valueUnbound(HttpSessionBindingEvent event) {
        HttpSession session = event.getSession();

        // now just tell the SessionManager we have logged out
        SessionManager.logout(session, "PORTAL");

    }


    /**
     *  Sets whether this layouBean is readOnly or not. This is done by the
     *  findLayoutInstance method when the user is a guest. It is meant to
     *  prevent guest users from making changes to the layout for all guests
     *  (since they all share one layout). It does two additional things: It
     *  turns off constrained caching of channels and dynamic loading of the
     *  layoutXml document. This is done to avoid performance hits for the guest
     *  users. This is private for right now, to prevent accidentally making
     *  this readwrite.
     *
     *@param  state  The new ReadOnly value
     */

    private final void setReadOnly(boolean state) {
        readOnly = state;
        // turn off the constrained caching so that they are loaded faster
        channelCache.setConstrainedCaching(false);

        // create a LayoutXmlCache right away for the guest user
        layoutCache = new LayoutXmlCache("guest");

        // make sure the layoutXmlCache is not claimable too
        layoutCache.setClaimable(false);
    }


    /**
     *  Returns the current read only state of this layout. Not really used by
     *  anyone internally.
     *
     *@return    The ReadOnly value
     *@author    $Author$
     */
    private final boolean getReadOnly() {
        return readOnly;
    }


    /**
     *  This is the method that JSP pages should use to get at the specific
     *  layoutBean for a user.
     *
     *@param  application  Description of Parameter
     *@param  session      Description of Parameter
     *@return              Description of the Returned Value
     *@author              Zed A. Shaw (zed.shaw@ubc.ca)
     */
    public final static ILayoutBean findLayoutInstance(ServletContext application, HttpSession session) {
        String username = (String) session.getAttribute("userName");
        org.jasig.portal.ILayoutBean layoutBean = null;

        // we do the check for the single_guest_layout configuration by seeing if it is NOT "no"
        // so that we'll default to "yes" and have a safer configuration.  Just in case the user doesn't do things right.
        if ((username == null || username.equals("guest")) && bSingleGuestLayout) {

            // it's a guest user, and we are using a single_guest_layout, so give them the default layoutBean
            layoutBean = (org.jasig.portal.ILayoutBean) application.getAttribute("layoutBean");
            if (layoutBean == null) {
                // use a regular LayoutBean so we can access the setReadOnly method
                LayoutBean layout = new org.jasig.portal.LayoutBean();
                layout.setReadOnly(true);
                layout.getLayoutXml(null, "guest");
                // guests cannot make changes
                // then cast it to our interface to restrict people from changing it
                layoutBean = (ILayoutBean) layout;

                application.setAttribute("layoutBean", layoutBean);
            }
        }
        else {
            // this is a user, they have their own layoutBean
            layoutBean = (org.jasig.portal.ILayoutBean) session.getAttribute("layoutBean");
            if (layoutBean == null) {
                layoutBean = new org.jasig.portal.LayoutBean();
                session.setAttribute("layoutBean", layoutBean);
            }
        }
        // the layoutBean should now either be a user bean, or the default for guests
        return layoutBean;
    }
}


