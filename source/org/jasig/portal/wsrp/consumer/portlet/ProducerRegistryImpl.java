/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/


package org.jasig.portal.wsrp.consumer.portlet;

import org.apache.wsrp4j.consumer.ProducerRegistry;
import org.apache.wsrp4j.consumer.driver.GenericProducerRegistryImpl;

/**
 * Custom ProducerRegistry implementation for uPortal.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ProducerRegistryImpl extends GenericProducerRegistryImpl {

    private static ProducerRegistry instance = null;
    
    private ProducerRegistryImpl() {
        super();
    }

    /**
     * Get an instance of the singleton producer registry object
     **/
    public static ProducerRegistry getInstance() {
        if (instance == null) {
            instance = new ProducerRegistryImpl();
        }

        return instance;
    }

}
