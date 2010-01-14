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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Runs a list of {@see ICheck}s with type checking and exception handling to ensure every
 * check gets run and that the {@see #doChecks()} will always return correctly.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class SafeCheckRunner implements ICheckRunner {
    private static final String CHECK_FAILED_DESCRIPTION = "Check failed to provide a description.";
    protected final Log logger = LogFactory.getLog(this.getClass());
    private List<?> checks = Collections.emptyList();
    

    /**
     * @return the checks
     */
    public List<?> getChecks() {
        return this.checks;
    }
    /**
     * @param checks the checks to set
     */
    public void setChecks(List<?> checks) {
        Validate.notNull(checks, "The List of IChecks cannot be null");

        this.checks = checks;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.tools.checks.ICheckRunner#doChecks()
     */
    public List<CheckAndResult> doChecks() {
        final List<CheckAndResult> results = new ArrayList<CheckAndResult>(this.checks.size());

        for (final Object check : checks) {
            if (check instanceof ICheck) {
                final CheckAndResult checkResult = this.executeCheck((ICheck) check);
                results.add(checkResult);
            }
            else {
                // entry in list of checks was not actually an instance of ICheck.
                // log this annoyance.
                final String classWas;
                if (check == null) {
                    classWas = "null";
                }
                else {
                    classWas = check.getClass().getName();
                }

                CheckResult entryNotAnICheck = CheckResult
                        .createFailure("Entry in list of checks did not implement ICheck.  "
                                + "It was an instance of [" + classWas + "]",
                                "Fix the list of checks to include only instances of ICheck.");

                CheckAndResult notAnEntryCheckAndResult = new CheckAndResult(
                        "Check whether entries in the list of checks we're going to execute implement the required ICheck interface.",
                        entryNotAnICheck);
                
                results.add(notAnEntryCheckAndResult);
            }
        }

        return results;
    }

    /**
     * Helper method to safely execute a given check.
     * @param check
     * @return
     */
    private CheckAndResult executeCheck(ICheck check) {
        // default the check description in case the check fails to provide one
        String checkDescription = CHECK_FAILED_DESCRIPTION;

        try {
            checkDescription = check.getDescription();
            CheckResult result = check.doCheck();
            return new CheckAndResult(checkDescription, result);
        }
        catch (Throwable t) {
            // why catch throwable?  Because no matter what goes wrong, we
            // don't want any check to be able to break this CheckRunner.

            // we especially log here to get this stack trace in the log
            logger.error("Check failed by throwing.", t);

            String message = "Check failed by throwing exception. " + t;

            String remediation = "Since the check failed by throwing an exception, we cannot advise any particular solution.";

            CheckResult result = CheckResult.createFailure(message, remediation);

            return new CheckAndResult(checkDescription, result);

        }
    }
}
