/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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

import org.jasig.portal.UtilitiesBean;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.services.LogService;
import org.xml.sax.DocumentHandler;
import java.io.StringWriter;

/** <p>Displays an applet. To pass in applet parameters, construct
 * channel parameters whose keys start with the string "APPLET."</p>
 * <p>For example, the key/value pair
 * <code>APPLET.data=foo</code>
 * as a channel parameter is translated to an applet parameter as
 * <code>data=foo</code></p>
 * <p><i>This code was adapted from uPortal 1.0's
 * <code>org.jasig.portal.channels.CApplet</code></i></p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class CApplet extends BaseChannel
{
  private static final String sslLocation = UtilitiesBean.fixURI("webpages/stylesheets/org/jasig/portal/channels/CApplet/CApplet.ssl");

  /** Output channel content to the portal
   * @param out a sax document handler
   */
  public void renderXML (DocumentHandler out)
  {
    try
    {
      StringWriter w = new StringWriter ();
      w.write ("<?xml version='1.0'?>\n");
      w.write ("<applet code=\"" + staticData.getParameter ("code") + "\"\n");
      w.write ("        codebase=\"" + staticData.getParameter ("codeBase") + "\"\n");
      w.write ("        width=\"" + staticData.getParameter ("width") + "\"\n");
      w.write ("        height=\"" + staticData.getParameter ("height")  + "\"\n");
      w.write ("        align=\"top\"\n");
      w.write ("        border=\"0\"\n");
      w.write ("        archive=\"" + staticData.getParameter ("archive") + "\">\n");

      // Take all parameters whose names start with "APPLET." and pass them
      // to the applet (after stripping "APPLET.")
      java.util.Enumeration allKeys = staticData.keys ();

      while (allKeys.hasMoreElements ())
      {
        String p = (String) allKeys.nextElement();

        if (p.startsWith ("APPLET."))
        {
          String name = p.substring (7); // skip "APPLET."
          String value = (String) staticData.getParameter (p);
          w.write ("  <param name=\"" + name + "\" value=\"" + value + "\"/>\n");
        }
      }

      w.write ("</applet>\n");

      XSLT xslt = new XSLT();
      xslt.setXML(w.toString());
      xslt.setXSL(sslLocation, "main", runtimeData.getBrowserInfo());
      xslt.setTarget(out);
      xslt.transform();
    }
    catch (Exception e)
    {
      LogService.instance().log(LogService.ERROR, e);
    }
  }
}
