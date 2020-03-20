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
package org.apereo.portal.io.xml;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.Set;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apereo.portal.xml.XmlUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

/** */
public class XsltDataUpgrader implements IDataUpgrader {
    private Set<PortalDataKey> portalDataKeys;
    private Resource xslResource;
    private XmlUtilities xmlUtilities;

    // volatile because it's initialized in a synchronized method
    private volatile Templates upgradeTemplates;

    public void setPortalDataKey(PortalDataKey portalDataKey) {
        this.portalDataKeys = Collections.singleton(portalDataKey);
    }

    public void setPortalDataKeys(Set<PortalDataKey> portalDataKeys) {
        this.portalDataKeys = ImmutableSet.copyOf(portalDataKeys);
    }

    public void setXslResource(Resource xslResource) {
        this.xslResource = xslResource;
    }

    @Autowired
    public void setXmlUtilities(XmlUtilities xmlUtilities) {
        this.xmlUtilities = xmlUtilities;
    }

    @Override
    public Set<PortalDataKey> getSourceDataTypes() {
        return portalDataKeys;
    }

    @Override
    public boolean upgradeData(Source source, Result result) {
        if (upgradeTemplates == null) {
            initializeUpgradeTemplates();
        }

        final Transformer transformer;
        try {
            transformer = upgradeTemplates.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(
                    "Failed to create new Transformer from the configured templates: "
                            + xslResource,
                    e);
        }

        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new RuntimeException(
                    "Failed to upgrade data using XSLT transformation: " + xslResource, e);
        }

        return true;
    }

    private synchronized void initializeUpgradeTemplates() {
        /*
         * Lazy-initialize the upgradeTemplates because of a potential race condition in the Spring
         * application context.
         */
        if (upgradeTemplates == null) {
            try {
                upgradeTemplates = xmlUtilities.getTemplates(xslResource);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize upgrade templates", e);
            }
        }
    }
}
