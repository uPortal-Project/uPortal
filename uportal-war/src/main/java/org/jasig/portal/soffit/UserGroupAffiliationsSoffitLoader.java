package org.jasig.portal.soffit;

import java.util.Set;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apereo.portlet.soffit.connector.AbstractSoffitLoader;
import org.apereo.portlet.soffit.connector.ISoffitLoader;
import org.apereo.portlet.soffit.model.v1_0.Role;
import org.apereo.portlet.soffit.model.v1_0.User;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Concrete {@link ISoffitLoader} implementation responsible for loading the
 * payload with user group affiliations from GaP.
 *
 * @author drewwills
 */
@Component
public class UserGroupAffiliationsSoffitLoader extends AbstractSoffitLoader {

    @Value("${org.jasig.portal.security.PersonFactory.guest_user_name:guest}")
    private String guestUserName;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public UserGroupAffiliationsSoffitLoader() {
        super(ISoffitLoader.DEFAULT_LOADER_ORDER + 1);
    }

    @Override
    public void load(org.apereo.portlet.soffit.model.v1_0.Payload soffit,
            RenderRequest renderRequest, RenderResponse renderResponse) {

        logger.debug("Loading group affiliations for REMOTE_USER='{}'", renderRequest.getRemoteUser());

        final String username = renderRequest.getRemoteUser() != null
                ? renderRequest.getRemoteUser()
                : guestUserName;
        final IGroupMember groupMember = GroupService.getGroupMember(username, IPerson.class);
        if (groupMember != null) {
            Set<IEntityGroup> ancestors = groupMember.getAncestorGroups();
            User user = soffit.getUser();
            for (IEntityGroup g : ancestors) {
                final Role role = new Role();
                role.setId(g.getKey());
                role.setName(g.getName());
                user.addRole(role);
            }
            logger.debug("Loaded the following group affiliations for username='{}':  {}", username, user.getRoles());
        }

    }

}
