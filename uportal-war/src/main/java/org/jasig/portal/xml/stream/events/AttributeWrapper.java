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

package org.jasig.portal.xml.stream.events;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AttributeWrapper extends XMLEventWrapper implements Attribute {
    private final Attribute attribute;
    
    public AttributeWrapper(Attribute attribute) {
        super(attribute);
        this.attribute = attribute;
    }

    @Override
    public String getDTDType() {
        return this.attribute.getDTDType();
    }

    @Override
    public QName getName() {
        return this.attribute.getName();
    }

    @Override
    public String getValue() {
        return this.attribute.getValue();
    }

    @Override
    public boolean isSpecified() {
        return this.attribute.isSpecified();
    }
}
