/* Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.chanpub;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.Element;

import org.jasig.portal.RDBMServices;

public final class UrlChannelPublisher {

    public static void publishChannel(Element m) {

        try {

            RDBMServices.setGetDatasourceFromJndi(false);

            m.remove(m.attribute("script"));
            Document d = m.getDocument();
            d.addDocType("channel-definition", null, "channelDefinition.dtd");

            String xml = d.asXML();
            InputStream inpt = new ByteArrayInputStream(xml.getBytes());
            ChannelPublisher pub = ChannelPublisher.getCommandLineInstance();
            pub.publishChannel(inpt);

        } catch (Throwable t) {
            String msg = "Error publishing the channel definition.";
            throw new RuntimeException(msg, t);
        }

    }

}