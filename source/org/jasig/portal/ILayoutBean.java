package org.jasig.portal;

import java.util.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*; 
import org.jasig.portal.layout.*;
import com.objectspace.xml.*;

public interface ILayoutBean
{
  // Page generation
  public void writeBodyTag (HttpServletRequest req, HttpServletResponse res, JspWriter out);
  public void writeTabs (HttpServletRequest req, HttpServletResponse res, JspWriter out);
  public void writeChannels (HttpServletRequest req, HttpServletResponse res, JspWriter out);
  public void writePersonalizeLayoutPage (HttpServletRequest req, HttpServletResponse res, JspWriter out);
  
  // Helper methods
  public IXml getLayoutXml (HttpServletRequest req, String sUserName);
  public void setLayoutXml (String sUserName, IXml layoutXml);
  public ITab getTab (HttpServletRequest req, int iTab);
  public IColumn getColumn (HttpServletRequest req, int iTab, int iCol);
  public org.jasig.portal.layout.IChannel getChannel (HttpServletRequest req, int iTab, int iCol, int iChan);
  public int getActiveTab (HttpServletRequest req);
  public void setActiveTab (HttpServletRequest req, int iTab);
  public org.jasig.portal.IChannel getChannelInstance (org.jasig.portal.layout.IChannel channel);
  public String getUserName (HttpServletRequest req);
  
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
  
  // Channels
  public void minimizeChannel (HttpServletRequest req);
  public void maximizeChannel (HttpServletRequest req);
  public void removeChannel (HttpServletRequest req);
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