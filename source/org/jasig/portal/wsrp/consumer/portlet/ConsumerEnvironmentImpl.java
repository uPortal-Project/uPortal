/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/


package org.jasig.portal.wsrp.consumer.portlet;

import org.apache.wsrp4j.consumer.ProducerRegistry;

/**
 * Class implements the consumer environment interface for the
 * proxy portlet consumer.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ConsumerEnvironmentImpl extends org.apache.wsrp4j.consumer.portlet.impl.ConsumerEnvironmentImpl {

    private static String CONSUMER_AGENT = "WSRP4J proxy portlet consumer v0.1";

    public ProducerRegistry getProducerRegistry() {
        if (super.getProducerRegistry() == null) {
            // Get producer registry
            ProducerRegistry producerRegistry = ProducerRegistryImpl.getInstance();
            setProducerRegistry(producerRegistry);
        }

        return super.getProducerRegistry();
    }
}
