/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.channels.cusermanager;

import org.jasig.portal.security.IPerson;

/**
 * <p>Title: IDataHander.java</p>
 * <p>Description: Interface for data gets/sets</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Cornell University</p>
 * @author <a href='mailto:smb1@cornell.edu?subject=CUserManager Channel'>Steve Barrett</a>
 * @version 1.0
 * <br><br>
 * This interface uses the default org.jasig.portal.security.provider.PersonImpl of
 * the IPerson object to send and recieve data arrays.  These are not functional
 * IPerson objects.  Rather, the attributes of a person in the IPerson object are
 * the key/value pairs of the column/values contained within the up_person_dir
 * table.
 */

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