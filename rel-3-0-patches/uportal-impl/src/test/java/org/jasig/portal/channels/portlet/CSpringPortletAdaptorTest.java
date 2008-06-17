/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.channels.portlet;

import java.io.PrintWriter;

import junit.framework.TestCase;

import org.apache.commons.io.output.NullOutputStream;
import org.easymock.EasyMock;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.channels.support.IChannelTitle;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CSpringPortletAdaptorTest extends TestCase {
    private CSpringPortletAdaptor portletAdaptor;
    private ISpringPortletChannel portletChannel;
    private ChannelStaticData sd;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        this.portletAdaptor = new CSpringPortletAdaptor();
        this.portletChannel = EasyMock.createMock(ISpringPortletChannel.class);
        this.sd = new ChannelStaticData();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        this.portletAdaptor = null;
        this.portletChannel = null;
        this.sd = null;
    }

    public void testBadInitLifecycle() throws Exception {
        try {
            this.portletAdaptor.setStaticData(this.sd);
            fail("IllegalStateException expected, no portal control structures");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        
        final PortalControlStructures pcs = new PortalControlStructures(new MockHttpServletRequest(), null);
        this.portletAdaptor.setPortalControlStructures(pcs);
        try {
            this.portletAdaptor.setStaticData(sd);
            fail("IllegalStateException expected, no springBeanName");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        
        this.portletAdaptor.setPortalControlStructures(pcs);
        this.sd.setParameter("springBeanName", "portletChannel");
        try {
            this.portletAdaptor.setStaticData(sd);
            fail("IllegalStateException expected, no WebApplicationContext");
        }
        catch (IllegalStateException ise) {
            //expected
        }
    }
    
    public void testInitLifecycle() throws Exception {
        this.sd.setParameter("springBeanName", "portletChannel");
        
        final PortalControlStructures pcs = new PortalControlStructures(new MockHttpServletRequest(), null);
        
        final WebApplicationContext webApplicationContext = EasyMock.createMock(WebApplicationContext.class);
        EasyMock.expect(webApplicationContext.getBean("portletChannel", ISpringPortletChannel.class)).andReturn(this.portletChannel);
        EasyMock.replay(webApplicationContext);

        this.sd.setWebApplicationContext(webApplicationContext);
        
        EasyMock.reset(this.portletChannel);
        this.portletChannel.initSession(this.sd, pcs);
        EasyMock.expectLastCall().once();
        
        EasyMock.replay(this.portletChannel);
        
        this.portletAdaptor.setPortalControlStructures(pcs);
        this.portletAdaptor.setStaticData(this.sd);
        
        EasyMock.verify(webApplicationContext, this.portletChannel);
        
        //Verify that cleanup happened
        try {
            this.portletAdaptor.setStaticData(this.sd);
            fail("IllegalStateException expected, no portal control structures");
        }
        catch (IllegalStateException ise) {
            //expected
        }
    }
    
    public void testBadActionLifecycle() throws Exception {
        try {
            this.portletAdaptor.processAction();
            fail("IllegalStateException expected, no channel static data");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        
        //Have to init first
        this.testInitLifecycle();
        
        try {
            this.portletAdaptor.processAction();
            fail("IllegalStateException expected, no portal control structures");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        
        final PortalControlStructures pcs = new PortalControlStructures(new MockHttpServletRequest(), null);
        this.portletAdaptor.setPortalControlStructures(pcs);
        try {
            this.portletAdaptor.processAction();
            fail("IllegalStateException expected, no channel runtime data");
        }
        catch (IllegalStateException ise) {
            //expected
        }
    }
    
    public void testActionLifecycle() throws Exception {
        //Have to init first
        this.testInitLifecycle();
        
        final PortalControlStructures pcs = new PortalControlStructures(new MockHttpServletRequest(), null);
        
        final ChannelRuntimeData rd = new ChannelRuntimeData();
        this.portletAdaptor.setPortalControlStructures(pcs);
        this.portletAdaptor.setRuntimeData(rd);
        
        EasyMock.reset(this.portletChannel);
        this.portletChannel.action(this.sd, pcs, rd);
        EasyMock.expectLastCall().once();
        EasyMock.replay(this.portletChannel);
        
        this.portletAdaptor.processAction();
        
        EasyMock.verify(this.portletChannel);
        
        //Verify that cleanup happened
        try {
            this.portletAdaptor.processAction();
            fail("IllegalStateException expected, no portal control structures");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        
        this.portletAdaptor.setPortalControlStructures(pcs);
        try {
            this.portletAdaptor.processAction();
            fail("IllegalStateException expected, no channel runtime data");
        }
        catch (IllegalStateException ise) {
            //expected
        }
    }
    
    public void testBadRenderLifecycle() throws Exception {
        final PrintWriter pw = new PrintWriter(new NullOutputStream());
        final Object validity = new Object();
        try {
            this.portletAdaptor.generateKey();
            fail("IllegalStateException expected, no channel static data");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        try {
            this.portletAdaptor.isCacheValid(validity);
            fail("IllegalStateException expected, no channel static data");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        try {
            this.portletAdaptor.renderCharacters(pw);
            fail("IllegalStateException expected, no channel static data");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        try {
            this.portletAdaptor.getRuntimeProperties();
            fail("IllegalStateException expected, no channel static data");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        
        
        //Have to init first
        this.testInitLifecycle();
        
        try {
            this.portletAdaptor.generateKey();
            fail("IllegalStateException expected, no portal control structures");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        try {
            this.portletAdaptor.isCacheValid(validity);
            fail("IllegalStateException expected, no portal control structures");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        try {
            this.portletAdaptor.renderCharacters(pw);
            fail("IllegalStateException expected, no portal control structures");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        try {
            this.portletAdaptor.getRuntimeProperties();
            fail("IllegalStateException expected, no portal control structures");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        
        
        final PortalControlStructures pcs = new PortalControlStructures(new MockHttpServletRequest(), null);
        this.portletAdaptor.setPortalControlStructures(pcs);
        try {
            this.portletAdaptor.generateKey();
            fail("IllegalStateException expected, no channel runtime data");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        try {
            this.portletAdaptor.isCacheValid(validity);
            fail("IllegalStateException expected, no channel runtime data");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        try {
            this.portletAdaptor.renderCharacters(pw);
            fail("IllegalStateException expected, no channel runtime data");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        try {
            this.portletAdaptor.getRuntimeProperties();
            fail("IllegalStateException expected, no channel runtime data");
        }
        catch (IllegalStateException ise) {
            //expected
        }
    }
    
    public void testRenderLifecycle() throws Exception {
      //Have to init first
        this.testInitLifecycle();
        
        final PortalControlStructures pcs = new PortalControlStructures(new MockHttpServletRequest(), null);
        
        final ChannelRuntimeData rd = new ChannelRuntimeData();
        this.portletAdaptor.setPortalControlStructures(pcs);
        this.portletAdaptor.setRuntimeData(rd);

        final ChannelCacheKey channelCacheKey = new ChannelCacheKey();
        final Object validity = new Object();
        final PrintWriter pw = new PrintWriter(new NullOutputStream());
        
        EasyMock.reset(this.portletChannel);
        EasyMock.expect(this.portletChannel.generateKey(this.sd, pcs, rd)).andReturn(channelCacheKey).once();
        EasyMock.expect(this.portletChannel.isCacheValid(this.sd, pcs, rd, validity)).andReturn(false).once();
        this.portletChannel.render(this.sd, pcs, rd, pw);
        EasyMock.expectLastCall().once();
        EasyMock.expect(this.portletChannel.getTitle(this.sd, pcs, rd)).andReturn("Test Title").once();
        EasyMock.replay(this.portletChannel);
        
        ChannelCacheKey generatedKey = this.portletAdaptor.generateKey();
        assertEquals(channelCacheKey, generatedKey);
        
        boolean cacheValid = this.portletAdaptor.isCacheValid(validity);
        assertFalse(cacheValid);
        
        this.portletAdaptor.renderCharacters(pw);
        
        final IChannelTitle channelTitle = (IChannelTitle)this.portletAdaptor.getRuntimeProperties();
        final String title = channelTitle.getChannelTitle();
        assertEquals("Test Title", title);
        
        EasyMock.verify(this.portletChannel);
     
        //Verify the cleanup happened after getRuntimeProperties
        try {
            this.portletAdaptor.generateKey();
            fail("IllegalStateException expected, no portal control structures");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        try {
            this.portletAdaptor.isCacheValid(validity);
            fail("IllegalStateException expected, no portal control structures");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        try {
            this.portletAdaptor.renderCharacters(pw);
            fail("IllegalStateException expected, no portal control structures");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        try {
            this.portletAdaptor.getRuntimeProperties();
            fail("IllegalStateException expected, no portal control structures");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        
        
        this.portletAdaptor.setPortalControlStructures(pcs);
        try {
            this.portletAdaptor.generateKey();
            fail("IllegalStateException expected, no channel runtime data");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        try {
            this.portletAdaptor.isCacheValid(validity);
            fail("IllegalStateException expected, no channel runtime data");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        try {
            this.portletAdaptor.renderCharacters(pw);
            fail("IllegalStateException expected, no channel runtime data");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        try {
            this.portletAdaptor.getRuntimeProperties();
            fail("IllegalStateException expected, no channel runtime data");
        }
        catch (IllegalStateException ise) {
            //expected
        }
    }
    
    public void testBadPortalEventLifecycle() throws Exception {
        final PortalEvent ev = PortalEvent.MAXIMIZE;
        try {
            this.portletAdaptor.receiveEvent(ev);
            fail("IllegalStateException expected, no channel static data");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        
        
        //Have to init first
        this.testInitLifecycle();
        
        try {
            this.portletAdaptor.receiveEvent(ev);
            fail("IllegalStateException expected, no portal control structures");
        }
        catch (IllegalStateException ise) {
            //expected
        }
    }
    
    public void testPortalEventLifecycle() throws Exception {
        //Have to init first
        this.testInitLifecycle();
        
        final PortalControlStructures pcs = new PortalControlStructures(new MockHttpServletRequest(), null);
        this.portletAdaptor.setPortalControlStructures(pcs);

        final PortalEvent ev = PortalEvent.MAXIMIZE;
        
        EasyMock.reset(this.portletChannel);
        this.portletChannel.portalEvent(this.sd, pcs, ev);
        EasyMock.expectLastCall().once();
        EasyMock.replay(this.portletChannel);
        
        this.portletAdaptor.receiveEvent(ev);
        
        EasyMock.verify(this.portletChannel);
        
        
        try {
            this.portletAdaptor.receiveEvent(ev);
            fail("IllegalStateException expected, no portal control structures");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        
        
        //Try again to ensure only the pcs was nulled
        this.portletAdaptor.setPortalControlStructures(pcs);

        EasyMock.reset(this.portletChannel);
        this.portletChannel.portalEvent(this.sd, pcs, ev);
        EasyMock.expectLastCall().once();
        EasyMock.replay(this.portletChannel);
        
        this.portletAdaptor.receiveEvent(ev);
        
        EasyMock.verify(this.portletChannel);
    }
    
    public void testSessionDoneEventLifecycle() throws Exception {
        //Have to init first
        this.testInitLifecycle();
        
        final PortalControlStructures pcs = new PortalControlStructures(new MockHttpServletRequest(), null);
        this.portletAdaptor.setPortalControlStructures(pcs);

        final PortalEvent ev = PortalEvent.SESSION_DONE_EVENT;
        
        EasyMock.reset(this.portletChannel);
        this.portletChannel.portalEvent(this.sd, pcs, ev);
        EasyMock.expectLastCall().once();
        EasyMock.replay(this.portletChannel);
        
        this.portletAdaptor.receiveEvent(ev);
        
        EasyMock.verify(this.portletChannel);
        
        
        try {
            this.portletAdaptor.receiveEvent(ev);
            fail("IllegalStateException expected, no portal control structures");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        
        
        //Try again to ensure everything was nulled
        this.portletAdaptor.setPortalControlStructures(pcs);

        try {
            this.portletAdaptor.receiveEvent(ev);
            fail("IllegalStateException expected, no static data");
        }
        catch (IllegalStateException ise) {
            //expected
        }
    }
}
