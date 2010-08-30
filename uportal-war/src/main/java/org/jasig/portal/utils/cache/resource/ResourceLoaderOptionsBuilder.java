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

package org.jasig.portal.utils.cache.resource;

/**
 * Builder for {@link ResourceLoaderOptions}. All options are default to null.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ResourceLoaderOptionsBuilder implements ResourceLoaderOptions {
    private Boolean digestInput = null;
    private String digestAlgorithm = null;
    private Long checkInterval = null;

    @Override
    public Boolean isDigestInput() {
        return this.digestInput;
    }

    @Override
    public String getDigestAlgorithm() {
        return this.digestAlgorithm;
    }

    @Override
    public Long getCheckInterval() {
        return this.checkInterval;
    }
    
    //***** Setters to make it a valid JavaBean *****//
    
    public void setDigestInput(Boolean digestInput) {
        this.digestInput = digestInput;
    }
    
    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    public void setCheckInterval(Long checkInterval) {
        this.checkInterval = checkInterval;
    }
    
    //***** Setters that return this for utility *****//
    public ResourceLoaderOptionsBuilder digestInput(Boolean digestInput) {
        this.digestInput = digestInput;
        return this;
    }
    
    public ResourceLoaderOptionsBuilder digestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
        return this;
    }

    public ResourceLoaderOptionsBuilder checkInterval(Long checkInterval) {
        this.checkInterval = checkInterval;
        return this;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.checkInterval == null) ? 0 : this.checkInterval.hashCode());
        result = prime * result + ((this.digestAlgorithm == null) ? 0 : this.digestAlgorithm.hashCode());
        result = prime * result + ((this.digestInput == null) ? 0 : this.digestInput.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ResourceLoaderOptionsBuilder other = (ResourceLoaderOptionsBuilder) obj;
        if (this.checkInterval == null) {
            if (other.checkInterval != null) {
                return false;
            }
        }
        else if (!this.checkInterval.equals(other.checkInterval)) {
            return false;
        }
        if (this.digestAlgorithm == null) {
            if (other.digestAlgorithm != null) {
                return false;
            }
        }
        else if (!this.digestAlgorithm.equals(other.digestAlgorithm)) {
            return false;
        }
        if (this.digestInput == null) {
            if (other.digestInput != null) {
                return false;
            }
        }
        else if (!this.digestInput.equals(other.digestInput)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ResourceLoaderOptionsBuilder [digestInput=" + this.digestInput + ", digestAlgorithm="
                + this.digestAlgorithm + ", checkInterval=" + this.checkInterval + "]";
    }
}
