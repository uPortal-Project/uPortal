
package org.jasig.portal.layout.dlm;

import org.w3c.dom.Document;
import org.jasig.portal.UserProfile;
import org.jasig.portal.security.IPerson;

public interface LayoutDecorator
{
    public static final String RCS_ID = "@(#) $Header$";

    public void decorate (Document layout,
                          IPerson person,
                          UserProfile profile)
        throws Exception;
}
