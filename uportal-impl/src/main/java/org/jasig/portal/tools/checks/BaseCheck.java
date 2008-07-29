/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.tools.checks;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class BaseCheck implements ICheck {
    protected final Log log = LogFactory.getLog(getClass());
    
    private boolean fatal;
    private String description;
    

    /**
     * @return the fatal
     */
    public final boolean isFatal() {
        return fatal;
    }
    /**
     * If a failed check should be marked as fatal
     */
    public final void setFatal(boolean fatal) {
        this.fatal = fatal;
    }

    /**
     * The description of the check
     */
    public final void setDescription(String description) {
        Validate.notNull(description, "description can not be null");
        this.description = description;
    }

    
    /* (non-Javadoc)
     * @see org.jasig.portal.tools.checks.ICheck#doCheck()
     */
    public final CheckResult doCheck() {
        CheckResult checkResult = this.doCheckInternal();
        if (this.fatal && !checkResult.isSuccess() && !checkResult.isFatal()) {
            checkResult = CheckResult.createFatalFailure(checkResult.getMessage(), checkResult.getRemediationAdvice());
        }
        return checkResult;
    }
    
    /**
     * @return The check result
     */
    protected abstract CheckResult doCheckInternal();

    /* (non-Javadoc)
     * @see org.jasig.portal.tools.checks.ICheck#getDescription()
     */
    public final String getDescription() {
        return this.description;
    }
}
