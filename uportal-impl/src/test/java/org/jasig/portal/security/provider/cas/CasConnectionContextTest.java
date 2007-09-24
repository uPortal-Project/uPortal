/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider.cas;

import java.util.ArrayList;
import java.util.List;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.security.provider.BrokenSecurityContext;
import org.jasig.portal.security.provider.PersonImpl;

import junit.framework.TestCase;

/**
 * JUnit testcase exercising CasConnectionContext.
 */
public class CasConnectionContextTest 
    extends TestCase {

    /**
     * Test that when no ICasSecurityContext is present, 
     * the CasConnectionContext does not modify descriptors.
     */
    public void testNoCasSecurityContextPresent() {
        
        PersonImpl person = new PersonImpl();
        // not a CAS security context
        BrokenSecurityContext brokenContext = new BrokenSecurityContext();
        
        person.setSecurityContext(brokenContext);
        
        ChannelStaticData staticData = new ChannelStaticData();
        staticData.setPerson(person);
        
        CasConnectionContext connectionContext = new CasConnectionContext();
        connectionContext.init(staticData);

        String service = "http://www.someschool.edu/someservice"; 
        
        // in absence of CAS security context, return descriptor unchanged
        assertEquals(service, connectionContext.getDescriptor(service));
        
        ChannelRuntimeData runtimeData = new ChannelRuntimeData();
        runtimeData.setHttpRequestMethod("GET");
        
        
        String descriptorWithRuntimeData = 
            connectionContext.getDescriptor(service, runtimeData);
        
        // in absence of CAS security context, return descriptor unchanged.
        assertEquals(service, descriptorWithRuntimeData);
    
        runtimeData.setHttpRequestMethod("POST");
        
        // when method is POST, return descriptor unchanged
        descriptorWithRuntimeData = 
            connectionContext.getDescriptor(service, runtimeData);
        
        assertEquals(service, descriptorWithRuntimeData);
        
    }
    
    /**
     * When an ICasSecurityContext is present but unauthenticated, 
     * CasConnectionContext will ignore it.
     */
    public void testUnauthenticatedCasSecurityContext() {
        PersonImpl person = new PersonImpl();
        // a CAS security context
        CasSecurityContextMock mockCasContext = new CasSecurityContextMock();
        mockCasContext.setAuthenticated(false);
        
        person.setSecurityContext(mockCasContext);
        
        ChannelStaticData staticData = new ChannelStaticData();
        staticData.setPerson(person);
        
        CasConnectionContext connectionContext = new CasConnectionContext();
        connectionContext.init(staticData);

        String service = "http://www.someschool.edu/someservice"; 
        
        // in absence of authenticated CAS security context, return descriptor unchanged
        assertEquals(service, connectionContext.getDescriptor(service));
        
        ChannelRuntimeData runtimeData = new ChannelRuntimeData();
        runtimeData.setHttpRequestMethod("GET");
        
        
        String descriptorWithRuntimeData = 
            connectionContext.getDescriptor(service, runtimeData);
        
        // in absence of authenticated CAS security context, return descriptor unchanged.
        assertEquals(service, descriptorWithRuntimeData);
    
        runtimeData.setHttpRequestMethod("POST");
        
        // when method is POST, return descriptor unchanged
        descriptorWithRuntimeData = 
            connectionContext.getDescriptor(service, runtimeData);
        
        assertEquals(service, descriptorWithRuntimeData);
    }
    
    /**
     * When an ICasSecurityContext is present, CasConnectionContext will
     * use the ICasSecurityContext as a source of proxy tickets 
     */
    public void testCasSecurityContext() {
        PersonImpl person = new PersonImpl();
        // a CAS security context
        CasSecurityContextMock mockCasContext = new CasSecurityContextMock();
        mockCasContext.setAuthenticated(true);
        
        List proxyTickets = new ArrayList();
        proxyTickets.add("proxyTicket1");
        proxyTickets.add("proxyTicket2");
        proxyTickets.add("proxyTicket3");
        
        mockCasContext.setServiceTokensToVend(proxyTickets);
        
        person.setSecurityContext(mockCasContext);
        
        ChannelStaticData staticData = new ChannelStaticData();
        staticData.setPerson(person);
        
        CasConnectionContext connectionContext = new CasConnectionContext();
        connectionContext.init(staticData);

        String serviceOne = "http://www.someschool.edu/someservice"; 
        String expectedResponse = "http://www.someschool.edu/someservice?ticket=proxyTicket1";
        
        // the connection context should append the ticket from the security context
        assertEquals(expectedResponse, connectionContext.getDescriptor(serviceOne));
        
//      the descriptor presented to the cas security context was the service.
        assertEquals(serviceOne, (String) mockCasContext.getServiceTokenTargets().get(0));
        
        ChannelRuntimeData runtimeData = new ChannelRuntimeData();
        runtimeData.setHttpRequestMethod("GET");
        
        
        String serviceTwo = "https://www.ja-sig.org/somepath?queryParamsPresent=true";
        String expectedResponseTwo = "https://www.ja-sig.org/somepath?queryParamsPresent=true&ticket=proxyTicket2";
        String descriptorWithRuntimeData = 
            connectionContext.getDescriptor(serviceTwo, runtimeData);
        
        // the connection context should properly append the ticket parameter with an ampersand
        assertEquals(expectedResponseTwo, descriptorWithRuntimeData);
    
    }
    
    /**
     * When the Http request method is post, 
     * CasConnectionContext returns the descriptor unaltered.
     */
    public void testPostCasSecurityContext() {
        PersonImpl person = new PersonImpl();
        // a CAS security context
        CasSecurityContextMock mockCasContext = new CasSecurityContextMock();
        mockCasContext.setAuthenticated(true);
        
        List proxyTickets = new ArrayList();
        proxyTickets.add("proxyTicket1");
        proxyTickets.add("proxyTicket2");
        proxyTickets.add("proxyTicket3");
        
        mockCasContext.setServiceTokensToVend(proxyTickets);
        
        person.setSecurityContext(mockCasContext);
        
        ChannelStaticData staticData = new ChannelStaticData();
        staticData.setPerson(person);
        
        CasConnectionContext connectionContext = new CasConnectionContext();
        connectionContext.init(staticData);
        
        String serviceOne = "http://www.uportal.org/someservice";
        
        ChannelRuntimeData runtimeData = new ChannelRuntimeData();
        
        runtimeData.setHttpRequestMethod("POST");
        
        // when method is POST, return descriptor unchanged
        String descriptorWithRuntimeData = 
            connectionContext.getDescriptor(serviceOne, runtimeData);
        
        assertEquals(serviceOne, descriptorWithRuntimeData);
    }
    
    /**
     * The CasConnectionContext behaves slightly differently when 
     * a particular ChannelStaticData parameter "upc_cas_service_uri" is
     * present.  When present,
     * this static parameter identifies the service, rather than 
     * the descriptors passed at runtime, for the getDescriptor(String descriptor)
     * method (but not for the method that takes the ChannelRuntimeData
     * as an argument).  This affects what service the
     * CasConnectionContext passes to the ICasSecurityContext for acquisition
     * of a proxy ticket, but it *does not* change the descriptor the 
     * CasConnectionContext uses to compose the modified descriptor it returns.
     */
    public void testStaticData() {
        PersonImpl person = new PersonImpl();
        // a CAS security context
        CasSecurityContextMock mockCasContext = new CasSecurityContextMock();
        mockCasContext.setAuthenticated(true);
        
        List proxyTickets = new ArrayList();
        proxyTickets.add("proxyTicket1");
        proxyTickets.add("proxyTicket2");
        proxyTickets.add("proxyTicket3");
        proxyTickets.add("proxyTicket4");
        
        mockCasContext.setServiceTokensToVend(proxyTickets);
        
        person.setSecurityContext(mockCasContext);
        
        ChannelStaticData staticData = new ChannelStaticData();
        staticData.setPerson(person);
        String staticallyDefinedUri = "https://statically.defined.uri.com/";
        staticData.setParameter("upc_cas_service_uri", staticallyDefinedUri);
        
        CasConnectionContext connectionContext = new CasConnectionContext();
        connectionContext.init(staticData);

        String serviceOne = "http://www.someschool.edu/someservice"; 
        String expectedResponse = "http://www.someschool.edu/someservice?ticket=proxyTicket1";
        
        // the connection context should append the ticket from the security context
        assertEquals(expectedResponse, connectionContext.getDescriptor(serviceOne));
        
        // the descriptor presented to the cas security context was the statically defined URI.
        assertEquals(staticallyDefinedUri, (String) mockCasContext.getServiceTokenTargets().get(0));
        
        ChannelRuntimeData runtimeData = new ChannelRuntimeData();
        runtimeData.setHttpRequestMethod("GET");
        
        
        String serviceTwo = "https://www.ja-sig.org/somepath?queryParamsPresent=true";
        String expectedResponseTwo = "https://www.ja-sig.org/somepath?queryParamsPresent=true&ticket=proxyTicket2";
        String descriptorWithRuntimeData = 
            connectionContext.getDescriptor(serviceTwo, runtimeData);
        
        // the connection context should properly append the ticket parameter with an ampersand
        assertEquals(expectedResponseTwo, descriptorWithRuntimeData);
    
        // the descriptor presented to the cas security context was the uri presented on the
        // method call -- the method that takes the ChannelRuntimeData as an argument
        // does not honor the upc_cas_service_url ChannelStaticData parameter.
        assertEquals(serviceTwo, (String) mockCasContext.getServiceTokenTargets().get(1));
        
        String expectedResponseThree = "proxyTicket3";
        
        // even if the runtime descriptor is null, responds with the channel static data configured
        // descriptor
        assertEquals(expectedResponseThree, connectionContext.getDescriptor(null, runtimeData));
        
        // the descriptor presented to the cas security context was the null passed in
        assertEquals(null, (String) mockCasContext.getServiceTokenTargets().get(2));
        
    }
    
    /**
     * CasConnectionContext offers a getPostData() method used which reads
     * a specific parameter from ChannelRuntimeData (cw_xml) and returns the
     * parameter name=value pair for presenting a proxy ticket to that URL.  When
     * that parameter is not present, it reads a parameter from ChannelStaticData.
     * When neither are present, it attempts to acquire a proxy ticket targetted
     * at the null service.
     */
    public void testGetPostData() {
        
        PersonImpl person = new PersonImpl();
        // a CAS security context
        CasSecurityContextMock mockCasContext = new CasSecurityContextMock();
        mockCasContext.setAuthenticated(true);
        
        List proxyTickets = new ArrayList();
        proxyTickets.add("proxyTicket1");
        proxyTickets.add("proxyTicket2");
        proxyTickets.add("proxyTicket3");
        proxyTickets.add("proxyTicket4");
        
        mockCasContext.setServiceTokensToVend(proxyTickets);
        
        person.setSecurityContext(mockCasContext);
        
        ChannelStaticData staticData = new ChannelStaticData();
        staticData.setPerson(person);
        
        CasConnectionContext connectionContext = new CasConnectionContext();
        connectionContext.init(staticData);

        ChannelRuntimeData withoutParameter = new ChannelRuntimeData();
        
        // when the "cw_xml" parameter is neither present in ChannelRuntimeData nor
        // in ChannelStaticData, the CasConnectionContext acquires a ticket for the
        // null service
        assertEquals("ticket=proxyTicket1", connectionContext.getPostData(withoutParameter));
        assertEquals(null, mockCasContext.getServiceTokenTargets().get(0));
        
        
        ChannelRuntimeData withParameter = new ChannelRuntimeData();
        String runtimeService = "http://baseactionurl.blogspot.com/2005/12/ja-sig-facebook.html";
        withParameter.setParameter("cw_xml", runtimeService);
        
        // read the cw_xml parameter from ChannelRuntimeData
        assertEquals("ticket=proxyTicket2", connectionContext.getPostData(withParameter));
        assertEquals(runtimeService, mockCasContext.getServiceTokenTargets().get(1));
        
        // prefer the parameter from ChannelRuntimeData rather than ChannelStaticData
        ChannelStaticData staticDataWithUri = new ChannelStaticData();
        staticDataWithUri.setPerson(person);
        String staticService = "http://www.ja-sig.org/wiki/";
        staticDataWithUri.setParameter("cw_xml", staticService);
        
        connectionContext.init(staticDataWithUri);
        
        assertEquals("ticket=proxyTicket3", connectionContext.getPostData(withParameter));
        assertEquals(runtimeService, mockCasContext.getServiceTokenTargets().get(2));
        
        // fall back on the parameter in ChannelStaticData when not present in runtime data
        assertEquals("ticket=proxyTicket4", connectionContext.getPostData(withoutParameter));
        assertEquals(staticService, mockCasContext.getServiceTokenTargets().get(3));
    }
    
}
