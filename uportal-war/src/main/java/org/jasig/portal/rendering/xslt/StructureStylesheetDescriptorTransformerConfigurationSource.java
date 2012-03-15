package org.jasig.portal.rendering.xslt;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.layout.IStylesheetUserPreferencesService.PreferencesScope;

public class StructureStylesheetDescriptorTransformerConfigurationSource extends
        StylesheetDescriptorTransformerConfigurationSource {

    @Override
    protected PreferencesScope getStylesheetPreferencesScope(HttpServletRequest request) {
        return PreferencesScope.STRUCTURE;
    }

}
