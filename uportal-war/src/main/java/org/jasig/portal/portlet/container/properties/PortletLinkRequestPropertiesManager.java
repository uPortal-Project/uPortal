package org.jasig.portal.portlet.container.properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

/**
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
@Service
public class PortletLinkRequestPropertiesManager extends BaseRequestPropertiesManager {
    
    protected final String LINK_PROPERTY = "externalPortletLink";

    @Override
    public void setResponseProperty(HttpServletRequest portletRequest, IPortletWindow portletWindow, String property, String value) {
        if (LINK_PROPERTY.equals(property)) {
            if (StringUtils.isNotBlank(value)) {
                portletRequest.setAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_LINK, value);
            }
        }
    }
    
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
