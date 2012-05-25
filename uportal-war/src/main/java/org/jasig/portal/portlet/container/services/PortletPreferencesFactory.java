package org.jasig.portal.portlet.container.services;

import javax.portlet.PortletPreferences;

import org.apache.pluto.container.PortletRequestContext;

/**
 * Creates {@link PortletPreferences} objects
 * 
 * @author Eric Dalquist
 */
public interface PortletPreferencesFactory {

    /**
     * Create portlet preferences for the specified portlet request context
     */
    PortletPreferences createPortletPreferences(final PortletRequestContext requestContext, boolean render);
}