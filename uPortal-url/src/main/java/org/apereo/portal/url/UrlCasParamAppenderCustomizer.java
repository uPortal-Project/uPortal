package org.apereo.portal.url;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/** Customizer to append CAS params to service url. */
public class UrlCasParamAppenderCustomizer implements IAuthUrlCustomizer, InitializingBean {

    /** Logger. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Regex condition to apply Customization. */
    private String applyCondition = ".*login\\?service=https?://.*";

    /** Param name with equal and value to append to url. */
    private String paramToAppend;

    public void setApplyCondition(final String applyCondition) {
        this.applyCondition = applyCondition;
    }

    public void setParamToAppend(final String paramToAppend) {
        this.paramToAppend = paramToAppend;
    }

    public boolean supports(final HttpServletRequest request, final String url) {
        return url != null && url.matches(applyCondition);
    }

    public String customizeUrl(final HttpServletRequest request, final String url) {
        if (url != null && !url.isEmpty() && supports(request, url)) {
            final String updatedUrl = url + "&" + paramToAppend;

            if (logger.isDebugEnabled()) {
                logger.debug(
                        String.format("Modifying Url Domain from [%s] to [%s]", url, updatedUrl));
            }
            return updatedUrl;
        }
        return url;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(this.applyCondition, "No applyCondition supplied !");
        Assert.hasText(this.paramToAppend, "No paramToAppend supplied !");
    }
}
