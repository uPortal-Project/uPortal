/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
