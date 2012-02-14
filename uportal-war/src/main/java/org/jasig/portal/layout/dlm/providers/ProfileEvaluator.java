package org.jasig.portal.layout.dlm.providers;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.EvaluatorFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ProfileEvaluator extends Evaluator {

    @Transient
    private IPortalRequestUtils portalRequestUtils;
    
    @Autowired(required = true)
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }
    
    @Transient
    private IUserInstanceManager userInstanceManager;
    
    @Autowired(required = true)
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Column(name = "PROFILE_FNAME")
    protected String profileFname;

    public ProfileEvaluator() { }
    
    public ProfileEvaluator(String profileFname) {
        this.profileFname = profileFname;
    }
    
    @Override
    public boolean isApplicable(IPerson person) {

        final HttpServletRequest request = portalRequestUtils.getCurrentPortalRequest();
        final IUserInstance userInstance = userInstanceManager.getUserInstance(request);
        final IUserProfile profile = userInstance.getPreferencesManager().getUserProfile();
        
        return profileFname.equals(profile.getProfileFname());
    }

    @Override
    public void toElement(Element parent) {
        // Assertions.
        if (parent == null) {
            String msg = "Argument 'parent' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        Element rslt = DocumentHelper.createElement("profile");
        rslt.addAttribute("fname", this.profileFname);
        parent.add(rslt);
    }

    @Override
    public Class<? extends EvaluatorFactory> getFactoryClass() {
        return ProfileEvaluatorFactory.class;
    }

    @Override
    public String getSummary() {
        return "(PROFILE IS '" + this.profileFname + "')";
    }

}
