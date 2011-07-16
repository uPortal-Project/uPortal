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

import org.apache.commons.lang.Validate;

/**
 * Simple {@link IPortalData} impl that just uses string fields
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class SimpleStringPortalData implements IPortalData {
    private final String dataId;
    private final String dataTitle;
    private final String dataDescription;
    
    public SimpleStringPortalData(String dataId, String dataTitle, String dataDescription) {
        Validate.notNull(dataId);
        this.dataId = dataId;
        this.dataTitle = dataTitle;
        this.dataDescription = dataDescription;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IPortalData#getDataId()
     */
    @Override
    public String getDataId() {
        return this.dataId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IPortalData#getDataTitle()
     */
    @Override
    public String getDataTitle() {
        return this.dataTitle;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IPortalData#getDataDescription()
     */
    @Override
    public String getDataDescription() {
        return this.dataDescription;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.dataDescription == null) ? 0 : this.dataDescription.hashCode());
        result = prime * result + ((this.dataId == null) ? 0 : this.dataId.hashCode());
        result = prime * result + ((this.dataTitle == null) ? 0 : this.dataTitle.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!IPortalData.class.isAssignableFrom(obj.getClass()))
            return false;
        IPortalData other = (IPortalData) obj;
        if (this.dataDescription == null) {
            if (other.getDataDescription() != null)
                return false;
        }
        else if (!this.dataDescription.equals(other.getDataDescription()))
            return false;
        if (this.dataId == null) {
            if (other.getDataId() != null)
                return false;
        }
        else if (!this.dataId.equals(other.getDataId()))
            return false;
        if (this.dataTitle == null) {
            if (other.getDataTitle() != null)
                return false;
        }
        else if (!this.dataTitle.equals(other.getDataTitle()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SimpleStringPortalData [dataId=" + this.dataId + ", dataTitle=" + this.dataTitle + ", dataDescription=" + this.dataDescription + "]";
    }
}
