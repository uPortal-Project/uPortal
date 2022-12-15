package org.apereo.portal.events;

import com.fasterxml.jackson.databind.util.StdConverter;
import javax.portlet.WindowState;

public class WindowStateJsonConverter extends StdConverter<WindowState, String> {

    @Override
    public String convert(WindowState windowState) {
        return windowState.toString();
    }
}
