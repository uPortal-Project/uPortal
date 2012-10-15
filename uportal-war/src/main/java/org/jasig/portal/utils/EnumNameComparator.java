package org.jasig.portal.utils;

/**
 * Compare Enums by name
 * 
 * @author Eric Dalquist
 */
public class EnumNameComparator extends ComparableExtractingComparator<Enum<?>, String> {
    public static final EnumNameComparator INSTANCE = new EnumNameComparator();
    
    private EnumNameComparator() {
    }
    
    @Override
    protected String getComparable(Enum<?> o) {
        return o.name();
    }
}
