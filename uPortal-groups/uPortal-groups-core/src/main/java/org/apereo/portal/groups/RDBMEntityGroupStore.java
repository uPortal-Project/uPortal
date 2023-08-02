/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.groups;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.jdbc.RDBMServices;
import org.apereo.portal.services.GroupService;
import org.apereo.portal.spring.locator.ApplicationContextLocator;
import org.apereo.portal.spring.locator.CounterStoreLocator;
import org.apereo.portal.spring.locator.EntityTypesLocator;
import org.apereo.portal.utils.SqlTransaction;
import org.springframework.context.ApplicationContext;

/** Store for <code>EntityGroupImpl</code>. */
public class RDBMEntityGroupStore implements IEntityGroupStore, IGroupConstants {

    private static RDBMEntityGroupStore singleton;

    // Constant SQL strings:
    private static final String EQ = " = ";
    private static final String QUOTE = "'";
    private static final String EQUALS_PARAM = EQ + "?";

    // Constant strings for GROUP table:
    private static final String GROUP_TABLE = "UP_GROUP";
    private static final String GROUP_TABLE_ALIAS = "T1";
    private static final String GROUP_TABLE_WITH_ALIAS = GROUP_TABLE + " " + GROUP_TABLE_ALIAS;
    private static final String GROUP_ID_COLUMN = "GROUP_ID";
    private static final String GROUP_CREATOR_COLUMN = "CREATOR_ID";
    private static final String GROUP_TYPE_COLUMN = "ENTITY_TYPE_ID";
    private static final String GROUP_NAME_COLUMN = "GROUP_NAME";
    private static final String GROUP_DESCRIPTION_COLUMN = "DESCRIPTION";

    // Constant strings for MEMBERS table:
    private static final String MEMBER_TABLE = "UP_GROUP_MEMBERSHIP";
    private static final String MEMBER_TABLE_ALIAS = "T2";
    private static final String MEMBER_TABLE_WITH_ALIAS = MEMBER_TABLE + " " + MEMBER_TABLE_ALIAS;
    private static final String MEMBER_GROUP_ID_COLUMN = "GROUP_ID";
    private static final String MEMBER_MEMBER_SERVICE_COLUMN = "MEMBER_SERVICE";
    private static final String MEMBER_MEMBER_KEY_COLUMN = "MEMBER_KEY";
    private static final String MEMBER_IS_GROUP_COLUMN = "MEMBER_IS_GROUP";
    private static final String MEMBER_IS_ENTITY = "F";
    private static final String MEMBER_IS_GROUP = "T";

    private static final String SEARCH_CACHE_NAME =
            "org.apereo.portal.groups.RDBMEntityGroupStore.search";
    private static final String PARENT_GROUP_BY_ENTITY_CACHE_NAME =
            "org.apereo.portal.groups.RDBMEntityGroupStore.parentGroupEntity";
    private static final String PARENT_GROUP_BY_ENTTITY_GROUP_CACHE_NAME =
            "org.apereo.portal.groups.RDBMEntityGroupStore.parentGroupEntityGroup";

    // SQL group search string
    private static final String SEARCH_GROUPS_PARTIAL_CASE_INSENSITIVE =
            "SELECT "
                    + GROUP_ID_COLUMN
                    + " FROM "
                    + GROUP_TABLE
                    + " WHERE "
                    + GROUP_TYPE_COLUMN
                    + "=? AND UPPER("
                    + GROUP_NAME_COLUMN
                    + ") LIKE UPPER(?)";
    private static final String SEARCH_GROUPS_PARTIAL =
            "SELECT "
                    + GROUP_ID_COLUMN
                    + " FROM "
                    + GROUP_TABLE
                    + " WHERE "
                    + GROUP_TYPE_COLUMN
                    + "=? AND "
                    + GROUP_NAME_COLUMN
                    + " LIKE ?";
    private static final String SEARCH_GROUPS_CASE_INSENSITIVE =
            "SELECT "
                    + GROUP_ID_COLUMN
                    + " FROM "
                    + GROUP_TABLE
                    + " WHERE "
                    + GROUP_TYPE_COLUMN
                    + "=? AND UPPER("
                    + GROUP_NAME_COLUMN
                    + ") = UPPER(?)";
    private static final String SEARCH_GROUPS =
            "SELECT "
                    + GROUP_ID_COLUMN
                    + " FROM "
                    + GROUP_TABLE
                    + " WHERE "
                    + GROUP_TYPE_COLUMN
                    + "=? AND "
                    + GROUP_NAME_COLUMN
                    + " = ?";

    private static final String SEARCH_ENTITIES_FOR_GROUP =
            "SELECT "
                    + MEMBER_MEMBER_KEY_COLUMN
                    + " FROM "
                    + MEMBER_TABLE
                    + " WHERE "
                    + MEMBER_GROUP_ID_COLUMN
                    + "=? AND "
                    + MEMBER_IS_GROUP_COLUMN
                    + " = '"
                    + MEMBER_IS_ENTITY
                    + "'";

    private static final Log LOG = LogFactory.getLog(RDBMEntityGroupStore.class);

    private static String groupNodeSeparator;

    // SQL strings for GROUP crud:
    private static String allGroupColumns;
    private static String allGroupColumnsWithTableAlias;
    private static String countAMemberGroupSql;
    private static String countAMemberEntitySql;
    private static String findParentGroupsForEntitySql;
    private static String findParentGroupsForGroupSql;
    private static String findGroupSql;
    private static String findMemberGroupKeysSql;
    private static String findMemberGroupsSql;
    private static String insertGroupSql;
    private static String updateGroupSql;

    // SQL strings for group MEMBERS crud:
    private static String allMemberColumns;
    private static String deleteMembersInGroupSql;
    private static String deleteMemberGroupSql;
    private static String deleteMemberEntitySql;
    private static String insertMemberSql;

    // Group search cache
    private Ehcache groupSearchCache;
    private Ehcache parentGroupEntityCache;
    private Ehcache parentGroupEntityGroupCache;

    /** RDBMEntityGroupStore constructor. */
    public RDBMEntityGroupStore() {
        initialize();
    }

    /**
     * Get the node separator character from the GroupServiceConfiguration. Default it to
     * IGroupConstants.DEFAULT_NODE_SEPARATOR.
     */
    private void initialize() {
        String sep;
        try {
            sep = GroupServiceConfiguration.getConfiguration().getNodeSeparator();
        } catch (Exception ex) {
            sep = DEFAULT_NODE_SEPARATOR;
        }
        groupNodeSeparator = sep;
        if (LOG.isDebugEnabled()) {
            LOG.debug("RDBMEntityGroupStore.initialize(): Node separator set to " + sep);
        }

        // Cache for group search
        groupSearchCache = getGroupSearchCache(SEARCH_CACHE_NAME);
        parentGroupEntityCache = getGroupSearchCache(PARENT_GROUP_BY_ENTITY_CACHE_NAME);
        parentGroupEntityGroupCache = getGroupSearchCache(PARENT_GROUP_BY_ENTTITY_GROUP_CACHE_NAME);
    }

    private Ehcache getGroupSearchCache(String cacheName) {
        final ApplicationContext context = ApplicationContextLocator.getApplicationContext();
        assert context != null;
        final CacheManager cacheManager = context.getBean("cacheManager", CacheManager.class);
        assert cacheManager != null;
        if (!cacheManager.cacheExists(cacheName)) {
            cacheManager.addCache(cacheName);
        }
        return cacheManager.getCache(cacheName);
    }

    /**
     * @param conn Connection
     * @exception SQLException
     */
    protected static void commit(Connection conn) throws SQLException {
        SqlTransaction.commit(conn);
    }

    /**
     * Answers if <code>IGroupMember</code> member is a member of <code>group</code>.
     *
     * @return boolean
     * @param group IEntityGroup
     * @param member IGroupMember
     */
    @Override
    public boolean contains(IEntityGroup group, IGroupMember member) throws GroupsException {
        return (member.isGroup())
                ? containsGroup(group, (IEntityGroup) member)
                : containsEntity(group, member);
    }

    private boolean containsEntity(IEntityGroup group, IGroupMember member) throws GroupsException {
        String groupKey = group.getLocalKey();
        String memberKey = member.getKey();
        Connection conn = RDBMServices.getConnection();
        try {
            String sql = getCountAMemberEntitySql();
            PreparedStatement ps = conn.prepareStatement(sql);
            try {
                ps.clearParameters();
                ps.setString(1, groupKey);
                ps.setString(2, memberKey);
                if (LOG.isDebugEnabled())
                    LOG.debug(
                            "RDBMEntityGroupStore.containsEntity(): "
                                    + ps
                                    + " ("
                                    + groupKey
                                    + ", "
                                    + memberKey
                                    + ")");
                ResultSet rs = ps.executeQuery();
                try {
                    return (rs.next()) && (rs.getInt(1) > 0);
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (Exception e) {
            LOG.error("RDBMEntityGroupStore.containsEntity(): " + e);
            throw new GroupsException("Problem retrieving data from store: " + e);
        } finally {
            RDBMServices.releaseConnection(conn);
        }
    }

    private boolean containsGroup(IEntityGroup group, IEntityGroup member) throws GroupsException {
        String memberService = member.getServiceName().toString();
        String groupKey = group.getLocalKey();
        String memberKey = member.getLocalKey();
        Connection conn = RDBMServices.getConnection();
        try {
            String sql = getCountAMemberGroupSql();
            PreparedStatement ps = conn.prepareStatement(sql);
            try {
                ps.clearParameters();
                ps.setString(1, groupKey);
                ps.setString(2, memberKey);
                ps.setString(3, memberService);
                if (LOG.isDebugEnabled())
                    LOG.debug(
                            "RDBMEntityGroupStore.containsGroup(): "
                                    + ps
                                    + " ("
                                    + groupKey
                                    + ", "
                                    + memberKey
                                    + ", "
                                    + memberService
                                    + ")");
                ResultSet rs = ps.executeQuery();
                try {
                    return (rs.next()) && (rs.getInt(1) > 0);
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (Exception e) {
            LOG.error("RDBMEntityGroupStore.containsGroup(): " + e);
            throw new GroupsException("Problem retrieving data from store: " + e);
        } finally {
            RDBMServices.releaseConnection(conn);
        }
    }

    /**
     * If this entity exists, delete it.
     *
     * @param group IEntityGroup
     */
    @Override
    public void delete(IEntityGroup group) throws GroupsException {
        if (existsInDatabase(group)) {
            try {
                primDelete(group);
            } catch (SQLException sqle) {
                throw new GroupsException("Problem deleting " + group, sqle);
            }
        }
    }

    /**
     * Answer if the IEntityGroup entity exists in the database.
     *
     * @return boolean
     * @param group IEntityGroup
     */
    private boolean existsInDatabase(IEntityGroup group) throws GroupsException {
        IEntityGroup ug = this.find(group.getLocalKey());
        return ug != null;
    }

    /**
     * Find and return an instance of the group.
     *
     * @param groupID the group ID
     * @return IEntityGroup
     */
    @Override
    public IEntityGroup find(String groupID) throws GroupsException {
        return primFind(groupID, false);
    }

    /**
     * Find the groups that this entity belongs to.
     *
     * @param ent the entity in question
     * @return java.util.Iterator
     */
    public java.util.Iterator<IEntityGroup> findParentGroups(IEntity ent) throws GroupsException {
        // https://github.com/uPortal-Project/uPortal/issues/1903 #2
        String memberKey = ent.getKey();
        Integer type = EntityTypesLocator.getEntityTypes().getEntityIDFromType(ent.getLeafType());
        String cacheKey = memberKey + ":" + type.intValue();
        List<IEntityGroup> list;
        Element el = parentGroupEntityCache.get(cacheKey);
        if (el == null) {
            list = findParentGroupsForEntity(memberKey, type.intValue());
            list = Collections.unmodifiableList(list);
            parentGroupEntityCache.put(new Element(cacheKey, list));
        } else {
            list = (List) el.getObjectValue();
            assert list != null;
        }
        return list.iterator();
    }

    /**
     * Find the groups that this group belongs to.
     *
     * @param group IEntityGroup
     * @return java.util.Iterator
     */
    public java.util.Iterator<IEntityGroup> findParentGroups(IEntityGroup group)
            throws GroupsException {
        // https://github.com/uPortal-Project/uPortal/issues/1903 #8
        String memberKey = group.getLocalKey();
        String serviceName = group.getServiceName().toString();
        Integer type = EntityTypesLocator.getEntityTypes().getEntityIDFromType(group.getLeafType());
        String cacheKey = memberKey + ":" + type.intValue() + ":" + serviceName;
        Element el = parentGroupEntityGroupCache.get(cacheKey);
        List<IEntityGroup> list;
        if (el == null) {
            list = findParentGroupsForGroup(serviceName, memberKey, type.intValue());
            list = Collections.unmodifiableList(list);
            parentGroupEntityGroupCache.put(new Element(cacheKey, list));
        } else {
            list = (List) el.getObjectValue();
            assert list != null;
        }
        return list.iterator();
    }

    /**
     * Find the groups that this group member belongs to.
     *
     * @param gm the group member in question
     * @return java.util.Iterator
     */
    @Override
    public Iterator findParentGroups(IGroupMember gm) throws GroupsException {
        if (gm.isGroup()) {
            IEntityGroup group = (IEntityGroup) gm;
            return findParentGroups(group);
        } else {
            IEntity ent = (IEntity) gm;
            return findParentGroups(ent);
        }
    }

    /**
     * Find the groups associated with this member key.
     *
     * @param memberKey
     * @param type
     * @return list of groups (IEntityGroup)
     */
    private List<IEntityGroup> findParentGroupsForEntity(String memberKey, int type)
            throws GroupsException {
        Connection conn = null;
        List<IEntityGroup> groups = new ArrayList<>();
        IEntityGroup eg = null;

        try {
            conn = RDBMServices.getConnection();
            String sql = getFindParentGroupsForEntitySql();
            PreparedStatement ps = conn.prepareStatement(sql);
            try {
                ps.setString(1, memberKey);
                ps.setInt(2, type);
                if (LOG.isDebugEnabled())
                    LOG.debug(
                            "RDBMEntityGroupStore.findParentGroupsForEntity(): "
                                    + ps
                                    + " ("
                                    + memberKey
                                    + ", "
                                    + type
                                    + ", memberIsGroup = F)");
                ResultSet rs = ps.executeQuery();
                try {
                    while (rs.next()) {
                        eg = instanceFromResultSet(rs);
                        groups.add(eg);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (Exception e) {
            LOG.error("RDBMEntityGroupStore.findParentGroupsForEntity(): " + e);
            throw new GroupsException("Problem retrieving containing groups: " + e);
        } finally {
            RDBMServices.releaseConnection(conn);
        }

        return groups;
    }

    /**
     * Find the groups associated with this member key.
     *
     * @param serviceName
     * @param memberKey
     * @param type
     * @return list of groups (IEntityGroup)
     */
    private List<IEntityGroup> findParentGroupsForGroup(
            String serviceName, String memberKey, int type) throws GroupsException {
        Connection conn = null;
        List<IEntityGroup> groups = new ArrayList<>();
        IEntityGroup eg = null;

        try {
            conn = RDBMServices.getConnection();
            String sql = getFindParentGroupsForGroupSql();
            PreparedStatement ps = conn.prepareStatement(sql);
            try {
                ps.setString(1, serviceName);
                ps.setString(2, memberKey);
                ps.setInt(3, type);
                if (LOG.isDebugEnabled())
                    LOG.debug(
                            "RDBMEntityGroupStore.findParentGroupsForGroup(): "
                                    + ps
                                    + " ("
                                    + serviceName
                                    + ", "
                                    + memberKey
                                    + ", "
                                    + type
                                    + ", memberIsGroup = T)");
                ResultSet rs = ps.executeQuery();
                try {
                    while (rs.next()) {
                        eg = instanceFromResultSet(rs);
                        groups.add(eg);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (Exception e) {
            LOG.error("RDBMEntityGroupStore.findParentGroupsForGroup(): " + e);
            throw new GroupsException("Problem retrieving containing groups: " + e);
        } finally {
            RDBMServices.releaseConnection(conn);
        }

        return groups;
    }

    /**
     * Find the <code>IEntities</code> that are members of the <code>IEntityGroup</code>.
     *
     * @param group the entity group in question
     * @return java.util.Iterator
     */
    @Override
    public Iterator findEntitiesForGroup(IEntityGroup group) throws GroupsException {
        Collection entities = new ArrayList();
        Connection conn = null;
        PreparedStatement ps = null;
        String groupID = group.getLocalKey();
        Class cls = group.getLeafType();

        try {
            conn = RDBMServices.getConnection();
            ps = conn.prepareStatement(SEARCH_ENTITIES_FOR_GROUP);
            try {

                ps.clearParameters();
                ps.setString(1, groupID);

                ResultSet rs = ps.executeQuery();
                try {
                    while (rs.next()) {
                        String key = rs.getString(1);
                        IEntity e = newEntity(cls, key);
                        entities.add(e);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (SQLException sqle) {
            LOG.error("Problem retrieving Entities for Group: " + group, sqle);
            throw new GroupsException("Problem retrieving Entities for Group", sqle);
        } finally {
            RDBMServices.releaseConnection(conn);
        }

        return entities.iterator();
    }

    /**
     * Find and return an instance of the group.
     *
     * @param groupID the group ID
     * @return ILockableEntityGroup
     */
    @Override
    public ILockableEntityGroup findLockable(String groupID) throws GroupsException {
        return (ILockableEntityGroup) primFind(groupID, true);
    }

    /**
     * Find the keys of groups that are members of group.
     *
     * @param group the IEntityGroup
     * @return String[]
     */
    @Override
    public String[] findMemberGroupKeys(IEntityGroup group) throws GroupsException {
        Connection conn = null;
        Collection groupKeys = new ArrayList();
        String groupKey = null;

        try {
            conn = RDBMServices.getConnection();
            String sql = getFindMemberGroupKeysSql();
            PreparedStatement ps = conn.prepareStatement(sql);
            try {
                ps.setString(1, group.getLocalKey());
                if (LOG.isDebugEnabled())
                    LOG.debug(
                            "RDBMEntityGroupStore.findMemberGroupKeys(): "
                                    + ps
                                    + " ("
                                    + group.getLocalKey()
                                    + ")");
                ResultSet rs = ps.executeQuery();
                try {
                    while (rs.next()) {
                        groupKey = rs.getString(1) + groupNodeSeparator + rs.getString(2);
                        groupKeys.add(groupKey);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (Exception sqle) {
            LOG.error("RDBMEntityGroupStore.findMemberGroupKeys(): " + sqle);
            throw new GroupsException("Problem retrieving member group keys: " + sqle);
        } finally {
            RDBMServices.releaseConnection(conn);
        }

        return (String[]) groupKeys.toArray(new String[groupKeys.size()]);
    }
    /**
     * Find the IUserGroups that are members of the group.
     *
     * @param group IEntityGroup
     * @return java.util.Iterator
     */
    @Override
    public Iterator findMemberGroups(IEntityGroup group) throws GroupsException {
        Connection conn = null;
        Collection groups = new ArrayList();
        IEntityGroup eg = null;
        String serviceName = group.getServiceName().toString();
        String localKey = group.getLocalKey();

        try {
            conn = RDBMServices.getConnection();
            String sql = getFindMemberGroupsSql();
            PreparedStatement ps = conn.prepareStatement(sql);
            try {
                ps.setString(1, localKey);
                ps.setString(2, serviceName);
                if (LOG.isDebugEnabled())
                    LOG.debug(
                            "RDBMEntityGroupStore.findMemberGroups(): "
                                    + ps
                                    + " ("
                                    + localKey
                                    + ", "
                                    + serviceName
                                    + ")");
                ResultSet rs = ps.executeQuery();
                try {
                    while (rs.next()) {
                        eg = instanceFromResultSet(rs);
                        groups.add(eg);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (Exception sqle) {
            LOG.error("RDBMEntityGroupStore.findMemberGroups(): " + sqle);
            throw new GroupsException("Problem retrieving member groups: " + sqle);
        } finally {
            RDBMServices.releaseConnection(conn);
        }

        return groups.iterator();
    }
    /** @return String */
    private static String getAllGroupColumns() {

        if (allGroupColumns == null) {
            StringBuffer buff = new StringBuffer(100);
            buff.append(GROUP_ID_COLUMN);
            buff.append(", ");
            buff.append(GROUP_CREATOR_COLUMN);
            buff.append(", ");
            buff.append(GROUP_TYPE_COLUMN);
            buff.append(", ");
            buff.append(GROUP_NAME_COLUMN);
            buff.append(", ");
            buff.append(GROUP_DESCRIPTION_COLUMN);

            allGroupColumns = buff.toString();
        }
        return allGroupColumns;
    }
    /** @return String */
    private static String getAllGroupColumnsWithTableAlias() {

        if (allGroupColumnsWithTableAlias == null) {
            StringBuffer buff = new StringBuffer(100);
            buff.append(groupAlias(GROUP_ID_COLUMN));
            buff.append(", ");
            buff.append(groupAlias(GROUP_CREATOR_COLUMN));
            buff.append(", ");
            buff.append(groupAlias(GROUP_TYPE_COLUMN));
            buff.append(", ");
            buff.append(groupAlias(GROUP_NAME_COLUMN));
            buff.append(", ");
            buff.append(groupAlias(GROUP_DESCRIPTION_COLUMN));

            allGroupColumnsWithTableAlias = buff.toString();
        }
        return allGroupColumnsWithTableAlias;
    }
    /** @return String */
    private static String getAllMemberColumns() {
        if (allMemberColumns == null) {
            StringBuffer buff = new StringBuffer(100);

            buff.append(MEMBER_GROUP_ID_COLUMN);
            buff.append(", ");
            buff.append(MEMBER_MEMBER_SERVICE_COLUMN);
            buff.append(", ");
            buff.append(MEMBER_MEMBER_KEY_COLUMN);
            buff.append(", ");
            buff.append(MEMBER_IS_GROUP_COLUMN);

            allMemberColumns = buff.toString();
        }
        return allMemberColumns;
    }
    /** @return String */
    private static String getCountAMemberEntitySql() {
        if (countAMemberEntitySql == null) {
            StringBuffer buff = new StringBuffer(100);
            buff.append("SELECT COUNT(*) FROM " + MEMBER_TABLE);
            buff.append(" WHERE " + MEMBER_GROUP_ID_COLUMN + EQUALS_PARAM);
            buff.append(" AND " + MEMBER_MEMBER_KEY_COLUMN + EQUALS_PARAM);
            buff.append(" AND " + MEMBER_IS_GROUP_COLUMN + EQ + sqlQuote(MEMBER_IS_ENTITY));
            countAMemberEntitySql = buff.toString();
        }
        return countAMemberEntitySql;
    }
    /** @return String */
    private static String getCountAMemberGroupSql() {
        if (countAMemberGroupSql == null) {
            StringBuffer buff = new StringBuffer(100);
            buff.append("SELECT COUNT(*) FROM " + MEMBER_TABLE);
            buff.append(" WHERE " + MEMBER_GROUP_ID_COLUMN + EQUALS_PARAM);
            buff.append(" AND " + MEMBER_MEMBER_KEY_COLUMN + EQUALS_PARAM);
            buff.append(" AND " + MEMBER_MEMBER_SERVICE_COLUMN + EQUALS_PARAM);
            buff.append(" AND " + MEMBER_IS_GROUP_COLUMN + EQ + sqlQuote(MEMBER_IS_GROUP));
            countAMemberGroupSql = buff.toString();
        }
        return countAMemberGroupSql;
    }

    /** @return String */
    private static String getDeleteGroupSql(IEntityGroup group) {
        StringBuffer buff = new StringBuffer(100);
        buff.append("DELETE FROM ");
        buff.append(GROUP_TABLE);
        buff.append(" WHERE ");
        buff.append(GROUP_ID_COLUMN + EQ + sqlQuote(group.getLocalKey()));
        return buff.toString();
    }
    /** @return String */
    private static String getDeleteMemberEntitySql() {
        if (deleteMemberEntitySql == null) {
            StringBuffer buff = new StringBuffer(100);
            buff.append("DELETE FROM ");
            buff.append(MEMBER_TABLE);
            buff.append(" WHERE ");
            buff.append(MEMBER_GROUP_ID_COLUMN + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(MEMBER_MEMBER_KEY_COLUMN + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(MEMBER_IS_GROUP_COLUMN + EQ + sqlQuote(MEMBER_IS_ENTITY));

            deleteMemberEntitySql = buff.toString();
        }
        return deleteMemberEntitySql;
    }
    /** @return String */
    private static String getDeleteMemberGroupSql() {
        if (deleteMemberGroupSql == null) {
            StringBuffer buff = new StringBuffer(100);
            buff.append("DELETE FROM ");
            buff.append(MEMBER_TABLE);
            buff.append(" WHERE ");
            buff.append(MEMBER_GROUP_ID_COLUMN + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(MEMBER_MEMBER_SERVICE_COLUMN + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(MEMBER_MEMBER_KEY_COLUMN + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(MEMBER_IS_GROUP_COLUMN + EQ + sqlQuote(MEMBER_IS_GROUP));
            deleteMemberGroupSql = buff.toString();
        }
        return deleteMemberGroupSql;
    }
    /** @return String */
    private static String getDeleteMembersInGroupSql() {
        if (deleteMembersInGroupSql == null) {
            StringBuffer buff = new StringBuffer(100);
            buff.append("DELETE FROM ");
            buff.append(MEMBER_TABLE);
            buff.append(" WHERE ");
            buff.append(GROUP_ID_COLUMN + EQ);

            deleteMembersInGroupSql = buff.toString();
        }
        return deleteMembersInGroupSql;
    }
    /** @return String */
    private static String getDeleteMembersInGroupSql(IEntityGroup group) {
        return getDeleteMembersInGroupSql() + sqlQuote(group.getLocalKey());
    }
    /** @return String */
    private static String getFindParentGroupsForEntitySql() {
        if (findParentGroupsForEntitySql == null) {
            StringBuffer buff = new StringBuffer(500);
            buff.append("SELECT ");
            buff.append(getAllGroupColumnsWithTableAlias());
            buff.append(" FROM " + GROUP_TABLE_WITH_ALIAS + ", " + MEMBER_TABLE_WITH_ALIAS);
            buff.append(" WHERE ");
            buff.append(groupAlias(GROUP_ID_COLUMN) + EQ);
            buff.append(memberAlias(MEMBER_GROUP_ID_COLUMN));
            buff.append(" AND ");
            buff.append(memberAlias(MEMBER_MEMBER_KEY_COLUMN) + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(groupAlias(GROUP_TYPE_COLUMN) + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(memberAlias(MEMBER_IS_GROUP_COLUMN) + EQ + sqlQuote(MEMBER_IS_ENTITY));

            findParentGroupsForEntitySql = buff.toString();
        }
        return findParentGroupsForEntitySql;
    }
    /** @return String */
    private static String getFindParentGroupsForGroupSql() {
        if (findParentGroupsForGroupSql == null) {
            StringBuffer buff = new StringBuffer(500);
            buff.append("SELECT ");
            buff.append(getAllGroupColumnsWithTableAlias());
            buff.append(" FROM ");
            buff.append(GROUP_TABLE_WITH_ALIAS);
            buff.append(", ");
            buff.append(MEMBER_TABLE_WITH_ALIAS);
            buff.append(" WHERE ");
            buff.append(groupAlias(GROUP_ID_COLUMN) + EQ);
            buff.append(memberAlias(MEMBER_GROUP_ID_COLUMN));
            buff.append(" AND ");
            buff.append(memberAlias(MEMBER_MEMBER_SERVICE_COLUMN) + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(memberAlias(MEMBER_MEMBER_KEY_COLUMN) + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(groupAlias(GROUP_TYPE_COLUMN) + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(memberAlias(MEMBER_IS_GROUP_COLUMN) + EQ + sqlQuote(MEMBER_IS_GROUP));

            findParentGroupsForGroupSql = buff.toString();
        }
        return findParentGroupsForGroupSql;
    }

    /** @return String */
    private static String getFindGroupSql() {

        if (findGroupSql == null) {
            StringBuffer buff = new StringBuffer(200);
            buff.append("SELECT ");
            buff.append(getAllGroupColumns());
            buff.append(" FROM ");
            buff.append(GROUP_TABLE);
            buff.append(" WHERE ");
            buff.append(GROUP_ID_COLUMN + EQUALS_PARAM);

            findGroupSql = buff.toString();
        }
        return findGroupSql;
    }
    /** @return String */
    private static String getFindMemberGroupKeysSql() {
        if (findMemberGroupKeysSql == null) {
            StringBuffer buff = new StringBuffer(200);
            buff.append("SELECT ");
            buff.append(MEMBER_MEMBER_SERVICE_COLUMN + ", " + MEMBER_MEMBER_KEY_COLUMN);
            buff.append(" FROM ");
            buff.append(MEMBER_TABLE);
            buff.append(" WHERE ");
            buff.append(MEMBER_GROUP_ID_COLUMN + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(MEMBER_IS_GROUP_COLUMN + EQ);
            buff.append(sqlQuote(MEMBER_IS_GROUP));

            findMemberGroupKeysSql = buff.toString();
        }

        return findMemberGroupKeysSql;
    }
    /** @return String */
    private static String getFindMemberGroupsSql() {
        if (findMemberGroupsSql == null) {
            StringBuffer buff = new StringBuffer(500);
            buff.append("SELECT ");
            buff.append(getAllGroupColumnsWithTableAlias());
            buff.append(" FROM ");
            buff.append(GROUP_TABLE + " " + GROUP_TABLE_ALIAS);
            buff.append(", ");
            buff.append(MEMBER_TABLE + " " + MEMBER_TABLE_ALIAS);
            buff.append(" WHERE ");
            buff.append(groupAlias(GROUP_ID_COLUMN) + EQ);
            buff.append(memberAlias(MEMBER_MEMBER_KEY_COLUMN));
            buff.append(" AND ");
            buff.append(memberAlias(MEMBER_IS_GROUP_COLUMN) + EQ);
            buff.append(sqlQuote(MEMBER_IS_GROUP));
            buff.append(" AND ");
            buff.append(memberAlias(MEMBER_GROUP_ID_COLUMN) + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(memberAlias(MEMBER_MEMBER_SERVICE_COLUMN) + EQUALS_PARAM);

            findMemberGroupsSql = buff.toString();
        }

        return findMemberGroupsSql;
    }
    /** @return String */
    private static String getInsertGroupSql() {
        if (insertGroupSql == null) {
            StringBuffer buff = new StringBuffer(200);
            buff.append("INSERT INTO ");
            buff.append(GROUP_TABLE);
            buff.append(" (");
            buff.append(getAllGroupColumns());
            buff.append(") VALUES (?, ?, ?, ?, ?)");

            insertGroupSql = buff.toString();
        }
        return insertGroupSql;
    }
    /** @return String */
    private static String getInsertMemberSql() {
        if (insertMemberSql == null) {
            StringBuffer buff = new StringBuffer(200);
            buff.append("INSERT INTO ");
            buff.append(MEMBER_TABLE);
            buff.append(" (");
            buff.append(getAllMemberColumns());
            buff.append(") VALUES (?, ?, ?, ? )");

            insertMemberSql = buff.toString();
        }
        return insertMemberSql;
    }
    /**
     * @return String
     * @exception java.lang.Exception
     */
    private String getNextKey() throws java.lang.Exception {
        return Integer.toString(CounterStoreLocator.getCounterStore().getNextId(GROUP_TABLE));
    }
    /** @return String */
    private static String getUpdateGroupSql() {
        if (updateGroupSql == null) {
            StringBuffer buff = new StringBuffer(200);
            buff.append("UPDATE ");
            buff.append(GROUP_TABLE);
            buff.append(" SET ");
            buff.append(GROUP_CREATOR_COLUMN + EQUALS_PARAM);
            buff.append(", ");
            buff.append(GROUP_TYPE_COLUMN + EQUALS_PARAM);
            buff.append(", ");
            buff.append(GROUP_NAME_COLUMN + EQUALS_PARAM);
            buff.append(", ");
            buff.append(GROUP_DESCRIPTION_COLUMN + EQUALS_PARAM);
            buff.append(" WHERE ");
            buff.append(GROUP_ID_COLUMN + EQUALS_PARAM);

            updateGroupSql = buff.toString();
        }
        return updateGroupSql;
    }

    /**
     * Find and return an instance of the group.
     *
     * @param rs the SQL result set
     * @return IEntityGroup
     */
    private IEntityGroup instanceFromResultSet(ResultSet rs) throws SQLException, GroupsException {
        IEntityGroup eg = null;

        String key = rs.getString(1);
        String creatorID = rs.getString(2);
        Integer entityTypeID = rs.getInt(3);
        Class entityType = EntityTypesLocator.getEntityTypes().getEntityTypeFromID(entityTypeID);
        String groupName = rs.getString(4);
        String description = rs.getString(5);

        if (key != null) {
            eg = newInstance(key, entityType, creatorID, groupName, description);
        }

        return eg;
    }

    /**
     * Find and return an instance of the group.
     *
     * @param rs the SQL result set
     * @return ILockableEntityGroup
     */
    private ILockableEntityGroup lockableInstanceFromResultSet(ResultSet rs)
            throws SQLException, GroupsException {
        ILockableEntityGroup eg = null;

        String key = rs.getString(1);
        String creatorID = rs.getString(2);
        Integer entityTypeID = rs.getInt(3);
        Class entityType = EntityTypesLocator.getEntityTypes().getEntityTypeFromID(entityTypeID);
        String groupName = rs.getString(4);
        String description = rs.getString(5);

        if (key != null) {
            eg = newLockableInstance(key, entityType, creatorID, groupName, description);
        }

        return eg;
    }

    /** @return IEntity */
    public IEntity newEntity(Class type, String key) throws GroupsException {
        if (EntityTypesLocator.getEntityTypes().getEntityIDFromType(type) == null) {
            throw new GroupsException("Invalid group type: " + type);
        }
        return GroupService.getEntity(key, type);
    }
    /** @return IEntityGroup */
    @Override
    public IEntityGroup newInstance(Class type) throws GroupsException {
        if (EntityTypesLocator.getEntityTypes().getEntityIDFromType(type) == null) {
            throw new GroupsException("Invalid group type: " + type);
        }
        try {
            return new EntityGroupImpl(getNextKey(), type);
        } catch (Exception ex) {
            throw new GroupsException("Could not create new group", ex);
        }
    }
    /** @return IEntityGroup */
    private IEntityGroup newInstance(
            String newKey,
            Class newType,
            String newCreatorID,
            String newName,
            String newDescription)
            throws GroupsException {
        EntityGroupImpl egi = new EntityGroupImpl(newKey, newType);
        egi.setCreatorID(newCreatorID);
        egi.primSetName(newName);
        egi.setDescription(newDescription);
        return egi;
    }
    /** @return ILockableEntityGroup */
    private ILockableEntityGroup newLockableInstance(
            String newKey,
            Class newType,
            String newCreatorID,
            String newName,
            String newDescription)
            throws GroupsException {
        LockableEntityGroupImpl group = new LockableEntityGroupImpl(newKey, newType);
        group.setCreatorID(newCreatorID);
        group.primSetName(newName);
        group.setDescription(newDescription);
        return group;
    }
    /** @return String */
    private static String groupAlias(String column) {
        return GROUP_TABLE_ALIAS + "." + column;
    }

    /** @return String */
    private static String memberAlias(String column) {
        return MEMBER_TABLE_ALIAS + "." + column;
    }

    /**
     * Insert the entity into the database.
     *
     * @param group IEntityGroup
     * @param conn the database connection
     */
    private void primAdd(IEntityGroup group, Connection conn) throws SQLException, GroupsException {
        try {
            PreparedStatement ps = conn.prepareStatement(getInsertGroupSql());
            try {
                Integer typeID =
                        EntityTypesLocator.getEntityTypes()
                                .getEntityIDFromType(group.getLeafType());
                ps.setString(1, group.getLocalKey());
                ps.setString(2, group.getCreatorID());
                ps.setInt(3, typeID.intValue());
                ps.setString(4, group.getName());
                ps.setString(5, group.getDescription());

                if (LOG.isDebugEnabled())
                    LOG.debug(
                            "RDBMEntityGroupStore.primAdd(): "
                                    + ps
                                    + "("
                                    + group.getLocalKey()
                                    + ", "
                                    + group.getCreatorID()
                                    + ", "
                                    + typeID
                                    + ", "
                                    + group.getName()
                                    + ", "
                                    + group.getDescription()
                                    + ")");

                int rc = ps.executeUpdate();

                if (rc != 1) {
                    String errString = "Problem adding " + group;
                    LOG.error(errString);
                    throw new GroupsException(errString);
                }
            } finally {
                ps.close();
            }
        } catch (SQLException sqle) {
            LOG.error("Error inserting an entity into the database. Group:" + group, sqle);
            throw sqle;
        }
    }
    /**
     * Delete this entity from the database after first deleting its memberships. Exception
     * SQLException - if we catch a SQLException, we rollback and re-throw it.
     *
     * @param group IEntityGroup
     */
    private void primDelete(IEntityGroup group) throws SQLException {
        Connection conn = null;
        String deleteGroupSql = getDeleteGroupSql(group);
        String deleteMembershipSql = getDeleteMembersInGroupSql(group);

        try {
            conn = RDBMServices.getConnection();
            Statement stmnt = conn.createStatement();
            setAutoCommit(conn, false);

            try {
                if (LOG.isDebugEnabled())
                    LOG.debug("RDBMEntityGroupStore.primDelete(): " + deleteMembershipSql);

                stmnt.executeUpdate(deleteMembershipSql);

                if (LOG.isDebugEnabled())
                    LOG.debug("RDBMEntityGroupStore.primDelete(): " + deleteGroupSql);
                stmnt.executeUpdate(deleteGroupSql);
            } finally {
                stmnt.close();
            }
            commit(conn);

        } catch (SQLException sqle) {
            rollback(conn);
            throw sqle;
        } finally {
            try {
                setAutoCommit(conn, true);
            } finally {
                RDBMServices.releaseConnection(conn);
            }
        }
    }
    /**
     * Find and return an instance of the group.
     *
     * @param groupID the group ID
     * @param lockable boolean
     * @return IEntityGroup
     */
    private IEntityGroup primFind(String groupID, boolean lockable) throws GroupsException {
        IEntityGroup eg = null;
        Connection conn = null;
        try {
            conn = RDBMServices.getConnection();
            String sql = getFindGroupSql();
            PreparedStatement ps = conn.prepareStatement(sql);
            try {
                ps.setString(1, groupID);
                if (LOG.isDebugEnabled())
                    LOG.debug("RDBMEntityGroupStore.find(): " + ps + " (" + groupID + ")");
                ResultSet rs = ps.executeQuery();
                try {
                    while (rs.next()) {
                        eg =
                                (lockable)
                                        ? lockableInstanceFromResultSet(rs)
                                        : instanceFromResultSet(rs);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (Exception e) {
            LOG.error("RDBMEntityGroupStore.find(): ", e);
            throw new GroupsException("Error retrieving " + groupID + ": ", e);
        } finally {
            RDBMServices.releaseConnection(conn);
        }

        return eg;
    }

    /**
     * Update the entity in the database.
     *
     * @param group IEntityGroup
     * @param conn the database connection
     */
    private void primUpdate(IEntityGroup group, Connection conn)
            throws SQLException, GroupsException {
        try {
            PreparedStatement ps = conn.prepareStatement(getUpdateGroupSql());

            try {
                Integer typeID =
                        EntityTypesLocator.getEntityTypes()
                                .getEntityIDFromType(group.getLeafType());

                ps.setString(1, group.getCreatorID());
                ps.setInt(2, typeID.intValue());
                ps.setString(3, group.getName());
                ps.setString(4, group.getDescription());
                ps.setString(5, group.getLocalKey());

                if (LOG.isDebugEnabled())
                    LOG.debug(
                            "RDBMEntityGroupStore.primUpdate(): "
                                    + ps
                                    + "("
                                    + group.getCreatorID()
                                    + ", "
                                    + typeID
                                    + ", "
                                    + group.getName()
                                    + ", "
                                    + group.getDescription()
                                    + ", "
                                    + group.getLocalKey()
                                    + ")");

                int rc = ps.executeUpdate();

                if (rc != 1) {
                    String errString = "Problem updating " + group;
                    LOG.error(errString);
                    throw new GroupsException(errString);
                }
            } finally {
                ps.close();
            }
        } catch (SQLException sqle) {
            LOG.error("Error updating entity in database. Group: " + group, sqle);
            throw sqle;
        }
    }

    /**
     * Insert and delete group membership rows. The transaction is maintained by the caller.
     *
     * @param egi EntityGroupImpl
     * @param conn the database connection
     */
    private void primUpdateMembers(EntityGroupImpl egi, Connection conn) throws SQLException {
        String groupKey = egi.getLocalKey();
        String memberKey, isGroup, serviceName = null;
        try {
            if (egi.hasDeletes()) {
                List deletedGroups = new ArrayList();
                List deletedEntities = new ArrayList();
                Iterator deletes = egi.getRemovedMembers().values().iterator();
                while (deletes.hasNext()) {
                    IGroupMember gm = (IGroupMember) deletes.next();
                    if (gm.isGroup()) {
                        deletedGroups.add(gm);
                    } else {
                        deletedEntities.add(gm);
                    }
                }

                if (!deletedGroups.isEmpty()) {
                    PreparedStatement psDeleteMemberGroup =
                            conn.prepareStatement(getDeleteMemberGroupSql());

                    try {
                        for (Iterator groups = deletedGroups.iterator(); groups.hasNext(); ) {
                            IEntityGroup removedGroup = (IEntityGroup) groups.next();
                            memberKey = removedGroup.getLocalKey();
                            isGroup = MEMBER_IS_GROUP;
                            serviceName = removedGroup.getServiceName().toString();

                            psDeleteMemberGroup.setString(1, groupKey);
                            psDeleteMemberGroup.setString(2, serviceName);
                            psDeleteMemberGroup.setString(3, memberKey);

                            if (LOG.isDebugEnabled())
                                LOG.debug(
                                        "RDBMEntityGroupStore.primUpdateMembers(): "
                                                + psDeleteMemberGroup
                                                + "("
                                                + groupKey
                                                + ", "
                                                + serviceName
                                                + ", "
                                                + memberKey
                                                + ", isGroup = T)");

                            psDeleteMemberGroup.executeUpdate();
                        } // for
                    } // try
                    finally {
                        psDeleteMemberGroup.close();
                    }
                } // if ( ! deletedGroups.isEmpty() )

                if (!deletedEntities.isEmpty()) {
                    PreparedStatement psDeleteMemberEntity =
                            conn.prepareStatement(getDeleteMemberEntitySql());

                    try {
                        for (Iterator entities = deletedEntities.iterator(); entities.hasNext(); ) {
                            IGroupMember removedEntity = (IGroupMember) entities.next();
                            memberKey = removedEntity.getUnderlyingEntityIdentifier().getKey();
                            isGroup = MEMBER_IS_ENTITY;

                            psDeleteMemberEntity.setString(1, groupKey);
                            psDeleteMemberEntity.setString(2, memberKey);

                            if (LOG.isDebugEnabled())
                                LOG.debug(
                                        "RDBMEntityGroupStore.primUpdateMembers(): "
                                                + psDeleteMemberEntity
                                                + "("
                                                + groupKey
                                                + ", "
                                                + memberKey
                                                + ", "
                                                + "isGroup = F)");

                            psDeleteMemberEntity.executeUpdate();
                        } // for
                    } // try
                    finally {
                        psDeleteMemberEntity.close();
                    }
                } //  if ( ! deletedEntities.isEmpty() )
            }

            if (egi.hasAdds()) {
                PreparedStatement psAdd = conn.prepareStatement(getInsertMemberSql());

                try {
                    Iterator adds = egi.getAddedMembers().values().iterator();
                    while (adds.hasNext()) {
                        IGroupMember addedGM = (IGroupMember) adds.next();
                        memberKey = addedGM.getKey();
                        if (addedGM.isGroup()) {
                            IEntityGroup addedGroup = (IEntityGroup) addedGM;
                            isGroup = MEMBER_IS_GROUP;
                            serviceName = addedGroup.getServiceName().toString();
                            memberKey = addedGroup.getLocalKey();
                        } else {
                            isGroup = MEMBER_IS_ENTITY;
                            serviceName = egi.getServiceName().toString();
                            memberKey = addedGM.getUnderlyingEntityIdentifier().getKey();
                        }

                        psAdd.setString(1, groupKey);
                        psAdd.setString(2, serviceName);
                        psAdd.setString(3, memberKey);
                        psAdd.setString(4, isGroup);

                        if (LOG.isDebugEnabled())
                            LOG.debug(
                                    "RDBMEntityGroupStore.primUpdateMembers(): "
                                            + psAdd
                                            + "("
                                            + groupKey
                                            + ", "
                                            + memberKey
                                            + ", "
                                            + isGroup
                                            + ")");

                        psAdd.executeUpdate();
                    }
                } finally {
                    psAdd.close();
                }
            }

        } catch (SQLException sqle) {
            LOG.error("Error inserting/deleting membership rows.", sqle);
            throw sqle;
        }
    }

    /**
     * @param conn Connection
     * @exception SQLException
     */
    protected static void rollback(Connection conn) throws SQLException {
        SqlTransaction.rollback(conn);
    }

    @Override
    public EntityIdentifier[] searchForGroups(String query, SearchMethod method, Class leaftype)
            throws GroupsException {
        assert query != null;
        assert method != null;
        assert leaftype != null;
        final EntityIdentifier[] r = new EntityIdentifier[0];
        ArrayList ar = new ArrayList();

        final String cacheKey = query + ":" + method.name() + ":" + leaftype.getSimpleName();
        Element el = groupSearchCache.get(cacheKey);
        if (el != null) {
            ar = (ArrayList) el.getObjectValue();
            assert ar != null;
            return (EntityIdentifier[]) ar.toArray(r);
        }

        Connection conn = null;
        PreparedStatement ps = null;
        int type = EntityTypesLocator.getEntityTypes().getEntityIDFromType(leaftype).intValue();
        // System.out.println("Checking out groups of leaftype "+leaftype.getName()+" or "+type);

        try {
            conn = RDBMServices.getConnection();

            switch (method) {
                case DISCRETE:
                    ps = conn.prepareStatement(RDBMEntityGroupStore.SEARCH_GROUPS);
                    break;
                case DISCRETE_CI:
                    ps = conn.prepareStatement(RDBMEntityGroupStore.SEARCH_GROUPS_CASE_INSENSITIVE);
                    break;
                case STARTS_WITH:
                    query = query + "%";
                    ps = conn.prepareStatement(RDBMEntityGroupStore.SEARCH_GROUPS_PARTIAL);
                    break;
                case STARTS_WITH_CI:
                    query = query + "%";
                    ps =
                            conn.prepareStatement(
                                    RDBMEntityGroupStore.SEARCH_GROUPS_PARTIAL_CASE_INSENSITIVE);
                    break;
                case ENDS_WITH:
                    query = "%" + query;
                    ps = conn.prepareStatement(RDBMEntityGroupStore.SEARCH_GROUPS_PARTIAL);
                    break;
                case ENDS_WITH_CI:
                    query = "%" + query;
                    ps =
                            conn.prepareStatement(
                                    RDBMEntityGroupStore.SEARCH_GROUPS_PARTIAL_CASE_INSENSITIVE);
                    break;
                case CONTAINS:
                    query = "%" + query + "%";
                    ps = conn.prepareStatement(RDBMEntityGroupStore.SEARCH_GROUPS_PARTIAL);
                    break;
                case CONTAINS_CI:
                    query = "%" + query + "%";
                    ps =
                            conn.prepareStatement(
                                    RDBMEntityGroupStore.SEARCH_GROUPS_PARTIAL_CASE_INSENSITIVE);
                    break;
                default:
                    throw new GroupsException("Unknown search type");
            }
            try {
                ps.clearParameters();
                ps.setInt(1, type);
                ps.setString(2, query);
                ResultSet rs = ps.executeQuery();
                try {
                    // System.out.println(ps.toString());
                    while (rs.next()) {
                        // System.out.println("result");
                        ar.add(
                                new EntityIdentifier(
                                        rs.getString(1), ICompositeGroupService.GROUP_ENTITY_TYPE));
                    }
                } finally {
                    close(rs);
                }
            } finally {
                close(ps);
            }
        } catch (Exception e) {
            LOG.error("RDBMChannelDefSearcher.searchForEntities(): " + ps, e);
        } finally {
            RDBMServices.releaseConnection(conn);
        }
        el = new Element(cacheKey, ar);
        groupSearchCache.put(el);
        return (EntityIdentifier[]) ar.toArray(r);
    }

    /**
     * @param conn Connection
     * @param newValue boolean
     * @exception SQLException The exception description.
     */
    protected static void setAutoCommit(Connection conn, boolean newValue) throws SQLException {
        SqlTransaction.setAutoCommit(conn, newValue);
    }

    /** @return RDBMEntityGroupStore */
    public static synchronized RDBMEntityGroupStore singleton() throws GroupsException {
        if (singleton == null) {
            singleton = new RDBMEntityGroupStore();
        }
        return singleton;
    }
    /** @return String */
    private static String sqlQuote(Object o) {
        return QUOTE + o + QUOTE;
    }
    /**
     * Commit this entity AND ITS MEMBERSHIPS to the underlying store.
     *
     * @param group IEntityGroup
     */
    @Override
    public void update(IEntityGroup group) throws GroupsException {
        Connection conn = null;
        boolean exists = existsInDatabase(group);
        try {
            conn = RDBMServices.getConnection();
            setAutoCommit(conn, false);

            try {
                if (exists) {
                    primUpdate(group, conn);
                } else {
                    primAdd(group, conn);
                }
                primUpdateMembers((EntityGroupImpl) group, conn);
                commit(conn);
            } catch (Exception ex) {
                rollback(conn);
                throw new GroupsException("Problem updating " + this + ex);
            }
        } catch (SQLException sqlex) {
            throw new GroupsException(sqlex);
        } finally {
            if (conn != null) {
                try {
                    setAutoCommit(conn, true);
                } catch (SQLException sqle) {
                    throw new GroupsException(sqle);
                } finally {
                    RDBMServices.releaseConnection(conn);
                }
            }
        }
    }

    /**
     * Insert and delete group membership rows inside a transaction.
     *
     * @param eg IEntityGroup
     */
    @Override
    public void updateMembers(IEntityGroup eg) throws GroupsException {
        Connection conn = null;
        EntityGroupImpl egi = (EntityGroupImpl) eg;
        if (egi.isDirty())
            try {
                conn = RDBMServices.getConnection();
                setAutoCommit(conn, false);

                try {
                    primUpdateMembers(egi, conn);
                    commit(conn);
                } catch (SQLException sqle) {
                    rollback(conn);
                    throw new GroupsException("Problem updating memberships for " + egi, sqle);
                }
            } catch (SQLException sqlex) {
                throw new GroupsException(sqlex);
            } finally {
                if (conn != null) {
                    try {
                        setAutoCommit(conn, true);
                    } catch (SQLException sqle) {
                        throw new GroupsException(sqle);
                    } finally {
                        RDBMServices.releaseConnection(conn);
                    }
                }
            }
    }

    private static final void close(final Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LOG.warn("problem closing statement", e);
            }
        }
    }

    private static final void close(final ResultSet resultset) {
        if (resultset != null) {
            try {
                resultset.close();
            } catch (SQLException e) {
                LOG.warn("problem closing resultset", e);
            }
        }
    }
}
