/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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
 */

package org.jasig.portal.security.provider;

import java.util.Vector;
import java.util.Properties;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

import org.jasig.portal.SmartCache;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IRole;
import org.jasig.portal.security.RoleImpl;
import org.jasig.portal.security.PersonImpl;

import org.jasig.portal.security.IAuthorization;
import org.jasig.portal.security.PortalSecurityException;

import org.jasig.portal.GenericPortalBean;
import org.jasig.portal.RdbmServices;
import org.jasig.portal.Logger;

/**
 * @author Bernie Durfee
 */
public class ReferenceAuthorization implements IAuthorization
{
  // Clear the caches every 10 seconds
  protected static SmartCache userRolesCache = new SmartCache(300);
  protected static SmartCache chanRolesCache = new SmartCache(300);
  protected static String s_channelPublisherRole = null;

  static
  {
    try
    {
      // Find our properties file and open it
      String filename = GenericPortalBean.getPortalBaseDir() + "properties" + File.separator + "security.properties";
      File propFile = new File(filename);

      Properties securityProps = new Properties();

      try
      {
        securityProps.load (new FileInputStream(propFile));
        s_channelPublisherRole = securityProps.getProperty("channelPublisherRole");
      }
      catch(IOException e)
      {
        Logger.log(Logger.ERROR, new PortalSecurityException(e.getMessage()));
      }
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
  }

  public ReferenceAuthorization()
  {
  }

  // For the publish mechanism to use
  public boolean isUserInRole(IPerson person, IRole role)
  {

    if(person == null || role == null)
    {
      return(false);
    }

    String sUserName = person.getID();

    if(sUserName == null)
    {
      return(false);
    }

    RdbmServices rdbmServices = null;
    Connection conn = null;

    try
    {
      rdbmServices = new RdbmServices();

      conn = rdbmServices.getConnection();
      Statement stmt = conn.createStatement();

      String query = "select * from portal_user_roles where " +
                     "upper(user_name)=upper('" + sUserName + "') and " +
                     "upper(role)=upper('" + (String)role.getRoleTitle() + "')";

      ResultSet rs = stmt.executeQuery(query);

      if (rs.next())
      {
        return(true);
      }
      else
      {
        return(false);
      }
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
      return(false);
    }
    finally
    {
      rdbmServices.releaseConnection(conn);
    }
  }

  public Vector getAllRoles()
  {
    RdbmServices rdbmService = new RdbmServices();
    Connection con = null;
    ResultSet rs = null;
    Statement stmt = null;

    try
    {
      con = rdbmService.getConnection();
      stmt = con.createStatement();

      String sQuery = "SELECT ROLE, DESCR FROM PORTAL_ROLES";
      Logger.log(Logger.DEBUG, sQuery);

      rs = stmt.executeQuery(sQuery);

      Vector roles = new Vector();
      RoleImpl roleImpl = null;

      // Add all of the roles in the portal database to to the vector
      while(rs.next())
      {
        roleImpl = new RoleImpl(rs.getString("ROLE"));
        roles.add(roleImpl);
      }

      stmt.close();

      return(roles);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
      return(null);
    }
    finally
    {
      rdbmService.releaseConnection(con);
    }
  }

  public int setChannelRoles(int channelID, Vector roles)
  {
    // Don't do anything if no roles were passed in
    if(roles == null || roles.size() < 1)
    {
      return(0);
    }

    RdbmServices rdbmServices = null;
    Connection con = null;
    Statement stmt = null;

    try
    {
      rdbmServices = new RdbmServices ();
      con = rdbmServices.getConnection();
      stmt = con.createStatement();

      // Count the number of records inserted
      int recordsInserted = 0;

      for(int i = 0; i < roles.size(); i++)
      {
        String sInsert = "INSERT INTO PORTAL_CHAN_ROLES (CHAN_ID, ROLE) VALUES ('" + channelID + "','"
        + roles.elementAt(i) + "')";

        Logger.log (Logger.DEBUG, sInsert);
        recordsInserted += stmt.executeUpdate(sInsert);
      }

      stmt.close();

      return(recordsInserted);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
      return(-1);
    }
    finally
    {
      rdbmServices.releaseConnection(con);
    }
  }

  public boolean canUserPublish(IPerson person)
  {
    if(person == null || person.getID() == null)
    {
      // Possibly throw security exception
      return(false);
    }

    String sUserName = person.getID();
    boolean canPublish = isUserInRole(person, new RoleImpl(s_channelPublisherRole));
    return(canPublish);
  }

  // For the subscribe mechanism to use
  public Vector getAuthorizedChannels(IPerson person)
  {
    if(person == null || person.getID() == null)
    {
      // Possibly throw security exception
      return(null);
    }

    String sUserName = person.getID();

    return(new Vector());
  }

  public boolean canUserSubscribe(IPerson person, int channelID)
  {
    // Fail immediatly if the inputs aren't reasonable
    if(person == null || person.getID() == null)
    {
      return(false);
    }

    String sUserName = person.getID();

    // Get all of the channel roles
    Vector chanRoles = getChannelRoles(channelID);

    // If the channel has no roles associated then it's globally accessable
    if( chanRoles.size() == 0 )
    {
      return(true);
    }

    // Get all of the user's roles
    Vector userRoles = getUserRoles(person);

    // If the user has no roles and the channel does then he can't have access
    if( userRoles.size() == 0 )
    {
      return(false);
    }

    // Check to see if the user has at least one role in common with the channel
    for( int i = 0; i < userRoles.size(); i++ )
    {
      if(chanRoles.contains(userRoles.elementAt(i)))
      {
        return(true);
      }
    }

    return(false);
  }

  public boolean canUserRender(IPerson person, int channelID)
  {
    // If the user can subscribe to a channel, then they can render it!
    return(canUserSubscribe(person, channelID));
  }

  public Vector getChannelRoles(int channelID)
  {
    // Check the smart cache for the roles first
    Vector channelRoles = (Vector)chanRolesCache.get("" + channelID);
    if( channelRoles != null )
    {
      return(channelRoles);
    }
    else
    {
      channelRoles = new Vector();
    }

    RdbmServices rdbmServices = null;
    Connection conn = null;

    try
    {
      rdbmServices = new RdbmServices();

      conn = rdbmServices.getConnection();
      Statement stmt = conn.createStatement();

      String query = "select role from portal_chan_roles where " +
      "chan_id='" + channelID + "'";

      ResultSet rs = stmt.executeQuery(query);

      while(rs.next())
      {
        channelRoles.addElement(rs.getString("ROLE"));
      }

      chanRolesCache.put("" + channelID, channelRoles);

      return(channelRoles);
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
      return(null);
    }
    finally
    {
      rdbmServices.releaseConnection(conn);
    }
  }

  // For the render mechanism to use
  public Vector getUserRoles(IPerson person)
  {
    if(person == null || person.getID() == null)
    {
      return(null);
    }

    String sUserName = person.getID();

    // Check the smart cache for the roles first
    Vector userRoles = (Vector)userRolesCache.get(sUserName);
    if( userRoles != null )
    {
      return(userRoles);
    }
    else
    {
      userRoles = new Vector();
    }

    RdbmServices rdbmServices = null;
    Connection conn = null;

    try
    {
      rdbmServices = new RdbmServices();

      conn = rdbmServices.getConnection();
      Statement stmt = conn.createStatement();

      String query = "select role from portal_user_roles where " +
      "user_name='" + sUserName + "'";

      ResultSet rs = stmt.executeQuery(query);

      while(rs.next())
      {
        userRoles.addElement(rs.getString("ROLE"));
      }

      userRolesCache.put(sUserName, userRoles);
      return(userRoles);
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
      return(null);
    }
    finally
    {
      rdbmServices.releaseConnection(conn);
    }
  }

  // For the administration mechanism to use
  public void addUserRoles(IPerson person, Vector roles)
  {
    if(person == null || person.getID() == null || roles == null || roles.size() < 1)
    {
      return;
    }

    RdbmServices rdbmServices = null;
    Connection conn = null;

    try
    {
      rdbmServices = new RdbmServices();

      conn = rdbmServices.getConnection();
      Statement stmt = conn.createStatement();

      String insert = new String();
      int insertCount = 0;

      for(int i = 0; i < roles.size(); i++)
      {
        insert = "insert into portal_user_roles (user_name, role) values (" + person.getID() + ", " + roles.elementAt(i) + ")";
        insertCount = stmt.executeUpdate(insert);

        if(insertCount != 1)
        {
          Logger.log(Logger.ERROR, "AuthorizationBean addUserRoles(): SQL failed -> " + insert);
        }
      }

      return;
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
      return;
    }
    finally
    {
      rdbmServices.releaseConnection(conn);
    }
  }

  public void removeUserRoles(IPerson person, Vector roles)
  {
    if(person == null || person.getID() == null || roles == null || roles.size() < 1)
    {
      return;
    }

    RdbmServices rdbmServices = null;
    Connection conn = null;

    try
    {
      rdbmServices = new RdbmServices();

      conn = rdbmServices.getConnection();
      Statement stmt = conn.createStatement();

      String delete = new String();
      int deleteCount = 0;

      for(int i = 0; i < roles.size(); i++)
      {
        delete = "delete from portal_user_roles where " + "user_name = " + person.getID() + " and " + "role = " + roles.elementAt(i);
        deleteCount = stmt.executeUpdate(delete);

        if(deleteCount != 1)
        {
          Logger.log(Logger.ERROR, "AuthorizationBean removeUserRoles(): SQL failed -> " + delete);
        }
      }

      return;
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
      return;
    }
    finally
    {
      rdbmServices.releaseConnection(conn);
    }
  }
}
