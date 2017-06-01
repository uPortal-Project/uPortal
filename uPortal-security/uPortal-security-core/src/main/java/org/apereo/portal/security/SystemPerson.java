/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.security;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apereo.portal.EntityIdentifier;

public final class SystemPerson implements IPerson {
    private static final long serialVersionUID = 1L;

    public static final IPerson INSTANCE = new SystemPerson();
    private static final String USERNAME = "system";

    /* (non-Javadoc)
     * @see java.security.Principal#getName()
     */
    @Override
    public String getName() {
        return this.getUserName();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IBasicEntity#getEntityIdentifier()
     */
    @Override
    public EntityIdentifier getEntityIdentifier() {
        return new EntityIdentifier(this.getUserName(), IPerson.class);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.security.IPerson#setID(int)
     */
    @Override
    public void setID(int sID) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.security.IPerson#getID()
     */
    @Override
    public int getID() {
        return 1;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.security.IPerson#getUserName()
     */
    @Override
    public String getUserName() {
        return USERNAME;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.security.IPerson#setUserName(java.lang.String)
     */
    @Override
    public void setUserName(String userName) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.security.IPerson#setFullName(java.lang.String)
     */
    @Override
    public void setFullName(String sFullName) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.security.IPerson#getFullName()
     */
    @Override
    public String getFullName() {
        return this.getUserName();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.security.IPerson#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String key) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.security.IPerson#getAttributeValues(java.lang.String)
     */
    @Override
    public Object[] getAttributeValues(String key) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.security.IPerson#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.security.IPerson#setAttribute(java.lang.String, java.util.List)
     */
    @Override
    public void setAttribute(String key, List<Object> values) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.security.IPerson#setAttributes(java.util.Map)
     */
    @Override
    public void setAttributes(Map<String, List<Object>> attrs) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.security.IPerson#getAttributeMap()
     */
    @Override
    public Map<String, List<Object>> getAttributeMap() {
        return Collections.emptyMap();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.security.IPerson#setSecurityContext(org.apereo.portal.security.ISecurityContext)
     */
    @Override
    public void setSecurityContext(ISecurityContext securityContext) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.security.IPerson#getSecurityContext()
     */
    @Override
    public ISecurityContext getSecurityContext() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.security.IPerson#isGuest()
     */
    @Override
    public boolean isGuest() {
        return false;
    }
}
