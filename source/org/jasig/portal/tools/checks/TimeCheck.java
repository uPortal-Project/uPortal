/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.checks;

import java.text.DateFormat;
import java.util.Date;

/**
 * An example ICheck implementation which is just a diagnostic and not an assertion.
 * This check cannot fail, but it still exposes useful information, namely when the check
 * was run.  When included in a batch of checks, allows you to see when the batch
 * was run.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class TimeCheck 
    implements ICheck {

    public CheckResult doCheck() {
        
        String dateTime = DateFormat.getDateTimeInstance().format(new Date());
        return CheckResult.createSuccess("Check ran at " + dateTime);
    }

    public String getDescription() {
        return "This check reports the date and time at which it is run.";
    }

}

