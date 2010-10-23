package org.jasig.portal.portlets.search;

import java.util.Comparator;

import org.jasig.services.persondir.IPersonAttributes;

/**
 * DisplayNameComparator allows the sorting of people by display name.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class DisplayNameComparator implements Comparator<IPersonAttributes> {

    @Override
    public int compare(IPersonAttributes o1, IPersonAttributes o2) {
        Object name1 = o1.getAttributeValue("displayName");
        Object name2 = o2.getAttributeValue("displayName");
        if (name1 == null && name2 == null) {
            return 0;
        } else if (name1 == null) {
            return -1;
        } else if (name2 == null) {
            return 1;
        } else {
            return name1.toString().compareTo(name2.toString());
        }
    }

}
