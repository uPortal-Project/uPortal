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
 * This is a user-defined channel for rendering a web page.
 * 
 * @author Ken Weiner
 */
public class CPageRenderer implements org.jasig.portal.IChannel                            
{ 
  protected String m_sUrl = null;
  private Hashtable params = null;
  
  public void initParams (Hashtable params) {this.params = params;}
  public String getName () {return (String) params.get ("name");}
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
      m_sUrl = (String) params.get ("url");
      String sLine = null;
      URL url = new URL (m_sUrl);
      HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
      BufferedReader theHTML = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
      
      StringBuffer sbHTML = new StringBuffer (1024);
      
      while ((sLine = theHTML.readLine()) != null)
        sbHTML.append (sLine);
      
      // Filter out HTML between body tags
      String sHTML = grabHtmlBody (sbHTML.toString ());
      
      if (sHTML != null)
      {
        // Fix relative images sources
        sHTML = replaceRelativeImages (sHTML);
        
        // Replace links
                    
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
        System.out.println ("\nERROR: \n" + e);
      }
      
      System.out.println ("\nERROR: \n" + e);
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
  
  protected String grabHtmlBody (String sHTML)
  {    
    try 
    {
      int iBegin, iEnd;
      
      if (sHTML.indexOf ("<body") >= 0)
      {
        iBegin = sHTML.indexOf ('>', sHTML.indexOf ("<body")) + 1;
        iEnd = sHTML.indexOf ("</body>");
      }
      else
      {
        iBegin = sHTML.indexOf ('>', sHTML.indexOf ("<BODY")) + 1;
        iEnd = sHTML.indexOf ("</BODY>");
      }
      
      return sHTML.substring (iBegin, iEnd);
    }
    catch (Exception e)
    {
      System.out.println ("\nERROR: \n" + e);
    }
    return null;
  }
  
  protected String replaceRelativeImages (String sHTML)
  {    
    try 
    {
      int iStartAt = 0;
      
      while (iStartAt >= 0)
      {
        int iSrcAt;
        
        int iNextSRC = sHTML.indexOf ("SRC", iStartAt);
        int iNextSrc = sHTML.indexOf ("src", iStartAt);
        
        if (iNextSRC < 0 && iNextSrc < 0)
          iSrcAt = -1;
        else
          iSrcAt = iNextSRC < iNextSrc ? iNextSRC : iNextSrc;
          
        if (iSrcAt >= 0)
        { 
          if (! (sHTML.substring (iSrcAt + 4).startsWith ("http://") || sHTML.substring (iSrcAt + 5).startsWith ("http://")))
          {
            if (sHTML.charAt (iSrcAt + 4) == '\"')
              sHTML = sHTML.substring (0, iSrcAt + 5) + m_sUrl + ((sHTML.charAt (iSrcAt + 5)) == '/' ? "" : "/") + sHTML.substring (iSrcAt + 5);
            else
              sHTML = sHTML.substring (0, iSrcAt + 4) + m_sUrl + ((sHTML.charAt (iSrcAt + 4)) == '/' ? "" : "/") + sHTML.substring (iSrcAt + 4);
          }
          
          iStartAt = iSrcAt + 1;
        }
        else
        {
          iStartAt = -1;
        }
      }
      
      return sHTML;
    }
    catch (Exception e)
    {
      System.out.println ("\nERROR: \n" + e);
    }
    return null;
  }
}