/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.channels.webproxy;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.PortalException;
import org.jasig.portal.utils.SAX2FilterImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Rewrites URLs for CWebProxy.
 * @author Sarah Arnott, sarnott@mun.ca
 * @version $Revision$
 */
public abstract class CWebProxyURLFilter extends SAX2FilterImpl
{

  protected ChannelRuntimeData runtimeData;
  protected String baseUrl;

  /**
   * A constructor which receives a ContentHandler to which
   * filtered SAX events are passed. 
   * @param handler the ContentHandler to which filtered SAX events are passed
   */  
  protected CWebProxyURLFilter(ContentHandler handler) 
  {
    super(handler);
  }

  /**
   * A factory method that uses mimeType to determine 
   * which type of CWebProxyURLFilter to return.
   * There are currently two types of markup supported: XHTML and WML.
   * @param handler the ContentHandler used to pass along filtered SAX events
   * @param runtimeData the CWebProxy channel runtime data 
   */  
  public static final CWebProxyURLFilter newCWebProxyURLFilter(String mimeType, ChannelRuntimeData runtimeData, ContentHandler handler) throws PortalException 
  {
    // Create a CWebProxyURLFilter, depending on mime type
    CWebProxyURLFilter filter = null;
    if (mimeType != null)
    {
      if (mimeType.equals("text/html"))
        filter = new CWebProxyXHTMLURLFilter(handler);
      else if (mimeType.equals("text/vnd.wap.wml"))
        filter = new CWebProxyWMLURLFilter(handler);
      else
        throw new PortalException("CWebProxyURLFilter.newCWebProxyURLFilter(): Unable to locate CWebProxyURLFilter for mime type '" + mimeType + "'");
    }
    else
    {
      throw new PortalException("CWebProxyURLFilter.newCWebProxyURLFilter(): Unable to create CWebProxyURLFilter. Mime type is null.");
    }

    // Set CWebProxyURLFilter properties
    filter.runtimeData = runtimeData;
    filter.baseUrl = (String)runtimeData.get("cw_xml");

    return filter;
  }
  
  /**
   * A helper method which rewrites an attribute that has a URL value
   * for CWebProxy.  The URL rewriting is dependant upon the values
   * of runtime data parameters (see CWebProxy documentation).
   *
   * @param elementName the element name containing an attribute of name attName
   * @param attName the name of the attribute of elementName
   * @param qName the name of the current element
   * @param attsImpl the attributes implementation to contain the new attribute value
   */
  protected final void rewriteURL(String elementName, String attName, String qName, Attributes atts, AttributesImpl attsImpl)
  {
    if (qName.equalsIgnoreCase(elementName)) 
    {
      String passThrough = (String) runtimeData.get("cw_passThrough");
      if (passThrough != null && (passThrough.equals("all")
                                  || passThrough.equals("application")
                                  || passThrough.equals("marked")) )
      {
        String attValue = atts.getValue(attName);
        if (attValue != null && (attValue.startsWith("http://") || attValue.startsWith("https://")))
        {
          String query = getQueryString(attValue); 
          String base = getBase(attValue);

          // determine the actionURL
          String actionURL;
          if (attValue.indexOf("cw_download=") != -1)
            actionURL = (String)runtimeData.get("downloadActionURL");
          else
            actionURL = (String)runtimeData.get("baseActionURL"); 

          String xmlUri = (String)runtimeData.get("cw_xml");

          // rewrite URL when required
          if (passThrough.equals("marked")) 
          {
            if (attValue.indexOf("cw_inChannelLink=") != -1)
            {
              if ((attValue.trim().equals("") || xmlUri.equals(base)))
                attValue = actionURL + query;
              else
                if (!query.equals(""))
                  attValue =  actionURL + query + "&cw_xml=" + base;
                else
                  attValue = actionURL + "?cw_xml=" + base;
            }
          }
          else if (passThrough.equals("application"))
          {
            if (attValue.trim().equals("") || xmlUri.equals(base))
              attValue = actionURL + query;
          }
          else if (passThrough.equals("all"))
          {
            if (attValue.trim().equals("") || xmlUri.equals(base))
              attValue = actionURL + query;
            else
              if (!query.equals(""))
                attValue = actionURL + query + "&cw_xml=" + base;
              else
                attValue = actionURL + "?cw_xml=" + attValue;
          }

          int index = atts.getIndex(attName);
          attsImpl.setAttribute(index, atts.getURI(index), atts.getLocalName(index), attName, atts.getType(index), attValue);
        }
      }
    }
  }

  /**
   * Returns the portion of the URL without the query string.
   * @param url A String representing the absolute URL.
   */
  protected String getBase(String url)
  {
    if (url.indexOf("?") != -1)
      return url.substring(0, url.indexOf("?"));
    else
      return url;
  }

  /**
   * Returns the query string portion of the URL.
   * @param url A String representing the absolute URL.
   */
  protected String getQueryString(String url)
  {
    if (url.indexOf("?") != -1)
      return url.substring(url.indexOf("?"));
    else
      return "";
  }

}
