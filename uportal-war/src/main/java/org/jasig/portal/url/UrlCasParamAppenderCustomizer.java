package org.jasig.portal.url;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Customizer to append CAS params to url.
 * @Author  Julien Gribonvald
 * @version $Revision$
 */
public class UrlCasParamAppenderCustomizer implements IAuthUrlCustomizer, InitializingBean {

    /** Logger. */
    private static final Log LOG = LogFactory.getLog(UrlCasParamAppenderCustomizer.class);

    /** Regex condition to apply Customization. */
    private String applyCondition = ".*login\\?service=https?://.*";;

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

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Modifying Url Domain from [%s] to [%s]", url, updatedUrl));
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
