package org.jasig.portal.portlets.account;

import java.util.Comparator;

public class GroupedPersonAttributeByNameComparator implements Comparator<GroupedPersonAttribute> {

    @Override
    public int compare(GroupedPersonAttribute attribute1, GroupedPersonAttribute attribute2) {
        return attribute1.getDisplayName().compareTo(attribute2.getDisplayName());
    }

}
