package org.jasig.portal.rendering.predicates;

import com.google.common.base.Predicate;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;

/**
 * Predicate determining whether the given request represents a request in the context of a profile
 * matching the configured profile fname.
 * @since uPortal 4.2
 */
public class ProfileFNamePredicate
    implements Predicate<HttpServletRequest> {

    // auto-wired
    private IUserInstanceManager userInstanceManager;

    // dependency-injected
    private String profileFNameToMatch;


    @Override
    public boolean apply(final HttpServletRequest request) {

        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);

        final String profileFName = userInstance.getPreferencesManager().getUserProfile().getProfileFname();

        if (profileFNameToMatch.equals(profileFName)) {
            return true;
        }

        return false;
    }

    @Autowired
    public void setUserInstanceManager(final IUserInstanceManager userInstanceManager) {
        Assert.notNull(userInstanceManager);
        this.userInstanceManager = userInstanceManager;
    }

    @Required
    public void setProfileFNameToMatch(final String profileFNameToMatch) {
        this.profileFNameToMatch = profileFNameToMatch;
    }

}
