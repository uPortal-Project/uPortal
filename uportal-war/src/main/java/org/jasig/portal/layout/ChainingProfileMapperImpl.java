package org.jasig.portal.layout;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.security.IPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class ChainingProfileMapperImpl implements IProfileMapper {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    
    private String defaultProfileName = "default";
    
    public void setDefaultProfileName(String defaultProfileName) {
        this.defaultProfileName = defaultProfileName;
    }
    
    private List<IProfileMapper> subMappers = Collections.<IProfileMapper>emptyList();
    
    public void setSubMappers(List<IProfileMapper> subMappers) {
        this.subMappers = subMappers;
    }

    
    @Override
    public String getProfileFname(IPerson person, HttpServletRequest request) {
        for (IProfileMapper mapper : subMappers) {
            final String fname = mapper.getProfileFname(person, request);
            if (fname != null) {
                return fname;
            }
        }
        return defaultProfileName;
    }

}
