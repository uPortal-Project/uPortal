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

import java.io.InputStream;

import org.jasig.portal.PortalSessionManager;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Provides a means to resolve uPortal DTDs
 * @author Peter Kharchenko, peterk@interactivebusiness.com
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @author Dave Wallace, dwallace@udel.edu modifications 
 * @version $Revision$
 */
public class DTDResolver implements EntityResolver
{
  private String dtdPath = "dtd";
  private String dtdName = null;

  /**
   * Constructor for DTDResolver
   */
  public DTDResolver () {
  }

  /**
   * Constructor for DTDResolver
   * @param dtdName the name of the dtd
   */
  public DTDResolver (String dtdName) {
    this.dtdName = dtdName;
  }

  /**
   * Sets up a new input source based on the dtd specified in the xml document
   * @param publicId the public ID
   * @param systemId the system ID
   * @return an input source based on the dtd specified in the xml document
   *               or null if we don't have a dtd that matches systemId or publicId
   */
  public InputSource resolveEntity (String publicId, String systemId) {
    InputStream inStream = null;

    // Check for a match on the systemId
    if (systemId != null) {
      if (dtdName != null && systemId.indexOf(dtdName) != -1)
        inStream = PortalSessionManager.getResourceAsStream(dtdPath + "/" + dtdName);
      else if (systemId.trim().equalsIgnoreCase("http://my.netscape.com/publish/formats/rss-0.91.dtd"))
        inStream = PortalSessionManager.getResourceAsStream(dtdPath + "/rss-0.91.dtd");
         
      if ( null != inStream )
          return new InputSource(inStream);
    }
    
    // Check for a match on the public id
    if ( publicId != null ) {
        if ( publicId.trim().equalsIgnoreCase("-//Netscape Communications//DTD RSS 0.91//EN"))
            inStream = PortalSessionManager.getResourceAsStream(dtdPath + "/rss-0.91.dtd");
            
        if ( null != inStream )
            return new InputSource(inStream);
    }
        
    // Return null to let the parser handle this entity 
    return null;
  }
}
