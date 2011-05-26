package org.jasig.portal.json.rendering;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;
import org.jasig.portal.rendering.StylesheetAttributeSource;

public class JsonThemeAttributeSource extends StylesheetAttributeSource {

    @Override
    protected IStylesheetDescriptor getStylesheetDescriptor(HttpServletRequest request) {
        return this.stylesheetDescriptorDao.getStylesheetDescriptorByName("JsonLayout");
    }
    
    @Override
    protected IStylesheetUserPreferences getStylesheetUserPreferences(HttpServletRequest request) {
        return this.stylesheetUserPreferencesService.getThemeStylesheetUserPreferences(request);
    }
}

