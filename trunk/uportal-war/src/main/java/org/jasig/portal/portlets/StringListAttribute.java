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

package org.jasig.portal.portlets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringListAttribute implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<String> value = new ArrayList<String>();
    
    public StringListAttribute() {
    }
    
    public StringListAttribute(List<String> value) {
        this.value = value;
    }

    public StringListAttribute(String[] value) {
    	List<String> values = new ArrayList<String>();
        values.addAll(Arrays.<String>asList(value));
        this.value = values;
    }

    /**
     * @return the value
     */
    public List<String> getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(List<String> value) {
        this.value = value;
    }

    public void setValue(String[] value) {
    	List<String> values = new ArrayList<String>();
        values.addAll(Arrays.<String>asList(value));
        this.value = values;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object anObject) {
        return this.value == anObject || (this.value != null && this.value.equals(anObject));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.value != null ? this.value.hashCode() : 0;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}