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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base IPortalDataType implementation that should simplify most implementations
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractPortalDataType implements IPortalDataType {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final QName defaultQName;
    
    public AbstractPortalDataType(QName defaultQName) {
        Validate.notNull(defaultQName);
        this.defaultQName = defaultQName;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IPortalDataType#getTypeId()
     */
    @Override
    public String getTypeId() {
        return this.defaultQName.getLocalPart();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IPortalDataType#getTitle()
     */
    @Override
    public String getTitleCode() {
        return this.defaultQName.getLocalPart();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IPortalDataType#getDescription()
     */
    @Override
    public String getDescriptionCode() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IPortalDataType#postProcessPortalDataKey(org.springframework.core.io.Resource, org.jasig.portal.io.xml.PortalDataKey, javax.xml.stream.XMLEventReader)
     */
    @Override
    public Set<PortalDataKey> postProcessPortalDataKey(String systemId, PortalDataKey portalDataKey, XMLEventReader reader) {
        final PortalDataKey singlePortalDataKey = this.postProcessSinglePortalDataKey(systemId, portalDataKey, reader);
        return Collections.singleton(singlePortalDataKey);
    }
    
    protected PortalDataKey postProcessSinglePortalDataKey(String systemId, PortalDataKey portalDataKey, XMLEventReader reader) {
        return portalDataKey;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.getTypeId() == null) ? 0 : this.getTypeId().hashCode());
        result = prime * result + ((this.getDataKeyImportOrder() == null) ? 0 : this.getDataKeyImportOrder().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IPortalDataType other = (IPortalDataType) obj;
        if (this.getTypeId() == null) {
            if (other.getTypeId() != null)
                return false;
        }
        else if (!this.getTypeId().equals(other.getTypeId()))
            return false;
        if (this.getDataKeyImportOrder() == null) {
            if (other.getDataKeyImportOrder() != null)
                return false;
        }
        else if (!this.getDataKeyImportOrder().equals(other.getDataKeyImportOrder()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [QName=" + this.defaultQName + ", supportedDataKeys=" + this.getDataKeyImportOrder() + "]";
    }
}
