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
package org.jasig.portal.services.dom;

import org.jasig.portal.properties.PropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Nick Bolton, nbolton@unicon.net
 * @version $Revision$
 */
public class DOMInitServiceFactory {

    private static final Log log = LogFactory.getLog(DOMInitServiceFactory.class);
    
  private static IDOMInitService service = null;

  private static String className = null;

  static {
    try {
      // Retrieve the class name of the concrete IDOMInitService implementation
      className = PropertiesManager.getProperty("org.jasig.portal.services.dom.DOMInitServiceFactory.implementation");
    } catch (Exception e ) {}
  }

  /**
   * Returns an instance of the IDOMInitService specified in portal.properties.
   * If the property doesn't exist or is empty, null is returned.
   * @return an IDOMInitService implementation
   */
  public static IDOMInitService getService() {
    try {
      if (className == null || "".equals(className)) return null;

      if (service == null) {
        service = (IDOMInitService)Class.forName(className).newInstance();
      }
      return service;
    } catch (Exception e) {
      log.error( "DOMInitServiceFactory: Could not instantiate " + className, e);
    }
    return service;
  }
}
