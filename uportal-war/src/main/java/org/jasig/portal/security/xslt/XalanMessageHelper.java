package org.jasig.portal.security.xslt;

import org.jasig.portal.spring.locator.XalanMessageHelperLocator;

public class XalanMessageHelper implements IXalanMessageHelper {

    private IXalanMessageHelper messageHelper;

    public XalanMessageHelper() {
        this.messageHelper = XalanMessageHelperLocator.getXalanMessageHelper();
    }

    public String getMessage(String code, String language) {
        return this.messageHelper.getMessage(code, language);
    }

}
