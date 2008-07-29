/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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

