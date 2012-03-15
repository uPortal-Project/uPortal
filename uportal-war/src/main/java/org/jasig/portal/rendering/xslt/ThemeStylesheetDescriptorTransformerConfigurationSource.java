package org.jasig.portal.rendering.xslt;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.layout.om.IStylesheetUserPreferences;

public class ThemeStylesheetDescriptorTransformerConfigurationSource extends
        StylesheetDescriptorTransformerConfigurationSource {

    @Override
    protected String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    protected IStylesheetUserPreferences getStylesheetUserPreferences(HttpServletRequest request) {
        return this.stylesheetUserPreferencesService.getThemeStylesheetUserPreferences(request);
    }

}
