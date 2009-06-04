/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channel.dao.jpa;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelType;
import org.jasig.portal.channels.CGenericXSLT;
import org.jasig.portal.channels.portlet.CSpringPortletAdaptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Testcase for ChannelDefinition class.
 * @version $Revision$ $Date$
 */
public class ChannelDefinitionTest extends TestCase {

    /**
     * Test that ChannelDefinition identifies CPortletAdaptor as a portlet but
     * does not identify CGenericXSLT as a portlet.
     */
    public void testIsPortlet() {
        IChannelType channelType = EasyMock.createMock(IChannelType.class);
        EasyMock.expect(channelType.getId()).andReturn(1).anyTimes();
        EasyMock.replay(channelType);
        
        IChannelDefinition cd1 = new ChannelDefinitionImpl(channelType, "fname", CGenericXSLT.class.getName(), "Name", "title");
        assertFalse(cd1.isPortlet());
        
        IChannelDefinition cd2 = new ChannelDefinitionImpl(channelType, "fname", CSpringPortletAdaptor.class.getName(), "Name", "title");
        assertTrue(cd2.isPortlet());
        
    }
    
    /**
     * Test Element representation of channel to see that its node name is 
     * 'channel' and that it has the proper owning document and that the channel
     * element has the correct identifer such that it can be gotten by ID.
     * @throws ParserConfigurationException
     */
    public void testGetDocument() throws ParserConfigurationException {
        IChannelType channelType = EasyMock.createMock(IChannelType.class);
        EasyMock.expect(channelType.getId()).andReturn(12).anyTimes();
        EasyMock.replay(channelType);
        
        IChannelDefinition cd = new ChannelDefinitionImpl(channelType, "test_fname", CGenericXSLT.class.getName(), "testName", "testTitle");
        
        cd.setDescription("A test channel description.");
        cd.setEditable(false);
        cd.setHasAbout(true);
        cd.setHasHelp(false);
        cd.setIsSecure(false);
        cd.setTimeout(500);
        
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        
        Element channelElement = cd.getDocument(doc, "testId");
        
        assertEquals("channel", channelElement.getNodeName());
        assertSame(doc, channelElement.getOwnerDocument());
        
        doc.appendChild(channelElement);
        
        assertSame(channelElement, doc.getElementById("testId"));
        
        assertEquals("A test channel description.", channelElement.getAttribute("description"));
        assertEquals("false", channelElement.getAttribute("editable"));
        assertEquals("test_fname", channelElement.getAttribute("fname"));
        assertEquals("true", channelElement.getAttribute("hasAbout"));
        assertEquals("false", channelElement.getAttribute("hasHelp"));
        assertEquals("false", channelElement.getAttribute("secure"));
        assertEquals("org.jasig.portal.channels.CGenericXSLT", channelElement.getAttribute("class"));
        assertEquals("testName", channelElement.getAttribute("name"));
        assertEquals("500", channelElement.getAttribute("timeout"));
        assertEquals("testTitle", channelElement.getAttribute("title"));
        assertEquals("12", channelElement.getAttribute("typeID"));

        
    }

}