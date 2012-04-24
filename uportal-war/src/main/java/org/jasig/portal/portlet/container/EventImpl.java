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

package org.jasig.portal.portlet.container;

import java.io.Serializable;

import javax.portlet.Event;
import javax.xml.namespace.QName;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class EventImpl implements Event {

    private final QName qname;
    private final Serializable value;
    
    public EventImpl(QName qname){
        this.qname = qname;
        this.value = null;
    }
    
    public EventImpl(QName qname, Serializable value){
        this.qname = qname;
        this.value = value;
    }

    @Override
    public QName getQName() {
        return qname;
    }

    @Override
    public Serializable getValue() {
        return value;
    }

    @Override
    public String getName() {
        return qname.getLocalPart();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.qname == null) ? 0 : this.qname.hashCode());
        result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Event)) {
            return false;
        }
        Event other = (Event) obj;
        if (this.qname == null) {
            if (other.getQName() != null)
                return false;
        }
        else if (!this.qname.equals(other.getQName()))
            return false;
        if (this.value == null) {
            if (other.getValue() != null)
                return false;
        }
        else if (!this.value.equals(other.getValue()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Event [" + this.qname + "]";
    }
}
