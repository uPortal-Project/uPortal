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
package org.apereo.portal.layout.dlm.remoting.registry.v43;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apereo.portal.portlet.marketplace.MarketplacePortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionParameter;

/**
 * Gets serialized into JSON representing a portlet for the 4.3 version of the portletRegistry.json
 * API.
 *
 * @since 4.3
 */
public final class PortletDefinitionBean
        implements Comparable<PortletDefinitionBean>, Serializable {

    private static final long serialVersionUID = 1L;

    private static final List<String> EMPTY_KEYWORDS = Collections.emptyList();

    private final long id;
    private final String fname;
    private final String title;
    private final String name;
    private final String description;
    private final String state;
    private final int typeId;
    private final Double averageRating;
    private final long ratingsCount;
    private final Map<String, IPortletDefinitionParameter> parameters;
    private final List<String> keywords;

    public static PortletDefinitionBean fromMarketplacePortletDefinition(
            final MarketplacePortletDefinition mpd, final Locale locale) {
        return new PortletDefinitionBean(mpd, locale);
    }

    public long getId() {
        return id;
    }

    public String getFname() {
        return fname;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getState() {
        return state;
    }

    public int getTypeId() {
        return typeId;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public long getRatingsCount() {
        return ratingsCount;
    }

    public Map<String, IPortletDefinitionParameter> getParameters() {
        return parameters;
    }

    public void putParameter(IPortletDefinitionParameter parameter) {
        parameters.put(parameter.getName(), parameter);
    }

    public List<String> getKeywords() {
        return keywords;
    }

    @Override
    public int compareTo(PortletDefinitionBean pdb) {
        return new CompareToBuilder().append(this.name, pdb.getName()).toComparison();
    }

    @Override
    public String toString() {
        return "PortletDefinitionBean{"
                + "id="
                + id
                + ", fname='"
                + fname
                + '\''
                + ", title='"
                + title
                + '\''
                + ", name='"
                + name
                + '\''
                + ", description='"
                + description
                + '\''
                + ", state='"
                + state
                + '\''
                + ", typeId="
                + typeId
                + ", averageRating="
                + averageRating
                + ", ratingsCount="
                + ratingsCount
                + ", parameters="
                + parameters
                + ", keywords="
                + keywords
                + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof PortletDefinitionBean)) {
            return false;
        }
        PortletDefinitionBean rhs = (PortletDefinitionBean) object;
        return new EqualsBuilder().append(this.id, rhs.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143).append(this.id).toHashCode();
    }

    /*
     * Implementation
     */

    private PortletDefinitionBean(final MarketplacePortletDefinition mpd, final Locale locale) {
        this.id = mpd.getPortletDefinitionId().getLongId();
        this.fname = mpd.getFName();
        this.title = mpd.getTitle(locale.toString());
        this.name = mpd.getName(locale.toString());
        this.description = mpd.getDescription(locale.toString());
        this.state = mpd.getLifecycleState().toString();
        this.typeId = mpd.getType().getId();
        this.averageRating = mpd.getRating();
        this.ratingsCount = mpd.getUsersRated();
        this.parameters = new HashMap<>(mpd.getParametersAsUnmodifiableMap());
        this.keywords =
                mpd.getKeywords() != null
                        ? Collections.unmodifiableList(mpd.getKeywords())
                        : EMPTY_KEYWORDS;
    }
}
