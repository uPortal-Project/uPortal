package org.jasig.portal.channels;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import org.jasig.portal.channels.rss.*;
import com.objectspace.xml.*;

import java.net.*;


/**
 * This is a user-defined channel for displaying an RSS Channel.
 * 
 * @author Ken Weiner
 */
public class CRSSChannel implements org.jasig.portal.IChannel                           
{ 
  private String m_sUrl = null;
  private Hashtable params = null;
  
  public void initParams (Hashtable params) {this.params = params;}
  
  public String getName () 
  {
    try
    {
      m_sUrl = (String) params.get ("url");
        
      URL url = new URL (m_sUrl);
      
      InputStream xmlStream = url.openStream();
      
      String xmlPackage = "org.jasig.portal.channels.rss";
      IXml xml = Xml.openDocument (xmlPackage, xmlStream);
      IRss rss = (IRss) xml.getRoot ();            
      
      // Get Channel
      org.jasig.portal.channels.rss.IChannel channel = (org.jasig.portal.channels.rss.IChannel) rss.getChannel ();
      
      ITitleOrDescriptionOrLinkOrLanguageOrOneOrMoreItemOrRatingOrImageOrTextinputOrCopyrightOrPubDateOrLastBuildDateOrDocsOrManagingEditorOrWebMasterOrSkipHoursOrSkipDays[] channelAttr = channel.getTitleOrDescriptionOrLinkOrLanguageOrOneOrMoreItemOrRatingOrImageOrTextinputOrCopyrightOrPubDateOrLastBuildDateOrDocsOrManagingEditorOrWebMasterOrSkipHoursOrSkipDayss ();
      String sTitle = null;
      
      for (int i = 0; i < channelAttr.length; i++)
      {
        if (channelAttr[i].getTitle () != null)
          sTitle = channelAttr[i].getTitle ();  
      }
      return sTitle;
    }
    catch (Exception e)
    {
      System.out.println ("\nERROR: \n" + e);
    }
    
    return "&nbsp;";
  }
  
  public boolean isMinimizable () {return true;}
  public boolean isDetachable () {return true;}
  public boolean isRemovable () {return true;}
  public boolean isEditable () {return false;}
  public boolean hasHelp () {return false;}  
  
  public int getDefaultDetachWidth () {return 550;}
  public int getDefaultDetachHeight () {return 450;}
      
  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {
      m_sUrl = (String) params.get ("url");
        
      URL url = new URL (m_sUrl);
      InputStream xmlStream = url.openStream();
      
      String xmlPackage = "org.jasig.portal.channels.rss";
      IXml xml = Xml.openDocument (xmlPackage, xmlStream);
      IRss rss = (IRss) xml.getRoot ();            
      
      // Get Channel
      org.jasig.portal.channels.rss.IChannel channel = (org.jasig.portal.channels.rss.IChannel) rss.getChannel ();
      
      ITitleOrDescriptionOrLinkOrLanguageOrOneOrMoreItemOrRatingOrImageOrTextinputOrCopyrightOrPubDateOrLastBuildDateOrDocsOrManagingEditorOrWebMasterOrSkipHoursOrSkipDays[] channelAttr = channel.getTitleOrDescriptionOrLinkOrLanguageOrOneOrMoreItemOrRatingOrImageOrTextinputOrCopyrightOrPubDateOrLastBuildDateOrDocsOrManagingEditorOrWebMasterOrSkipHoursOrSkipDayss ();
      String sTitleLink = null;
      String sDescription = null;
      IImage image = null;
      
      for (int i = 0; i < channelAttr.length; i++)
      {
        if (channelAttr[i].getLink () != null)
          sTitleLink = channelAttr[i].getLink ();  
          
        if (channelAttr[i].getDescription () != null)
          sDescription = channelAttr[i].getDescription ();  
          
        if (channelAttr[i].getImage () != null)
          image = (IImage) channelAttr[i].getImage ();          
      }
            
      ITitleOrUrlOrLinkOrWidthOrHeightOrDescription[] imageAttr = image.getTitleOrUrlOrLinkOrWidthOrHeightOrDescriptions ();
      
      String sImageUrl = null;
      
      for (int i = 0; i < imageAttr.length; i++)
      {
        if (imageAttr[i].getUrl () != null)
          sImageUrl = imageAttr[i].getUrl ();  
      }
      
      out.println ("<p><a href=\"" + sTitleLink + "\"><img src=\"" + sImageUrl + "\" border=0 align=right></a>");
      out.println ("<p><font face=Arial size=2><i>" + sDescription + "</i></font>");
      
      for (int i = 0; i < channelAttr.length; i++)
      {
        if (channelAttr[i].getItems () != null)
        {
          IItem[] items = channelAttr[i].getItems ();
                    
          ITitleOrLinkOrDescription[] itemAttr = null;
          
          for (int j = 0; j < items.length; j++)
          {
            if (items[j].getTitleOrLinkOrDescriptions () != null)
            {
              itemAttr = items[j].getTitleOrLinkOrDescriptions ();
              
              String sItemTitle = null;
              String sItemLink = null;
              String sItemDescription = null;
                        
              for (int k = 0; k < itemAttr.length; k++)
              {
                if (itemAttr[k].getTitle () != null)
                  sItemTitle = itemAttr[k].getTitle ();
                  
                if (itemAttr[k].getLink () != null)
                  sItemLink = itemAttr[k].getLink ();
                  
                if (itemAttr[k].getDescription () != null)
                  sItemDescription = itemAttr[k].getDescription ();
              }
              
              out.println ("<ul>");
              
              if (sItemTitle.length() > 0)
                out.println ("<li><a href=\"" + sItemLink + "\">" + sItemTitle + "</a><br>");
              else
                out.println ("<p>" + sItemTitle);
                
              if (sItemDescription != null)
                out.println ("<font face=Arial size=2>" + sItemDescription + "</font>");
              
              out.println ("</ul>");
            }
          }
        }          
      }
    }
    catch (Exception e)
    {
      System.out.println ("\nERROR: \n" + e);
      
      try
      {
        out.println ("Problem rendering channel.");
      }
      catch (Exception ex)
      {
      }
    }
  }
  public void edit (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {
      String sAction = req.getParameter ("action");
      
      if (sAction == null)
        doEditPage (req, res, out);
      else
        doSavePage (req, res, out);
    }
    catch (Exception e)
    {
      System.out.println ("\nERROR: \n" + e);
    }
  }
  
  protected void doEditPage (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {
      out.println ("<form action=\"edit.jsp\">");
      out.println ("Enter a URL:");
      out.println ("<input type=text name=url size=50><br>");
      out.println ("<input type=hidden name=action value=\"save\">");
      out.println ("<input type=submit name=submit value=\"Submit\">");
      out.println ("</form>");
    }
    catch (Exception e)
    {
      System.out.println ("\nERROR: \n" + e);
    }
  }

  protected void doSavePage (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {
      m_sUrl = req.getParameter ("url");
      // Write this to a database
      res.sendRedirect ("layout.jsp?tab=Misc");
    }
    catch (Exception e)
    {
      System.out.println ("\nERROR: \n" + e);
    }
  }
  
}