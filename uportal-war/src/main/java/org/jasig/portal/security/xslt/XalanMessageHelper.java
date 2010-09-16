package org.jasig.portal.security.xslt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Static wrapper around {@link IXalanMessageHelper} to make calls from XSTLC easier
 */
@Service
public final class XalanMessageHelper {
    private static IXalanMessageHelper messageHelper;
    
    @Autowired
    public void setMessageHelper(IXalanMessageHelper messageHelper) {
        XalanMessageHelper.messageHelper = messageHelper;
    }

    public static String getMessage(String code, String language) {
        return messageHelper.getMessage(code, language);
    }
}
