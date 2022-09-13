package org.apereo.portal.url;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Class to register all login/logout IAuthUrlCustomizer. */
public class UrlAuthCustomizerRegistry {

    /** Logger. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private List<IAuthUrlCustomizer> registry;

    public void setRegistry(List<IAuthUrlCustomizer> registry) {
        this.registry = registry;
    }

    public String customizeUrl(final HttpServletRequest request, final String url) {
        String customizedUrl = url;
        if (registry != null && !registry.isEmpty()) {
            for (IAuthUrlCustomizer customizer : this.registry) {
                if (customizer != null && customizer.supports(request, customizedUrl)) {
                    customizedUrl = customizer.customizeUrl(request, customizedUrl);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("The url returned after customization is " + customizedUrl);
            }
            return customizedUrl;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "No IAuthUrlCustomizer was set, the url " + customizedUrl + " wasn't modified");
        }
        return customizedUrl;
    }
}
