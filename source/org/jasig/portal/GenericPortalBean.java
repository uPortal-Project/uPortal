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

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import java.io.*;

/**
 * This is a base class for all the Portal beans to extend.
 * The base class functionality contains all of the reusable code.
 * 
 * @author Ken Weiner
 * @author John Laker
 */
public class GenericPortalBean
{  
  private static String sPortalBaseDir = null;
  public boolean DEBUG = false;
  
  /**
   * Set the top level directory for the portal.  This makes it possible
   * to use relative paths in the application for loading properties files, etc.
   * @param sPathToPortal
   */
  public static void setPortalBaseDir (String sPathToPortal)
  {
    sPortalBaseDir = sPathToPortal;
  }
  
  /**
   * Get the top level directory for the portal.  This makes it possible
   * to use relative paths in the application for loading properties files, etc.
   */
  public static String getPortalBaseDir ()
  {
    return sPortalBaseDir;
  }

  /**
   * Just a simple debug method that prints
   * messages to System.out
   */
  public void debug(String message)
  {
    if (DEBUG) System.out.println ("DEBUG: " + message);
  }
}

