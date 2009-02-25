/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.tools.checks;

/**
 * Represents a description of a check as well as the result of having performed that
 * check.
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class CheckAndResult {

    /**
     * A String describing what check was performed.
     */
    private final String checkDescription;
    
    /**
     * The result of the check.
     */
    private final CheckResult result;
    
    public CheckAndResult(String checkDescription, CheckResult result) {
        this.checkDescription = checkDescription;
        this.result = result;
    }
    
    /**
     * @return Returns the checkDescription.
     */
    public String getCheckDescription() {
        return this.checkDescription;
    }
    /**
     * @return Returns the result.
     */
    public CheckResult getResult() {
        return this.result;
    }
    
    public boolean isSuccess() {
        return this.result.isSuccess();
    }

    public boolean isFatal() {
        return this.result.isFatal();
    }
}
