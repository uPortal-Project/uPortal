package org.apereo.portal.groups.pags.dao.jpa;

import java.util.HashSet;
import java.util.Set;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupDefinition;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupDefinitionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PagsGroupService {
    @Autowired private IPersonAttributesGroupDefinitionDao pagsGroupDefDao;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void addMember(
            IPersonAttributesGroupDefinition parentDef, IPersonAttributesGroupDefinition result) {

        Set<IPersonAttributesGroupDefinition> members = new HashSet<>(parentDef.getMembers());
        members.add(result);
        parentDef.setMembers(members);
        pagsGroupDefDao.updatePersonAttributesGroupDefinition(parentDef);
    }
}
