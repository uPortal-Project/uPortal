/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
        System.out.println("portletKey=" + portletKey);
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
        System.out.println("returning producer=" + producer.toString());
        return producer;

    }

}
