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

package org.jasig.portal.utils;

/**
 * This utility provides a simple way of escaping the special or
 * reserved characters in XML that serve as delimiters so that
 * a string of characters can be left untouched by an XML parser.
 * See http://www.w3.org/TR/2000/REC-xml-20001006#syntax
 * and http://www.w3.org/TR/2000/REC-xml-20001006#sec-predefined-ent
 * and http://www.w3.org/TR/2000/REC-xml-20001006#sec-entexpand
 * Most of the code was borrowed from Xerces serializer classes.
 * If anyone finds a useable method in a standard XML API
 * that escapes XML strings, we should use it in place of this class.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class XMLEscaper
{
  /**
   * Escapes an XML string
   * @param source a String to be escaped
   * @return an escaped String
   */
  public static String escape (String source)
  {
    String result = null;

    if ( source != null)
    {
      StringBuffer sb = new StringBuffer (source.length () + 256);

      for (int i = 0 ; i < source.length() ; i++)
        sb.append (escape (source.charAt (i)));

      result = sb.toString();

    }

    return (result);
  }

  /**
   * Escapes an XML character
   * @param ch a char to be escaped
   * @return an escaped char
   */
  public static String escape (char ch)
  {
    StringBuffer sb = new StringBuffer (10);
    String charRef;

    // If there is a suitable entity reference for this character, print it.
    charRef = getEntityRef (ch);

    if ( charRef != null )
    {
      sb.append ('&');
      sb.append (charRef);
      sb.append (';');
    }
    else if (( ch >= ' ' && ch <= 0x7E && ch != 0xF7 ) ||
                ch == '\n' || ch == '\r' || ch == '\t' )
    {
      // If the character is not printable, print as character reference.
      // Non printables are below ASCII space but not tab or line
      // terminator, ASCII delete, or above a certain Unicode threshold.
      sb.append (ch);
    }
    else
    {
      sb.append ("&#");
      sb.append (Integer.toString (ch));
      sb.append (';');
    }

    return sb.toString ();
  }

  private static String getEntityRef (char ch)
  {
    // Encode special XML characters into the equivalent character references.
    // These five are defined by default for all XML documents.
    switch (ch)
    {
      case '<':
        return "lt";
      case '>':
        return "gt";
      case '"':
        return "quot";
      case '\'':
        return "apos";
      case '&':
        return "amp";
    }
    return null;
  }

  /**
    * This method is provided to test out the escape method.
    * @param args the command line arguments
  */
  public static void main (String args[])
  {
    if (args.length < 1)
    {
      System.out.println ("Usage: XMLEscaper \"<string to escape>\"");
    }
    else
    {
      String before = args[0];
      String after = escape (before);
      System.out.println ("Before escaping: " + before);
      System.out.println (" After escaping: " + after);
    }
  }
}
