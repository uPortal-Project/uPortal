/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.checks;

import org.springframework.core.SpringVersion;

/**
 * Test that the spring.jar is available on the classpath.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class SpringPresenceCheck 
    implements ICheck {

    public CheckResult doCheck() {
        try {
            String springVersion = SpringVersion.getVersion();
            return CheckResult.createSuccess("Spring jar version [" + springVersion + "] successfully found.");
        } catch (Throwable t) {
            return CheckResult.createFailure("Spring jar doesn't appear to be present on runtime classpath as evidenced by inability to get Spring version.", 
                    "Include spring.jar in the /lib/ directory of the uPortal webapplication.");
        }
    }

    public String getDescription() {
        return "Checks that spring.jar is present on runtime classpath by attempting to" +
                " invoke Spring's static getVersion() facility.";
    }

}
