package org.jasig.portal.channels;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import com.objectspace.xml.*;
import org.jasig.portal.*;
import org.jasig.portal.layout.*;

import java.net.*;


/**
 * This is a user-defined channel for rendering a web page in an IFrame.
 * For Browsers without support for Inline Frames the channel just presents
 * a link to open in a separate window.
 * 
 */
public class CInlineFrame implements org.jasig.portal.IChannel                            
{ 
  protected String m_sUrl = null;
  private ChannelConfig chConfig = null;
  
  public void init (ChannelConfig chConfig) {this.chConfig = chConfig;}
  public String getName () {return (String) chConfig.get ("name");}
  public boolean isMinimizable () {return true;}
  public boolean isDetachable () {return false;}
  public boolean isRemovable () {return true;}
  public boolean isEditable () {return false;}  
  public boolean hasHelp () {return false;}  
  
  public int getDefaultDetachWidth () {return 0;}
  public int getDefaultDetachHeight () {return 0;}
  
  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {
      String sBrowser = req.getHeader("User-Agent");
      String sHeight = (String) chConfig.get ("height");
      m_sUrl = (String) chConfig.get ("url");
      String sLine = null;
      URL url = new URL (m_sUrl);
      HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
      BufferedReader theHTML = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
      
      StringBuffer sbHTML = new StringBuffer (1024);
      
      while ((sLine = theHTML.readLine()) != null)
        sbHTML.append (sLine);
      
        String sHTML = sbHTML.toString ();
        if (sHTML != null)
        {
        // This test gets IE 4 and 5 and Netscape 6 into the Iframe world
        // all others get just a link.  This could undoubtedly get refined.
        // IE3 also handled Iframes but I'm not sure of the string it returns.
          if ((sBrowser.indexOf("MSIE 3")>=0)||
              (sBrowser.indexOf(" MSIE 4")>=0) ||
              (sBrowser.indexOf("MSIE 5")>=0) ||
              (sBrowser.indexOf("Mozilla/5")>=0) )
            sHTML = "<IFRAME height=" + sHeight + " width=100% frameborder='no' src='" +
              m_sUrl + "'></IFRAME>";
          else
            sHTML = "<p>This browser does not support inline frames.</p>" +
               "<A href="+ m_sUrl +
               " target='IFrame_Window'>Click this link to view content</A> in a separate window.";
         out.println (sHTML);
      }
      else
      {
        out.println ("<p>The page you chose cannot be rendered within a channel. Please choose another.  <p><i>Note: Pages containing framesets are not allowed.</i>");
      }
    }
    catch (Exception e)
    {
      try
      {
        out.println ("<b>" + m_sUrl + "</b> is currently unreachable.");
        out.println ("Please choose another.");
      }
      catch (Exception ex)
      {
        Logger.log (Logger.ERROR, e);
      }
      
      Logger.log (Logger.ERROR, e);
    }
  }
  
  public void edit (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    // This channel is not editable
  }
  
  public void help (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    // This channel has no help
  }
  

}