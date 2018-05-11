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
package org.apereo.portal.layout.dlm;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletDefinitionParameter;
import org.apereo.portal.portlet.om.IPortletDescriptorKey;
import org.apereo.portal.portlet.om.IPortletLifecycleEntry;
import org.apereo.portal.portlet.om.IPortletPreference;
import org.apereo.portal.portlet.om.IPortletType;
import org.apereo.portal.portlet.om.PortletLifecycleState;
import org.apereo.portal.security.IPerson;

/**
 * {@link IPortletDefinition} implementation representing a missing portlet, meaning a portlet that
 * could not be found, or a portlet that is not approved.
 */
public class MissingPortletDefinition implements IPortletDefinition {

    private static final String FNAME = "DLMStaticMissingChannel";
    /* package-private */ static final String CHANNEL_ID = "-1";

    public static final IPortletDefinition INSTANCE = new MissingPortletDefinition();

    private MissingPortletDefinition() {
        // prevent instantiation outside of this class
    }

    @Override
    public String getName() {
        return "Missing channel";
    }

    @Override
    public String getName(String locale) {
        return "Missing channel";
    }

    @Override
    public int getTimeout() {
        return 20000;
    }

    @Override
    public String getTitle() {
        return "Missing channel";
    }

    @Override
    public String getTitle(String locale) {
        return "Missing channel";
    }

    @Override
    public String getAlternativeMaximizedLink() {
        return null;
    }

    @Override
    public String getTarget() {
        return null;
    }

    @Override
    public String getFName() {
        return FNAME;
    }

    @Override
    public String getDataId() {
        return null;
    }

    @Override
    public String getDataTitle() {
        return this.getName();
    }

    @Override
    public String getDataDescription() {
        return this.getDescription();
    }

    @Override
    public Integer getActionTimeout() {
        return null;
    }

    @Override
    public Integer getEventTimeout() {
        return null;
    }

    @Override
    public Integer getRenderTimeout() {
        return null;
    }

    @Override
    public Integer getResourceTimeout() {
        return null;
    }

    @Override
    public void setActionTimeout(Integer actionTimeout) {}

    @Override
    public void setEventTimeout(Integer eventTimeout) {}

    @Override
    public void setRenderTimeout(Integer renderTimeout) {}

    @Override
    public void setResourceTimeout(Integer resourceTimeout) {}

    @Override
    public Double getRating() {
        return null;
    }

    @Override
    public void setRating(Double rating) {}

    @Override
    public Long getUsersRated() {
        return null;
    }

    @Override
    public void setUsersRated(Long usersRated) {}

    @Override
    public void addLocalizedDescription(String locale, String chanDesc) {}

    @Override
    public void addLocalizedName(String locale, String chanName) {}

    @Override
    public void addLocalizedTitle(String locale, String chanTitle) {}

    @Override
    public void addParameter(IPortletDefinitionParameter parameter) {}

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getDescription(String locale) {
        return null;
    }

    @Override
    public EntityIdentifier getEntityIdentifier() {
        return null;
    }

    @Override
    public IPortletDefinitionParameter getParameter(String key) {
        return null;
    }

    @Override
    public Set<IPortletDefinitionParameter> getParameters() {
        return Collections.emptySet();
    }

    @Override
    public Map<String, IPortletDefinitionParameter> getParametersAsUnmodifiableMap() {
        return Collections.emptyMap();
    }

    @Override
    public void removeParameter(IPortletDefinitionParameter parameter) {}

    @Override
    public void removeParameter(String name) {}

    @Override
    public void setDescription(String descr) {}

    @Override
    public void setFName(String fname) {}

    @Override
    public void setName(String name) {}

    @Override
    public void setParameters(Set<IPortletDefinitionParameter> parameters) {}

    @Override
    public void setTimeout(int timeout) {}

    @Override
    public void setTitle(String title) {}

    @Override
    public IPortletType getType() {
        return new MissingPortletType();
    }

    public void setType(IPortletType channelType) {}

    @Override
    public PortletLifecycleState getLifecycleState() {
        return null;
    }

    @Override
    public void updateLifecycleState(PortletLifecycleState lifecycleState, IPerson user) {}

    @Override
    public void updateLifecycleState(
            PortletLifecycleState lifecycleState, IPerson user, Date timestamp) {}

    @Override
    public List<IPortletLifecycleEntry> getLifecycle() {
        return null;
    }

    @Override
    public void clearLifecycle() {}

    @Override
    public IPortletDefinitionId getPortletDefinitionId() {
        return new MissingPortletDefinitionId();
    }

    @Override
    public List<IPortletPreference> getPortletPreferences() {
        return Collections.emptyList();
    }

    @Override
    public void addParameter(String name, String value) {}

    @Override
    public boolean setPortletPreferences(List<IPortletPreference> portletPreferences) {
        return false;
    }

    @Override
    public IPortletDescriptorKey getPortletDescriptorKey() {
        return null;
    }

    @Override
    public String toString() {
        return "MissingPortletDefinition [fname=" + FNAME + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + FNAME.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        // there is only one instance, so this should suffice
        return this == obj;
    }

    private static final class MissingPortletDefinitionId implements IPortletDefinitionId {
        private static final long serialVersionUID = 1L;
        private final long id = -1;
        private final String strId = Long.toString(id);

        @Override
        public String getStringId() {
            return strId;
        }

        @Override
        public long getLongId() {
            return id;
        }
    }

    private static final class MissingPortletType implements IPortletType {
        @Override
        public int getId() {
            return -1;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String getCpdUri() {
            return null;
        }

        @Override
        public void setDescription(String descr) {}

        @Override
        public void setCpdUri(String cpdUri) {}

        @Override
        public String getDataId() {
            return null;
        }

        @Override
        public String getDataTitle() {
            return null;
        }

        @Override
        public String getDataDescription() {
            return null;
        }
    }
}
