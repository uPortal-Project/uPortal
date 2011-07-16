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

package org.jasig.portal.io.xml;

import java.util.Collections;
import java.util.Set;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.jasig.portal.xml.XmlUtilities;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import com.google.common.collect.ImmutableSet;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XsltDataUpgrader implements IDataUpgrader, InitializingBean {
    private Set<PortalDataKey> portalDataKeys;
    private Resource xslResource;
    private XmlUtilities xmlUtilities;
    
    private Templates upgradeTemplates;


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
    public void afterPropertiesSet() throws Exception {
        this.upgradeTemplates = this.xmlUtilities.getTemplates(xslResource);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IDataUpgrader#getSourceDataType()
     */
    @Override
    public Set<PortalDataKey> getSourceDataTypes() {
        return this.portalDataKeys;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IDataUpgrader#upgradeData(javax.xml.transform.Source, javax.xml.transform.Result)
     */
    @Override
    public boolean upgradeData(Source source, Result result) {
        final Transformer transformer;
        try {
            transformer = this.upgradeTemplates.newTransformer();
        }
        catch (TransformerConfigurationException e) {
            throw new RuntimeException("Failed to create new Transformer from the configured templates: " + this.xslResource, e);
        }
        
        try {
            transformer.transform(source, result);
        }
        catch (TransformerException e) {
            throw new RuntimeException("Failed to upgrade data using XSLT transformation: " + this.xslResource, e);
        }
        
        return true;
    }

}
