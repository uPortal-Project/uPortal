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
