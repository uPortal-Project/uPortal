/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
 * @deprecated See the deprecation message on {@link ChannelPublisher}.
 */
@Deprecated
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
            InputStream inStream = ResourceLoader.getResourceAsStream(this.getClass(), "/dtd/channelDefinition.dtd");
            if (inStream != null) {
                inputSource =  new InputSource(inStream);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	  
	    return inputSource;            
	}
}
