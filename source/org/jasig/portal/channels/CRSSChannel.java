package org.jasig.portal.channels;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import org.jasig.portal.*;
import org.jasig.portal.channels.rss.*;
import com.objectspace.xml.*;

import java.net.*;


/**
 * This is a channel for displaying an RSS broadcast.
 * The first time an RSS file is requested, it is retrieved 
 * from its URL, and then stored in a memory cache for future requests.
 * After one hour from the initial request, it is flushed from the
 * cache and retrieved again.
 * 
 * @author Ken Weiner
 * @version $Revision$
 */
public class CRSSChannel implements org.jasig.portal.IChannel                           
{ 
  private static RSSCache m_RSSCache = new RSSCache (3600);
  private ChannelConfig chConfig = null;

  private static Vector params = null;

  public CRSSChannel()
  {
    params = new Vector();
    params.addElement(new String[] {"URL", "url", "50", "70", "You have chosen to publish a channel that requires you to provide a URL. Please enter the URL for the channel you wish to publish below."} );
  }

  public void init (ChannelConfig chConfig) {this.chConfig = chConfig;}
  
  public Vector getParameters()
  {
    return params;
  }

  public String getName ()
  {
    try
    {
      // Check if RSSXml is already in the cache
      String sUrl = (String) chConfig.get ("url");
      IXml xml = (IXml) m_RSSCache.get (sUrl);
      
      // If the RSSXml isn't already cached, download it and put it in the cache
      if (xml == null)
      {
        URL url = new URL (sUrl);
        String sXmlPackage = "org.jasig.portal.channels.rss";
        InputStream xmlStream = url.openStream();
        xml = Xml.openDocument (sXmlPackage, xmlStream);
        m_RSSCache.put (sUrl, xml);
        Logger.log (Logger.INFO, "Caching RSS at URL: " + sUrl);
      }
      
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
      Logger.log (Logger.ERROR, e);
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
      // Check if RSSXml is already in the cache
      String sUrl = (String) chConfig.get ("url");
      IXml xml = (IXml) m_RSSCache.get (sUrl);
      
      // If the RSSXml isn't already cached, download it and put it in the cache
      if (xml == null)
      {
        URL url = new URL (sUrl);
        String sXmlPackage = "org.jasig.portal.channels.rss";
        InputStream xmlStream = url.openStream();
        xml = Xml.openDocument (sXmlPackage, xmlStream);
        m_RSSCache.put (sUrl, xml);
      }
      
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
// An image is optional but there should be a description and a link for the channel
      if (image != null)
      {
        ITitleOrUrlOrLinkOrWidthOrHeightOrDescription[] imageAttr = image.getTitleOrUrlOrLinkOrWidthOrHeightOrDescriptions ();
        String sImageUrl = null;
        String sImageLink = null;

        for (int i = 0; i < imageAttr.length; i++)
        {
          if (imageAttr[i].getUrl () != null)
            sImageUrl = imageAttr[i].getUrl ();
          if (imageAttr[i].getLink () != null)
            sImageLink = imageAttr[i].getLink ();
        }
        // if there is an image and an image-link use it as intended
        if (sImageLink!= null) out.println ("<a href=\"" + sImageLink + "\"><img src=\"" + sImageUrl + "\" border=0 align=right /></a>");
        else
          // if no image-link but have title-link put title-link on the image
          if (sTitleLink != null) out.println ("<a href=\"" + sTitleLink + "\"><img src=\"" + sImageUrl + "\" border=0 align=right /></a>");
          // if no image-link and no title-link just put the image out with no link
          else out.println ("<img src=\"" + sImageUrl + "\" border=0 align=right />");
      }
      // if there is a title link but no image then insert our own little icon for the link
      else
        if (sTitleLink != null) out.println ("<a href=\"" + sTitleLink + "\"><img src=\"images/rsslink.gif\" border=0 align=right /></a>");

      out.println ("<p><em>" + sDescription + "</em></p>");

      out.println ("<ul>");
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

              if (sItemTitle.length() > 0)
                out.println ("<li><a href=\"" + sItemLink + "\">" + sItemTitle + "</a></li>");
              else
                out.println ("<li>" + sItemTitle + "</li>");

              if (sItemDescription != null)
                out.println ("<br />" + sItemDescription );
            }
          }
        }
      }
        out.println ("</ul>");
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
      
      try
      {
        out.println ("Problem rendering channel.");
      }
      catch (Exception ex)
      {
        Logger.log (Logger.ERROR, ex);
      }
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

  /**
   * A class for caching RSS Xml objects.  Each object will be
   * cached for a specified amount of time (specified in seconds)
   */
  protected static class RSSCache extends java.util.Hashtable
  {
    protected int m_iExpirationTimeout = 3600000;   // default to 1 hour
    
    public RSSCache (int timeout)
    {
      super ();
      m_iExpirationTimeout = timeout * 1000;
    }
    
    public synchronized Object get (Object key)
    {
      Placeholder placeholder = (Placeholder) super.get (key);
      
      if (placeholder != null)
      {
        if (placeholder.m_lCreationTime + m_iExpirationTimeout < System.currentTimeMillis())
        {
          remove (key);
          return null;
        }
        return placeholder.m_Value;
      }
      else
        return null;
    }
    
    public synchronized Object put (Object key, Object value)
    {
      Placeholder placeholder = new Placeholder (value);
      return super.put (key, placeholder);
    }
    
    protected class Placeholder
    {
      protected long m_lCreationTime = System.currentTimeMillis ();
      protected Object m_Value;
      
      protected Placeholder (Object value)
      {
        m_lCreationTime = System.currentTimeMillis ();
        m_Value = value;
      }
    }
  }  
}