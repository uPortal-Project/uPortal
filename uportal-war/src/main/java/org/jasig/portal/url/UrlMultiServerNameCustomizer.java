package org.jasig.portal.url;

import javax.servlet.http.HttpServletRequest;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

/**
 * Customizer to replace a text by a serverName.
 * @Author  Julien Gribonvald
 * @version $Revision$
 */
public class UrlMultiServerNameCustomizer implements IAuthUrlCustomizer {

    /** Logger. */
    private static final Log LOG = LogFactory.getLog(UrlMultiServerNameCustomizer.class);

    private Set<String> allServerNames = new HashSet<>();

    private String serverNameTextReplacement = "_CURRENT_SERVER_NAME_";

    @Required
    public void setAllServerNames(final Set<String> serverNames) {
        this.allServerNames = Sets.newHashSet(serverNames);
        Assert.notEmpty(this.allServerNames);
    }

    public void setServerNameTextReplacement(final String serverNameTextReplacement) {
        Assert.hasText(serverNameTextReplacement);
        this.serverNameTextReplacement = serverNameTextReplacement;
    }

    public boolean supports(final HttpServletRequest request, final String url) {
        return url != null && url.contains(serverNameTextReplacement);
    }

    public String customizeUrl(final HttpServletRequest request, final String url) {
        if (url != null && !url.isEmpty() && supports(request, url)) {
            final String updateUrl = url.replaceFirst(serverNameTextReplacement, findMatchingServerName(request));

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Modifying Url Domain from [%s] to [%s]", url, updateUrl));
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
