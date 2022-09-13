package org.apereo.portal.url;

import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

/** Customizer to replace a text by a serverName. */
public class UrlMultiServerNameCustomizer implements IAuthUrlCustomizer {

    /** Logger. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Set<String> allServerNames = new HashSet<>();

    private String serverNameTextReplacement = "_CURRENT_SERVER_NAME_";

    @Required
    public void setAllServerNames(final Set<String> serverNames) {
        this.allServerNames = new HashSet<>(serverNames);
        Assert.notEmpty(this.allServerNames, "The attribute serverNames should not be empty");
    }

    public void setServerNameTextReplacement(final String serverNameTextReplacement) {
        Assert.hasText(
                serverNameTextReplacement,
                "The serverNameTextReplacement attribute should not be empty");
        this.serverNameTextReplacement = serverNameTextReplacement;
    }

    public boolean supports(final HttpServletRequest request, final String url) {
        return url != null && url.contains(serverNameTextReplacement);
    }

    public String customizeUrl(final HttpServletRequest request, final String url) {
        if (url != null && !url.isEmpty() && supports(request, url)) {
            final String updateUrl =
                    url.replaceFirst(serverNameTextReplacement, findMatchingServerName(request));

            if (logger.isDebugEnabled()) {
                logger.debug("Modifying Url Domain from [{}] to [{}]", url, updateUrl);
            }
            return updateUrl;
        }
        return url;
    }

    protected String findMatchingServerName(final HttpServletRequest request) {
        if (request != null) {
            final String comparisonHost = request.getHeader("Host");
            if (comparisonHost != null && !comparisonHost.isEmpty()) {
                for (final String server : this.allServerNames) {
                    if (server.contains(comparisonHost)) {
                        return server;
                    }
                }
            }
        }

        return this.allServerNames.iterator().next();
    }
}
