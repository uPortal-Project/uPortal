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
package org.apereo.portal.rest.layout;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.portal.layout.LayoutPortlet;
import org.apereo.portal.portlet.marketplace.MarketplacePortletDefinition;
import org.apereo.portal.portlet.marketplace.PortletReleaseNotes;
import org.apereo.portal.portlet.marketplace.ScreenShot;
import org.apereo.portal.portlet.om.IPortletDefinitionParameter;
import org.apereo.portal.portlet.om.PortletCategory;
import org.apereo.portal.security.IPerson;

/**
 * User-specific representation of a Marketplace portlet definition suitable for JSON serialization
 * and for use in view implementations.
 */
public class MarketplaceEntry implements Serializable {

    private Set<String> getPortletCategories(MarketplacePortletDefinition pdef) {
        Set<PortletCategory> categories = pdef.getCategories();
        Set<String> rslt = new HashSet<String>();
        for (PortletCategory category : categories) {
            String lowerCase = category.getName().toLowerCase();
            if (!"all categories".equals(lowerCase)) {
                rslt.add(StringUtils.capitalize(category.getName().toLowerCase()));
            }
        }
        return rslt;
    }

    private static final long serialVersionUID = 1L;

    private final MarketplacePortletDefinition pdef;
    private final LayoutPortlet layoutObject;
    private String maxURL;
    private Set<MarketplaceEntry> relatedEntries;
    private boolean generateRelatedPortlets = true;
    private boolean canAdd;

    /** User for whom this MarketplaceEntity is tailored. */
    private IPerson user;

    public MarketplaceEntry(MarketplacePortletDefinition pdef, final IPerson user) {
        this.pdef = pdef;
        this.maxURL = pdef.getRenderUrl();
        this.user = user;
        this.layoutObject = new LayoutPortlet(pdef);
    }

    public MarketplaceEntry(MarketplacePortletDefinition pdef, String maxURL, final IPerson user) {
        this.pdef = pdef;
        this.maxURL = maxURL;
        this.user = user;
        this.layoutObject = new LayoutPortlet(pdef);
    }

    public MarketplaceEntry(
            MarketplacePortletDefinition pdef,
            boolean generateRelatedPortlets,
            final IPerson user) {
        this.pdef = pdef;
        this.maxURL = pdef.getRenderUrl();
        this.generateRelatedPortlets = generateRelatedPortlets;
        this.user = user;
        this.layoutObject = new LayoutPortlet(pdef);
    }

    public LayoutPortlet getLayoutObject() {
        return layoutObject;
    }

    public String getId() {
        return pdef.getPortletDefinitionId().getStringId();
    }

    public String getTitle() {
        return pdef.getTitle();
    }

    public String getName() {
        return pdef.getName();
    }

    public String getFname() {
        return pdef.getFName();
    }

    public String getDescription() {
        return pdef.getDescription();
    }

    public String getType() {
        return pdef.getType().getName();
    }

    public String getLifecycleState() {
        return pdef.getLifecycleState().toString();
    }

    public Set<String> getCategories() {
        return getPortletCategories(pdef);
    }

    @JsonIgnore
    public MarketplacePortletDefinition getMarketplacePortletDefinition() {
        return pdef;
    }

    public String getFaIcon() {
        IPortletDefinitionParameter parameter = pdef.getParameter("faIcon");
        return parameter != null ? parameter.getValue() : null;
    }

    /**
     * @param parameterName
     * @return
     * @since 4.2
     */
    public IPortletDefinitionParameter getParameter(final String parameterName) {
        return this.pdef.getParameter(parameterName);
    }

    public String getMaxUrl() {
        return maxURL;
    }

    public void setMaxUrl(String urlString) {
        this.maxURL = urlString;
    }

    public String getShortUrl() {
        return pdef.getShortURL();
    }

    public List<ScreenShot> getMarketplaceScreenshots() {
        return pdef.getScreenShots();
    }

    public PortletReleaseNotes getPortletReleaseNotes() {
        return pdef.getPortletReleaseNotes();
    }

    public Set<MarketplaceEntry> getRelatedPortlets() {

        if (!generateRelatedPortlets) {
            //disabled so we don't have infinite related portlets.
            return null;
        }
        if (relatedEntries == null) {
            relatedEntries =
                    new HashSet<MarketplaceEntry>(
                            MarketplacePortletDefinition.QUANTITY_RELATED_PORTLETS_TO_SHOW);
            final Set<MarketplacePortletDefinition> randomSamplingRelatedPortlets =
                    pdef.getRandomSamplingRelatedPortlets(user);
            for (MarketplacePortletDefinition def : randomSamplingRelatedPortlets) {
                relatedEntries.add(new MarketplaceEntry(def, false, user));
            }
        }
        return relatedEntries;
    }

    public Double getRating() {
        return pdef.getRating() == null ? 0 : pdef.getRating();
    }

    public String getRenderUrl() {
        return pdef.getRenderUrl();
    }

    public Long getUserRated() {
        return pdef.getUsersRated();
    }

    public boolean isCanAdd() {
        return canAdd;
    }

    public void setCanAdd(boolean canAdd) {
        this.canAdd = canAdd;
    }

    public String getTarget() {
        return pdef.getTarget();
    }

    public List<String> getKeywords() {
        return pdef.getKeywords();
    }

    @Override
    public boolean equals(Object other) {

        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (other.getClass() != getClass()) {
            return false;
        }
        final MarketplaceEntry rhs = (MarketplaceEntry) other;

        return new EqualsBuilder()
                .append(getMarketplacePortletDefinition(), rhs.getMarketplacePortletDefinition())
                .append(this.user, rhs.user)
                .append(isCanAdd(), rhs.isCanAdd())
                .append(generateRelatedPortlets, rhs.generateRelatedPortlets)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getMarketplacePortletDefinition())
                .append(user)
                .append(isCanAdd())
                .append(generateRelatedPortlets)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("fname", getFname()).toString();
    }
}
