package org.jasig.portal.io.xml.user;

import org.jasig.portal.utils.ComparableExtractingComparator;

/**
 * Compare AttributeComparator instances
 * 
 * @author Eric Dalquist
 */
public class AttributeComparator extends ComparableExtractingComparator<Attribute, String> {
    public static final AttributeComparator INSTANCE = new AttributeComparator();
    
    private AttributeComparator() {
    }
    
    @Override
    protected String getComparable(Attribute o) {
        return o.getName();
    }
}
