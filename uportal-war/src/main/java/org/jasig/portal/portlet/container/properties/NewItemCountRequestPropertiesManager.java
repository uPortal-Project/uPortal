package org.jasig.portal.portlet.container.properties;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

/**
 * 
 * @author Jen Bourey
 * @version $Revision$
 */
@Service
public class NewItemCountRequestPropertiesManager extends BaseRequestPropertiesManager {
    
    protected final String NEW_ITEM_COUNT_PROPERTY = "newItemCount";

    @Override
    public void setResponseProperty(HttpServletRequest portletRequest, IPortletWindow portletWindow, String property, String value) {
        if (NEW_ITEM_COUNT_PROPERTY.equals(property)) {
            portletRequest.setAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_NEW_ITEM_COUNT, value);
        }
    }
    
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
