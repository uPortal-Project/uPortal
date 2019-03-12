package org.apereo.portal.index;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDescriptorKey;
import org.apereo.portal.portlet.om.IPortletPreference;
import org.junit.Test;

public class HtmlPortletPreferenceSearchContentExtractorTest
        extends HtmlPortletPreferenceSearchContentExtractor {

    private final String PORTLET_NAME = "MockPortlet";
    private final String WEBAPP_NAME = "/MockPortletWebapp";
    private final String PREFERENCE_NAME = "mockContent";

    private final String NOT_A_MATCH = "Not a Match!";

    private final HtmlPortletPreferenceSearchContentExtractor EXTRACTOR =
            new HtmlPortletPreferenceSearchContentExtractor(
                    PORTLET_NAME, WEBAPP_NAME, PREFERENCE_NAME);

    /** Superclass has 3-arg constructor. */
    public HtmlPortletPreferenceSearchContentExtractorTest() {
        super(null, null, null);
    }

    @Test
    public void testAppliesToPortletMatch() {

        final IPortletDescriptorKey portletDescriptorKey = mock(IPortletDescriptorKey.class);
        when(portletDescriptorKey.getPortletName()).thenAnswer(invocation -> PORTLET_NAME);
        when(portletDescriptorKey.getWebAppName()).thenAnswer(invocation -> NOT_A_MATCH);

        final IPortletDefinition portletDefinition = mock(IPortletDefinition.class);
        when(portletDefinition.getPortletDescriptorKey())
                .thenAnswer(invocation -> portletDescriptorKey);
        when(portletDefinition.getPortletPreferences())
                .thenAnswer(invocation -> Collections.emptyList());

        assertFalse(EXTRACTOR.appliesTo(portletDefinition));
    }

    @Test
    public void testAppliesToWebappMatch() {

        final IPortletDescriptorKey portletDescriptorKey = mock(IPortletDescriptorKey.class);
        when(portletDescriptorKey.getPortletName()).thenAnswer(invocation -> NOT_A_MATCH);
        when(portletDescriptorKey.getWebAppName()).thenAnswer(invocation -> WEBAPP_NAME);

        final IPortletDefinition portletDefinition = mock(IPortletDefinition.class);
        when(portletDefinition.getPortletDescriptorKey())
                .thenAnswer(invocation -> portletDescriptorKey);
        when(portletDefinition.getPortletPreferences())
                .thenAnswer(invocation -> Collections.emptyList());

        assertFalse(EXTRACTOR.appliesTo(portletDefinition));
    }

    @Test
    public void testAppliesToPreferenceMatch() {

        final IPortletDescriptorKey portletDescriptorKey = mock(IPortletDescriptorKey.class);
        when(portletDescriptorKey.getPortletName()).thenAnswer(invocation -> NOT_A_MATCH);
        when(portletDescriptorKey.getWebAppName()).thenAnswer(invocation -> NOT_A_MATCH);

        final IPortletPreference portletPreference = mock(IPortletPreference.class);
        when(portletPreference.getName()).thenAnswer(invocation -> PREFERENCE_NAME);

        final IPortletDefinition portletDefinition = mock(IPortletDefinition.class);
        when(portletDefinition.getPortletDescriptorKey())
                .thenAnswer(invocation -> portletDescriptorKey);
        when(portletDefinition.getPortletPreferences())
                .thenAnswer(invocation -> Collections.singletonList(portletPreference));

        assertFalse(EXTRACTOR.appliesTo(portletDefinition));
    }

    @Test
    public void testAppliesToMatchAll() {

        final IPortletDescriptorKey portletDescriptorKey = mock(IPortletDescriptorKey.class);
        when(portletDescriptorKey.getPortletName()).thenAnswer(invocation -> PORTLET_NAME);
        when(portletDescriptorKey.getWebAppName()).thenAnswer(invocation -> WEBAPP_NAME);

        final IPortletPreference portletPreference = mock(IPortletPreference.class);
        when(portletPreference.getName()).thenAnswer(invocation -> PREFERENCE_NAME);

        final IPortletDefinition portletDefinition = mock(IPortletDefinition.class);
        when(portletDefinition.getPortletDescriptorKey())
                .thenAnswer(invocation -> portletDescriptorKey);
        when(portletDefinition.getPortletPreferences())
                .thenAnswer(invocation -> Collections.singletonList(portletPreference));

        assertTrue(EXTRACTOR.appliesTo(portletDefinition));
    }
}
