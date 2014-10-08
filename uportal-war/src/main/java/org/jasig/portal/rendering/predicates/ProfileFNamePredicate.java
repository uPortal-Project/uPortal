package org.jasig.portal.rendering.predicates;

import com.google.common.base.Predicate;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // auto-wired
    private IUserInstanceManager userInstanceManager;

    // dependency-injected
    private String profileFNameToMatch;


    @Override
    public boolean apply(final HttpServletRequest request) {

        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);

        final String profileFName = userInstance.getPreferencesManager().getUserProfile().getProfileFname();

        // used for logging
        final String username = userInstance.getPerson().getUserName();

        if (profileFNameToMatch.equals(profileFName)) {

            logger.debug("User {} does have profile with matching fname {}.",
                    username,
                    profileFName );

            return true;
        }

        logger.debug("Request for user {} presents profile fname {} which does not match configured profile fname {}.",
                username,
                profileFName,
                profileFNameToMatch);

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

    @Override
    public String toString() {
        return "Predicate: true where profile fname is " + this.profileFNameToMatch + ".";
    }

}
