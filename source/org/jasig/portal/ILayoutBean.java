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

import java.util.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import org.jasig.portal.layout.*;
import com.objectspace.xml.*;
import org.jasig.portal.security.IPerson;

public interface ILayoutBean {
    // Page generation
    public void writeBodyStyle (HttpServletRequest req, HttpServletResponse res, JspWriter out);
    public void writeTabs (HttpServletRequest req, HttpServletResponse res, JspWriter out);
    public void writeChannels (HttpServletRequest req, HttpServletResponse res, JspWriter out);
    public void writePersonalizeLayoutPage (HttpServletRequest req, HttpServletResponse res, JspWriter out);

    // Helper methods
    public IXml getLayoutXml (HttpServletRequest req, String sUserName);
    public void setLayoutXml (String sUserName, IXml layoutXml);
    public void reloadLayoutXml ();
    public void protectLayoutXml();
    public void releaseLayoutXml();
    public IXml getDefaultLayoutXml (HttpServletRequest req);
    public ITab[] getTabs (HttpServletRequest req);
    public ITab getTab (HttpServletRequest req, int iTab);
    public IColumn getColumn (HttpServletRequest req, int iTab, int iCol);
    public org.jasig.portal.layout.IChannel getChannel (HttpServletRequest req, int iTab, int iCol, int iChan);
    public int getActiveTab (HttpServletRequest req);
    public void setActiveTab (HttpServletRequest req, int iTab);
    public org.jasig.portal.IChannel getChannelInstance (org.jasig.portal.layout.IChannel channel);
    public org.jasig.portal.IChannel getChannelInstance (String sChannelID);
    public void removeChannelInstance (String sChannelID);
    public String getChannelID (org.jasig.portal.layout.IChannel channel);
    public String getGlobalChannelID(org.jasig.portal.layout.IChannel channel);
    public String getUserName (HttpServletRequest req);
    public IPerson getPerson (HttpServletRequest req);
    public boolean canUserPublish (IPerson person);

    // Tabs
    public void addTab (HttpServletRequest req);
    public void renameTab (HttpServletRequest req);
    public void setDefaultTab (HttpServletRequest req);
    public void removeTab (HttpServletRequest req);
    public void moveTabDown (HttpServletRequest req);
    public void moveTabUp (HttpServletRequest req);

    // Columns
    public void addColumn (HttpServletRequest req);
    public void removeColumn (HttpServletRequest req);
    public void moveColumnRight (HttpServletRequest req);
    public void moveColumnLeft (HttpServletRequest req);
    public void setColumnWidth (HttpServletRequest req);

    // Channels
    public void minimizeChannel (HttpServletRequest req);
    public void maximizeChannel (HttpServletRequest req);
    public void addChannel (HttpServletRequest req);
    public void removeChannel (HttpServletRequest req);
    public void moveChannelLeft (HttpServletRequest req);
    public void moveChannelRight (HttpServletRequest req);
    public void moveChannelUp (HttpServletRequest req);
    public void moveChannelDown (HttpServletRequest req);

    // Colors
    public String getForegroundColor (HttpServletRequest req, HttpServletResponse res, JspWriter out);
    public String getBackgroundColor (HttpServletRequest req, HttpServletResponse res, JspWriter out);
    public String getTabColor (HttpServletRequest req, HttpServletResponse res, JspWriter out);
    public String getActiveTabColor (HttpServletRequest req, HttpServletResponse res, JspWriter out);
    public String getChannelHeadingColor (HttpServletRequest req, HttpServletResponse res, JspWriter out);
    public void setColors (HttpServletRequest req, HttpServletResponse res, JspWriter out);

}
