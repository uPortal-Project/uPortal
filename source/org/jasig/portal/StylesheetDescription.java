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

package org.jasig.portal;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.w3c.dom.ProcessingInstruction;

/**
 * Combines all of the information required to describe
 * an XSLT stylesheet.
 * @author Peter Kharchenko, pkharchenko@interactivebusiness.com
 * @version $Revision$
 */
public class StylesheetDescription
{
  public String s_href;
  public String s_type;
  public String s_title;
  public String s_media;
  public String s_charset;
  public boolean b_alternate;

  public StylesheetDescription ()
  {
  }

  public StylesheetDescription (String uri, String type)
  {
    s_href = uri;
    s_type=type;
    s_media = new String ("");
    s_charset = null;
    b_alternate = false;
    s_title = new String ("");
  }

  public StylesheetDescription (String uri, String type, String title, String media, String charset, boolean alternate)
  {
    s_href = uri;
    s_type = type;
    s_media = media;
    s_charset = charset;
    b_alternate = alternate;
    s_title = title;
  }

  public StylesheetDescription (ProcessingInstruction pi)
  {
    // determine parameters from the ProcessingInstruction
    if (pi.getNodeName ().equals ("xml-stylesheet"))
    {
      PIAttributes pia = new PIAttributes (pi);
      s_href = pia.getAttribute ("href");
      s_type = pia.getAttribute ("type");
      s_title = pia.getAttribute ("title");
      s_media = pia.getAttribute ("media");
      s_charset = pia.getAttribute ("charset");

      if ("yes".equals (pia.getAttribute ("alternate")))
        b_alternate=true; else b_alternate=false;

      if (s_media==null)
        s_media=new String ("");

      if (s_title==null)
        s_title=new String ("");
    }
  }

  public StylesheetDescription (String data)
  {
    PIAttributes pia = new PIAttributes (data);
    s_href = pia.getAttribute ("href");
    s_type = pia.getAttribute ("type");
    s_title = pia.getAttribute ("title");
    s_media = pia.getAttribute ("media");
    s_charset = pia.getAttribute ("charset");

    if ("yes".equals (pia.getAttribute ("alternate")))
      b_alternate=true;
    else
      b_alternate=false;

    if (s_media==null)
      s_media=new String ("");

    if (s_title==null)
      s_title=new String ("");
  }

  public String getTitle ()
  {
    return s_title;
  }

  public String getURI ()
  {
    return s_href;
  }

  public String getType ()
  {
    return s_type;
  }

  public String getMedia ()
  {
    return s_media;
  }

  public boolean getAlternate ()
  {
    return b_alternate;
  }

  public String getCharset ()
  {
    return s_charset;
  }

  public void setTitle (String title)
  {
    s_title=title;
  }

  public void setType (String type)
  {
    s_type=type;
  }

  public void setMedia (String media)
  {
    s_media=media;
  }

  public void setURI (String uri)
  {
    s_href=uri;
  }

  public void setCharset (String charset)
  {
    s_charset=charset;
  }

  public void setAlternate (boolean alternate)
  {
    b_alternate=alternate;
  }

    /**
     * Parses a processing instruction's (PI) attributes for easy retrieval.
     */
    class PIAttributes
    {
        private Hashtable piAttributes = null;

        PIAttributes (String data)
        {
            piAttributes = new Hashtable ();
            StringTokenizer tokenizer = new StringTokenizer (data, "=\"");
            while (tokenizer.hasMoreTokens ())
                {
                    piAttributes.put (tokenizer.nextToken ().trim (), tokenizer.nextToken ().trim ());
                }
        }

        /**
         * Constructor.
         * @param pi The processing instruction whose attributes are to be parsed
         */
        PIAttributes (ProcessingInstruction pi)
        {
            this (pi.getNodeValue ());
        }

        /**
         * Returns value of specified attribute.
         *  @param name Attribute name
         *  @return Attribute value, or null if the attribute name does not exist
         */
        String getAttribute (String name)
        {
            return (String) piAttributes.get (name);
        }
    }

}


