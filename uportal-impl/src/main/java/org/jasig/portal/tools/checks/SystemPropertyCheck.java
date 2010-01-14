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

import java.util.Comparator;

import org.jasig.portal.utils.compare.EqualsComparator;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class SystemPropertyCheck extends BaseCheck {
    private final String property;
    private final String expected;
    private final Comparator<String> comparator;
    private String remediation;
    
    public SystemPropertyCheck(String property, String expected) {
        this(property, expected, new EqualsComparator());
    }
    
    public SystemPropertyCheck(String property, String expected, Comparator<String> comparator) {
        this.property = property;
        this.expected = expected;
        this.comparator = comparator;
        
        this.setDescription("Checking system property '" + this.property + "' against '" + this.expected + "' using " + this.comparator);
        this.setRemediation("");
    }
    
    /**
     * @return the remediation
     */
    public String getRemediation() {
        return remediation;
    }
    /**
     * @param remediation the remediation to set
     */
    public void setRemediation(String remediation) {
        this.remediation = remediation;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.tools.checks.BaseCheck#doCheckInternal()
     */
    @Override
    protected CheckResult doCheckInternal() {
        final String sysProp = System.getProperty(this.property);
        final int compareResult = this.comparator.compare(sysProp, this.expected);
        if (compareResult == 0) {
            return CheckResult.createSuccess("System property '" + this.property + "' passed comparison to '" + this.expected + "' using " + this.comparator);
        }

        return CheckResult.createFailure("System property '" + this.property + "' FAILED comparison to '" + this.expected + "' using " + this.comparator, this.remediation);
    }
}

