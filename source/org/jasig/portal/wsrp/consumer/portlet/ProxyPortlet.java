/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/


package org.jasig.portal.wsrp.consumer.portlet;

import java.util.Map;

import oasis.names.tc.wsrp.v1.types.RegistrationData;

import org.apache.wsrp4j.consumer.PortletKey;
import org.apache.wsrp4j.consumer.Producer;
import org.apache.wsrp4j.consumer.ProducerRegistry;
import org.apache.wsrp4j.consumer.driver.PortletKeyImpl;
import org.apache.wsrp4j.consumer.driver.ProducerImpl;
import org.apache.wsrp4j.consumer.util.ConsumerConstants;
import org.apache.wsrp4j.exception.WSRPException;

/**
 * Custom version of WSRP4J Consumer Proxy Portlet that
 * reads Portlet preferences in order to determine the
 * producer configuration.  The ID of a Producer will be
 * derived from the Portlet preferences themselves.
 * This is different from the WSRP4J ProxyPortlet which
 * relies on a pre-configured Producer registry.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ProxyPortlet extends org.apache.wsrp4j.consumer.portlet.impl.ProxyPortlet {

    private String markupUrl;
    private String serviceDescUrl;
    private String regUrl;
    private String mgmtUrl;

    /**
     * Construct producer ID by concatinating the 4 producer URLs
     * and Consumer registration data.
     */
    protected PortletKey getPortletKey(Map preferences) {
        PortletKey portletKey = null;
        String portletHandle = (String)preferences.get(ConsumerConstants.WSRP_PORTLET_HANDLE);        
        markupUrl = (String)preferences.get(ConsumerConstants.WSRP_MARKUP_URL);
        serviceDescUrl = (String)preferences.get(ConsumerConstants.WSRP_SERVICE_DESC_URL);
        regUrl = (String)preferences.get(ConsumerConstants.WSRP_REGISTRATION_URL);
        mgmtUrl = (String)preferences.get(ConsumerConstants.WSRP_PORTLET_MGMT_URL);
        String producerId = markupUrl + serviceDescUrl + regUrl + mgmtUrl;
        portletKey = new PortletKeyImpl(portletHandle, producerId);
        //System.out.println("portletKey=" + portletKey);
        return portletKey;
    }

    protected Producer getProducer(String producerID) throws WSRPException {
        ProducerRegistry producerRegistry = getConsumerEnvironment().getProducerRegistry();
        Producer producer = producerRegistry.getProducer(producerID);
        if (producer == null) {
            // Create producer and register it
            RegistrationData regData = new RegistrationData();
            regData.setConsumerName("uPortal WSRP Consumer");
            regData.setConsumerAgent("uPortal WSRP Consumer");
            producer = new ProducerImpl(producerID, markupUrl, serviceDescUrl, regUrl, mgmtUrl, regData);
            producerRegistry.addProducer(producer);
        }
        //System.out.println("returning producer=" + producer.toString());
        return producer;

    }

}
