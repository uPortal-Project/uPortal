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

package org.jasig.portal.layout.dao.jpa;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jasig.portal.layout.om.IStylesheetParameterDescriptor;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(
        name = "UP_SS_PARAM_DESC"
    )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class StylesheetParameterDescriptorImpl extends AbstractStylesheetDataImpl implements IStylesheetParameterDescriptor {
    //Required by hibernate for reflective creation
    @SuppressWarnings("unused")
    private StylesheetParameterDescriptorImpl() {
        super();
    }

    public StylesheetParameterDescriptorImpl(String name, Scope scope) {
        super(name, scope);
    }
}
