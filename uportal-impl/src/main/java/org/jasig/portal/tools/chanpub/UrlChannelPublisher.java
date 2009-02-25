/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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