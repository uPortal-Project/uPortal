package org.apereo.portal.index;

import org.apereo.portal.portlet.om.IPortletDefinition;

/**
 * Beans of this type know how to access the content (for search indexing) of a subset of portlets.
 */
public interface ISearchContentExtractor {

    boolean appliesTo(IPortletDefinition portlet);

    String extractContent(IPortletDefinition portlet);

}
