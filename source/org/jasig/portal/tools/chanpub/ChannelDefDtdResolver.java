/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.chanpub;

import java.io.InputStream;

import org.jasig.portal.utils.ResourceLoader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Resolves the channelDefinition.dtd
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ChannelDefDtdResolver implements EntityResolver {

    public ChannelDefDtdResolver() {
        super();
    }

	/**
	 * Sets the input source to the channel definition dtd.
	 * @param publicId the public ID
	 * @param systemId the system ID
	 * @return an input source based on the channel definition dtd
	 */
	public InputSource resolveEntity (String publicId, String systemId) {
	    InputSource inputSource = null;

        try {
            InputStream inStream = ResourceLoader.getResourceAsStream(this.getClass(), "/properties/chanpub/chandefs/channelDefinition.dtd");
            if (inStream != null) {
                inputSource =  new InputSource(inStream);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
	  
	    return inputSource;            
	}
}
