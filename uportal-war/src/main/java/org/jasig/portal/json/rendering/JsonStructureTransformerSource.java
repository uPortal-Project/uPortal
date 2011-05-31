package org.jasig.portal.json.rendering;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.rendering.xslt.BaseTransformerSource;

public class JsonStructureTransformerSource extends BaseTransformerSource {

    @Override
    protected long getStylesheetDescriptorId(IUserPreferencesManager preferencesManager) {
        IStylesheetDescriptor descriptor = this.stylesheetDescriptorDao.getStylesheetDescriptorByName("DLMMobileColumns");
        return descriptor.getId();
    }

}
