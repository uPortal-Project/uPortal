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
import org.jasig.portal.*;
import org.jasig.portal.layout.*;

import java.net.*;


/**
 * Weather.
 *
 * @author Ken Weiner
 * @version $Revision$
 */
public class CWeather implements org.jasig.portal.IChannel
{
  private ChannelConfig chConfig = null;
  private static Vector params = null;

  public void init (ChannelConfig chConfig) {this.chConfig = chConfig;}
  public String getName () {return "Weather";}
  public boolean isMinimizable () {return true;}
  public boolean isDetachable () {return true;}
  public boolean isRemovable () {return true;}
  public boolean isEditable () {return false;}
  public boolean hasHelp () {return false;}

  public int getDefaultDetachWidth () {return 250;}
  public int getDefaultDetachHeight () {return 250;}

  public Vector getParameters()
  {
    return params;
  }

  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try
    {
      // Change station parameter to reflect local area.
      // Station guide at http://www.abcnews.go.com/local/
      String sState = (String) chConfig.get ("state");
      String sCity = ((String) chConfig.get ("city")).replace (' ', '_');

      out.println ("<center>");
      out.println ("<a href=\"http://www.wunderground.com/US/" + sState + "/" + sCity + ".html\">");
      out.println ("<img src=\"http://banners.wunderground.com/banner/infoboxtr/US/" + sState + "/" + sCity + ".gif\" alt=\"Click for " + sCity.replace ('_', ' ') + ", " + sState + " Forecast\" height=108 width=144 border=0></a>");
      out.println ("</center>");
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }
  public void edit (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    // This channel is not editable
  }

  public void save (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    // This channel is not editable
  }

  public void help (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    // This channel has no help
  }
}