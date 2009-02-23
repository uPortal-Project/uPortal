package org.jasig.portal.url.processing;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.ThemeStylesheetUserPreferences;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.url.IWritableHttpServletRequest;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Required;

/**
 * This implementation of <code>IRequestParameterProcessor</code> sets the value 
 * of specified structure and theme parameters by evaluating RegEx patterns 
 * against the request URL. Use capturing groups to specify the information you 
 * want;  the first capturing group of each expression specifies the value of 
 * the parameter. 
 */
public class UrlRequestParameterProcessor implements IRequestParameterProcessor {
    
    // Static Members.
    private static final Map<String,Pattern> structureParameters = new ConcurrentHashMap<String,Pattern>();
    private static final Map<String,Pattern> themeParameters = new ConcurrentHashMap<String,Pattern>();
    
    // Instance Members.
    private IUserInstanceManager userInstanceManager;
    private final Log log = LogFactory.getLog(getClass());
    
    /*
     * Public API.
     */

    /**
     * Called by Spring to inject the <code>IUserInstanceManager</code> dependency.
     * 
     * @param uim The <code>IUserInstanceManager</code> from which user 
     * preferences are accessed
     */
    @Required
    public void setUserInstanceManager(IUserInstanceManager uim) {
        Validate.notNull(uim);
        this.userInstanceManager = uim;
    }

    /**
     * Called by Spring to set the structure parameters that will be processed 
     * by this instance.  The key of each entry is the name of a parameter, and 
     * the value is a regular expression that will be evaluated against the 
     * request URL.  Call this method only once per instance.
     * 
     * @param params A collection of name/regex pairs that define structure 
     * parameters processed by this instance
     */
    public void setStructureParameters(Map<String,String> params) {
        
        // Assertions.
        if (!structureParameters.isEmpty()) {
            String msg = "Property 'requestUrlParameters' may only be set once.";
            throw new IllegalStateException(msg);
        }
        
        for (Map.Entry<String, String> y : params.entrySet()) {
            structureParameters.put(y.getKey(), Pattern.compile(y.getValue()));
        }
        
    }

    /**
     * Called by Spring to set the theme parameters that will be processed 
     * by this instance.  The key of each entry is the name of a parameter, and 
     * the value is a regular expression that will be evaluated against the 
     * request URL.  Call this method only once per instance.
     * 
     * @param params A collection of name/regex pairs that define theme 
     * parameters processed by this instance
     */
    public void setThemeParameters(Map<String,String> params) {
        
        // Assertions.
        if (!structureParameters.isEmpty()) {
            String msg = "Property 'requestUrlParameters' may only be set once.";
            throw new IllegalStateException(msg);
        }
        
        for (Map.Entry<String, String> y : params.entrySet()) {
            themeParameters.put(y.getKey(), Pattern.compile(y.getValue()));
        }
        
    }

    public boolean processParameters(IWritableHttpServletRequest req, HttpServletResponse res) {
        
        final IUserInstance user = this.userInstanceManager.getUserInstance(req);
        final UserPreferences preferences = user.getPreferencesManager().getUserPreferences();
        
        final String requestUrl = req.getRequestURL().toString();
        
        // Process Structure Parameters...
        final StructureStylesheetUserPreferences ssup = preferences.getStructureStylesheetUserPreferences();
        for (Map.Entry<String, Pattern> y : structureParameters.entrySet()) {
            final Matcher m = y.getValue().matcher(requestUrl);
            if (m.find()) {
                ssup.putParameterValue(y.getKey(), m.group(1));
            }
        }
        
        // Process Theme Parameters...
        final ThemeStylesheetUserPreferences tsup = preferences.getThemeStylesheetUserPreferences();
        for (Map.Entry<String, Pattern> y : themeParameters.entrySet()) {
            final Matcher m = y.getValue().matcher(requestUrl);
            if (m.find()) {
                tsup.putParameterValue(y.getKey(), m.group(1));
            }
        }

        // Signals that processing is complete...
        return true;
        
    }

}
