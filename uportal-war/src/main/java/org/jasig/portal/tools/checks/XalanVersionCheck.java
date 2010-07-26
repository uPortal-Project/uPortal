/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.tools.checks;

import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import org.apache.commons.lang.SystemUtils;

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
            	String jarLocation = "";
                String locationInfo = "";
                final ProtectionDomain protectionDomain = org.apache.xalan.Version.class.getProtectionDomain();
                if (protectionDomain != null) {
                    final CodeSource codeSource = protectionDomain.getCodeSource();
                    if (codeSource != null) {
                        final URL location = codeSource.getLocation();
                        jarLocation = location.toString();
                        locationInfo = " loaded from '" + jarLocation + "'";
                    }
                }
                StringBuffer message = new StringBuffer();
                message.append("Xalan version [").append(versionFound).append("]");
                message.append(locationInfo).append(" is present, rather than the desired version [");
                message.append(this.desiredVersion).append("]");
                StringBuffer remediation = new StringBuffer();
                if(SystemUtils.IS_OS_MAC_OSX && jarLocation.endsWith("14compatibility.jar")) {
                	remediation.append("Running uPortal on Mac OS X requires you to disable a library that is included in its Java distribution; rename or delete ")
                		.append(jarLocation);
                } else {
                	remediation.append("uPortal includes the appropriate version of Xalan; please remove the file ")
                		.append(jarLocation)
                		.append(" from the classpath.");
                }
                result = CheckResult.createFailure(message.toString(), remediation.toString());
            }
        } catch (NoClassDefFoundError ncdfe) {
            result = CheckResult.createFailure("Class org.apache.xalan.Version could not be found.", "Install the xalan jar corresponding to [" + this.desiredVersion + "] in the /lib/endorsed directory of the JRE.");
        }
        
        return result;
    }
}
