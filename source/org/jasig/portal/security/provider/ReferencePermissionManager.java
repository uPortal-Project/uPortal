/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal.security.provider;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.jasig.portal.AuthorizationException;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.security.Permission;
import org.jasig.portal.security.PermissionManager;
import org.jasig.portal.services.LogService;


/**
 * @author Bernie Durfee (bdurfee@interactivebusiness.com)
 * @deprecated As of uPortal 2.0, replaced by {@link PermissionManagerImpl}
 */
public class ReferencePermissionManager extends PermissionManager {

  /**
   * This constructor ensures that the PermissionManager will be created with an owner specified
   * @param owner
   */
  public ReferencePermissionManager (String owner) {
    // Make sure to call the constructor of the PermissionManager class
    super(owner);
  }

  /**
   * Add a new Permission to the system.
   * @param newPermission
   */
  public void setPermission (Permission newPermission) {
    Connection connection = RDBMServices.getConnection();
    try {
      StringBuffer updateStatement = new StringBuffer(1000);
      updateStatement.append("INSERT INTO UP_PERMISSION (OWNER, PRINCIPAL, ACTIVITY, TARGET, PERMISSION_TYPE, EFFECTIVE, EXPIRES) VALUES (");
      updateStatement.append("'" + m_owner + "',");
      if (newPermission.getPrincipal() != null) {
        updateStatement.append("'" + newPermission.getPrincipal() + "',");
      }
      else {
        updateStatement.append("'*',");
      }
      if (newPermission.getActivity() != null) {
        updateStatement.append("'" + newPermission.getActivity() + "',");
      }
      else {
        updateStatement.append("'*',");
      }
      if (newPermission.getTarget() != null) {
        updateStatement.append("'" + newPermission.getTarget() + "',");
      }
      else {
        updateStatement.append("'*',");
      }
      if (newPermission.getType() != null) {
        updateStatement.append("'" + newPermission.getType() + "',");
      }
      else {
        updateStatement.append("'*',");
      }

      if (newPermission.getEffective() != null) {
        updateStatement.append(RDBMServices.sqlTimeStamp(newPermission.getEffective()) + ",");
      } else {
        updateStatement.append("null,");
      }

      if (newPermission.getExpires() != null) {
        updateStatement.append(RDBMServices.sqlTimeStamp(newPermission.getExpires()) + ",");
      } else {
        updateStatement.append("null");
      }
      updateStatement.append(")");
      Statement statement = connection.createStatement();
      try {
        LogService.log(LogService.DEBUG, "ReferencePermissionManager::setPermission() :" + updateStatement.toString());
        statement.executeUpdate(updateStatement.toString());
      } finally {
        statement.close();
      }
    } catch (Exception e) {
      // Log the exception
      LogService.log(LogService.ERROR, e);
    } finally {
      RDBMServices.releaseConnection(connection);
    }
  }

  /**
   * Add a new set of Permission objects to the system.
   * @param newPermissions
   */
  public void setPermissions (Permission[] newPermissions) {
    for (int i = 0; i < newPermissions.length; i++) {
      setPermission(newPermissions[i]);
    }
  }

  /**
   * Retrieve an array of Permission objects based on the given parameters. Any null parameters
   * will be ignored. So to retrieve a set of Permission objects for a given principal you would call
   * this method like pm.getPermissions('principal name', null, null, null)
   * @param principal
   * @param activity
   * @param target
   * @param type
   * @return array of Permission objects
   * @exception AuthorizationException
   */
  public Permission[] getPermissions (String principal, String activity, String target, String type) throws AuthorizationException {
    if (principal == null) {
      principal = "*";
    }
    if (activity == null) {
      activity = "*";
    }
    if (target == null) {
      target = "*";
    }
    if (type == null) {
      type = "*";
    }
    Connection connection = RDBMServices.getConnection();
    try {
      // Create the select statement to retrieve the permissions
      StringBuffer queryString = new StringBuffer(255);
      queryString.append("SELECT * FROM UP_PERMISSION WHERE OWNER = '");
      queryString.append(m_owner.toUpperCase());
      queryString.append("'");
      queryString.append(" AND PRINCIPAL = '");
      queryString.append(principal.toUpperCase());
      queryString.append("'");
      queryString.append(" AND ACTIVITY = '");
      queryString.append(activity.toUpperCase());
      queryString.append("'");
      queryString.append(" AND TARGET = '");
      queryString.append(target.toUpperCase());
      queryString.append("'");
      queryString.append(" AND PERMISSION_TYPE = '");
      queryString.append(type.toUpperCase());
      queryString.append("'");

      // Create a JDBC statement to the database
      Statement statement = connection.createStatement();
      try {

        // DEBUG
        LogService.log(LogService.DEBUG, "ReferencePermissionManager::getPermissions() :" + queryString.toString());

        // Execute the query
        ResultSet rs = statement.executeQuery(queryString.toString());
        try {
          // Create an array list to store the retrieved permissions
          ArrayList permissions = new ArrayList();
          while (rs.next()) {
            Permission permission = new ReferencePermission(m_owner);
            permission.setPrincipal(rs.getString("PRINCIPAL_KEY"));
            permission.setActivity(rs.getString("ACTIVITY"));
            permission.setTarget(rs.getString("TARGET"));
            permission.setType(rs.getString("PERMISSION_TYPE"));
            permission.setEffective(rs.getDate("EFFECTIVE"));
            permission.setExpires(rs.getDate("EXPIRES"));
            permissions.add(permission);
          }

          // Return the array of permissions
          return  ((Permission[])permissions.toArray(new Permission[0]));
        } finally {
          rs.close();
        }
      } finally {
        statement.close();
      }
    } catch (Exception e) {
      throw  new AuthorizationException(e.getMessage());
    } finally {
      RDBMServices.releaseConnection(connection);
    }
  }
}



