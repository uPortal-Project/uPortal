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

/**
 * Provides methods associated with displaying and modifying
 * a user's layout.  This includes changing the colors, size and
 * positions of tabs, columns, and channels.
 * @author Ken Weiner
 * @version $Revision$
 */
public class LayoutBean extends GenericPortalBean
                        implements ILayoutBean
{       
  private static boolean bPropsLoaded = false;
  private static String sPathToLayoutDtd = null;
  private static String sLayoutDtd = "layout.dtd";
  private Hashtable htChannelInstances = new Hashtable ();

  /**
   * Default constructor
   */
  public LayoutBean ()
  {
    try
    {
      if (!bPropsLoaded)
      {
        File layoutPropsFile = new File (getPortalBaseDir () + "properties\\layout.properties");
        Properties layoutProps = new Properties ();
        layoutProps.load (new FileInputStream (layoutPropsFile));
        sPathToLayoutDtd = layoutProps.getProperty ("pathToLayoutDtd");
        bPropsLoaded = true;
      }
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }
  
  /**
   * Gets the tabs
   * @param the servlet request object
   * @param the tab's index
   */
  public ITab[] getTabs (HttpServletRequest req)
  {
    IXml layoutXml = getLayoutXml (req, getUserName (req));
    ILayout layout = (ILayout) layoutXml.getRoot ();
    ITab[] tabs = layout.getTabs (); 
    return tabs;
  }  
  
  /**
   * Gets a tab
   * @param the servlet request object
   * @param the tab's index
   */
  public ITab getTab (HttpServletRequest req, int iTab)
  {
    IXml layoutXml = getLayoutXml (req, getUserName (req));
    ILayout layout = (ILayout) layoutXml.getRoot ();
    ITab tab = layout.getTabAt (iTab); 
    return tab;
  }
  
  /**
   * Gets a column
   * @param the servlet request object
   * @param the tab's index
   * @param the column's index
   */
  public IColumn getColumn (HttpServletRequest req, int iTab, int iCol)
  {
    IXml layoutXml = getLayoutXml (req, getUserName (req));
    ILayout layout = (ILayout) layoutXml.getRoot ();
    ITab tab = layout.getTabAt (iTab); 
    IColumn column = tab.getColumnAt (iCol);
    return column;
  }
  
  /**
   * Gets a channel
   * @param the servlet request object
   * @param the tab's index
   * @param the column's index
   * @param the channels's index
   */
  public org.jasig.portal.layout.IChannel getChannel (HttpServletRequest req, int iTab, int iCol, int iChan)
  {
    IXml layoutXml = getLayoutXml (req, getUserName (req));
    ILayout layout = (ILayout) layoutXml.getRoot ();
    ITab tab = layout.getTabAt (iTab); 
    IColumn column = tab.getColumnAt (iCol);
    org.jasig.portal.layout.IChannel channel = column.getChannelAt (iChan);
    return channel;
  }
  
  /**
   * Writes an html body tag with colors set according to user preferences
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void writeBodyTag (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {    
      IXml layoutXml = getLayoutXml (req, getUserName (req));
      ILayout layout = (ILayout) layoutXml.getRoot ();
      String sBgColor = layout.getAttribute ("bgcolor");
      String sFgColor = layout.getAttribute ("fgcolor");
      out.println ("<body bgcolor=\"" + sBgColor + "\" text=\"" + sFgColor + "\">");
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }
    
  /**
   * Retrieves a handle to the layout xml
   * @param the servlet request object
   * @param user name
   * @return handle to the layout xml
   */
  public IXml getLayoutXml (HttpServletRequest req, String sUserName)
  {
    RdbmServices rdbmService = new RdbmServices ();
    Connection con = null;
    IXml layoutXml = null;

    try
    {    
      HttpSession session = req.getSession (false);
      layoutXml = (IXml) session.getAttribute ("layoutXml");
          
      if (layoutXml != null)
        return layoutXml;
          
      if (sUserName == null)
        sUserName = "guest";
                    
      con = rdbmService.getConnection ();
      Statement stmt = con.createStatement();
            
      String sQuery = "SELECT LAYOUT_XML FROM USERS WHERE USER_NAME='" + sUserName + "'";
      Logger.log (Logger.DEBUG, sQuery);
        
      ResultSet rs = stmt.executeQuery (sQuery);
          
      if (rs.next ())
      {
        String sLayoutXml = rs.getString ("LAYOUT_XML");
            
        // Tack on the full path to layout.dtd
        int iInsertBefore = sLayoutXml.indexOf (sLayoutDtd);
        sLayoutXml = sLayoutXml.substring (0, iInsertBefore) + sPathToLayoutDtd + sLayoutXml.substring (iInsertBefore);

        String xmlFilePackage = "org.jasig.portal.layout";
        layoutXml = Xml.openDocument (xmlFilePackage, new StringReader (sLayoutXml));
        session.setAttribute ("layoutXml", layoutXml);
      }
      stmt.close ();
        
      return layoutXml;
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

  public void setLayoutXml (String sUserName, IXml layoutXml)
  {
    RdbmServices rdbmService = new RdbmServices ();
    Connection con = null;
    
    try 
    { 
      if (sUserName == null)
        sUserName = "guest";
          
      con = rdbmService.getConnection ();
      Statement stmt = con.createStatement();
          
      StringWriter sw = new StringWriter ();
      layoutXml.saveDocument (sw);
      String sLayoutXml = sw.toString();
        
      // Remove path to layout dtd before saving
      int iRemoveFrom = sLayoutXml.indexOf (sPathToLayoutDtd);
      int iRemoveTo = sLayoutXml.indexOf (sLayoutDtd);
      sLayoutXml = sLayoutXml.substring (0, iRemoveFrom) + sLayoutXml.substring (iRemoveTo);
          
      String sUpdate = "UPDATE USERS SET LAYOUT_XML='" + sLayoutXml + "' WHERE USER_NAME='" + sUserName + "'";
      int iUpdated = stmt.executeUpdate (sUpdate);
      Logger.log (Logger.DEBUG, "Saving layout xml for " + sUserName + ". Updated " + iUpdated + " rows.");
      stmt.close ();
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
    
  /**
   * Retrieves a handle to the default layout xml.
   * @param the servlet request object
   * @param user name
   * @return handle to the layout xml
   */
  public IXml getDefaultLayoutXml (HttpServletRequest req, String sUserName)
  {    
    RdbmServices rdbmService = new RdbmServices ();
    Connection con = null;
    IXml layoutXml = null;
    
    try 
    {    
      HttpSession session = req.getSession (false);      
      con = rdbmService.getConnection ();
      Statement stmt = con.createStatement();
          
      String sQuery = "SELECT DEFAULT_LAYOUT_XML FROM USERS WHERE USER_NAME='" + sUserName + "'";
      Logger.log (Logger.DEBUG, sQuery);
      
      ResultSet rs = stmt.executeQuery (sQuery);
        
      if (rs.next ())
      {
        String sLayoutXml = rs.getString ("DEFAULT_LAYOUT_XML");
          
        // Tack on the full path to layout.dtd
        int iInsertBefore = sLayoutXml.indexOf (sLayoutDtd);
        sLayoutXml = sLayoutXml.substring (0, iInsertBefore) + sPathToLayoutDtd + sLayoutXml.substring (iInsertBefore);

        String xmlFilePackage = "org.jasig.portal.layout";
        layoutXml = Xml.openDocument (xmlFilePackage, new StringReader (sLayoutXml));
        session.setAttribute ("layoutXml", layoutXml);
      }
      stmt.close ();
      
    return layoutXml;
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
   * Retrieves the active tab
   * @param the servlet request object
   * @return the active tab
   */
  public int getActiveTab (HttpServletRequest req)
  {    
    int iActiveTab = 0;
    
    try 
    {    
      IXml layoutXml = getLayoutXml (req, getUserName (req));
      ILayout layout = (ILayout) layoutXml.getRoot ();
      
      HttpSession session = req.getSession (false);
      String sTabParameter = req.getParameter ("tab");
      String sTabSession = (String) session.getAttribute ("activeTab");
      
      if (sTabParameter != null)
        iActiveTab = Integer.parseInt (sTabParameter);
      else if (sTabSession != null)
        iActiveTab = Integer.parseInt (sTabSession);
      else
      {
        // Active tab has not yet been set. Read it from layout.xml
        iActiveTab = Integer.parseInt (layout.getAttribute ("activeTab"));
      }

      // If tab is not within acceptable range, use the first tab
      if (iActiveTab >= layout.getTabCount ())
        iActiveTab = 0;
            
      setActiveTab (req, iActiveTab);
      return iActiveTab;
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }    
    return iActiveTab;
  }  
        
  /**
   * Stores the active tab in the session
   * @param the servlet request object
   * @param active tab
   * @param user name
   */
  public void setActiveTab (HttpServletRequest req, int iTab)
  {    
    try 
    {      
      HttpSession session = req.getSession (false);
      session.setAttribute ("activeTab", String.valueOf (iTab));
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }    
  }          
        
  /**
   * Displays tabs
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void writeTabs (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {      
      out.println ("<!-- Tabs -->");
      out.println ("<table border=0 width=100% cellspacing=0 cellpadding=0>");
      out.println ("<tr>");
      
      IXml layoutXml = getLayoutXml (req, getUserName (req));
      ILayout layout = (ILayout) layoutXml.getRoot ();
      
      // Get Tabs
      ITab[] tabs = layout.getTabs ();
            
      int iTab = getActiveTab (req);
      ITab activeTab = getTab (req, iTab);
                                      
      String sBgcolor = null;
      String sTabName = activeTab.getAttribute ("name");    
      String sActiveTab = activeTab.getAttribute ("name");
      String sTabColor = layout.getAttribute ("tabColor");
      String sActiveTabColor = layout.getAttribute ("activeTabColor");
      
      for (int i = 0; i < tabs.length; i++)
      {
        sTabName = tabs[i].getAttribute ("name");
        sBgcolor = sTabName.equals (sActiveTab) ? sActiveTabColor : sTabColor;  
        
        if (sTabName.equals (sActiveTab))
          activeTab = tabs[i];
        
        out.println ("<td bgcolor=" + sBgcolor + " align=center width=20%>");                        
        out.println ("  <table bgcolor=" + sBgcolor + " border=0 width=100% cellspacing=0 cellpadding=2>");
        out.println ("    <tr align=center>");
        
        if (sTabName.equals (sActiveTab))
          out.println ("      <td><font face=Arial >&nbsp;<b>" + sTabName + "</b></font>&nbsp;</td>");
        else
          out.println ("      <td><font face=Arial size=-1>&nbsp;<b><a href=\"layout.jsp?tab=" + i + "\">" + sTabName + "</a></b></font>&nbsp;</td>");
        
        out.println ("    </tr>");
        out.println ("  </table>");
        out.println ("</td>");
        out.println ("<td width=1%>&nbsp;</td>");                
      }
      
      // Links to personalize layout for users who are logged in
      if (getUserName (req) != null && !getUserName (req).equals ("guest"))
        out.println ("<td align=right bgcolor=" + sTabColor + " width=98%><font size=-1 face=Arial,Helvetica>Personalize&nbsp;[<a href=\"personalizeTabs.jsp\">Tabs</a>]&nbsp;-&nbsp;[<a href=\"personalizeColors.jsp\">Colors</a>]&nbsp;-&nbsp;[<a href=\"personalizeLayout.jsp\">Layout</a>]&nbsp;-&nbsp;[<a href=\"subscribe.jsp\">Channels</a>]&nbsp;</font></td>");
      else
        out.println ("<td width=98%></td>");
        
      out.println ("</tr>");
      
      // This is the strip beneath the tabs
      out.println ("<!-- Strip beneath tabs -->");
      out.println ("<tr><td width=\"100%\" colspan=\"" + (2 * tabs.length + 1) + "\">");
      out.println ("  <table border=0 cellspacing=0 width=\"100%\">");
      out.println ("    <tr><td bgcolor=\"" + sActiveTabColor + "\">");
      out.println ("      <table border=0 cellspacing=0 cellpadding=0><tr><td height=3></td></tr></table>");
      out.println ("    </td></tr>");
      out.println ("  </table>");
      out.println ("</td></tr>");

      out.println ("</table>");
      out.println ("<br>");      
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }
  
  /**
   * Displays channels
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void writeChannels (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {      
      int iTab = getActiveTab (req);
      ITab activeTab = getTab (req, iTab);
      
      HttpSession session = req.getSession (false);
      
      if (activeTab != null)
      {
        out.println ("<!-- Channels -->");
        out.println ("<table border=0 cellpadding=0 cellspacing=0 width=100%>");
        out.println ("  <tr>");
                
        IColumn[] columns = activeTab.getColumns ();
        
        for (int iCol = 0; iCol < columns.length; iCol++)
        {
          out.println ("    <td valign=top width=" + columns[iCol].getAttribute ("width") + ">");
          
          // Get channels for column iCol
          org.jasig.portal.layout.IChannel[] channels = columns[iCol].getChannels ();
          
          for (int iChan = 0; iChan < channels.length; iChan++)
          {
            org.jasig.portal.IChannel ch = getChannelInstance (channels[iChan]);            
                                                
            // Check for minimized, maximized, added or removed channel
            String sResize = req.getParameter ("resize");
            String sTab = req.getParameter ("tab");
            String sColumn = req.getParameter ("column");
            String sChannel = req.getParameter ("channel");
            
            if (sResize != null && iTab == Integer.parseInt (sTab) && iCol == Integer.parseInt (sColumn) && iChan == Integer.parseInt (sChannel))
            {
              if (sResize.equals("minimize"))
                channels[iChan].setAttribute("minimized", "true");
              else if (sResize.equals("maximize"))
                channels[iChan].setAttribute("minimized", "false");
              else if (sResize.equals ("remove"))
              {
                columns[iCol].removeChannel (channels[iChan]);
                continue;
              }
            }
            
            out.println ("<table border=0 cellpadding=1 cellspacing=4 width=100%>");
            out.println ("  <tr>");
            out.println ("    <td bgcolor=cccccc>");
                        
            // Channel heading
            IXml layoutXml = getLayoutXml (req, getUserName (req));
            ILayout layout = (ILayout) layoutXml.getRoot ();
            
            out.println ("      <table border=0 cellpadding=0 cellspacing=0 width=100% bgcolor=" + layout.getAttribute ("channelHeadingColor") + ">");
            out.println ("        <tr>");
            out.println ("          <td>");
            out.println ("            <font face=arial color=#000000><b>&nbsp;" + ch.getName() + "</b></font>");
            out.println ("          </td>");
            out.println ("          <td nowrap valign=center align=right>");
            out.println ("            &nbsp;");
            
            // Channel control buttons
            if (channels[iChan].getAttribute ("minimized").equals ("true"))
              out.println ("<a href=\"layout.jsp?tab=" + iTab + "&column=" + iCol + "&channel=" + iChan + "&resize=maximize\"><img border=0 width=\"18\" height=\"15\" src=\"images/maximize.gif\" alt=\"Maximize\"></a>");
            else if (ch.isMinimizable ())
              out.println ("<a href=\"layout.jsp?tab=" + iTab + "&column=" + iCol + "&channel=" + iChan + "&resize=minimize\"><img border=0 width=\"18\" height=\"15\" src=\"images/minimize.gif\" alt=\"Minimize\"></a>");
            
            if (ch.isDetachable ())
              out.println ("<a href=\"JavaScript:openWin(\'detach.jsp?tab=" + iTab + "&column=" + iCol + "&channel=" + iChan + "\', \'detachedWindow\', " + ch.getDefaultDetachWidth () + ", " + ch.getDefaultDetachHeight () + ")\"><img border=0 width=\"18\" height=\"15\" src=\"images/detach.gif\" alt=\"Detach\"></a>");
            
            if (ch.isRemovable ())
              out.println ("<a href=\"layout.jsp?tab=" + iTab + "&column=" + iCol + "&channel=" + iChan + "&resize=remove\"><img border=0 width=\"18\" height=\"15\" src=\"images/remove.gif\" alt=\"Remove\"></a>");
            
            if (ch.isEditable ())
              out.println ("<a href=\"dispatch.jsp?method=edit&tab=" + iTab + "&column=" + iCol + "&channel=" + iChan + "\"><img border=0 width=\"28\" height=\"15\" src=\"images/edit.gif\" alt=\"Edit\"></a>");
            
            if (ch.hasHelp ())
              out.println ("<a href=\"dispatch.jsp?method=help&tab=" + iTab + "&column=" + iCol + "&channel=" + iChan + "\"><img border=0 width=\"18\" height=\"15\" src=\"images/help.gif\" alt=\"Help\"></a>");
            
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
                            
            if (channels[iChan].getAttribute ("minimized").equals ("false"))
            {                          
              // Render channel contents
              ch.render (req, res, out);
            }
            else
            {
              // Channel is minimized -- don't render it
            }
              
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
          
          out.println ("    </td>");
        }
        
        out.println ("  </tr>");
        out.println ("</table>");
      }
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }  
  
  /**
   * Presents a GUI for manipulating the layout of a tab.
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void writePersonalizeLayoutPage (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {           
      IXml layoutXml = getLayoutXml (req, getUserName (req));
      ILayout layout = (ILayout) layoutXml.getRoot ();
      
      // Get Tabs
      ITab[] tabs = layout.getTabs ();
        
      String sTabName = null;
      int iTab;
      
      // Get tab to personalize from the request if it's there,
      // otherwise use the active tab
      try
      {
        iTab = Integer.parseInt (req.getParameter ("tab"));
      }
      catch (NumberFormatException nfe)
      {
        iTab = getActiveTab (req);
      }
      

      sTabName = tabs[iTab].getAttribute ("name");
        
      out.println ("<form name=\"tabControls\" action=\"personalizeLayout.jsp\" method=post>");
      out.println ("Tab " + (iTab + 1) +": ");        
        
      // Rename tab
      out.println ("<input type=hidden name=\"action\" value=\"renameTab\">");
      out.println ("<input type=hidden name=\"tab\" value=\"" + iTab + "\">");
      out.println ("<input type=text name=\"tabName\" value=\"" + sTabName + "\" onBlur=\"document.tabControls.submit()\">");
                          
      // Set tab as default
      int iDefaultTab;
        
      try
      {
        iDefaultTab = Integer.parseInt (layout.getActiveTabAttribute ());
      }
      catch (NumberFormatException ne)
      {
        iDefaultTab = 0;
      }
      out.println ("<input type=radio name=\"defaultTab\" onClick=\"location='personalizeLayout.jsp?action=setDefaultTab&tab=" + iTab + "'\"" + (iDefaultTab == iTab ? " checked" : "") + ">Set as default");
        
      out.println ("</form>");
        
      // Get the columns for this tab
      IColumn[] columns = tabs[iTab].getColumns ();        
                        
      // Fill columns with channels
      out.println ("<table border=0 cellpadding=3 cellspacing=3>");
      out.println ("<tr bgcolor=#dddddd>");
        
      for (int iCol = 0; iCol < columns.length; iCol++)
      {
        out.println ("<td>"); 
        out.println ("Column " + (iCol + 1));
                    
        // Move column left
        if (iCol > 0)
        {
          out.println ("<a href=\"personalizeLayout.jsp?action=moveColumnLeft&tab=" + iTab + "&column=" + iCol + "\">");
          out.println ("<img src=\"images/left.gif\" border=0 alt=\"Move column left\"></a>");
        }
          
        // Remove column
        out.println ("<a href=\"personalizeLayout.jsp?action=removeColumn&tab=" + iTab + "&column=" + iCol + "\">");
        out.println ("<img src=\"images/remove.gif\" border=0 alt=\"Remove column\"></a>");
        
        // Move column right
        if (iCol < columns.length - 1)
        {
          out.println ("<a href=\"personalizeLayout.jsp?action=moveColumnRight&tab=" + iTab + "&column=" + iCol + "\">");
          out.println ("<img src=\"images/right.gif\" border=0 alt=\"Move column right\"></a>");
        }
          
        // Column width
        String sWidth = columns[iCol].getAttribute ("width");
        String sDisplayWidth = sWidth;
          
        if (sWidth.endsWith ("%"))
          sDisplayWidth = sWidth.substring(0, sWidth.length () - 1);
          
        out.println ("<form name=\"columnWidth" + iTab + "_" + iCol + "\" action=\"personalizeLayout.jsp\" method=post>");
        out.println ("<input type=hidden name=action value=\"setColumnWidth\">");
        out.println ("<input type=hidden name=tab value=\"" + iTab + "\">");
        out.println ("<input type=hidden name=column value=\"" + iCol + "\">");
        out.println ("Width ");
        out.println ("<input type=text name=\"columnWidth\" value=\"" + sDisplayWidth + "\" size=4 onBlur=\"document.columnWidth" + iTab + "_" + iCol + ".submit()\">");
        out.println ("<select name=\"columnWidthType\" onChange=\"document.columnWidth" + iTab + "_" + iCol + ".submit()\">");
        out.println ("<option value=\"\"" + (sWidth.endsWith ("%") ? "" : " selected") + ">Pixels</option>");
        out.println ("<option value=\"%\"" + (sWidth.endsWith ("%") ? " selected" : "") + ">%</option>");
        out.println ("</select>");
        out.println ("</form>");
        out.println ("<hr noshade>");
          
        out.println ("<table><tr>");
        out.println ("<td align=center>");   
        
        out.println ("<form name=\"channels" + iTab + "_" + iCol + "\" action=\"personalizeLayout.jsp\" method=post>");
        
        // Move channel left
        if (iCol > 0)
          out.println ("<a href=\"javascript:getActionAndSubmit (document.channels"+ iTab +"_" + iCol + ", 'moveChannelLeft')\"><img src=\"images/left.gif\" border=0 alt=\"Move channel left\"></a>&nbsp;");
        
        // Remove channel
        out.println ("<a href=\"javascript:getActionAndSubmit (document.channels"+ iTab +"_" + iCol + ", 'removeChannel')\"><img src=\"images/remove.gif\" border=0 alt=\"Remove channel\"></a>&nbsp;");
       
        // Move channel right
        if (iCol < columns.length - 1)
          out.println ("<a href=\"javascript:getActionAndSubmit (document.channels"+ iTab +"_" + iCol + ", 'moveChannelRight')\"><img src=\"images/right.gif\" border=0 alt=\"Move channel right\"></a>");
        
        out.println ("<br>");
        out.println ("<select name=\"channel\" size=10>");
          
        // Get the channels for this column
        org.jasig.portal.layout.IChannel[] channels = columns[iCol].getChannels ();
          
        // List channels for this column
        for (int iChan = 0; iChan < channels.length; iChan++)
        {
          org.jasig.portal.IChannel ch = getChannelInstance (channels[iChan]);            
          out.println ("<option value=\"" + iChan + "\">" + ch.getName () + "</option>");
        }
          
        out.println ("</select>");
        out.println ("</td>"); 
        out.println ("<td>");  
          
        // Move channel up
        out.println ("<a href=\"javascript:getActionAndSubmit (document.channels"+ iTab +"_" + iCol + ", 'moveChannelUp')\"><img src=\"images/up.gif\" border=0 alt=\"Move channel up\"></a><br><br>");
          
        // Remove channel
        out.println ("<a href=\"javascript:getActionAndSubmit (document.channels"+ iTab +"_" + iCol + ", 'removeChannel')\"><img src=\"images/remove.gif\" border=0 alt=\"Remove channel\"></a><br><br>");
          
        // Move channel down
        out.println ("<a href=\"javascript:getActionAndSubmit (document.channels"+ iTab +"_" + iCol + ", 'moveChannelDown')\"><img src=\"images/down.gif\" border=0 alt=\"Move channel down\"></a>");

        out.println ("</td>");          
        out.println ("</tr></table>");
        out.println ("<input type=hidden name=\"tab\" value=\"" + iTab + "\">");
        out.println ("<input type=hidden name=\"column\" value=\"" + iCol + "\">");
        out.println ("<input type=hidden name=\"action\" value=\"none\">");
        out.println ("</form>");
          
        out.println ("</td>");          
      }
        
      out.println ("</tr>");
      out.println ("</table>");
        
      // Add a new column for this tab
      out.println ("<form action=\"personalizeLayout.jsp\" method=post>");
      out.println ("<input type=hidden name=\"tab\" value=\"" + iTab + "\">");
      out.println ("<input type=hidden name=\"action\" value=\"addColumn\">");
      out.println ("<input type=submit name=\"submit\" value=\"Add\">");
      out.println ("new column");
      out.println ("<select name=\"column\">");
                
      for (int iCol = 0; iCol < columns.length; iCol++)
        out.println ("<option value=" + iCol + ">before column " + (iCol + 1) + "</option>");
        
      out.println ("<option value=" + columns.length + "selected>at the end</option>");
      out.println ("</select>");
      out.println ("</form>");
      out.println ("<br>");        
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }    
  
  /**
   * Gets page background color
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public String getBackgroundColor (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {  
      IXml layoutXml = getLayoutXml (req, getUserName (req));
      ILayout layout = (ILayout) layoutXml.getRoot ();
      String sBgColor = layout.getAttribute ("bgcolor");
      
      if (sBgColor != null)
        return sBgColor;
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    
    return "";
  }    
  
  /**
   * Gets page foreground color
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public String getForegroundColor (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {  
      IXml layoutXml = getLayoutXml (req, getUserName (req));
      ILayout layout = (ILayout) layoutXml.getRoot ();
      String sFgColor = layout.getAttribute ("fgcolor");
      
      if (sFgColor != null)
        return sFgColor;
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    
    return "";
  }    

  /**
   * Gets color of non-active tabs
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public String getTabColor (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {  
      IXml layoutXml = getLayoutXml (req, getUserName (req));
      ILayout layout = (ILayout) layoutXml.getRoot ();
      String sTabColor = layout.getAttribute ("tabColor");
      
      if (sTabColor != null)
        return sTabColor;
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    
    return "";
  }    
  
  /**
   * Gets color of active tab
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public String getActiveTabColor (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {  
      IXml layoutXml = getLayoutXml (req, getUserName (req));      
      ILayout layout = (ILayout) layoutXml.getRoot ();
      String sActiveTabColor = layout.getAttribute ("activeTabColor");
      
      if (sActiveTabColor != null)
        return sActiveTabColor;
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    
    return "";
  }    
  
  /**
   * Gets color of channel heading background
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public String getChannelHeadingColor (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {  
      IXml layoutXml = getLayoutXml (req, getUserName (req));
      ILayout layout = (ILayout) layoutXml.getRoot ();
      String sChannelHeadingColor = layout.getAttribute ("channelHeadingColor");
      
      if (sChannelHeadingColor != null)
        return sChannelHeadingColor;
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    
    return "";
  }    
  
  /**
   * Saves colors.  Assumes that the session object contains the following variables:
   * "bgcolor", "fgcolor", "tabColor", "activeTabColor", and "channelHeadingColor"
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void setColors (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {      
      IXml layoutXml = getLayoutXml (req, getUserName (req));
      ILayout layout = (ILayout) layoutXml.getRoot ();
      
      layout.setAttribute ("bgcolor", req.getParameter ("bgColor"));
      layout.setAttribute ("fgcolor", req.getParameter ("fgColor"));
      layout.setAttribute ("tabColor", req.getParameter ("tabColor"));
      layout.setAttribute ("activeTabColor", req.getParameter ("activeTabColor"));
      layout.setAttribute ("channelHeadingColor", req.getParameter ("channelHeadingColor"));
      
      setLayoutXml (getUserName (req), layoutXml);
      HttpSession session = req.getSession (false);
      session.removeAttribute ("layoutXml");
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }      

  /**
   * Initializes and returns an instance of a channel, or gets it from 
   * a member hashtable if the channel has been previously initalized.
   * @param channel object from layout XML
   * @return portal channel object
   */
  public org.jasig.portal.IChannel getChannelInstance (org.jasig.portal.layout.IChannel channel)
  {    
    try 
    {
      String sClass = channel.getAttribute ("class");
      
      // Build a string from channel class and parameter values to be used
      // as a key for looking up channel instances
      StringBuffer sbKey = new StringBuffer (sClass);      
      org.jasig.portal.layout.IParameter[] parameters = channel.getParameters ();
            
      if (parameters != null)
      {
        for (int iParam = 0; iParam < parameters.length; iParam++)
        {
          String sParamValue = parameters[iParam].getAttribute ("value");
          sbKey.append (sParamValue);
        }
      }
      
      String sKey = sbKey.toString ();
      org.jasig.portal.IChannel ch = (org.jasig.portal.IChannel) htChannelInstances.get (sKey);
      
      if (ch == null)
      {
        // Create a hashtable of this channel's parameters
        Hashtable params = new Hashtable ();
              
        if (parameters != null)
        {
          for (int iParam = 0; iParam < parameters.length; iParam++)
          {
            String sParamName = parameters[iParam].getAttribute ("name");
            String sParamValue = parameters[iParam].getAttribute ("value");
            params.put (sParamName, sParamValue);
          }
        }
        
        // Get new instance of channel
        ch = (org.jasig.portal.IChannel) Class.forName (sClass).newInstance ();
     
        // Send the channel its parameters
        ch.initParams (params);   
        
        // Store an instance of this channel for later use
        htChannelInstances.put (sKey, ch);
      }
      
      return ch;
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    return null;
  }  
  
  /**
   * Gets the username from the session
   * @param the servlet request object
   * @return the username
   */
  public String getUserName (HttpServletRequest req)
  {
    HttpSession session = req.getSession (false);
    return (String) session.getAttribute ("userName");
  }
  
  /**
   * Adds a tab at the desired location
   * @param the servlet request object
   */
  public void addTab (HttpServletRequest req)
  {    
    try 
    {      
      int iTab = Integer.parseInt (req.getParameter ("tab"));
      String sNewTabName = "New Tab";
      
      IXml layoutXml = getLayoutXml (req, getUserName (req));
      ILayout layout = (ILayout) layoutXml.getRoot ();
      
      // Get a new tab and set its name
      ITab tab = Factory.newTab ();
      tab.setNameAttribute (sNewTabName);
      
      // Get a new column and set its width
      IColumn column = Factory.newColumn ();
      column.setWidthAttribute ("100%");
      tab.addColumn(column);
      layout.insertTabAt (tab, iTab);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }      
  
  /**
   * Renames a tab at the desired location
   * @param the servlet request object
   */
  public void renameTab (HttpServletRequest req)
  {    
    try 
    {      
      int iTab = Integer.parseInt (req.getParameter ("tab"));
      String sTabName = req.getParameter ("tabName");

      IXml layoutXml = getLayoutXml (req, getUserName (req));
      ILayout layout = (ILayout) layoutXml.getRoot ();
            
      ITab tabToRename = layout.getTabAt (iTab);
      tabToRename.setNameAttribute (sTabName);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }      
  
  /**
   * Sets the default tab
   * @param the servlet request object
   */
  public void setDefaultTab (HttpServletRequest req)
  {    
    try 
    {      
      String sDefaultTab = req.getParameter ("tab");

      IXml layoutXml = getLayoutXml (req, getUserName (req));
      ILayout layout = (ILayout) layoutXml.getRoot ();
      layout.setActiveTabAttribute (sDefaultTab);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }        
  
  /**
   * Removes a tab at the desired location
   * @param the servlet request object
   */
  public void removeTab (HttpServletRequest req)
  {    
    try 
    {      
      int iTab = Integer.parseInt (req.getParameter ("tab"));

      IXml layoutXml = getLayoutXml (req, getUserName (req));
      ILayout layout = (ILayout) layoutXml.getRoot ();
            
      layout.removeTabAt (iTab);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }      
  
  /**
   * Move the tab at the desired location down
   * @param the servlet request object
   */
  public void moveTabDown (HttpServletRequest req)
  {    
    try 
    {      
      int iTab = Integer.parseInt (req.getParameter ("tab"));

      IXml layoutXml = getLayoutXml (req, getUserName (req));
      ILayout layout = (ILayout) layoutXml.getRoot ();
      ITab tabToMoveDown = layout.getTabAt (iTab);
      
      // Only move tab if it isn't already at the bottom (right)
      if (iTab < layout.getTabCount () - 1)
      {
        layout.removeTabAt (iTab);
        layout.insertTabAt(tabToMoveDown, iTab + 1);
      }
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }      
  
  /**
   * Move the tab at the desired location up
   * @param the servlet request object
   */
  public void moveTabUp (HttpServletRequest req)
  {    
    try 
    {      
      int iTab = Integer.parseInt (req.getParameter ("tab"));

      IXml layoutXml = getLayoutXml (req, getUserName (req));
      ILayout layout = (ILayout) layoutXml.getRoot ();
      ITab tabToMoveUp = layout.getTabAt (iTab);
     
      // Only move tab if it isn't already at the top (left)
      if (iTab > 0)
      {
        layout.removeTabAt (iTab);
        layout.insertTabAt (tabToMoveUp, iTab - 1);
      }
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }      
  
  /**
   * Adds a column at the desired location
   * @param the servlet request object
   */
  public void addColumn (HttpServletRequest req)
  {    
    try 
    {      
      int iTab = Integer.parseInt (req.getParameter ("tab"));
      int iCol = Integer.parseInt (req.getParameter ("column"));
      
      ITab tab = getTab (req, iTab);
      
      // Get a new column and set its width
      IColumn column = Factory.newColumn ();
      column.setWidthAttribute ("100%");
      tab.insertColumnAt(column, iCol);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }      
  
  /**
   * Removes a column at the desired location
   * @param the servlet request object
   */
  public void removeColumn (HttpServletRequest req)
  {    
    try 
    {      
      int iTab = Integer.parseInt (req.getParameter ("tab"));
      int iCol = Integer.parseInt (req.getParameter ("column"));

      ITab tab = getTab (req, iTab);
      tab.removeColumnAt (iCol);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }      

  /**
   * Move the column at the desired location right
   * @param the servlet request object
   */
  public void moveColumnRight (HttpServletRequest req)
  {    
    try 
    {      
      int iTab = Integer.parseInt (req.getParameter ("tab"));
      int iCol = Integer.parseInt (req.getParameter ("column"));

      ITab tab = getTab (req, iTab);
      IColumn colToMoveRight = getColumn (req, iTab, iCol);
      tab.removeColumnAt (iCol);
      tab.insertColumnAt(colToMoveRight, iCol + 1);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }      
  
  /**
   * Move the column at the desired location left
   * @param the servlet request object
   */
  public void moveColumnLeft (HttpServletRequest req)
  {    
    try 
    {      
      int iTab = Integer.parseInt (req.getParameter ("tab"));
      int iCol = Integer.parseInt (req.getParameter ("column"));
  
      ITab tab = getTab (req, iTab);
      IColumn colToMoveLeft = getColumn (req, iTab, iCol);
      tab.removeColumnAt (iCol);
      tab.insertColumnAt(colToMoveLeft, iCol - 1);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }      

  /**
   * Changes the width of a column at the desired location
   * @param the servlet request object
   */
  public void setColumnWidth (HttpServletRequest req)
  {    
    try 
    {      
      int iTab = Integer.parseInt (req.getParameter ("tab"));
      int iCol = Integer.parseInt (req.getParameter ("column"));
      String sColumnWidth = req.getParameter ("columnWidth");
      String sColumnWidthType = req.getParameter ("columnWidthType");

      IColumn column = getColumn (req, iTab, iCol);
      column.setWidthAttribute (sColumnWidth + sColumnWidthType);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }      

  /**
   * Minimize a channel
   * @param the servlet request object
   */
  public void minimizeChannel (HttpServletRequest req)
  {    
    try 
    {      
      int iTab = Integer.parseInt (req.getParameter ("tab"));
      int iCol = Integer.parseInt (req.getParameter ("column"));
      int iChan = Integer.parseInt (req.getParameter ("channel"));

      org.jasig.portal.layout.IChannel channel = getChannel (req, iTab, iCol, iChan);
      channel.setMinimizedAttribute ("true");
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }      
  
  /**
   * Maximize a channel
   * @param the servlet request object
   */
  public void maximizeChannel (HttpServletRequest req)
  {    
    try 
    {      
      int iTab = Integer.parseInt (req.getParameter ("tab"));
      int iCol = Integer.parseInt (req.getParameter ("column"));
      int iChan = Integer.parseInt (req.getParameter ("channel"));

      org.jasig.portal.layout.IChannel channel = getChannel (req, iTab, iCol, iChan);
      channel.setMinimizedAttribute ("false");
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }      

  /**
   * Removes a channel
   * @param the servlet request object
   */
  public void removeChannel (HttpServletRequest req)
  {
    try
    {
      int iTab = Integer.parseInt (req.getParameter ("tab"));
      int iCol = Integer.parseInt (req.getParameter ("column"));
      int iChan = Integer.parseInt (req.getParameter ("channel"));

      IColumn column = getColumn (req, iTab, iCol);
      column.removeChannelAt (iChan);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }

  /**
   * Adds a channel to layout.xml
   * @param the servlet request object
   */
  public void addChannel (HttpServletRequest req)
  {
    SubscriberBean subscribe = new SubscriberBean();
    try
    {
      int iTab = getActiveTab(req);
      int iCol = Integer.parseInt (req.getParameter ("column"));

      IColumn column = getColumn (req, iTab, iCol);
      column.addChannel(subscribe.getChannel(req));
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }

  /**
   * Moves a channel to the bottom of the list of the column to the left
   * @param the servlet request object
   */
  public void moveChannelLeft (HttpServletRequest req)
  {    
    try 
    {      
      int iTab = Integer.parseInt (req.getParameter ("tab"));
      int iCol = Integer.parseInt (req.getParameter ("column"));
      int iChan = Integer.parseInt (req.getParameter ("channel"));

      IColumn column = getColumn (req, iTab, iCol);
      IColumn columnToTheLeft = getColumn (req, iTab, iCol - 1);
      org.jasig.portal.layout.IChannel channelToMoveLeft = column.getChannelAt (iChan);
      
      column.removeChannelAt (iChan);
      columnToTheLeft.addChannel(channelToMoveLeft);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }      

  /**
   * Moves a channel to the bottom of the list of the column to the right
   * @param the servlet request object
   */
  public void moveChannelRight (HttpServletRequest req)
  {    
    try 
    {      
      int iTab = Integer.parseInt (req.getParameter ("tab"));
      int iCol = Integer.parseInt (req.getParameter ("column"));
      int iChan = Integer.parseInt (req.getParameter ("channel"));

      IColumn column = getColumn (req, iTab, iCol);
      IColumn columnToTheRight = getColumn (req, iTab, iCol + 1);
      org.jasig.portal.layout.IChannel channelToMoveRight = column.getChannelAt (iChan);
      
      column.removeChannelAt (iChan);
      columnToTheRight.addChannel(channelToMoveRight);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  } 
  
  /**
   * Moves a channel up a position
   * @param the servlet request object
   */
  public void moveChannelUp (HttpServletRequest req)
  {    
    try 
    {      
      int iTab = Integer.parseInt (req.getParameter ("tab"));
      int iCol = Integer.parseInt (req.getParameter ("column"));
      int iChan = Integer.parseInt (req.getParameter ("channel"));

      IColumn column = getColumn (req, iTab, iCol);
      org.jasig.portal.layout.IChannel channelToMoveUp = column.getChannelAt (iChan);
      
      // Only move channel if it isn't already at the top
      if (iChan > 0)
      {
        column.removeChannelAt (iChan);
        column.insertChannelAt (channelToMoveUp, iChan - 1);
      }
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }      

  /**
   * Moves a channel down a position
   * @param the servlet request object
   */
  public void moveChannelDown (HttpServletRequest req)
  {    
    try 
    {      
      int iTab = Integer.parseInt (req.getParameter ("tab"));
      int iCol = Integer.parseInt (req.getParameter ("column"));
      int iChan = Integer.parseInt (req.getParameter ("channel"));

      IColumn column = getColumn (req, iTab, iCol);
      org.jasig.portal.layout.IChannel channelToMoveDown = column.getChannelAt (iChan);
      
      // Only move channel if it isn't already at the bottom
      if (iChan < column.getChannelCount () - 1)
      {
        column.removeChannelAt (iChan);
        column.insertChannelAt (channelToMoveDown, iChan + 1);
      }
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }


