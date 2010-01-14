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

package org.jasig.portal.channels.error;


import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Testcase for ErrorDocument.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class ErrorDocumentTest extends TestCase {
    protected final Log logger = LogFactory.getLog(this.getClass());

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetDocument() {
        ErrorDocument errorDocument = new ErrorDocument();
        
        RuntimeException testException = new RuntimeException("Test message");
        errorDocument.setThrowable(testException);
        
        String message = "A test message";
        errorDocument.setMessage(message);
        
        ErrorCode errorCode = ErrorCode.CHANNEL_MISSING_EXCEPTION;
        errorDocument.setCode(errorCode);
        
        String channelName = "SomeChannel";
        errorDocument.setChannelName(channelName);
        
        String channelSubscribeId = "someId";
        errorDocument.setChannelSubscribeId(channelSubscribeId);
        
        Document doc = errorDocument.getDocument();
        
        logger.info(XML.serializeNode(doc));
        
        Element docElement = doc.getDocumentElement();

        assertEquals("error", docElement.getNodeName());
        assertEquals(Integer.toString(errorCode.getCode()), docElement.getAttribute("code"));
        
        NodeList messageNodes = docElement.getElementsByTagName("message");

        Node messageNode = messageNodes.item(0);
        assertEquals(message, messageNode.getFirstChild().getNodeValue());
        
        NodeList channelNodes = docElement.getElementsByTagName("channel");
        Node channelNode = channelNodes.item(0);
        NodeList idNodes = ((Element) channelNode).getElementsByTagName("id");
        Node idNode = idNodes.item(0);
        
        assertEquals(channelSubscribeId, idNode.getFirstChild().getNodeValue());
        
        NodeList channelNameNodes = ((Element) channelNode).getElementsByTagName("name");
        Node channelNameNode = channelNameNodes.item(0);
        assertEquals(channelName, channelNameNode.getFirstChild().getNodeValue());
        
        // TODO: test "throwable" element.
    }
    
    /**
     * Test that ErrorDocument returns a basic and valid, if useless, Document
     * when its state is not configured.
     */
    public void testGetDocumentNoSettersCalled(){
        ErrorDocument errorDocument = new ErrorDocument();
        
        Document doc = errorDocument.getDocument();
        
        logger.info(XML.serializeNode(doc));
        
        Element docElement = doc.getDocumentElement();

        assertEquals("error", docElement.getNodeName());
        assertEquals(Integer.toString(ErrorCode.UNKNOWN_ERROR.getCode()), 
                docElement.getAttribute("code"));
        
    }

}