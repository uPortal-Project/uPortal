/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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


