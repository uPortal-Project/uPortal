package org.apereo.portal.url;

import javax.servlet.http.HttpServletRequest;
import org.apache.pluto.container.impl.HttpServletPortletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/** Customizer to append a parameter depending on PortalRequest. */
public class UrlCasParamAppenderFromPortalRequestCustomizer
        implements IAuthUrlCustomizer, InitializingBean {

    /** Logger. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Regex condition to apply Customization. */
    private String applyCondition = ".*\\?param=xyz.*";

    /** Param name with equal and value to append to url. */
    private String paramToAppend;

    public void setApplyCondition(final String applyCondition) {
        this.applyCondition = applyCondition;
    }

    public void setParamToAppend(final String paramToAppend) {
        this.paramToAppend = paramToAppend;
    }

    @Override
    public boolean supports(final HttpServletRequest request, final String url) {
        final String requestUrl = getFullURL(request);
        if (requestUrl == null) {
            logger.warn("Try to customize a cas url from a null HttpServletRequest !");
        }
        return url != null && requestUrl != null && requestUrl.matches(applyCondition);
    }

    public String customizeUrl(final HttpServletRequest request, final String url) {
        if (url != null && !url.isEmpty() && supports(request, url)) {
            final String updatedUrl = url + "&" + paramToAppend;

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Modifying CAS Url from [%s] to [%s]", url, updatedUrl));
            }
            return updatedUrl;
        }
        return url;
    }

    public static String getFullURL(HttpServletRequest request) {
        if (request == null) return null;
        StringBuilder requestURL;
        HttpServletRequest portalRequest = request;

        // Useful in case of request passed from the xslt/portlet context, we don't have the portal
        // request passed
        if (request instanceof HttpServletPortletRequestWrapper) {
            portalRequest =
                    (HttpServletRequest)
                            request.getAttribute(
                                    PortalHttpServletRequestWrapper
                                            .ATTRIBUTE__HTTP_SERVLET_REQUEST);
        }

        if (portalRequest == null || portalRequest.getRequestURL() == null) return null;

        requestURL = new StringBuilder(portalRequest.getRequestURL().toString());

        final String queryString = portalRequest.getQueryString();

        if (queryString != null) {
            return requestURL.append('?').append(queryString).toString();
        }
        return requestURL.toString();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(this.applyCondition, "No applyCondition supplied !");
        Assert.hasText(this.paramToAppend, "No paramToAppend supplied !");
    }
}
