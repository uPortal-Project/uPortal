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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Used by the import-channel Cernunnos script
 */
public final class UrlChannelPublisher {

    public static void publishChannel(Element m) {

        try {

            m.remove(m.attribute("script"));
            Document d = m.getDocument();
            d.addDocType("channel-definition", null, "channelDefinition.dtd");

            String xml = d.asXML();
            InputStream inpt = new ByteArrayInputStream(xml.getBytes());
            IChannelPublisher pub = ChannelPublisher.getCommandLineInstance();
            pub.publishChannel(inpt);

        } catch (Throwable t) {
            String msg = "Error publishing the channel definition.";
            throw new RuntimeException(msg, t);
        }

    }

}