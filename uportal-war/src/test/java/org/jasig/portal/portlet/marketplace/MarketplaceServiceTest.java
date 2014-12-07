package org.jasig.portal.portlet.marketplace;

import com.google.common.collect.ImmutableSet;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.security.IPerson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit tests for MarketplaceService.
 */
public class MarketplaceServiceTest {

    @Mock IPortletCategoryRegistry categoryRegistry;
    @Mock IMarketplaceRegistry marketplaceRegistry;

    @Mock IPerson person;

    @Mock IPortletDefinition portletDefinition;
    @Mock IPortletDefinition anotherPortletDefinition;
    @Mock IPortletDefinition yetAnotherPortletDefinition;

    MarketplacePortletDefinition marketplacePortletDefinition;
    MarketplacePortletDefinition anotherMarketplacePortletDefinition;
    MarketplacePortletDefinition yetAnotherMarketplacePortletDefinition;

    PortletCategory categoryContainingThePortlet;

    MarketplaceService marketplaceService;

    /**
     * Set up before each test case.
     */
    @Before
    public void setUp() {

        initMocks(this);

        marketplaceService = new MarketplaceService();
        marketplaceService.setPortletCategoryRegistry(categoryRegistry);
        marketplaceService.setMarketplaceRegistry(marketplaceRegistry);

        categoryContainingThePortlet = new PortletCategory("categoryContainingThePortlet");

        when(portletDefinition.getFName()).thenReturn("fname");
        when(anotherPortletDefinition.getFName()).thenReturn("anotherFname");
        when(yetAnotherPortletDefinition.getFName()).thenReturn("yetAnotherFname");

        when(person.getUserName()).thenReturn("bucky");

        marketplacePortletDefinition =
            new MarketplacePortletDefinition(portletDefinition, person,
                marketplaceService, categoryRegistry);

        anotherMarketplacePortletDefinition =
            new MarketplacePortletDefinition(anotherPortletDefinition, person,
            marketplaceService, categoryRegistry);

        yetAnotherMarketplacePortletDefinition =
            new MarketplacePortletDefinition(yetAnotherPortletDefinition, person,
            marketplaceService, categoryRegistry);


        when(marketplaceRegistry.marketplacePortletDefinition("fname", person))
            .thenReturn(marketplacePortletDefinition);
        when(marketplaceRegistry.marketplacePortletDefinition("anotherFname", person))
            .thenReturn(anotherMarketplacePortletDefinition);
        when(marketplaceRegistry.marketplacePortletDefinition("yetAnotherFname", person))
            .thenReturn(yetAnotherMarketplacePortletDefinition);


    }

    /**
     * A portlet that is in no categories has no related portlets.
     */
    @Test
    public void portletInNoCategoriesHasNoRelatedPortlets() {

        when(categoryRegistry.getParentCategories(portletDefinition))
            .thenReturn(new HashSet<PortletCategory>());

        final ImmutableSet<MarketplacePortletDefinition> relatedPortlets =
            marketplaceService.marketplacePortletDefinitionsRelatedTo(marketplacePortletDefinition);

        assertTrue(relatedPortlets.isEmpty());
    }

    /**
     * A portlet is not related to itself, though of course it is in the categories that it is in.
     */
    @Test
    public void portletDefinitionIsNotRelatedToItself() {

        final Set<PortletCategory> setOfCategoriesContainingThePortlet = new HashSet<>();
        setOfCategoriesContainingThePortlet.add(categoryContainingThePortlet);

        when(categoryRegistry.getParentCategories(marketplacePortletDefinition))
            .thenReturn(setOfCategoriesContainingThePortlet);

        final Set<IPortletDefinition> portletsInTheCategory = new HashSet<>();
        portletsInTheCategory.add(portletDefinition);

        when(categoryRegistry.getAllChildPortlets(categoryContainingThePortlet))
            .thenReturn(portletsInTheCategory);

        final ImmutableSet<MarketplacePortletDefinition> relatedPortlets =
            marketplaceService.marketplacePortletDefinitionsRelatedTo(marketplacePortletDefinition);

        assertTrue(relatedPortlets.isEmpty());

    }

    /**
     * Test that portlet definitions in the same Category (deep traversal) are related.
     */
    @Test
    public void portletsInSameCategoryAreRelated() {

        final Set<PortletCategory> setOfCategoriesContainingThePortlet = new HashSet<>();
        setOfCategoriesContainingThePortlet.add(categoryContainingThePortlet);

        when(categoryRegistry.getParentCategories(marketplacePortletDefinition))
            .thenReturn(setOfCategoriesContainingThePortlet);

        final Set<IPortletDefinition> portletsInTheCategory = new HashSet<>();
        portletsInTheCategory.add(portletDefinition);
        portletsInTheCategory.add(anotherPortletDefinition);

        // because this is *all*, it implies traversal of sub-categories.
        // this is why we don't need to test this case separately
        when(categoryRegistry.getAllChildPortlets(categoryContainingThePortlet))
            .thenReturn(portletsInTheCategory);

        final Set<MarketplacePortletDefinition> expectedRelatedPortlets = new HashSet<>();
        final MarketplacePortletDefinition expectedRelatedMarketplaceDefinition = new
            MarketplacePortletDefinition(anotherPortletDefinition, person,
            marketplaceService, categoryRegistry);
        expectedRelatedPortlets.add(expectedRelatedMarketplaceDefinition);

        assertEquals(expectedRelatedPortlets,
            marketplaceService.marketplacePortletDefinitionsRelatedTo(marketplacePortletDefinition));

    }

}
