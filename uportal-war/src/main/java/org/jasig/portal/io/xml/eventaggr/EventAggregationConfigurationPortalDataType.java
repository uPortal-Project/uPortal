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

/**
 * 
 */
package org.jasig.portal.io.xml.eventaggr;

import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import org.jasig.portal.io.xml.AbstractPortalDataType;
import org.jasig.portal.io.xml.PortalDataKey;

/**
 * @author Nicholas Blair
 * @version $Id$
 */
public class EventAggregationConfigurationPortalDataType extends AbstractPortalDataType {
	public static final QName EVENT_AGGREGATION_QNAME = new QName(
	        "https://source.jasig.org/schemas/uportal/io/event-aggregation", 
	        "event-aggregation");
	
	public static final PortalDataKey IMPORT_40_DATA_KEY = new PortalDataKey(
            EVENT_AGGREGATION_QNAME, 
            null,
            "4.0");
    
    private static final List<PortalDataKey> PORTAL_DATA_KEYS = Arrays.asList(IMPORT_40_DATA_KEY);
    
    public EventAggregationConfigurationPortalDataType() {
        super(EVENT_AGGREGATION_QNAME);
    }
    
    @Override
    public List<PortalDataKey> getDataKeyImportOrder() {
        return PORTAL_DATA_KEYS;
    }
    
	/* (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IPortalDataType#getTitle()
	 */
	@Override
	public String getTitleCode() {
		return "Event Aggregation Configuration";
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IPortalDataType#getDescription()
	 */
	@Override
	public String getDescriptionCode() {
		return "Configuration for event aggregators";
	}

}
