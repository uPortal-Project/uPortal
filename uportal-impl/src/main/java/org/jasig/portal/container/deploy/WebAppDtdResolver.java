/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.deploy;

import java.io.InputStream;

import org.jasig.portal.utils.ResourceLoader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Uses a local copy of the DTD which is normally located
 * at http://java.sun.com/dtd/web-app_2_3.dtd.  If we don't
 * do this, we are likely to get screwed if the java.sun.com
 * webserver goes down.  This actually happened yesterday,
 * March 1, 2004, and it caused the portlet container
 * initialization to hang until it issued a socket connection
 * timeout exception.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class WebAppDtdResolver implements EntityResolver {

    /**
     * Sets the input source to the dtd normally
     * located at http://java.sun.com/dtd/web-app_2_3.dtd.
     * @param publicId the public ID
     * @param systemId the system ID
     * @return an input source based on the web-app dtd
     */
    public InputSource resolveEntity (String publicId, String systemId) {
        InputSource inputSource = null;

        try {
            InputStream inStream = ResourceLoader.getResourceAsStream(this.getClass(), "/org/jasig/portal/container/deploy/web-app_2_3.dtd");
            if (inStream != null) {
                inputSource =  new InputSource(inStream);
            }
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
      
        return inputSource;            
    }

}
