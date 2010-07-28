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

    private QName _qname;
    private Serializable _value;
    
    public EventImpl(QName qname){
        _qname = qname;
    }
    
    public EventImpl(QName qname, Serializable value){
        this(qname);
        _value = value;
    }

    public QName getQName() {
        return _qname;
    }

    public Serializable getValue() {
        return _value;
    }

    public String getName() {
        return _qname.getLocalPart();
    }
}
