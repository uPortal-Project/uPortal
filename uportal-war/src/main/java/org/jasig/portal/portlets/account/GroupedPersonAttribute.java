package org.jasig.portal.portlets.account;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GroupedPersonAttribute {

    private final String displayName;
    private final Set<String> attributeNames;
    private final List<Object> values;
    
    public GroupedPersonAttribute(final String displayName, final List<Object> values) {
        this.displayName = displayName;
        this.values = values;
        this.attributeNames = new TreeSet<String>();
    }

    public GroupedPersonAttribute(final String displayName, final List<Object> values, final String attributeName) {
        this(displayName, values);
        this.attributeNames.add(attributeName);
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<String> getAttributeNames() {
        return attributeNames;
    }

    public List<Object> getValues() {
        return values;
    }

}
