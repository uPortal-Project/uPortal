/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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