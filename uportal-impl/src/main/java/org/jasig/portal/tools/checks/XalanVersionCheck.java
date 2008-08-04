/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.checks;

import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * Checks the Xalan version against a configured value.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class XalanVersionCheck extends BaseCheck {

    /**
     * The version for which we will check.
     */
    private final String desiredVersion;
    
    public XalanVersionCheck(String desiredVersion) {
        if (desiredVersion == null) {
            throw new IllegalArgumentException("XalanVersionCheck requires a particular version String to check for.");
        }
        this.desiredVersion = desiredVersion;
        
        this.setDescription("Check that the version of Xalan present is [" + this.desiredVersion + "]");
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.tools.checks.BaseCheck#doCheckInternal()
     */
    @Override
    protected CheckResult doCheckInternal() {
        CheckResult result;
        
        try {
            String versionFound = org.apache.xalan.Version.getVersion();
            if (this.desiredVersion.equals(versionFound)){
                result = CheckResult.createSuccess("Xalan version [" + versionFound + "] is present.");
            } else {
                String locationInfo = "";
                final ProtectionDomain protectionDomain = org.apache.xalan.Version.class.getProtectionDomain();
                if (protectionDomain != null) {
                    final CodeSource codeSource = protectionDomain.getCodeSource();
                    if (codeSource != null) {
                        final URL location = codeSource.getLocation();
                        locationInfo = " loaded from '" + location + "'";
                    }
                }
                
                result = CheckResult.createFailure("Xalan version [" + versionFound + "]" + locationInfo + " is present, rather than the desired version [" + this.desiredVersion + "]", "Install the Xalan jar corresponding to [" + this.desiredVersion + "] in the /endorsed/lib/ directory of the JRE.");
            }
        } catch (NoClassDefFoundError ncdfe) {
            result = CheckResult.createFailure("Class org.apache.xalan.Version could not be found.", "Install the xalan jar corresponding to [" + this.desiredVersion + "] in the /lib/endorsed directory of the JRE.");
        }
        
        return result;
    }
}
