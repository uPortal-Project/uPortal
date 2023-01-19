package org.apereo.portal.events;

import com.fasterxml.jackson.databind.util.StdConverter;
import javax.portlet.PortletMode;

public class PortletModeJsonConverter extends StdConverter<PortletMode, String> {

    @Override
    public String convert(PortletMode portletMode) {
        return portletMode.toString();
    }
}
