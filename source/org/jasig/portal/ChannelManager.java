/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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

import javax.servlet.http.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Map;
import org.xml.sax.*;

// this class shall have the burden of squeezing content
// out of channels.

// future prospects:
//  - Wrap IChannel classes
//     this should be done by parsing through
//     HTML that IChannel can output
//
//  - more complex caching ?
//  - Validation and timeouts
//      these two are needed for smooth operation of the portal
//      sometimes channels will timeout with information retreival
//      then the content should be skipped
//

public class ChannelManager 
{
  private HttpServletRequest req;
  private HttpServletResponse res;

  private Hashtable channelTable;
  private Hashtable rendererTable;

  private String channelTarget;
  private Hashtable targetParams;

  public ChannelManager ()
  { 
    channelTable = new Hashtable (); 
    rendererTable = new Hashtable ();
  }
  
  public ChannelManager (HttpServletRequest request, HttpServletResponse response) 
  {
    this ();
    this.req = request; 
    this.res = response;
  }
  
  public void setReqNRes (HttpServletRequest request, HttpServletResponse response) 
  {
    this.req = request; 
    this.res = response;
    rendererTable.clear ();
    processRequestChannelParameters (request);
  }

  /**
   * Look through request parameters for "channelTarget" and
   * pass corresponding actions/params to the channel
   * @param the request object
   */
  private void processRequestChannelParameters (HttpServletRequest req) 
  {
    // clear the previous settings
    channelTarget = null;
    targetParams = new Hashtable ();

    if ((channelTarget = req.getParameter ("channelTarget")) != null) 
    {
      Enumeration e = req.getParameterNames ();
      
      if (e != null) 
      {
        while (e.hasMoreElements ()) 
        {
          String pName= (String) e.nextElement ();
          
          if (!pName.equals ("channelTarget")) 
            targetParams.put (pName, req.getParameter (pName));
        }
      }
    }
  }

  /**
   * Start rendering the channel in a separate thread.
   * This function retreives a particular channel from cache, passes parameters to the
   * channel and then creates a new ChannelRenderer object to render the channel in a
   * separate thread.
   * @param chanID channel ID (unique)
   * @param className name of the channel class
   * @param params a table of parameters
   */
  public void startChannelRendering (String chanID, String className, long timeOut, Hashtable params) 
  {
    try
    {
      // see if the channel is cached
      IChannel ch;
      
      if ((ch = (IChannel) channelTable.get (chanID)) == null) 
      {
        ch = (org.jasig.portal.IChannel) Class.forName (className).newInstance ();
        
        // construct a ChannelStaticData object
        ChannelStaticData sd = new ChannelStaticData ();
        sd.setChannelID (chanID);
        sd.setTimeout (timeOut);
        sd.setParameters ( params);
        ch.setStaticData (sd);
        channelTable.put (chanID,ch);
      }

      // set up RuntimeData for the channel
      Hashtable chParams=new Hashtable ();
      
      if (chanID.equals (channelTarget)) 
        chParams = targetParams;
      
      //  RuntimeData rd=new ChannelRuntimeData(req,res,chanID,"index.jsp?channelTarget="+chanID+"&",chParams);
      ChannelRuntimeData rd = new ChannelRuntimeData ();
      rd.setParameters (chParams);
      rd.setHttpRequest (req);
      String reqURI = req.getRequestURI ();
      reqURI = reqURI.substring (reqURI.lastIndexOf ("/") + 1, reqURI.length ());
      rd.setBaseActionURL (reqURI + "?channelTarget=" + chanID + "&");
      ch.setRuntimeData (rd);

      ChannelRenderer cr = new ChannelRenderer (ch);
      cr.setTimeout (timeOut);
      cr.startRendering ();
      rendererTable.put (chanID,cr);
  } 
  catch (Exception e) 
  { 
    Logger.log (Logger.ERROR,e); }
  }

  /**
   * Output channel content.
   * Note that startChannelRendering had to be invoked on this channel prior to calling this function.
   * @param chanID unique channel ID
   * @param dh document handler that will receive channel content
   */
  public void outputChannel (String chanID, DocumentHandler dh) 
  {
    try 
    {
      ChannelRenderer cr;
      
      if ((cr = (ChannelRenderer) rendererTable.get (chanID)) != null) 
      {
        ChannelSAXStreamFilter custodian = new ChannelSAXStreamFilter (dh);
        int out = cr.outputRendering (custodian);

        //		Logger.log(Logger.DEBUG,"ChannelManager::outputChannel() : outputRendering() = "+Integer.toString(out));
      } 
      else 
      {
        Logger.log (Logger.ERROR,"ChannelManager::outputChannel() : ChannelRenderer for chanID=\""+chanID+"\" is absent from cache !!!");
      }
    } 
    catch (Exception e) 
    { 
      Logger.log (Logger.ERROR,e); 
    }
  }

  /**
   * Output channel content.
   * @param channel ID (unique)
   * @param name of the channel class
   * @param a table of parameters
   * @param an output DocumentHandler target
   */
  public void processChannel (String chanID, String className, Hashtable params, DocumentHandler dh) 
  {
    try
    {
      // see if the channel is cached
      IChannel ch;
      
      if ((ch = (IChannel) channelTable.get (chanID)) == null) 
      {
        ch = (org.jasig.portal.IChannel) Class.forName (className).newInstance ();
        
        // construct a ChannelStaticData object
        ChannelStaticData sd = new ChannelStaticData ();
        sd.setChannelID (chanID);
        sd.setParameters ( params);
        ch.setStaticData (sd);
        channelTable.put (chanID, ch);
      }

      ChannelSAXStreamFilter custodian = new ChannelSAXStreamFilter (dh);

      // set up RuntimeData for the channel
      Hashtable chParams = new Hashtable ();
      
      if (chanID.equals (channelTarget)) 
        chParams = targetParams;

      ChannelRuntimeData rd = new ChannelRuntimeData ();
      rd.setParameters (chParams);
      rd.setHttpRequest (req);
      String reqURI = req.getRequestURI ();
      reqURI = reqURI.substring (reqURI.lastIndexOf ("/") + 1, reqURI.length ());
      rd.setBaseActionURL (reqURI + "?channelTarget=" + chanID + "&");
      ch.setRuntimeData (rd);
      ch.renderXML (custodian);
    } 
    catch (Exception e) 
    { 
      Logger.log (Logger.ERROR,e); 
    }
  }

  /**
   * passes Layout-level event to a channel
   * @param channel ID
   * @param LayoutEvent object
   */
  public void passLayoutEvent (String chanID, LayoutEvent le) 
  {
    IChannel ch= (IChannel) channelTable.get (chanID);
    
    if (ch != null) 
    {
      ch.receiveEvent (le);
    } 
    else 
      Logger.log (Logger.ERROR, "ChannelManager::passLayoutEvent() : trying to pass an event to a channel that is not in cache. (cahnel=\"" + chanID + "\")");
  }
}
