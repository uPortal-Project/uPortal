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

package org.jasig.portal.channels.cusermanager;

import org.jasig.portal.security.IPerson;

/**
 * This interface uses the default org.jasig.portal.security.provider.PersonImpl of
 * the IPerson object to send and recieve data arrays.  These are not functional
 * IPerson objects.  Rather, the attributes of a person in the IPerson object are
 * the key/value pairs of the column/values contained within the up_person_dir
 * table.
 * 
 * @author smb1@cornell.edu
 * @version $Revision$ $Date$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface IDataHandler {

  public IPerson[] getAllUsers() throws Exception;
  public IPerson[] getAllUsersLike( String SearchString ) throws Exception;
  public IPerson getUser( String UID ) throws Exception;

  public void setUserInformation( IPerson AnIndividual ) throws Exception;
  public void addUser( IPerson AnIndividual ) throws Exception;
  public void removeUser( IPerson AnIndividual ) throws Exception;

  /** OriginalPassword is null if called in "UserManager" mode. */
  public void setUserPassword( IPerson AnIndividual, String OriginalPassword ) throws Exception;
}// eoi