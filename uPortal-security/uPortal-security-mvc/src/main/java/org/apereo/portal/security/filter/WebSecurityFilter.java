package org.apereo.portal.security.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSecurityFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(WebSecurityFilter.class);
    private FilterConfig config;

    // Parameters
    public static final String PARAM_HSTS_INCLUDE_SUBDOMAINS = "sec.hsts.include.subdomains";
    public static final String PARAM_HSTS_PRELOAD = "sec.hsts.preload";
    public static final String PARAM_HSTS_ENABLED = "sec.hsts.enabled";
    public static final String PARAM_HSTS_MAXAGE_SECONDS = "sec.hsts.maxage.seconds";
    public static final String PARAM_ANTI_JACK_CLICKING_ENABLED = "sec.anti.click.jacking.enabled";
    public static final String PARAM_ANTI_JACK_CLICKING_OPTIONS = "sec.anti.click.jacking.options";
    public static final String PARAM_ANTI_JACK_CLICKING_URI = "sec.anti.click.jacking.uri";
    public static final String PARAM_X_CONTENT_TYPE_ENABLED = "sec.x.content.type.enabled";
    public static final String PARAM_CONTENT_SECURITY_POLICY_ENABLED =
            "sec.content.sec.policy.enabled";
    public static final String PARAM_CONTENT_SECURITY_POLICY = "sec.content.sec.policy";
    public static final String PARAM_REFERRER_POLICY_ENABLED = "sec.referrer.policy.enabled";
    public static final String PARAM_REFERRER_POLICY = "sec.referrer.policy";

    // Default values
    public static final String DEFAULT_HSTS_HEADER_NAME = "Strict-Transport-Security";
    public static final String DEFAULT_HSTS_ENABLED = "false";
    public static final String DEFAULT_HSTS_MAXAGE_SECONDS = "0";
    public static final String DEFAULT_HSTS_INCLUDE_SUBDOMAINS = "false";
    public static final String DEFAULT_HSTS_PRELOAD = "false";
    public static final String DEFAULT_ANTI_JACK_CLICKING_HEADER = "X-Frame-Options";
    public static final String DEFAULT_ANTI_JACK_CLICKING_ENABLED = "false";
    public static final String DEFAULT_ANTI_JACK_CLICKING_OPTIONS = "deny,sameorigin,allow-from";
    public static final String DEFAULT_ANTI_JACK_CLICKING_URI = "";
    public static final String DEFAULT_X_CONTENT_TYPE_HEADER = "X-Content-Type-Options";
    public static final String DEFAULT_X_CONTENT_TYPE_ENABLED = "false";
    public static final String DEFAULT_CONTENT_SECURITY_POLICY_HEADER = "Content-Security-Policy";
    public static final String DEFAULT_CONTENT_SECURITY_POLICY_ENABLED = "false";
    public static final String DEFAULT_CONTENT_SECURITY_POLICY = "";
    public static final String DEFAULT_REFERRER_POLICY_ENABLED = "false";
    public static final String DEFAULT_REFERRER_POLICY = "";

    private boolean hstsEnabled;
    private long hstsMaxAgeSeconds;
    private boolean hstsIncludeSubDomains;
    private boolean hstsPreload;
    private boolean antiJackClickingEnabled;
    private String antiJackClickingOptions;
    private String antiJackClickingUri;
    private boolean xContentTypeEnabled;
    private boolean contentSecurityPolicyEnabled;
    private String contentSecurityPolicy;
    private boolean referrerPolicyEnabled;
    private String referrerPolicy;

    public void setFilterConfig(FilterConfig config) {
        this.config = config;
    }

    public FilterConfig getFilterConfig() {
        return config;
    }

    public WebSecurityFilter() throws ServletException {
        parseAndStore(
                DEFAULT_HSTS_ENABLED,
                DEFAULT_HSTS_MAXAGE_SECONDS,
                DEFAULT_HSTS_INCLUDE_SUBDOMAINS,
                DEFAULT_HSTS_PRELOAD,
                DEFAULT_ANTI_JACK_CLICKING_ENABLED,
                DEFAULT_ANTI_JACK_CLICKING_OPTIONS,
                DEFAULT_ANTI_JACK_CLICKING_URI,
                DEFAULT_X_CONTENT_TYPE_ENABLED,
                DEFAULT_CONTENT_SECURITY_POLICY_ENABLED,
                DEFAULT_CONTENT_SECURITY_POLICY,
                DEFAULT_REFERRER_POLICY_ENABLED,
                DEFAULT_REFERRER_POLICY);
    }

    @Override
    public void doFilter(
            ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        if (!(servletRequest instanceof HttpServletRequest)
                || !(servletResponse instanceof HttpServletResponse)) {
            throw new ServletException("WebSecurity doesn't support non-HTTP request or response");
        }

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (hstsEnabled) {
            String declarations = null;
            if (hstsMaxAgeSeconds > 0) {
                declarations = "max-age=" + hstsMaxAgeSeconds + ";";
            }
            if (hstsIncludeSubDomains) {
                declarations += " includeSubdomains;";
            }
            if (hstsPreload) {
                declarations += " preload;";
            }
            response.setHeader(
                    WebSecurityFilter.DEFAULT_HSTS_HEADER_NAME, StringUtils.chop(declarations));
        }

        if (antiJackClickingEnabled) {
            String options = null;
            if (!antiJackClickingOptions.equals("allow-from")) {
                options = antiJackClickingOptions.toUpperCase();
            } else if (antiJackClickingOptions.equals("allow-from")
                    && !antiJackClickingUri.isEmpty()) {
                options = antiJackClickingOptions.toUpperCase() + "'" + antiJackClickingUri + "'";
            }
            response.setHeader(DEFAULT_ANTI_JACK_CLICKING_HEADER, options);
            log.debug("Testing options for anti jack clicking: " + options);
        }

        if (contentSecurityPolicyEnabled) {
            response.setHeader(DEFAULT_CONTENT_SECURITY_POLICY_HEADER, contentSecurityPolicy);
            log.debug("Content-Security-Policy: " + contentSecurityPolicy);
        }

        if (xContentTypeEnabled) {
            log.debug("Content Type Options: " + request.getHeader("Content-Type"));
            response.setHeader(DEFAULT_X_CONTENT_TYPE_HEADER, "nosniff");
        }

        if (referrerPolicyEnabled) {
            log.debug("Referrer Policy: " + referrerPolicy);
            response.setHeader("Referrer-Policy", referrerPolicy);
        }

        filterChain.doFilter(request, response);
    }

    private void parseAndStore(
            final String hstsEnabled,
            final String hstsMaxAgeSeconds,
            final String hstsIncludeSubDomains,
            final String hstsPreload,
            final String antiJackClickingEnabled,
            final String antiJackClickingOptions,
            final String antiJackClickingUri,
            final String xContentTypeEnabled,
            final String contentSecurityPolicyEnabled,
            final String contentSecurityPolicy,
            final String referrerPolicyEnabled,
            final String referrerPolicy)
            throws ServletException {

        setHstsEnabled(hstsEnabled);
        setHstsMaxAgeSeconds(hstsMaxAgeSeconds);
        setHstsIncludeSubDomains(hstsIncludeSubDomains);
        setHstsPreload(hstsPreload);
        setAntiJackClickingEnabled(antiJackClickingEnabled);
        setAntiJackClickingOptions(antiJackClickingOptions);
        setAntiJackClickingUri(antiJackClickingUri);
        setXContentTypeEnabled(xContentTypeEnabled);
        setContentSecurityPolicyEnabled(contentSecurityPolicyEnabled);
        setContentSecurityPolicy(contentSecurityPolicy);
        setReferrerPolicyEnabled(referrerPolicyEnabled);
        setReferrerPolicy(referrerPolicy);
    }

    public void setHstsIncludeSubDomains(String hstsIncludeSubDomains) {
        log.debug("setHstsIncludeSubDomains set to {}", hstsIncludeSubDomains);
        // For any value other then 'true' this will be false.
        this.hstsIncludeSubDomains = Boolean.parseBoolean(hstsIncludeSubDomains);
    }

    public void setHstsPreload(String hstsPreload) {
        log.debug("setHstsPreload set to {}", hstsPreload);
        // For any value other then 'true' this will be false.
        this.hstsPreload = Boolean.parseBoolean(hstsPreload);
    }

    public void setHstsEnabled(String hstsEnabled) {
        log.debug("setHstsEnabled set to {}", hstsEnabled);
        this.hstsEnabled = Boolean.parseBoolean(hstsEnabled);
    }

    public void setHstsMaxAgeSeconds(String hstsMaxAgeSeconds) throws ServletException {
        log.debug("setHstsMaxAgeSeconds set to {}", hstsMaxAgeSeconds);
        try {
            if (!hstsMaxAgeSeconds.isEmpty()) {
                this.hstsMaxAgeSeconds = Long.parseLong(hstsMaxAgeSeconds);
            } else {
                this.hstsMaxAgeSeconds = 0L;
            }
        } catch (NumberFormatException e) {
            throw new ServletException("Unable to parse hstsMaxAgeSeconds", e);
        }
    }

    public void setAntiJackClickingEnabled(String antiJackClickingEnabled) {
        log.debug("setAntiJackClickingEnabled set to {}", antiJackClickingEnabled);
        this.antiJackClickingEnabled = Boolean.parseBoolean(antiJackClickingEnabled);
    }

    public void setAntiJackClickingOptions(String antiJackClickingOptions) {
        log.debug("setAntiJackClickingOptions set to {}", antiJackClickingOptions);
        this.antiJackClickingOptions = antiJackClickingOptions;
    }

    public void setAntiJackClickingUri(String antiJackClickingUri) {
        log.debug("setAntiJackClickingUri set to {}", antiJackClickingUri);
        this.antiJackClickingUri = antiJackClickingUri;
    }

    public void setXContentTypeEnabled(String xContentTypeEnabled) {
        log.debug("setXContentTypeEnabled set to {}", xContentTypeEnabled);
        this.xContentTypeEnabled = Boolean.parseBoolean(xContentTypeEnabled);
    }

    public void setContentSecurityPolicyEnabled(String contentSecurityPolicyEnabled) {
        log.debug("setContentSecurityPolicyEnabled set to {}", contentSecurityPolicyEnabled);
        this.contentSecurityPolicyEnabled = Boolean.parseBoolean(contentSecurityPolicyEnabled);
    }

    public void setContentSecurityPolicy(String contentSecurityPolicy) {
        log.debug("setContentSecurityPolicy set to {}", contentSecurityPolicy);
        this.contentSecurityPolicy = contentSecurityPolicy;
    }

    public void setReferrerPolicyEnabled(String referrerPolicyEnabled) {
        log.debug("setReferrerPolicyEnabled set to {}", referrerPolicyEnabled);
        this.referrerPolicyEnabled = Boolean.parseBoolean(referrerPolicyEnabled);
    }

    public void setReferrerPolicy(String referrerPolicy) {
        log.debug("setReferrerPolicy set to {}", referrerPolicy);
        this.referrerPolicy = referrerPolicy;
    }

    public boolean isHstsPreload() {
        return hstsPreload;
    }

    public boolean isHstsEnabled() {
        return hstsEnabled;
    }

    public long getHstsMaxAgeSeconds() {
        return hstsMaxAgeSeconds;
    }

    public boolean isHstsIncludeSubDomains() {
        return hstsIncludeSubDomains;
    }

    public boolean isAntiJackClickingEnabled() {
        return antiJackClickingEnabled;
    }

    public String getAntiJackClickingOptions() {
        return antiJackClickingOptions;
    }

    public String getAntiJackClickingUri() {
        return antiJackClickingUri;
    }

    public boolean isXContentTypeEnabled() {
        return xContentTypeEnabled;
    }

    public boolean isContentSecurityPolicyEnabled() {
        return contentSecurityPolicyEnabled;
    }

    public String getContentSecurityPolicy() {
        return contentSecurityPolicy;
    }

    public boolean isReferrerPolicyEnabled() {
        return referrerPolicyEnabled;
    }

    public String getReferrerPolicy() {
        return referrerPolicy;
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        setFilterConfig(config);
        init();
    }

    public void init() throws ServletException {
        parseAndStore(
                getInitParameter(PARAM_HSTS_ENABLED, DEFAULT_HSTS_ENABLED),
                getInitParameter(PARAM_HSTS_MAXAGE_SECONDS, DEFAULT_HSTS_MAXAGE_SECONDS),
                getInitParameter(PARAM_HSTS_INCLUDE_SUBDOMAINS, DEFAULT_HSTS_INCLUDE_SUBDOMAINS),
                getInitParameter(PARAM_HSTS_PRELOAD, DEFAULT_HSTS_PRELOAD),
                getInitParameter(
                        PARAM_ANTI_JACK_CLICKING_ENABLED, DEFAULT_ANTI_JACK_CLICKING_ENABLED),
                getInitParameter(
                        PARAM_ANTI_JACK_CLICKING_OPTIONS, DEFAULT_ANTI_JACK_CLICKING_OPTIONS),
                getInitParameter(PARAM_ANTI_JACK_CLICKING_URI, DEFAULT_ANTI_JACK_CLICKING_URI),
                getInitParameter(PARAM_X_CONTENT_TYPE_ENABLED, DEFAULT_X_CONTENT_TYPE_ENABLED),
                getInitParameter(
                        PARAM_CONTENT_SECURITY_POLICY_ENABLED,
                        DEFAULT_CONTENT_SECURITY_POLICY_ENABLED),
                getInitParameter(PARAM_CONTENT_SECURITY_POLICY, DEFAULT_CONTENT_SECURITY_POLICY),
                getInitParameter(PARAM_REFERRER_POLICY_ENABLED, DEFAULT_REFERRER_POLICY_ENABLED),
                getInitParameter(PARAM_REFERRER_POLICY, DEFAULT_REFERRER_POLICY));
    }

    public String getInitParameter(String name) {
        FilterConfig fc = getFilterConfig();
        if (fc == null) {
            throw new IllegalStateException("FilterConfig not initialized");
        }
        return fc.getInitParameter(name);
    }

    private String getInitParameter(String name, String defaultValue) {
        String value = getInitParameter(name);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    @Override
    public void destroy() {}
}
