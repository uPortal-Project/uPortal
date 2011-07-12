package org.jasig.portal.groups.pags.testers;

import org.jasig.portal.groups.pags.IPersonTester;
import org.jasig.portal.security.IPerson;

public class GuestUserTester implements IPersonTester {

    public boolean guestValue;

    public GuestUserTester(String attribute, String guestValue) {
        this.guestValue = Boolean.getBoolean(guestValue);
    }
    
    public boolean test(IPerson person) {
        if (guestValue) {
            return person.isGuest();
        } else {
            return !person.isGuest();
        }
    }

}
