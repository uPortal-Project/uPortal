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

import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.apereo.portal.IBasicEntity;

public interface IPerson extends Principal, IAdditionalDescriptor, IBasicEntity, Serializable {

    /** String used as a key for the eduPerson username attribute. */
    String USERNAME = "username";

    /** The default ID for person objects. */
    int UNDEFINED_ID = -1;

    String DISTINGUISHED_GROUP = IPerson.class.getName();

    /**
     * Sets the ID of the user
     *
     * @param sID
     */
    void setID(int sID);

    /**
     * Gets the ID of the user
     *
     * @return ID of the user
     */
    int getID();

    /** @return The userName for the user. */
    String getUserName();

    /** @param userName The userName to set for the user. */
    void setUserName(String userName);

    /**
     * Sets the full name of the user
     *
     * @param sFullName
     */
    void setFullName(String sFullName);

    /**
     * Gets the full name of the user
     *
     * @return full name of the user
     */
    String getFullName();

    /**
     * Gets an attribute associated with the user
     *
     * @param key
     * @return attribute associated with the user
     */
    Object getAttribute(String key);

    /**
     * Gets multiple values of an attribute associated with the user
     *
     * @param key
     * @return attributes associated with the user
     */
    Object[] getAttributeValues(String key);

    /**
     * Associates an attribute with the user
     *
     * @param key
     * @param value
     */
    void setAttribute(String key, Object value);

    /**
     * @param key
     * @param values
     */
    void setAttribute(String key, List<Object> values);

    /**
     * Associates attributes with the user
     *
     * @param attrs
     */
    void setAttributes(Map<String, List<Object>> attrs);

    Map<String, List<Object>> getAttributeMap();

    /**
     * Associates a security context object with the user
     *
     * @param securityContext
     */
    void setSecurityContext(ISecurityContext securityContext);

    /**
     * Gets the security context object associated with the user
     *
     * @return security context object associated with the user
     */
    ISecurityContext getSecurityContext();

    /**
     * Checks to see if this user is considered a guest
     *
     * @return true if user is considered a guest
     */
    boolean isGuest();
}
