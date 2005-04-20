
package org.jasig.portal.layout.dlm;

import org.jasig.portal.security.IPerson;

public interface Evaluator
{
    public static final String RCS_ID = "@(#) $Header$";

    public boolean isApplicable( IPerson person );
}
