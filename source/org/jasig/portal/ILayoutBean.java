/**
 *  Copyright (c) 2000 The JA-SIG Collaborative. All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer. 2. Redistributions in
 *  binary form must reproduce the above copyright notice, this list of
 *  conditions and the following disclaimer in the documentation and/or other
 *  materials provided with the distribution. 3. Redistributions of any form
 *  whatsoever must retain the following acknowledgment: "This product includes
 *  software developed by the JA-SIG Collaborative (http://www.jasig.org/)."
 *  THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 *  EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR ITS CONTRIBUTORS
 *  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */

package org.jasig.portal;

import java.util.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import org.jasig.portal.layout.*;
import com.objectspace.xml.*;
import org.jasig.portal.security.IPerson;

/**
 *  Description of the Interface
 *
 *@author     zedshaw
 *@created    June 20, 2001
 */
public interface ILayoutBean {
    // Page generation
    /**
     *  Description of the Method
     *
     *@param  req  Description of Parameter
     *@param  res  Description of Parameter
     *@param  out  Description of Parameter
     *@since
     */
    public void writeBodyStyle(HttpServletRequest req, HttpServletResponse res, JspWriter out);


    /**
     *  Description of the Method
     *
     *@param  req  Description of Parameter
     *@param  res  Description of Parameter
     *@param  out  Description of Parameter
     *@since
     */
    public void writeTabs(HttpServletRequest req, HttpServletResponse res, JspWriter out);


    /**
     *  Description of the Method
     *
     *@param  req  Description of Parameter
     *@param  res  Description of Parameter
     *@param  out  Description of Parameter
     *@since
     */
    public void writeChannels(HttpServletRequest req, HttpServletResponse res, JspWriter out);


    /**
     *  Description of the Method
     *
     *@param  req  Description of Parameter
     *@param  res  Description of Parameter
     *@param  out  Description of Parameter
     *@since
     */
    public void writePersonalizeLayoutPage(HttpServletRequest req, HttpServletResponse res, JspWriter out);


    // Helper methods
    /**
     *  Gets the LayoutXml attribute of the ILayoutBean object
     *
     *@param  sUserName  Description of Parameter
     *@return            The LayoutXml value
     *@since
     */
    public IXml getLayoutXml(String sUserName);


    /**
     *  Sets the LayoutXml attribute of the ILayoutBean object
     *
     *@param  sUserName  The new LayoutXml value
     *@param  layoutXml  The new LayoutXml value
     *@since
     */
    public void setLayoutXml(String sUserName, IXml layoutXml);


    /**
     *  Description of the Method
     *
     *@since
     */
    public void reloadLayoutXml();


    /**
     *  Description of the Method
     *
     *@since
     */
    public void protectLayoutXml();


    /**
     *  Description of the Method
     *
     *@since
     */
    public void releaseLayoutXml();


    /**
     *  Gets the DefaultLayoutXml attribute of the ILayoutBean object
     *
     *@return    The DefaultLayoutXml value
     *@since
     */
    public IXml getDefaultLayoutXml();


    /**
     *  Gets the Tabs attribute of the ILayoutBean object
     *
     *@param  req  Description of Parameter
     *@return      The Tabs value
     *@since
     */
    public ITab[] getTabs(HttpServletRequest req);


    /**
     *  Gets the Tab attribute of the ILayoutBean object
     *
     *@param  req   Description of Parameter
     *@param  iTab  Description of Parameter
     *@return       The Tab value
     *@since
     */
    public ITab getTab(HttpServletRequest req, int iTab);


    /**
     *  Gets the Column attribute of the ILayoutBean object
     *
     *@param  req   Description of Parameter
     *@param  iTab  Description of Parameter
     *@param  iCol  Description of Parameter
     *@return       The Column value
     *@since
     */
    public IColumn getColumn(HttpServletRequest req, int iTab, int iCol);


    /**
     *  Gets the Channel attribute of the ILayoutBean object
     *
     *@param  req    Description of Parameter
     *@param  iTab   Description of Parameter
     *@param  iCol   Description of Parameter
     *@param  iChan  Description of Parameter
     *@return        The Channel value
     *@since
     */
    public org.jasig.portal.layout.IChannel getChannel(HttpServletRequest req, int iTab, int iCol, int iChan);


    /**
     *  Gets the ActiveTab attribute of the ILayoutBean object
     *
     *@param  req  Description of Parameter
     *@return      The ActiveTab value
     *@since
     */
    public int getActiveTab(HttpServletRequest req);


    /**
     *  Sets the ActiveTab attribute of the ILayoutBean object
     *
     *@param  req   The new ActiveTab value
     *@param  iTab  The new ActiveTab value
     *@since
     */
    public void setActiveTab(HttpServletRequest req, int iTab);


    /**
     *  Gets the ChannelInstance attribute of the ILayoutBean object
     *
     *@param  channel  Description of Parameter
     *@return          The ChannelInstance value
     *@since
     */
    public org.jasig.portal.IChannel getChannelInstance(org.jasig.portal.layout.IChannel channel);


    /**
     *  Gets the ChannelInstance attribute of the ILayoutBean object
     *
     *@param  sChannelID  Description of Parameter
     *@return             The ChannelInstance value
     *@since
     */
    public org.jasig.portal.IChannel getChannelInstance(String sChannelID);


    /**
     *  Description of the Method
     *
     *@param  sChannelID  Description of Parameter
     *@since
     */
    public void removeChannelInstance(String sChannelID);


    /**
     *  Gets the ChannelID attribute of the ILayoutBean object
     *
     *@param  channel  Description of Parameter
     *@return          The ChannelID value
     *@since
     */
    public String getChannelID(org.jasig.portal.layout.IChannel channel);


    /**
     *  Gets the GlobalChannelID attribute of the ILayoutBean object
     *
     *@param  channel  Description of Parameter
     *@return          The GlobalChannelID value
     *@since
     */
    public String getGlobalChannelID(org.jasig.portal.layout.IChannel channel);


    /**
     *  Gets the UserName attribute of the ILayoutBean object
     *
     *@param  req  Description of Parameter
     *@return      The UserName value
     *@since
     */
    public String getUserName(HttpServletRequest req);


    /**
     *  Gets the Person attribute of the ILayoutBean object
     *
     *@param  req  Description of Parameter
     *@return      The Person value
     *@since
     */
    public IPerson getPerson(HttpServletRequest req);


    /**
     *  Description of the Method
     *
     *@param  person  Description of Parameter
     *@return         Description of the Returned Value
     *@since
     */
    public boolean canUserPublish(IPerson person);


    // Tabs
    /**
     *  Adds a feature to the Tab attribute of the ILayoutBean object
     *
     *@param  req  The feature to be added to the Tab attribute
     *@since
     */
    public void addTab(HttpServletRequest req);


    /**
     *  Description of the Method
     *
     *@param  req  Description of Parameter
     *@since
     */
    public void renameTab(HttpServletRequest req);


    /**
     *  Sets the DefaultTab attribute of the ILayoutBean object
     *
     *@param  req  The new DefaultTab value
     *@since
     */
    public void setDefaultTab(HttpServletRequest req);


    /**
     *  Description of the Method
     *
     *@param  req  Description of Parameter
     *@since
     */
    public void removeTab(HttpServletRequest req);


    /**
     *  Description of the Method
     *
     *@param  req  Description of Parameter
     *@since
     */
    public void moveTabDown(HttpServletRequest req);


    /**
     *  Description of the Method
     *
     *@param  req  Description of Parameter
     *@since
     */
    public void moveTabUp(HttpServletRequest req);


    // Columns
    /**
     *  Adds a feature to the Column attribute of the ILayoutBean object
     *
     *@param  req  The feature to be added to the Column attribute
     *@since
     */
    public void addColumn(HttpServletRequest req);


    /**
     *  Description of the Method
     *
     *@param  req  Description of Parameter
     *@since
     */
    public void removeColumn(HttpServletRequest req);


    /**
     *  Description of the Method
     *
     *@param  req  Description of Parameter
     *@since
     */
    public void moveColumnRight(HttpServletRequest req);


    /**
     *  Description of the Method
     *
     *@param  req  Description of Parameter
     *@since
     */
    public void moveColumnLeft(HttpServletRequest req);


    /**
     *  Sets the ColumnWidth attribute of the ILayoutBean object
     *
     *@param  req  The new ColumnWidth value
     *@since
     */
    public void setColumnWidth(HttpServletRequest req);


    // Channels
    /**
     *  Description of the Method
     *
     *@param  req  Description of Parameter
     *@since
     */
    public void minimizeChannel(HttpServletRequest req);


    /**
     *  Description of the Method
     *
     *@param  req  Description of Parameter
     *@since
     */
    public void maximizeChannel(HttpServletRequest req);


    /**
     *  Adds a feature to the Channel attribute of the ILayoutBean object
     *
     *@param  req  The feature to be added to the Channel attribute
     *@since
     */
    public void addChannel(HttpServletRequest req);


    /**
     *  Description of the Method
     *
     *@param  req  Description of Parameter
     *@since
     */
    public void removeChannel(HttpServletRequest req);


    /**
     *  Description of the Method
     *
     *@param  req  Description of Parameter
     *@since
     */
    public void moveChannelLeft(HttpServletRequest req);


    /**
     *  Description of the Method
     *
     *@param  req  Description of Parameter
     *@since
     */
    public void moveChannelRight(HttpServletRequest req);


    /**
     *  Description of the Method
     *
     *@param  req  Description of Parameter
     *@since
     */
    public void moveChannelUp(HttpServletRequest req);


    /**
     *  Description of the Method
     *
     *@param  req  Description of Parameter
     *@since
     */
    public void moveChannelDown(HttpServletRequest req);


    // Colors
    /**
     *  Gets the ForegroundColor attribute of the ILayoutBean object
     *
     *@param  req  Description of Parameter
     *@param  res  Description of Parameter
     *@param  out  Description of Parameter
     *@return      The ForegroundColor value
     *@since
     */
    public String getForegroundColor(HttpServletRequest req, HttpServletResponse res, JspWriter out);


    /**
     *  Gets the BackgroundColor attribute of the ILayoutBean object
     *
     *@param  req  Description of Parameter
     *@param  res  Description of Parameter
     *@param  out  Description of Parameter
     *@return      The BackgroundColor value
     *@since
     */
    public String getBackgroundColor(HttpServletRequest req, HttpServletResponse res, JspWriter out);


    /**
     *  Gets the TabColor attribute of the ILayoutBean object
     *
     *@param  req  Description of Parameter
     *@param  res  Description of Parameter
     *@param  out  Description of Parameter
     *@return      The TabColor value
     *@since
     */
    public String getTabColor(HttpServletRequest req, HttpServletResponse res, JspWriter out);


    /**
     *  Gets the ActiveTabColor attribute of the ILayoutBean object
     *
     *@param  req  Description of Parameter
     *@param  res  Description of Parameter
     *@param  out  Description of Parameter
     *@return      The ActiveTabColor value
     *@since
     */
    public String getActiveTabColor(HttpServletRequest req, HttpServletResponse res, JspWriter out);


    /**
     *  Gets the ChannelHeadingColor attribute of the ILayoutBean object
     *
     *@param  req  Description of Parameter
     *@param  res  Description of Parameter
     *@param  out  Description of Parameter
     *@return      The ChannelHeadingColor value
     *@since
     */
    public String getChannelHeadingColor(HttpServletRequest req, HttpServletResponse res, JspWriter out);


    /**
     *  Sets the Colors attribute of the ILayoutBean object
     *
     *@param  req  The new Colors value
     *@param  res  The new Colors value
     *@param  out  The new Colors value
     *@since
     */
    public void setColors(HttpServletRequest req, HttpServletResponse res, JspWriter out);

}

