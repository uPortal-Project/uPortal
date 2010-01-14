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

package org.jasig.portal.security.provider;

/**
 * A store for basic account information; username, passwords, etc.
 * Note: this interface is particular to the reference security provider
 * and is not part of the core portal interfaces.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version $Revision$
 */

public interface IAccountStore {

    /**
     * Obtain account information for a given username
     *
     * @param username a <code>String</code> value
     * @return a <code>String[]</code> array containing (in the order given):
     * md5 password, first name, last name.
     * @exception Exception if an error occurs
     */
    public String[] getUserAccountInformation(String username) throws Exception;
}
