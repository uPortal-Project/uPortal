package com.ibs.portal;

import com.ibs.Framework.*;
import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import com.objectspace.xml.*;
import com.ibs.portal.layout.*;

/**
 * @author Ken Weiner
 */
public class PersonalizeLayoutBean extends com.ibs.Framework.servlet.GenericJSPBean
{
  static protected IXml s_xml = null;
  static protected ITab s_activeTab = null; // Position in xml doc 
   
  public void writePage (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    { 
      // Hard coded for now, but eventually
      // retrieve available channels from database
      Vector vChannels = new Vector ();
      vChannels.addElement ("ABC News");
      vChannels.addElement ("My Bookmarks");
      vChannels.addElement ("Horoscope");
      vChannels.addElement ("My Applications");
      vChannels.addElement ("Page Renderer");
      vChannels.addElement ("Scoreboard");
      vChannels.addElement ("Search");
      vChannels.addElement ("Weather");
      
      // List available channels
      out.println ("<table border=0 width=100% cellspacing=0 cellpadding=0>");
      out.println ("<tr bgcolor=#dddddd>");
      out.println ("<td>");
      
      for (int i = 0; i < vChannels.size () / 2; i++)
        out.println ("<input type=checkbox name=\"channel" + i + "\">" + (String) vChannels.elementAt (i) + "<br>");
      
      out.println ("</td>");
      out.println ("<td>");
      
      for (int i = vChannels.size () / 2 + 1; i < vChannels.size (); i++)
        out.println ("<input type=checkbox name=\"channel" + i + "\">" + (String) vChannels.elementAt (i) + "<br>");
      
      out.println ("</td>");
      out.println ("</tr>");
      out.println ("</table><br>");
    
      // Open xml file -- this should eventually be retrieved from a database
      File xmlFile = new File ("D:\\Projects\\ibs\\Portal\\source\\xml\\layout\\layout.xml");
      String xmlFilePackage = "com.ibs.portal.layout";
      s_xml = Xml.openDocument (xmlFilePackage, xmlFile);
      ILayout layout = (ILayout) s_xml.getRoot ();
      
      // Get Tabs
      ITab[] tabs = layout.getTabs ();
 
      // Add new channels
      out.println ("<input type=submit name=\"\" value=\"Add\">");
      out.println ("checked channels to Tab ");
      out.println ("<select name=\"tab\">");
                
      for (int iTab = 0; iTab < tabs.length; iTab++)
        out.println ("<option>" + (iTab + 1) + "</option>");
        
      out.println ("</select>");
      out.println ("Column ");
      out.println ("<select name=\"column\">");
          
      // Find the max number of columns in any tab
      int iMaxCol = 0;
      
      for (int iTab = 0; iTab < tabs.length; iTab++)
      {
        IColumn[] columns = tabs[iTab].getColumns ();
        
        for (int iCol = 1; iCol <= columns.length; iCol++)
        {
          if (iCol > iMaxCol)
            iMaxCol = iCol;
        }
      }
      
      for (int iCol = 0; iCol < iMaxCol; iCol++)
        out.println ("<option>" + (iCol + 1) + "</option>");
        
      out.println ("</select>");
      out.println ("<hr noshade>");
 
      // Add a new tab
      out.println ("<input type=submit name=\"\" value=\"Add\">");
      out.println ("new tab");
      out.println ("<select name=\"addTab\">");
                
      for (int iTab = 0; iTab < tabs.length; iTab++)
        out.println ("<option>before tab " + (iTab + 1) + "</option>");
        
      out.println ("<option selected>at the end</option>");
      out.println ("</select>");
      out.println ("<hr noshade>");
      
      String sTabName = null;
      
      for (int iTab = 0; iTab < tabs.length; iTab++)
      {
        sTabName = tabs[iTab].getAttribute ("name");
        
        out.println ("Tab " + (iTab + 1) +": ");
        out.println ("<input type=text name=\"\" value=\"" + sTabName + "\">");
        out.println ("<input type=submit name=\"\" value=\"Rename\">");
        
        // Move tab down
        if (iTab < tabs.length - 1)
          out.println ("<img src=\"down.gif\" border=0 alt=\"Move tab down\">");
        
        // Remove tab
        out.println ("<img src=\"remove.gif\" border=0 alt=\"Remove tab\">");
        
        // Move tab up
        if (iTab > 0)
          out.println ("<img src=\"up.gif\" border=0 alt=\"Move tab up\">");
                  
        // Set tab as default
        out.println ("<input type=radio name=\"defaultTab\">Set as default");
        
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
            out.println ("<img src=\"left.gif\" border=0 alt=\"Move column left\">");
          
          // Remove column
          out.println ("<img src=\"remove.gif\" border=0 alt=\"Remove column\">");
          
          // Move column right
          if (iCol < columns.length - 1)
            out.println ("<img src=\"right.gif\" border=0 alt=\"Move column right\">");
          
          // Column width
          String sWidth = columns[iCol].getAttribute ("width");
          String sDisplayWidth = sWidth;
          
          if (sWidth.endsWith ("%"))
            sDisplayWidth = sWidth.substring(0, sWidth.length () - 1);
          
          out.println ("<br>");
          out.println ("Width ");
          out.println ("<input type=text name=\"\" value=\"" + sDisplayWidth + "\" size=4>");
          out.println ("<select name=\"\">");
          out.println ("<option" + (sWidth.endsWith ("%") ? "" : " selected") + ">Pixels</option>");
          out.println ("<option" + (sWidth.endsWith ("%") ? " selected" : "") + ">%</option>");
          out.println ("</select>");
          out.println ("<hr noshade>");
          
          out.println ("<table><tr>");
          out.println ("<td>");          
          out.println ("<select name=\"\" size=10>");
          
          // Get the channels for this column
          com.ibs.portal.layout.IChannel[] channels = columns[iCol].getChannels ();
          
          // List channels for this column
          for (int iChan = 0; iChan < channels.length; iChan++)
          {
            // Get instance of channel
            String sClass = channels[iChan].getAttribute ("class");
            com.ibs.portal.IChannel ch = (com.ibs.portal.IChannel) Class.forName (sClass).newInstance ();
            
            out.println ("<option value=\"\">" + ch.getName () + "</option>");
          }
          
          out.println ("</select>");
          out.println ("</td>"); 
          out.println ("<td>");  
          
          // Move channel up
          out.println ("<img src=\"up.gif\" border=0 alt=\"Move channel up\"><br><br>");
          
          // Remove channel
          out.println ("<img src=\"remove.gif\" border=0 alt=\"Remove channel\"><br><br>");
          
          // Move channel down
          out.println ("<img src=\"down.gif\" border=0 alt=\"Move channel down\">");

          out.println ("</td>");          
          out.println ("</tr></table>");
          
          out.println ("</td>");          
        }
        
        out.println ("</tr>");
        out.println ("</table>");
        
        // Add a new column for this tab
        out.println ("<input type=submit name=\"\" value=\"Add\">");
        out.println ("new column");
        out.println ("<select name=\"addColumn\">");
                
        for (int iCol = 0; iCol < columns.length; iCol++)
          out.println ("<option>before column " + (iCol + 1) + "</option>");
        
        out.println ("<option selected>at the end</option>");
        out.println ("</select>");
        out.println ("<br><br>");
        
        out.println ("<hr noshade>");
        
      } // end for Tabs      
    }
    catch (Exception e)
    {
      log (DEBUG, e);
      System.out.println ("\nERROR: \n" + e);
    }
  }
    
  public void saveLayout (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {      
      File xmlFile = new File ("D:\\Projects\\ibs\\Portal\\source\\xml\\layout\\layout.xml");
      s_xml.saveDocument (xmlFile);
    }
    catch (Exception e)
    {
      log (DEBUG, e);
      System.out.println ("\nERROR: \n" + e);
    }
  }
}