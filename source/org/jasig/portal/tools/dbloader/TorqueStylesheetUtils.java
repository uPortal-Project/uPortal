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

package org.jasig.portal.tools.dbloader;

import java.util.StringTokenizer;

/**
 * Extension functions for the stylesheets used to transform tables.xml
 * and data.xml for Torque.
 *
 * @author Sarah Arnott
 */
public class TorqueStylesheetUtils 
{

 /**
  * Returns the identifier in the default form for Torque.
  * (If table is called 'UP_USER' in database, this method returns
  * the identifier as 'UpUser')
  */
  public static String getName(String orig)
  {
    StringTokenizer stok = new StringTokenizer(orig, "_");
    String str = "" ;
    while(stok.hasMoreTokens())
    {
      String s = stok.nextToken();
      String s1 = s.substring(0, 1);
      String s2 = s.substring(1, s.length()).toLowerCase();

      str = str + s1 + s2;
    }
    return str;
  }
}
