package org.jasig.portal.security.xslt;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

public class XalanMessageHelperBean implements IXalanMessageHelper {

    private MessageSource messageSource;
    
    @Autowired(required = true)
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String code, String language) {
        final Locale locale = new Locale(language);
        final String message = messageSource.getMessage(code, null, locale);
        return message;
    }

}
