/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.checks;

import java.text.DateFormat;
import java.util.Date;

/**
 * An example ICheck implementation which is just a diagnostic and not an assertion.
 * This check cannot fail, but it still exposes useful information.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class TimeCheck 
    implements ICheck {

    /* (non-Javadoc)
     * @see org.jasig.portal.tools.checks.ICheck#doCheck()
     */
    public CheckResult doCheck() {
        
        String dateTime = DateFormat.getDateTimeInstance().format(new Date());
        return CheckResult.createSuccess("Check ran at " + dateTime);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.tools.checks.ICheck#getDescription()
     */
    public String getDescription() {
        return "This check reports the date and time at which it is run.";
    }

}

