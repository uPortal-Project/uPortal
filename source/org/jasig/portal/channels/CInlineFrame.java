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
 * @author Susan Bramhall
 * @version $Revision$
 */
public class CInlineFrame implements org.jasig.portal.IChannel
{
  protected String m_sUrl = null;
  private ChannelConfig chConfig = null;

  private static Vector params = null;

  public CInlineFrame()
  {
    params = new Vector();
    params.addElement(new ParameterField("URL", "url", "50", "70", "You have chosen to publish a channel that requires you to provide a URL. Please enter the URL for the channel you wish to publish below.") );
    params.addElement(new ParameterField("Height", "height", "3", "4", "The channel width will be determined by its layout but you must specify the height in pixels. " +
                                    "Please enter the height below.") );
    params.addElement(new ParameterField("Name", "name", "30", "40", "Please enter a decriptive name below.") );
  }

  public void init (ChannelConfig chConfig) {this.chConfig = chConfig;}
  public String getName () {return (String) chConfig.get ("name");}
  public boolean isMinimizable () {return true;}
  public boolean isDetachable () {return true;}
  public boolean isRemovable () {return true;}
  public boolean isEditable () {return false;}
  public boolean hasHelp () {return false;}

  public int getDefaultDetachWidth () {return 0;}
  public int getDefaultDetachHeight () {return 0;}



  public Vector getParameters()
  {
    return params;
  }

  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try
    {
      String sBrowser = req.getHeader("User-Agent");
      String sHeight = (String) chConfig.get ("height");
      m_sUrl = (String) chConfig.get ("url");

      StringBuffer sbHTML = new StringBuffer (1024);

        // This test gets IE 4 and 5 and Netscape 6 into the Iframe world
        // all others get just a link.  This could undoubtedly get refined.
        // IE3 also handled Iframes but I'm not sure of the string it returns.
          String  sHTML;
          if ((sBrowser.indexOf("MSIE 3")>=0)||
              (sBrowser.indexOf(" MSIE 4")>=0) ||
              (sBrowser.indexOf("MSIE 5")>=0) ||
              (sBrowser.indexOf("MSIE 6")>=0) ||
              (sBrowser.indexOf("Mozilla/5")>=0) )
            sHTML = "<IFRAME height=" + sHeight + " width=100% frameborder='no' src='" +
              m_sUrl + "'></IFRAME>";
          else
            sHTML = "<p>This browser does not support inline frames.</p>" +
               "<A href="+ m_sUrl +
               " target='IFrame_Window'>Click this link to view content</A> in a separate window.";
         out.println (sHTML);
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