/*
 * Created on Oct 6, 2004
 *
 * Copyright(c) Yale University, Oct 6, 2004.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portal.services.persondir;

import java.util.Map;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.RestrictedPerson;

/**
 * Interface for services to be provided by PersonDirectory implementations.
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public interface IPersonDirectory {

    /**
     * For a given user, return a Map from attribute names to values.
     * 
     * The expected implementation is that values will be Strings or Lists of
     * Strings, but there is nothing to prevent other implementations -- except
     * ensuring that your clients of this class will understand these other
     * typed values.
     * 
     * While clients of this method may not understand attribute values other
     * than Strings, they should treat attribute values they cannot understand
     * as equivalent to the attributes not being present at all. That is, do
     * *not* iterate over this Map casting it to String -- by doing so you will
     * be forclosing some other client which would like to obtain some Object as
     * an attribute.
     * 
     * @param username -
     *                name of user for whom information is desired
     * @return Map from String attribute names to values
     */
    public Map getUserDirectoryInformation(String username);

    /**
     * For a given user, obtain attributes for the user and then insert those
     * attributes into the given IPerson.  SIDE EFFECT: modifies the person
     * argument, adding the attributes for the given uid.
     * 
     * @param uid -
     *                unique identifier for user.
     * @param person -
     *                IPerson representing the user
     */
    public void getUserDirectoryInformation(String uid, IPerson person);

    /**
     * Returns a reference to a restricted IPerson represented by the supplied
     * user ID. The restricted IPerson allows access to person attributes
     * (which will be populated by this class), but not the security context.
     * 
     * @param uid
     *                the user ID
     * @return the corresponding person, restricted so that its security context
     *            is inaccessible
     */
    public RestrictedPerson getRestrictedPerson(String uid);

    /**
     * Suggests to the IPersonDirectory implementation that it update its cache
     * of IPersons with the uid->IPerson mapping supplied.
     * @param uid - user identifier
     * @param person - a fresh IPerson to be cached.
     */
    public void cachePerson(String uid, IPerson person);

}

/*
 * PersonDirectory.java
 * 
 * Copyright (c) Oct 6, 2004 Yale University. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY DISCLAIMED. IN NO EVENT SHALL
 * YALE UNIVERSITY OR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED, THE COSTS OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Redistribution and use of this software in source or binary forms, with or
 * without modification, are permitted, provided that the following conditions
 * are met.
 * 
 * 1. Any redistribution must include the above copyright notice and disclaimer
 * and this list of conditions in any related documentation and, if feasible, in
 * the redistributed software.
 * 
 * 2. Any redistribution must include the acknowledgment, "This product includes
 * software developed by Yale University," in any related documentation and, if
 * feasible, in the redistributed software.
 * 
 * 3. The names "Yale" and "Yale University" must not be used to endorse or
 * promote products derived from this software.
 */