/*
 * CGenericXSLTRenderer.java
 *
 * Created on August 22, 2000, 3:28 PM
 */

package org.jasig.portal.xmlchannels;

/**
 *
 * @author  Steve Toth
 * @version
 */

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import org.jasig.portal.*;
import org.apache.xalan.xslt.*;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;

import java.net.*;

// xml  parameter the XML to parse
// ssl  parameter the ssl style sheet list

public class CGenericXSLT extends GenericPortalBean implements org.jasig.portal.IXMLChannel
{

  protected Document sXml;
  protected Document sXsl;
  protected String sSSL;
  protected String sChannelTitle;
  protected ChannelRuntimeData runtimeData;
  protected StylesheetSet set;
  protected static String fs = System.getProperty ("file.separator");
  protected static String stylesheetDir = getPortalBaseDir ();
  public static String MEDIAPROPERTIES_FILE_LOCATION = getPortalBaseDir () + "properties" + fs + "media.properties";
  // Initialize a stylesheet set from our set stylesheet repository.
  public CGenericXSLT ()
  {
  }

  // Get xml channel parameters.
  public void setStaticData (ChannelStaticData sd)
  {
    String temp;
    try
    {
      temp  = sd.getParameter ("ssl");
      if (temp!=null)
      {
        if (temp.indexOf ("://") == -1 )
        {
          temp =  getPortalBaseDir ()+temp;
        }
        set = new StylesheetSet (temp);

        set.setMediaProps (MEDIAPROPERTIES_FILE_LOCATION);
        if ( (temp = sd.getParameter ("xml"))!=null)
        {
          if (temp.indexOf ("://") == -1 )
          {
            temp =  getPortalBaseDir ()+temp;
          }
          DOMParser DOMp = new DOMParser ();
          DOMp.parse (temp);
          sXml = DOMp.getDocument ();
        }
      }
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR,e);
    }
    sChannelTitle = sd.getParameter ("name");
  }

  // Set some channel properties.
  public ChannelSubscriptionProperties getSubscriptionProperties ()
  {
    ChannelSubscriptionProperties csb = new ChannelSubscriptionProperties ();
    csb.setName (sChannelTitle);
    csb.setHasHelp (false);
    csb.setDefaultDetachWidth ("550");
    csb.setDefaultDetachHeight ("450");
    return csb;
  }

  // We can get at the url request info.
  public void setRuntimeData (ChannelRuntimeData rd)
  {
    runtimeData = rd;
  }

  // Layout events coming from the portal.
  public void receiveEvent (LayoutEvent ev)
  {
  }

  // Access channel runtime properties.
  public ChannelRuntimeProperties getRuntimeProperties ()
  {
    return new ChannelRuntimeProperties ();
  }

  public void renderXML (DocumentHandler out)
  {
    try
    {
      // XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
      // processor.process(new XSLTInputSource(sXml),new XSLTInputSource(sXsl),new XSLTResultTarget(out));

      if (set != null)
      {
        XSLTInputSource stylesheet = set.getStylesheet (runtimeData.getHttpRequest ());

        if (stylesheet.getSystemId ().indexOf ("://")==-1)
        {
          stylesheet.setSystemId (this.getPortalBaseDir () + stylesheet.getSystemId () );
        }
        if (stylesheet != null)
        {
          XSLTProcessor processor = XSLTProcessorFactory.getProcessor ();
          processor.process (new XSLTInputSource (sXml),stylesheet,new XSLTResultTarget (out));
        }
      }
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }
}
