/**
 * Copyright (c) 2000-2001 The JA-SIG Collaborative.  All rights reserved.
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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.jasig.portal.Logger;

/**
 *  This servlet will take request parameters in the form url=(SomeURL) and will pipe the
 * contents of the url into the response output stream. This will allow a portal to run
 * completely in HTTPS. For instance, replacing the RSS image links with
 * proxyServlet?url=http:\\someImage.gif will pipe the image through the secure server that
 * the servlet is running on. This will keep browsers happy because they will not be mixing
 * http and https content.
 *  The servlet will look for the properties...
 * proxy.CacheTimeout             = This is the duration, in seconds, that proxied object will be cached for.
 * proxy.MaximumCachedContentSize = This is the maximum size, in bytes, of objects that will be cached
 * proxy.MaximumReadAttempts      = This is the number of consecutive reads that the servlet will attempt
 *                                  when downloading the object
 * ...from portal/properties/portal.properties.
 *
 * @author Bernie Durfee
 * @author Adam Rybicki
 * @version $Revision$
 */
public class ProxyServlet extends HttpServlet
{
  // Cache for incoming objects
  protected SmartCache cache = new SmartCache (300); 
  
  protected int maxCachedObjectSize;
  
  // Number of times to attempt a read from the input stream
  protected int maxReadAttempts = 100;

  public void init () throws ServletException
  {
    File secprops = new File (GenericPortalBean.getPortalBaseDir() + "properties" + File.separator + "portal.properties");
    Properties props = new Properties ();

    try
    {
      props.load (new FileInputStream (secprops));
    }
    catch (IOException e)
    {
      Logger.log (Logger.ERROR, e);
    }
    
    try
    {
      String stringValue = (String)props.get ("proxy.CacheTimeout");
      int intValue = new Integer (stringValue).intValue();
      cache = new SmartCache (intValue);
    }
    catch (Exception e)
    {
      // throw this away because we are accepting the default if the entry
      // in portal.properties was not present
    }
    
    try
    {
      String stringValue = (String)props.get ("proxy.MaximumCachedContentSize");
      maxCachedObjectSize = new Integer (stringValue).intValue();
    }
    catch (Exception e)
    {
      // throw this away because we are accepting the default if the entry
      // in portal.properties was not present
    }
    
    try
    {
      maxReadAttempts = Integer.parseInt((String)props.get("proxy.MaximumReadAttempts"));
    }
    catch(Exception e)
    {
      // throw this away because we are accepting the default if the entry
      // in portal.properties was not present
    }  
  }
  
  public ProxyServlet()
  {
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
  {
    String urlParameter = request.getParameter("url");

    if(urlParameter == null)
    {
      return;
    }
    
    // Check for cache hits
    CacheItem item = (CacheItem)cache.get(urlParameter);
    
    if (item == null)
    {
      try
      {
        Logger.log(Logger.INFO, "ProxyServlet - Proxying object from url: " + urlParameter);
        URL url = new URL(urlParameter);
        URLConnection connection = url.openConnection();
        InputStream inStream = connection.getInputStream();
        OutputStream outStream = response.getOutputStream();
        int contentLength = connection.getContentLength();
        String contentType = connection.getContentType();
  
        response.setContentType(contentType);
        
        // Don't bother with bad data
        if(contentLength < 1)
        {
          return;
        }
        
        // Create a buffer for the incoming stream
        byte [] byteBuffer = new byte [maxCachedObjectSize];

        // Only cache items under the set limit
        if(contentLength < maxCachedObjectSize)
        { 
          int i = 0;
          int totalBytesRead = 1;  // Start counting at 1 because arrays start at 0
          
          // Make multiple attempts read the object in up to it's total content length
          while(i < maxReadAttempts && totalBytesRead < contentLength)
          {
            // Read in a chunk from data
            totalBytesRead += inStream.read(byteBuffer, totalBytesRead - 1, contentLength - totalBytesRead);
            i++;
          }
          
          // Put the object into the cache only if we managed to get the whole thing
          if(totalBytesRead == contentLength)
          {
            Logger.log(Logger.INFO, "ProxyServlet - Object being cached: " + urlParameter + " contentLength: " + contentLength + " totalBytesRead: " + totalBytesRead);
            cache.put(urlParameter, new CacheItem(contentType, totalBytesRead, byteBuffer));
          }
          else
          {
            Logger.log(Logger.WARN, "ProxyServlet - Object not fully retrieved: " + urlParameter + " contentLength: " + contentLength + " totalBytesRead: " + totalBytesRead);
          }
          
          // Send the object to the browser
          response.setContentLength(totalBytesRead);
          outStream.write(byteBuffer, 0, totalBytesRead);
        }
        else
        {
          // Just assume that the content length is correct
          response.setContentLength(contentLength);
          
          // Read in a chunk of the object
          int readCount = inStream.read(byteBuffer);
          
          while(readCount > 0)
          {
            Logger.log(Logger.INFO, "ProxyServlet - Object too big to cache: " + urlParameter);
            
            // Write a chunk of data
            outStream.write(byteBuffer, 0, readCount);
            
            // Read in another chunk of data
            readCount = inStream.read(byteBuffer);
          }
        }
        
        inStream.close();
  
        return;
      }
      catch(Exception e)
      {
        Logger.log(Logger.ERROR, e);
      }
    }
    else
    {
      // Just send the cached item
      Logger.log(Logger.INFO, "ProxyServlet - Found object in cache from url: " + urlParameter);
      response.setContentLength(item.contentLength);
      response.setContentType(item.contentType);

      try
      {
        OutputStream outStream = response.getOutputStream();
        outStream.write (item.byteBuffer, 0, item.contentLength);
      }
      catch(Exception e)
      {
        Logger.log(Logger.ERROR, e);
      }
    }
  }
  
  protected class CacheItem
  {
    protected String contentType;
    protected int contentLength;
    protected byte[] byteBuffer;
    
    protected CacheItem (String type, int totalBytesRead, byte buffer[])
    {
      contentType = type;
      contentLength = totalBytesRead;
      byteBuffer = buffer;
    }
  }
}