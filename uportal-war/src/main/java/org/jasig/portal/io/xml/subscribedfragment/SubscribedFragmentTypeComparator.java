package org.jasig.portal.io.xml.subscribedfragment;

import org.jasig.portal.utils.ComparableExtractingComparator;

/**
 * Compares SubscribedFragmentType instances
 * 
 * @author Eric Dalquist
 */
public class SubscribedFragmentTypeComparator extends ComparableExtractingComparator<SubscribedFragmentType, String> {
    public static final SubscribedFragmentTypeComparator INSTANCE = new SubscribedFragmentTypeComparator();
    
    private SubscribedFragmentTypeComparator() {
    }

    @Override
    protected String getComparable(SubscribedFragmentType o) {
        return o.getFragmentOwner();
    }

}
