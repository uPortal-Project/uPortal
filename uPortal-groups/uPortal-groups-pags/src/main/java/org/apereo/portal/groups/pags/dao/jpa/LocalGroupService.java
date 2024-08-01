package org.apereo.portal.groups.pags.dao.jpa;

import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupDefinition;
import org.springframework.stereotype.Service;

@Service
public class LocalGroupService {
    public static final String SERVICE_NAME_LOCAL = "local";

    public void addMember(IEntityGroup parent, IPersonAttributesGroupDefinition result) {
        IEntityGroup member =
                org.apereo.portal.services.GroupService.findGroup(
                        result.getCompositeEntityIdentifierForGroup().getKey());
        if (member == null) {
            String msg =
                    "The specified group was created, but is not present in the store:  "
                            + result.getName();
            throw new RuntimeException(msg);
        }
        parent.addChild(member);
        parent.updateMembers();
    }
}
