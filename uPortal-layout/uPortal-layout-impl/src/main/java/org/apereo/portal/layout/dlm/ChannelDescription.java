/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.layout.dlm;

import org.apereo.portal.PortalException;
import org.apereo.portal.layout.node.UserLayoutChannelDescription;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** DLM specific Channel description to protect DLM artifacts of channels. */
public class ChannelDescription extends UserLayoutChannelDescription {
    private String plfId = null;
    private String origin = null;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        if (origin != null && origin.equals("")) origin = null;
        this.origin = origin;
    }

    public String getPlfId() {
        return plfId;
    }

    public void setPlfId(String plfId) {
        if (plfId != null && plfId.equals("")) plfId = null;
        this.plfId = plfId;
    }

    /** Overridden constructor of super class. */
    public ChannelDescription() {
        super();
    }
    /**
     * Overridden constructor of super class.
     *
     * @param xmlNode the Element to be represented
     * @throws PortalException
     */
    public ChannelDescription(Element xmlNode) throws PortalException {
        super(xmlNode);
        // dlm-specific attributes
        this.setPlfId(xmlNode.getAttributeNS(Constants.NS_URI, Constants.LCL_PLF_ID));
        this.setOrigin(xmlNode.getAttributeNS(Constants.NS_URI, Constants.LCL_ORIGIN));
    }

    @Override
    public Element getXML(Document root) {
        Element node = super.getXML(root);

        // now add in DLM specific attributes if found
        if (getPlfId() != null)
            node.setAttributeNS(Constants.NS_URI, Constants.ATT_PLF_ID, getPlfId());

        if (getOrigin() != null)
            node.setAttributeNS(Constants.NS_URI, Constants.ATT_ORIGIN, getOrigin());
        return node;
    }
}
