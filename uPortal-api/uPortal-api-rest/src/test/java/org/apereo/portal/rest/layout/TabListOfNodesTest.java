/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.rest.layout;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class TabListOfNodesTest {

    TabListOfNodes nodes;

    @Before
    public void setup() {
        nodes = new TabListOfNodes();
    }

    @Test(expected = NullPointerException.class)
    public void testAddAllChannelsNull() {
        nodes.addAllChannels(null);
    }

    @Test
    public void testAddAllChannels() {
        Document document;
        String xml =
                "<bookstore> <channel>  <title>Everyday Italian</title> "
                        + "<author>Giada De Laurentiis</author> "
                        + "<year>2005</year> "
                        + "<price>30.00</price> "
                        + "</channel> "
                        + "<channel>  <title>Harry Potter</title>  "
                        + "<author>J K. Rowling</author>"
                        + "  <year>2005</year> "
                        + "<price>29.99</price> </channel>"
                        + " <channel> <title>XQuery Kick Start</title>  "
                        + "<author>James McGovern</author> "
                        + " <author>Per Bothner</author> "
                        + "<author>Kurt Cagle</author> "
                        + "<author>James Linn</author> "
                        + "<author>Vaidyanathan Nagarajan</author> "
                        + "<year>2003</year> "
                        + "<price>49.99</price> </channel> "
                        + "<channel> <title>Learning XML</title> <author>Erik T. Ray</author> <year>2003</year>"
                        + "<price>39.95</price> </channel> "
                        + "</bookstore>";
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            document = documentBuilder.parse(is);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to parse region metadata file: " + e.getMessage(), e);
        }

        NodeList bookstores = document.getElementsByTagName("bookstore");
        nodes.addAllChannels(bookstores);
        Assert.assertEquals(4, nodes.getLength());
    }
}
