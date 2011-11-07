package org.jasig.portal.layout;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jasig.portal.security.IPerson;

/**
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class SessionAttributeProfileMapperImpl implements IProfileMapper {

    private Map<String,String> mappings = Collections.<String,String>emptyMap();
    private String defaultProfileName = null;
    private String attributeName = "profileKey";
    
    /**
     * Session profile key to database profile fname mappings.
     */
    public void setMappings(Map<String,String> mappings) {
        this.mappings = mappings;
    }
    
    /**
     * Default profile name to return if no match is found, defaults to <code>null</code>.
     */
    public void setDefaultProfileName(String defaultProfileName) {
        this.defaultProfileName = defaultProfileName;
    }

    /**
     * 
     * @param attributeName
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }
    
    @Override
    public String getProfileFname(IPerson person, HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        
        final String requestedProfileKey = (String) session.getAttribute(attributeName);
        if (requestedProfileKey != null) {
            final String profileName = mappings.get(requestedProfileKey);
            if (profileName != null) {
                return profileName;
            }
        }

        return defaultProfileName;
    }

}
