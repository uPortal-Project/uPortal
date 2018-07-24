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
package org.apereo.portal.portlets.layout.dlm.remoting.registry.v43;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.layout.dlm.remoting.registry.v43.PortletDefinitionBean;
import org.apereo.portal.portlet.marketplace.IMarketplaceService;
import org.apereo.portal.portlet.marketplace.MarketplacePortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletDefinitionParameter;
import org.apereo.portal.portlet.om.IPortletDescriptorKey;
import org.apereo.portal.portlet.om.IPortletLifecycleEntry;
import org.apereo.portal.portlet.om.IPortletPreference;
import org.apereo.portal.portlet.om.IPortletType;
import org.apereo.portal.portlet.om.PortletCategory;
import org.apereo.portal.portlet.om.PortletLifecycleState;
import org.apereo.portal.portlet.registry.IPortletCategoryRegistry;
import org.apereo.portal.rest.layout.MarketplaceEntry;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPerson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class PortletDefinitionBeanTest {

    @Mock private IPortletPreference portletPref;

    @Mock private IPortletDefinitionParameter portletDefParam;

    // Static test data
    private String title = "testTitle";
    private String fName = "testFName";
    private Double averageRating = 4.7;
    private String description = "testDesc";
    private PortletLifecycleState state = PortletLifecycleState.CREATED;
    private int typeId = 456;
    private Long usersRated = 567L;

    @Before
    public void setup() throws Exception {
        portletPref = Mockito.mock(IPortletPreference.class);
        portletDefParam = Mockito.mock(IPortletDefinitionParameter.class);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFromMarketplacePortletDefinition() {
        Long id = 345L;
        String name = "testName";

        String[] keywords = new String[] {"val1", "val2"};
        List<IPortletPreference> prefs = new ArrayList<>();
        prefs.add(portletPref);
        Mockito.when(portletPref.getName()).thenReturn("keywords");
        Mockito.when(portletPref.getValues()).thenReturn(keywords);

        Map<String, IPortletDefinitionParameter> params = new HashMap<>();
        params.put("test1", portletDefParam);

        MarketplacePortletDefinition mpd =
                buildMarketplacePortletDefinition(id, name, prefs, params);

        PortletDefinitionBean pdb =
                PortletDefinitionBean.fromMarketplacePortletDefinition(mpd, Locale.ENGLISH);
        assertEquals(averageRating, pdb.getAverageRating());
        assertEquals(id, (Long) pdb.getId());
        assertEquals(fName, pdb.getFname());
        assertEquals(title, pdb.getTitle());
        assertEquals(name, pdb.getName());
        assertEquals(description, pdb.getDescription());
        assertEquals(state.toString(), pdb.getState());
        assertEquals(typeId, pdb.getTypeId());
        assertEquals(usersRated, (Long) pdb.getRatingsCount());
        assertEquals(params, pdb.getParameters());
        assertEquals(Arrays.asList(keywords), pdb.getKeywords());
    }

    @Test
    public void testFromMarketplacePortletDefinitionNoKeywords() {
        Long id = 345L;
        String name = "testName";

        // Create a non-keyword list
        String[] nonKeywords = new String[] {"val1", "val2"};
        List<IPortletPreference> prefs = new ArrayList<>();
        prefs.add(portletPref);
        Mockito.when(portletPref.getName()).thenReturn("non-keywords");
        Mockito.when(portletPref.getValues()).thenReturn(nonKeywords);

        MarketplacePortletDefinition mpd = buildMarketplacePortletDefinition(id, name, prefs, null);

        PortletDefinitionBean pdb =
                PortletDefinitionBean.fromMarketplacePortletDefinition(mpd, Locale.ENGLISH);
        assertEquals(Collections.EMPTY_LIST, pdb.getKeywords());
    }

    @Test
    public void testFromMarketplacePortletDefinitionHashCode() {
        MarketplacePortletDefinition mpd1 =
                buildMarketplacePortletDefinition(678L, "testName1", null, null);
        MarketplacePortletDefinition mpd2 =
                buildMarketplacePortletDefinition(678L, "testName2", null, null);

        PortletDefinitionBean pdb1 =
                PortletDefinitionBean.fromMarketplacePortletDefinition(mpd1, Locale.ENGLISH);
        PortletDefinitionBean pdb2 =
                PortletDefinitionBean.fromMarketplacePortletDefinition(mpd2, Locale.ENGLISH);

        assertEquals(pdb1.hashCode(), pdb2.hashCode());
    }

    @Test
    public void testCompareToDifferent() {
        String name1 = "testName1";
        String name2 = "testName2";
        MarketplacePortletDefinition mpd1 =
                buildMarketplacePortletDefinition(678L, name1, null, null);
        MarketplacePortletDefinition mpd2 =
                buildMarketplacePortletDefinition(678L, name2, null, null);

        PortletDefinitionBean pdb1 =
                PortletDefinitionBean.fromMarketplacePortletDefinition(mpd1, Locale.ENGLISH);
        PortletDefinitionBean pdb2 =
                PortletDefinitionBean.fromMarketplacePortletDefinition(mpd2, Locale.ENGLISH);

        assertEquals(name1.compareTo(name2), pdb1.compareTo(pdb2));
    }

    @Test
    public void testCompareToSimilar() {
        String name1 = "testName1";
        String name2 = "testName1";
        MarketplacePortletDefinition mpd1 =
                buildMarketplacePortletDefinition(678L, name1, null, null);
        MarketplacePortletDefinition mpd2 =
                buildMarketplacePortletDefinition(678L, name2, null, null);

        PortletDefinitionBean pdb1 =
                PortletDefinitionBean.fromMarketplacePortletDefinition(mpd1, Locale.ENGLISH);
        PortletDefinitionBean pdb2 =
                PortletDefinitionBean.fromMarketplacePortletDefinition(mpd2, Locale.ENGLISH);

        assertEquals(name1.compareTo(name2), pdb1.compareTo(pdb2));
    }

    @Test
    public void testEqualsSameID() {
        Long id1 = 678L;
        Long id2 = 678L;

        MarketplacePortletDefinition mpd1 =
                buildMarketplacePortletDefinition(id1, "testName", null, null);
        MarketplacePortletDefinition mpd2 =
                buildMarketplacePortletDefinition(id2, "testName", null, null);

        PortletDefinitionBean pdb1 =
                PortletDefinitionBean.fromMarketplacePortletDefinition(mpd1, Locale.ENGLISH);
        PortletDefinitionBean pdb2 =
                PortletDefinitionBean.fromMarketplacePortletDefinition(mpd2, Locale.ENGLISH);

        assertTrue(pdb1.equals(pdb2));
    }

    @Test
    public void testEqualsDifferentID() {
        Long id1 = 678L;
        Long id2 = 732L;

        MarketplacePortletDefinition mpd1 =
                buildMarketplacePortletDefinition(id1, "testName", null, null);
        MarketplacePortletDefinition mpd2 =
                buildMarketplacePortletDefinition(id2, "testName", null, null);

        PortletDefinitionBean pdb1 =
                PortletDefinitionBean.fromMarketplacePortletDefinition(mpd1, Locale.ENGLISH);
        PortletDefinitionBean pdb2 =
                PortletDefinitionBean.fromMarketplacePortletDefinition(mpd2, Locale.ENGLISH);

        assertFalse(pdb1.equals(pdb2));
    }

    @Test
    public void testEqualsSelf() {
        MarketplacePortletDefinition mpd1 =
                buildMarketplacePortletDefinition(45L, "testName", null, null);

        PortletDefinitionBean pdb1 =
                PortletDefinitionBean.fromMarketplacePortletDefinition(mpd1, Locale.ENGLISH);

        assertTrue(pdb1.equals(pdb1));
    }

    @Test
    public void testEqualsOtherObject() {
        MarketplacePortletDefinition mpd1 =
                buildMarketplacePortletDefinition(45L, "testName", null, null);

        PortletDefinitionBean pdb1 =
                PortletDefinitionBean.fromMarketplacePortletDefinition(mpd1, Locale.ENGLISH);

        assertFalse(pdb1.equals("id1"));
    }

    @Test
    public void testEqualsNull() {
        MarketplacePortletDefinition mpd1 =
                buildMarketplacePortletDefinition(45L, "testName", null, null);

        PortletDefinitionBean pdb1 =
                PortletDefinitionBean.fromMarketplacePortletDefinition(mpd1, Locale.ENGLISH);

        assertFalse(pdb1.equals(null));
    }

    /*
     * Unable to mock the 3 interfaces needed to instantiate a MarkeyplacePortletDefinition.
     * Instead, created near-empty shells that get some test parameters.
     */
    private MarketplacePortletDefinition buildMarketplacePortletDefinition(
            Long id,
            String name,
            List<IPortletPreference> prefs,
            Map<String, IPortletDefinitionParameter> params) {

        List<IPortletPreference> prefsToUse = prefs;
        if (prefs == null) {
            prefsToUse = new ArrayList<>();
            prefsToUse.add(portletPref);
            Mockito.when(portletPref.getName()).thenReturn("keywords");
            Mockito.when(portletPref.getValues()).thenReturn(new String[] {"val1", "val2"});
        }

        Map<String, IPortletDefinitionParameter> paramsToUse = params;
        if (params == null) {
            paramsToUse = new HashMap<>();
            paramsToUse.put("test1", portletDefParam);
        }

        PortletDefinitionImplShell pdis = new PortletDefinitionImplShell();
        pdis.setRating(averageRating);
        pdis.setId(id);
        pdis.setTitle(title);
        pdis.setFName(fName);
        pdis.setName(name);
        pdis.setDescription(description);
        pdis.setLifecycleState(state);
        pdis.setTypeId(typeId);
        pdis.setUsersRated(usersRated);
        pdis.setPortletPreferences(prefsToUse);
        pdis.setParameters(paramsToUse);
        MarketplaceServiceImplShell msis = new MarketplaceServiceImplShell();
        PortletCategoryRegistryImplShell pcris = new PortletCategoryRegistryImplShell();
        return new MarketplacePortletDefinition(pdis, msis, pcris);
    }

    private static final class PortletDefinitionImplShell implements IPortletDefinition {
        String title;
        Long id;
        int typeId;

        public void setId(Long id) {
            this.id = id;
        }

        public void setTypeId(int id) {
            this.typeId = id;
        }

        @Override
        public IPortletDefinitionId getPortletDefinitionId() {
            return new IPortletDefinitionId() {

                @Override
                public long getLongId() {
                    return id;
                }

                @Override
                public String getStringId() {
                    return "" + id;
                }
            };
        }

        @Override
        public List<IPortletPreference> getPortletPreferences() {
            return prefs;
        }

        List<IPortletPreference> prefs;

        @Override
        public boolean setPortletPreferences(List<IPortletPreference> portletPreferences) {
            this.prefs = portletPreferences;
            return true;
        }

        PortletLifecycleState state;

        @Override
        public PortletLifecycleState getLifecycleState() {
            return state;
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

        public void setLifecycleState(PortletLifecycleState state) {
            this.state = state;
        }

        String fname;

        @Override
        public String getFName() {
            return fname;
        }

        String name;

        @Override
        public String getName() {
            return name;
        }

        String descr;

        @Override
        public String getDescription() {
            return this.descr;
        }

        @Override
        public IPortletDescriptorKey getPortletDescriptorKey() {
            return null;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public int getTimeout() {
            return 0;
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
        public IPortletType getType() {
            return new IPortletType() {
                @Override
                public int getId() {
                    return typeId;
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
            };
        }

        @Override
        public Set<IPortletDefinitionParameter> getParameters() {
            return null;
        }

        @Override
        public IPortletDefinitionParameter getParameter(String key) {
            return null;
        }

        Map<String, IPortletDefinitionParameter> params;

        public void setParameters(Map<String, IPortletDefinitionParameter> params) {
            this.params = params;
        }

        @Override
        public Map<String, IPortletDefinitionParameter> getParametersAsUnmodifiableMap() {
            return params;
        }

        @Override
        public String getName(String locale) {
            return null;
        }

        @Override
        public String getDescription(String locale) {
            return null;
        }

        @Override
        public String getTitle(String locale) {
            return title;
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
        public void setFName(String fname) {
            this.fname = fname;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public void setDescription(String descr) {
            this.descr = descr;
        }

        @Override
        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public void setTimeout(int timeout) {}

        @Override
        public void setActionTimeout(Integer actionTimeout) {}

        @Override
        public void setEventTimeout(Integer eventTimeout) {}

        @Override
        public void setRenderTimeout(Integer renderTimeout) {}

        @Override
        public void setResourceTimeout(Integer resourceTimeout) {}

        @Override
        public void setType(IPortletType channelType) {}

        @Override
        public void setParameters(Set<IPortletDefinitionParameter> parameters) {}

        @Override
        public void addLocalizedTitle(String locale, String chanTitle) {}

        @Override
        public void addLocalizedName(String locale, String chanName) {}

        @Override
        public void addLocalizedDescription(String locale, String chanDesc) {}

        Double rating;

        @Override
        public Double getRating() {
            return rating;
        }

        @Override
        public void setRating(Double rating) {
            this.rating = rating;
        }

        Long usersRated;

        @Override
        public Long getUsersRated() {
            return usersRated;
        }

        @Override
        public void setUsersRated(Long usersRated) {
            this.usersRated = usersRated;
        }

        @Override
        public void addParameter(IPortletDefinitionParameter parameter) {}

        @Override
        public void addParameter(String name, String value) {}

        @Override
        public void removeParameter(IPortletDefinitionParameter parameter) {}

        @Override
        public void removeParameter(String name) {}

        @Override
        public EntityIdentifier getEntityIdentifier() {
            return null;
        }

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

    private static final class MarketplaceServiceImplShell implements IMarketplaceService {

        @Override
        public ImmutableSet<MarketplaceEntry> browseableMarketplaceEntriesFor(
                IPerson user, Set<PortletCategory> categories) {
            return null;
        }

        @Override
        public Set<PortletCategory> browseableNonEmptyPortletCategoriesFor(
                IPerson user, Set<PortletCategory> categories) {
            return null;
        }

        @Override
        public boolean mayBrowsePortlet(
                IAuthorizationPrincipal principal, IPortletDefinition portletDefinition) {
            return false;
        }

        @Override
        public Set<MarketplaceEntry> featuredEntriesForUser(
                IPerson user, Set<PortletCategory> categories) {
            return null;
        }

        @Override
        public MarketplacePortletDefinition getOrCreateMarketplacePortletDefinition(
                IPortletDefinition portletDefinition) {
            return null;
        }

        @Override
        public MarketplacePortletDefinition getOrCreateMarketplacePortletDefinitionIfTheFnameExists(
                String fname) {
            return null;
        }

        @Override
        public boolean mayAddPortlet(IPerson user, IPortletDefinition portletDefinition) {
            return false;
        }
    }

    private static final class PortletCategoryRegistryImplShell
            implements IPortletCategoryRegistry {

        @Override
        public Set<PortletCategory> getAllChildCategories(PortletCategory parent) {
            return null;
        }

        @Override
        public Set<PortletCategory> getAllParentCategories(PortletCategory child) {
            return null;
        }

        @Override
        public Set<IPortletDefinition> getAllChildPortlets(PortletCategory parent) {
            return null;
        }

        @Override
        public PortletCategory getPortletCategory(String portletCategoryId) {
            return null;
        }

        @Override
        public PortletCategory getPortletCategoryByName(String portletCategoryName) {
            return null;
        }

        @Override
        public Set<PortletCategory> getChildCategories(PortletCategory parent) {
            return null;
        }

        @Override
        public Set<IPortletDefinition> getChildPortlets(PortletCategory parent) {
            return Collections.EMPTY_SET;
        }

        @Override
        public Set<PortletCategory> getParentCategories(PortletCategory child) {
            return Collections.EMPTY_SET;
        }

        @Override
        public Set<PortletCategory> getParentCategories(IPortletDefinition child) {
            return Collections.EMPTY_SET;
        }

        @Override
        public PortletCategory getTopLevelPortletCategory() {
            return null;
        }
    }
}
