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

package org.jasig.portal.utils.cache;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CacheKey implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String source;
    private final Serializable[] key;
    private final int hashCode;

    public CacheKey(String source, Serializable key) {
        this.source = source;
        if (key instanceof Collection) {
            final Collection<Serializable> keyCollection = (Collection)key;
            this.key = keyCollection.toArray(new Serializable[keyCollection.size()]);
        }
        else {
            this.key = new Serializable[] { key };
        }
        this.hashCode = this.internalHashCode();
    }
    
    /**
     * Takes a list of serializable objects and stores them in an {@link ArrayList} to create a single serializable key object
     */
    public CacheKey(String source, Serializable... keyParts) {
        this.source = source;
        if (keyParts == null) {
            this.key = null;
        }
        else {
            this.key = keyParts.clone();
        }
        this.hashCode = this.internalHashCode();
    }
    
    public CacheKey(String source, Collection<? extends Serializable> keyParts) {
        this.source = source;
        if (keyParts == null) {
            this.key = null;
        }
        else {
            this.key = new ArrayList<Serializable>(keyParts);
        }
        this.hashCode = this.internalHashCode();
    }
    
    public Serializable getKey() {
        return this.key;
    }
    
    public String getSource() {
        return this.source;
    }
    
    private int internalHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.key == null) ? 0 : this.key.hashCode());
        result = prime * result + ((this.source == null) ? 0 : this.source.hashCode());
        return result;
    }
    
    @Override
    public int hashCode() {
        return this.hashCode;
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
        CacheKey other = (CacheKey) obj;
        if (this.source == null) {
            if (other.source != null) {
                return false;
            }
        }
        else if (!this.source.equals(other.source)) {
            return false;
        }
        if (this.key == null) {
            if (other.key != null) {
                return false;
            }
        }
        else if (!this.key.equals(other.key)) {
            return false;
        }
        return true;
    }
    private final static ThreadLocal<Integer> depth = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };
    
    @Override
    public String toString() {
        int d = depth.get();
        depth.set(d + 1);
        final String indent = StringUtils.leftPad("", depth.get(), '\t');
        final String s = indent + "CacheKey [" +  this.source + "\n\t" + indent + this.key + "\n" + indent  + "]";
        depth.set(d);
        return s;
    }
}
