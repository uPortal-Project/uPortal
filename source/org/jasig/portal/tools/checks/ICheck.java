/*
 * Created on Feb 25, 2005
 *
 * Copyright(c) Yale University, Feb 25, 2005.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portal.tools.checks;

/**
 * A single runtime check that to be performed to validate an application deployment.
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public interface ICheck {

    /**
     * Perform an arbitrary check.  The result of this method should be 
     * a CheckResult representing either a success or failure of the check.  
     * 
     * Implementations should catch their own exceptions and translate them into
     * CheckResults representing failures, since the intent of this API is to translate
     * arcane deployment issues into friendly results with remediation messages.
     * 
     * However, the
     * implementation of this method may throw any RuntimeException, and 
     * clients must cope with such exceptions.  Cope with probably means translate
     * it into a CheckResult representing a failure of this check.  The
     * client of a Check implementation will be less effective in translating a thrown Throwable
     * into an intelligent CheckResult representing a failure than the Check would have been
     * in doing this itself.
     * 
     * @return a CheckResult representing the result of the check
     */
    public CheckResult doCheck();
    
    /**
     * Get a description of what it is the check is intended to check.
     * Implementations of this method must always return a non-null String and
     * should not throw anything.
     * @return a description of what it is that the check checks.
     */
    public String getDescription();
    
}


/* Check.java
 * 
 * Copyright (c) Feb 25, 2005 Yale University.  All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY
 * DISCLAIMED. IN NO EVENT SHALL YALE UNIVERSITY OR ITS EMPLOYEES BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED, THE COSTS OF
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 * 
 * Redistribution and use of this software in source or binary forms,
 * with or without modification, are permitted, provided that the
 * following conditions are met.
 * 
 * 1. Any redistribution must include the above copyright notice and
 * disclaimer and this list of conditions in any related documentation
 * and, if feasible, in the redistributed software.
 * 
 * 2. Any redistribution must include the acknowledgment, "This product
 * includes software developed by Yale University," in any related
 * documentation and, if feasible, in the redistributed software.
 * 
 * 3. The names "Yale" and "Yale University" must not be used to endorse
 * or promote products derived from this software.
 */