/**
 * Copyright <A9> 2001 The JA-SIG Collaborative.  All rights reserved.
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
 * This class includes extension functions for the CWebProxy stylesheets.
 *
 * @author Sarah Arnott, sarnott@mun.ca
 * @version $Revision$
 */
public class StylesheetUtils 
{

  /**
   * Returns the absolute uri calculated from the base and file parameters...
   * Returns an empty string when the base does not contain the correct
   * protocol (ie. <something>://<something>).
   */
  public static String getAbsURI (String base, String file)
  {
    String uri = "";
    if (file.startsWith("/"))
    {
      int i = base.indexOf("://");
      if (i != -1)
      {
        int first = base.indexOf("/", i+3);
        uri = base.substring(0, first).concat(file);
      }
    }
    else
    {
      int last = base.lastIndexOf("/");
      uri = base.substring(0, last+1).concat(file);
    }
   
    if ( uri.indexOf("/../") != -1 )
      return removeUpDirs(uri);
    else
      return uri;

  }

  /**
   * Returns the absolute URI without any '/../'.  Some browsers and web 
   * servers do not handle these URIs correctly.
   * @param uri the absolute URI generated from the input source document
   */
  private static String removeUpDirs (String uri)
  {
    String begin;
    String end;
    int upDirIndex;
    int endProtoIndex = uri.indexOf("//");

    while ((upDirIndex=uri.indexOf("/../")) != -1)
    {
      end = uri.substring(upDirIndex+4);
      begin = uri.substring(0, upDirIndex);

      if (begin.indexOf("/", endProtoIndex+2) != -1)       
        begin = uri.substring(0, begin.lastIndexOf("/")+1);
      else
        begin += "/";

      uri = begin.concat(end);
    }
    return uri;
  }

}
