/**
 * Copyright © 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
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
package org.jasig.portal.groups;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.jasig.portal.EntityTypes;
import org.jasig.portal.RDBMServices;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Reference implementation for IEntityStore.
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class RDBMEntityStore implements IEntityStore {
private static IEntityStore singleton;

private static final Log log = LogFactory.getLog(RDBMEntityStore.class);

/**
 * RDBMEntityStore constructor.
 */
public RDBMEntityStore()
{
    super();
}
/**
 * Find the <code>IEntities</code> that are members of the <code>IEntityGroup</code>.
 * @return java.util.Iterator
 * @param group org.jasig.portal.groups.IEntityGroup
 * @deprecated
 * replaced by <code>RDBMEntityGroupStore.findEntitiesForGroup(IEntityGroup)</code>.
 */
public Iterator findEntitiesForGroup(IEntityGroup group) throws GroupsException
{
    Collection entities = new ArrayList();
    Connection conn = null;
    String groupID = group.getLocalKey();
    Class cls = group.getLeafType();

    try
    {
        conn = RDBMServices.getConnection();
        Statement stmnt = conn.createStatement();
        try {

            String query = "SELECT MEMBER_KEY FROM UP_GROUP_MEMBERSHIP" +
                           " WHERE GROUP_ID = " + "'" + groupID + "'" +
                           " AND MEMBER_IS_GROUP = 'F' ";

            ResultSet rs = stmnt.executeQuery(query);
            try {
                while (rs.next())
                {
                    String key = rs.getString(1);
                    IEntity e = newInstance(key, cls);
                    entities.add(e);
                }
            } finally {
                  rs.close();
              }
        } finally {
              stmnt.close();
          }
        }
    catch (SQLException sqle)
    {
        log.error( sqle);
        throw new GroupsException("Problem retrieving Entities for Group: " + sqle.getMessage());
    }
    finally
        { RDBMServices.releaseConnection(conn); }

    return entities.iterator();
}
/**
 * @return org.jasig.portal.groups.IEntity
 * @param key java.lang.String
 */
public IEntity newInstance(String key) throws GroupsException
{
    return newInstance(key, null);
}
/**
 * @return org.jasig.portal.groups.IEntity
 * @param key java.lang.String
 * @param type java.lang.Class
 */
public IEntity newInstance(String key, Class type) throws GroupsException
{
    if ( EntityTypes.getEntityTypeID(type) == null )
        { throw new GroupsException("Invalid group type: " + type); }
    return new EntityImpl(key, type);
}
/**
 * @return org.jasig.portal.groups.IEntityStore
 */
public static synchronized IEntityStore singleton()
{
    if (singleton == null)
        { singleton = new RDBMEntityStore(); }
    return singleton;
}
}
