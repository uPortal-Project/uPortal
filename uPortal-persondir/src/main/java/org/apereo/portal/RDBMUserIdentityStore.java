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
package org.apereo.portal;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.jdbc.RDBMServices;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.apereo.portal.layout.dao.IStylesheetUserPreferencesDao;
import org.apereo.portal.layout.om.IStylesheetUserPreferences;
import org.apereo.portal.persondir.ILocalAccountDao;
import org.apereo.portal.persondir.ILocalAccountPerson;
import org.apereo.portal.portlet.dao.IPortletEntityDao;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.PersonFactory;
import org.apereo.portal.security.provider.BrokenSecurityContext;
import org.apereo.portal.security.provider.PersonImpl;
import org.apereo.portal.spring.locator.CounterStoreLocator;
import org.apereo.portal.utils.SerializableObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * SQL implementation for managing creation and removal of User Portal Data
 *
 * <p>Dalquist - edalquist@unicon.net)
 */
@Service("userIdentityStore")
public class RDBMUserIdentityStore implements IUserIdentityStore {

    private static final Log log = LogFactory.getLog(RDBMUserIdentityStore.class);
    private static final String PROFILE_TABLE = "UP_USER_PROFILE";

    private static final String USERNAME_VALIDATOR_REGEX = "^[^\\s]{1,100}$";
    private static final Pattern USERNAME_VALIDATOR_PATTERN =
            Pattern.compile(USERNAME_VALIDATOR_REGEX);

    /**
     * This value used to come from the user's "template user," but was always 1 anyways.
     *
     * @since 5.3
     */
    private static final int USER_DFLT_LAY_ID = 1;

    private JdbcOperations jdbcOperations;
    private TransactionOperations transactionOperations;
    private IPortletEntityDao portletEntityDao;
    private IStylesheetUserPreferencesDao stylesheetUserPreferencesDao;
    private ILocalAccountDao localAccountDao;
    private Ehcache userLockCache;

    @Autowired
    public void setPortletEntityDao(@Qualifier("persistence") IPortletEntityDao portletEntityDao) {
        this.portletEntityDao = portletEntityDao;
    }

    @Autowired
    public void setStylesheetUserPreferencesDao(
            IStylesheetUserPreferencesDao stylesheetUserPreferencesDao) {
        this.stylesheetUserPreferencesDao = stylesheetUserPreferencesDao;
    }

    @Autowired
    public void setLocalAccountDao(ILocalAccountDao localAccountDao) {
        this.localAccountDao = localAccountDao;
    }

    @Autowired
    @Qualifier("org.apereo.portal.RDBMUserIdentityStore.userLockCache")
    public void setUserLockCache(Ehcache userLockCache) {
        this.userLockCache =
                new SelfPopulatingCache(userLockCache, key -> new SerializableObject());
    }

    @Autowired
    public void setPlatformTransactionManager(
            @Qualifier(BasePortalJpaDao.PERSISTENCE_UNIT_NAME)
                    PlatformTransactionManager platformTransactionManager) {
        this.transactionOperations = new TransactionTemplate(platformTransactionManager);
    }

    @javax.annotation.Resource(name = BasePortalJpaDao.PERSISTENCE_UNIT_NAME)
    public void setDataSource(DataSource dataSource) {
        this.jdbcOperations = new JdbcTemplate(dataSource);
    }

    private Serializable getLock(IPerson person) {
        final String username = (String) person.getAttribute(IPerson.USERNAME);
        return this.userLockCache.get(username);
    }

    /**
     * getuPortalUID - return a unique uPortal key for a user. calls alternate signature with
     * createPortalData set to false.
     *
     * @param person the person object
     * @return uPortalUID number
     */
    @Override
    public int getPortalUID(IPerson person) throws AuthorizationException {
        return getPortalUID(person, false);
    }

    @Override
    public void removePortalUID(final String userName) {
        this.transactionOperations.execute(
                new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus arg0) {
                        if (PersonFactory.getGuestUsernames().contains(userName)) {
                            throw new IllegalArgumentException(
                                    "CANNOT RESET LAYOUT FOR A GUEST USER");
                        }

                        final int userId =
                                jdbcOperations.queryForObject(
                                        "SELECT USER_ID FROM UP_USER WHERE USER_NAME=?",
                                        Integer.class,
                                        userName);

                        final int type =
                                jdbcOperations.queryForObject(
                                        "SELECT ENTITY_TYPE_ID FROM UP_ENTITY_TYPE WHERE ENTITY_TYPE_NAME = ?",
                                        Integer.class,
                                        IPerson.class.getName());

                        jdbcOperations.update(
                                "DELETE FROM UP_PERMISSION WHERE PRINCIPAL_KEY=? AND PRINCIPAL_TYPE=?",
                                userName,
                                type);

                        final List<Integer> groupIds =
                                jdbcOperations.queryForList(
                                        "SELECT M.GROUP_ID "
                                                + "FROM UP_GROUP_MEMBERSHIP M, UP_GROUP G, UP_ENTITY_TYPE E "
                                                + "WHERE M.GROUP_ID = G.GROUP_ID "
                                                + "  AND G.ENTITY_TYPE_ID = E.ENTITY_TYPE_ID "
                                                + "  AND  E.ENTITY_TYPE_NAME = 'org.apereo.portal.security.IPerson'"
                                                + "  AND  M.MEMBER_KEY =? AND  M.MEMBER_IS_GROUP = 'F'",
                                        Integer.class,
                                        userName);

                        // Remove from local group
                        // Delete from DeleteUser.java and place here
                        // must be made before delete user in UP_USER
                        for (final Integer groupId : groupIds) {
                            // GROUP_ID is a VARCHAR
                            String gid = groupId.toString();
                            jdbcOperations.update(
                                    "DELETE FROM UP_GROUP_MEMBERSHIP WHERE MEMBER_KEY=? AND GROUP_ID=?",
                                    userName,
                                    gid);
                        }

                        jdbcOperations.update(
                                "DELETE FROM UP_USER            WHERE USER_ID = ?", userId);
                        jdbcOperations.update(
                                "DELETE FROM UP_USER_LAYOUT     WHERE USER_ID = ?", userId);
                        jdbcOperations.update(
                                "DELETE FROM UP_USER_PROFILE    WHERE USER_ID = ?", userId);
                        jdbcOperations.update(
                                "DELETE FROM UP_LAYOUT_PARAM    WHERE USER_ID = ?", userId);
                        jdbcOperations.update(
                                "DELETE FROM UP_LAYOUT_STRUCT   WHERE USER_ID = ?", userId);
                        jdbcOperations.update(
                                "DELETE FROM UP_USER_LOCALE     WHERE USER_ID = ?", userId);

                        // Purge all portlet entity data
                        final Set<IPortletEntity> portletEntities =
                                portletEntityDao.getPortletEntitiesForUser(userId);
                        for (final IPortletEntity portletEntity : portletEntities) {
                            portletEntityDao.deletePortletEntity(portletEntity);
                        }

                        // Purge all stylesheet preference data
                        final List<? extends IStylesheetUserPreferences> stylesheetUserPreferences =
                                stylesheetUserPreferencesDao.getStylesheetUserPreferencesForUser(
                                        userId);
                        for (final IStylesheetUserPreferences stylesheetUserPreference :
                                stylesheetUserPreferences) {
                            stylesheetUserPreferencesDao.deleteStylesheetUserPreferences(
                                    stylesheetUserPreference);
                        }

                        final ILocalAccountPerson person = localAccountDao.getPerson(userName);
                        if (person != null) {
                            localAccountDao.deleteAccount(person);
                        }
                    }
                });
    }

    /**
     * removeuPortalUID
     *
     * @param uPortalUID integer key to uPortal data for a user
     */
    @Override
    public void removePortalUID(final int uPortalUID) {

        this.transactionOperations.execute(
                new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus arg0) {
                        final String name =
                                jdbcOperations.queryForObject(
                                        "SELECT USER_NAME FROM UP_USER WHERE USER_ID=?",
                                        String.class,
                                        uPortalUID);
                        if (name == null) {
                            log.warn(
                                    "No user exists for id "
                                            + uPortalUID
                                            + " Nothing will be deleted");
                            return;
                        }

                        removePortalUID(name);
                    }
                });
    }

    /**
     * Get the portal user ID for this person object.
     *
     * @param person The {@link IPerson} for whom a UID is requested
     * @param createPortalData indicating whether to try to create all uPortal data for this user
     *     from template prototype
     * @return uPortalUID number or -1 if unable to create user.
     * @throws AuthorizationException if createPortalData is false and no user is found or if a sql
     *     error is encountered
     */
    @Override
    public int getPortalUID(IPerson person, boolean createPortalData)
            throws AuthorizationException {
        int uid;
        String username = (String) person.getAttribute(IPerson.USERNAME);

        // only synchronize a non-guest request.
        if (PersonFactory.getGuestUsernames().contains(username)) {
            uid = __getPortalUID(person, createPortalData);
        } else {
            synchronized (getLock(person)) {
                uid = __getPortalUID(person, createPortalData);
            }
        }
        return uid;
    }

    @Override
    public IPerson getPerson(String userName, boolean createPortalData)
            throws AuthorizationException {

        final IPerson result = new PersonImpl();
        result.setUserName(userName);
        result.setID(getPortalUID(result, createPortalData));
        result.setSecurityContext(new BrokenSecurityContext());
        return result;
    }

    /* (non-javadoc)
     * @see org.apereo.portal.IUserIdentityStore#getPortalUserName(int)
     */
    @Override
    public String getPortalUserName(final int uPortalUID) {
        final List<String> results =
                this.jdbcOperations.queryForList(
                        "SELECT USER_NAME FROM UP_USER WHERE USER_ID=?", String.class, uPortalUID);
        return DataAccessUtils.singleResult(results);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IUserIdentityStore#getPortalUserId(java.lang.String)
     */
    @Override
    public Integer getPortalUserId(String userName) {
        final List<Integer> results =
                this.jdbcOperations.queryForList(
                        "SELECT USER_ID FROM UP_USER WHERE USER_NAME=?", Integer.class, userName);
        return DataAccessUtils.singleResult(results);
    }

    @Override
    public boolean validateUsername(final String username) {
        /*
         * The rules, so far...
         *   - Must not be blank
         *   - Must not contain spaces
         *   - Must not exceed 100 characters (current DB column size)
         */
        return USERNAME_VALIDATOR_PATTERN.matcher(username).matches();
    }

    private int __getPortalUID(IPerson person, boolean createPortalData)
            throws AuthorizationException {

        PortalUser portalUser;

        try {
            String userName = person.getUserName();
            portalUser = getPortalUser(userName);

            if (createPortalData) {
                // If we are allowed to modify the database

                if (portalUser == null) {
                    // Get a new user ID for this user
                    int newUID = getNewPortalUID();

                    // Add new user to all appropriate tables
                    int newPortalUID = addNewUser(newUID, person);
                    portalUser = new PortalUser();
                    portalUser.setUserId(newPortalUID);
                }
            } else if (portalUser == null) {
                // If this is a new user and we can't create them
                throw new AuthorizationException(
                        "No portal information exists for user " + userName);
            }
        } catch (AuthorizationException e) {
            throw e;

        } catch (Exception e) {
            final String msg =
                    "Failed to obtain a portal user Id for the specified person:  " + person;
            throw new RuntimeException(msg, e);
        }

        return portalUser.getUserId();
    }

    private int getNewPortalUID() {
        return CounterStoreLocator.getCounterStore().getNextId("UP_USER");
    }

    /**
     * Gets the PortalUser data store object for the specified user name.
     *
     * @param userName The user's name
     * @return A PortalUser object or null if the user doesn't exist.
     */
    private PortalUser getPortalUser(final String userName) {
        return jdbcOperations.execute(
                (ConnectionCallback<PortalUser>)
                        con -> {
                            PortalUser portalUser = null;
                            PreparedStatement pstmt = null;

                            try {
                                String query = "SELECT USER_ID FROM UP_USER WHERE USER_NAME=?";

                                pstmt = con.prepareStatement(query);
                                pstmt.setString(1, userName);

                                ResultSet rs = null;
                                try {
                                    if (log.isDebugEnabled())
                                        log.debug(
                                                "RDBMUserIdentityStore::getPortalUID(userName="
                                                        + userName
                                                        + "): "
                                                        + query);
                                    rs = pstmt.executeQuery();
                                    if (rs.next()) {
                                        portalUser = new PortalUser();
                                        portalUser.setUserId(rs.getInt("USER_ID"));
                                        portalUser.setUserName(userName);
                                    }
                                } finally {
                                    try {
                                        if (rs != null) {
                                            rs.close();
                                        }
                                    } catch (Exception e) {
                                    }
                                }
                            } finally {
                                try {
                                    if (pstmt != null) {
                                        pstmt.close();
                                    }
                                } catch (Exception e) {
                                }
                            }

                            return portalUser;
                        });
    }

    private int addNewUser(final int newUID, final IPerson person) {

        return this.transactionOperations.execute(
                status ->
                        jdbcOperations.execute(
                                new ConnectionCallback<Integer>() {
                                    @Override
                                    public Integer doInConnection(Connection con)
                                            throws SQLException, DataAccessException {

                                        int uPortalUID;
                                        PreparedStatement queryStmt = null;
                                        PreparedStatement insertStmt = null;
                                        try {
                                            // Add to UP_USER
                                            String insert =
                                                    "INSERT INTO UP_USER (USER_ID, USER_NAME, USER_DFLT_LAY_ID, NEXT_STRUCT_ID, LST_CHAN_UPDT_DT)"
                                                            + "VALUES (?, ?, ?, null, null)";

                                            String userName = person.getUserName();

                                            insertStmt = con.prepareStatement(insert);
                                            insertStmt.setInt(1, newUID);
                                            insertStmt.setString(2, userName);
                                            insertStmt.setInt(3, USER_DFLT_LAY_ID);

                                            if (log.isDebugEnabled())
                                                log.debug(
                                                        "RDBMUserIdentityStore::addNewUser(USER_ID="
                                                                + newUID
                                                                + ", USER_NAME="
                                                                + userName
                                                                + ", USER_DFLT_LAY_ID="
                                                                + USER_DFLT_LAY_ID
                                                                + "): "
                                                                + insert);
                                            insertStmt.executeUpdate();
                                            insertStmt.close();
                                            insertStmt = null;

                                            // Start copying...
                                            ResultSet rs = null;
                                            String query;
                                            try {

                                                /*
                                                 * NOTE:  in former times, we used a "template user" for this
                                                 * purpose;  going forward we will use the system profile(s).
                                                 */
                                                final IPerson system =
                                                        PersonFactory.createSystemPerson();
                                                query =
                                                        "SELECT upup.USER_ID, upup.PROFILE_FNAME, upup.PROFILE_NAME, upup.DESCRIPTION, "
                                                                + "upup.STRUCTURE_SS_ID, upup.THEME_SS_ID "
                                                                + "FROM UP_USER upu, UP_USER_PROFILE upup "
                                                                + "WHERE upup.USER_ID = upu.USER_ID "
                                                                + "AND upu.USER_NAME = ?";
                                                queryStmt = con.prepareStatement(query);
                                                queryStmt.setString(1, system.getUserName());
                                                if (log.isDebugEnabled())
                                                    log.debug(
                                                            "RDBMUserIdentityStore::addNewUser(USER_NAME="
                                                                    + system.getUserName()
                                                                    + "): "
                                                                    + query);
                                                rs = queryStmt.executeQuery();

                                                insert =
                                                        "INSERT INTO UP_USER_PROFILE (USER_ID, PROFILE_ID, PROFILE_FNAME, PROFILE_NAME, DESCRIPTION, LAYOUT_ID, STRUCTURE_SS_ID, THEME_SS_ID) "
                                                                + "VALUES(?, ?, ?, ?, ?, NULL, ?, ?)";
                                                insertStmt = con.prepareStatement(insert);
                                                while (rs.next()) {
                                                    int id = getNextKey();

                                                    String profileFname =
                                                            rs.getString("PROFILE_FNAME");
                                                    String profileName =
                                                            rs.getString("PROFILE_NAME");
                                                    String description =
                                                            rs.getString("DESCRIPTION");
                                                    int structure = rs.getInt("STRUCTURE_SS_ID");
                                                    int theme = rs.getInt("THEME_SS_ID");

                                                    insertStmt.setInt(1, newUID);
                                                    insertStmt.setInt(2, id);
                                                    insertStmt.setString(3, profileFname);
                                                    insertStmt.setString(4, profileName);
                                                    insertStmt.setString(5, description);
                                                    insertStmt.setInt(6, structure);
                                                    insertStmt.setInt(7, theme);

                                                    if (log.isDebugEnabled())
                                                        log.debug(
                                                                "RDBMUserIdentityStore::addNewUser(USER_ID="
                                                                        + newUID
                                                                        + ", PROFILE_FNAME="
                                                                        + profileFname
                                                                        + ", PROFILE_NAME="
                                                                        + profileName
                                                                        + ", DESCRIPTION="
                                                                        + description
                                                                        + "): "
                                                                        + insert);
                                                    insertStmt.executeUpdate();
                                                }
                                                rs.close();
                                                queryStmt.close();

                                                if (insertStmt != null) {
                                                    insertStmt.close();
                                                    insertStmt = null;
                                                }

                                                // If we made it all the way though, commit the
                                                // transaction
                                                if (RDBMServices.getDbMetaData()
                                                        .supportsTransactions()) con.commit();

                                                uPortalUID = newUID;

                                            } finally {
                                                try {
                                                    if (rs != null) rs.close();
                                                } catch (Exception e) {
                                                }
                                            }
                                        } finally {
                                            try {
                                                if (queryStmt != null) queryStmt.close();
                                            } catch (Exception e) {
                                            }
                                            try {
                                                if (insertStmt != null) insertStmt.close();
                                            } catch (Exception e) {
                                            }
                                        }

                                        return uPortalUID;
                                    }
                                }));
    }

    private int getNextKey() {
        return CounterStoreLocator.getCounterStore().getNextId(PROFILE_TABLE);
    }

    protected static class PortalUser {
        String userName;
        int userId;

        public String getUserName() {
            return userName;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }
    }
}
