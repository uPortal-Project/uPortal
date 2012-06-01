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

package org.jasig.portal.portlets.account;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.LazyMap;
import org.jasig.portal.portletpublishing.xml.Preference;
import org.jasig.portal.portlets.StringListAttribute;
import org.jasig.portal.portlets.StringListAttributeFactory;

public class PersonForm implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Preference> accountEditAttributes;
    private long id = -1;
    private String username;
    private String password;
    private String confirmPassword;

    @SuppressWarnings("unchecked")
    private Map<String, StringListAttribute> attributes = LazyMap.decorate(
            new HashMap<String, StringListAttribute>(),
            new StringListAttributeFactory());

    public PersonForm(List<Preference> accountEditAttributes) {
        this.accountEditAttributes = accountEditAttributes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Provides the complete collection of the user's attributes.
     * 
     * @return
     */
    public Map<String, StringListAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, StringListAttribute> attributes) {
        this.attributes = attributes;
    }

    /**
     * Provides a collection of the user's attributes that are 'custom' in the 
     * sense that they are not listed in the <code>accountEditAttributes</code> 
     * bean in userContext.  This collection is a subset of those that would be 
     * returned by the {@link getAttributes} method.  This collection is 
     * <b>READ ONLY</b>.
     * 
     * @return The user's attributes that are not listed in <code>accountEditAttributes</code>
     */
    public Map<String, StringListAttribute> getCustomAttributes() {
        Map<String, StringListAttribute> rslt = new HashMap<String, StringListAttribute>(attributes);
        for (Preference p : accountEditAttributes) {
            rslt.remove(p.getName());
        }
        return Collections.unmodifiableMap(rslt);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

}
