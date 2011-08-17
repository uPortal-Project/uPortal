package org.jasig.portal.i18n;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * This interceptor sets response locale to the value determined by localeResolver. This allows to
 * use <code>response.getLocale()</code> in order to determine the portal locale whenever request
 * object is available.
 * 
 * @author Arvids Grabovskis
 * @version $Revision$
 */
public class LocaleManagementInterceptor extends HandlerInterceptorAdapter {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Locale locale = RequestContextUtils.getLocale(request);
        response.setLocale(locale);
        return true;
    }
}
