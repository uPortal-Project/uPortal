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

package org.jasig.portal.layout.simple;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.UserProfile;
import org.jasig.portal.i18n.ILocaleStore;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.LayoutStructure;
import org.jasig.portal.layout.dao.IStylesheetDescriptorDao;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.rdbm.DatabaseMetaDataImpl;
import org.jasig.portal.rdbm.IDatabaseMetadata;
import org.jasig.portal.rdbm.IJoinQueryString;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.provider.PersonImpl;
import org.jasig.portal.spring.locator.CounterStoreLocator;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.ICounterStore;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.cache.Cache;

/**
 * SQL implementation for the 2.x relational database model.
 *
 * Prior to uPortal 2.5, this class existed in the org.jasig.portal package.  It was
 * moved to its present package to express that it is part of the
 * Simple Layout Manager implementation.
 *
 * @author George Lindholm
 * @version $Revision$ $Date$
 */
public abstract class RDBMUserLayoutStore implements IUserLayoutStore, InitializingBean {

    protected final Log log = LogFactory.getLog(getClass());
    private static String PROFILE_TABLE = "UP_USER_PROFILE";

  //This class is instantiated ONCE so NO class variables can be used to keep state between calls
  protected static final String channelPrefix = "n";
  protected static final String folderPrefix = "s";
  
  protected TransactionOperations transactionOperations;
  protected TransactionOperations nextStructTransactionOperations;
  protected JdbcOperations jdbcOperations;
  
  private ILocaleStore localeStore; 
  protected IDatabaseMetadata databaseMetadata;
  protected IPersonManager personManager;
  protected ICounterStore counterStore;
  protected IPortletDefinitionRegistry portletDefinitionRegistry;
  protected IStylesheetDescriptorDao stylesheetDescriptorDao;
  protected SQLExceptionTranslator exceptionTranslator;
  
  // I18n property
  protected static final boolean localeAware = LocaleManager.isLocaleAware();
  
    @Autowired
    public void setLocaleStore(ILocaleStore localeStore) {
        this.localeStore = localeStore;
    }

    @Autowired
    public void setStylesheetDescriptorDao(IStylesheetDescriptorDao stylesheetDescriptorDao) {
        this.stylesheetDescriptorDao = stylesheetDescriptorDao;
    }

    @Autowired
    public void setPlatformTransactionManager(@Qualifier("PortalDb") PlatformTransactionManager platformTransactionManager) {
        this.transactionOperations = new TransactionTemplate(platformTransactionManager);
        
        final DefaultTransactionDefinition nextStructTransactionDefinition = new DefaultTransactionDefinition();
        nextStructTransactionDefinition.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.nextStructTransactionOperations = new TransactionTemplate(platformTransactionManager, nextStructTransactionDefinition);
    }

    @Resource(name="PortalDb")
    public void setDataSource(DataSource dataSource) {
        this.jdbcOperations = new JdbcTemplate(dataSource);
        this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    }

    @Autowired
    public void setDatabaseMetadata(IDatabaseMetadata databaseMetadata) {
        this.databaseMetadata = databaseMetadata;
    }

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }
    
    @Autowired
    public void setCounterStore(ICounterStore counterStore) {
        this.counterStore = counterStore;
    }
    
    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.databaseMetadata.supportsOuterJoins()) {
            final IJoinQueryString joinQuery = this.databaseMetadata.getJoinQuery();

            if (joinQuery instanceof DatabaseMetaDataImpl.JdbcDb) {
                joinQuery.addQuery("layout",
                        "{oj UP_LAYOUT_STRUCT ULS LEFT OUTER JOIN UP_LAYOUT_PARAM USP ON ULS.USER_ID = USP.USER_ID AND ULS.STRUCT_ID = USP.STRUCT_ID} WHERE");
                joinQuery.addQuery("ss_struct",
                        "{oj UP_SS_STRUCT USS LEFT OUTER JOIN UP_SS_STRUCT_PAR USP ON USS.SS_ID=USP.SS_ID} WHERE");
                joinQuery.addQuery("ss_theme",
                        "{oj UP_SS_THEME UTS LEFT OUTER JOIN UP_SS_THEME_PARM UTP ON UTS.SS_ID=UTP.SS_ID} WHERE");
            }
            else if (joinQuery instanceof DatabaseMetaDataImpl.PostgreSQLDb) {
                joinQuery.addQuery("layout",
                        "UP_LAYOUT_STRUCT ULS LEFT OUTER JOIN UP_LAYOUT_PARAM USP ON ULS.USER_ID = USP.USER_ID AND ULS.STRUCT_ID = USP.STRUCT_ID WHERE");
                joinQuery.addQuery("ss_struct",
                        "UP_SS_STRUCT USS LEFT OUTER JOIN UP_SS_STRUCT_PAR USP ON USS.SS_ID=USP.SS_ID WHERE");
                joinQuery.addQuery("ss_theme",
                        "UP_SS_THEME UTS LEFT OUTER JOIN UP_SS_THEME_PARM UTP ON UTS.SS_ID=UTP.SS_ID WHERE");
            }
            else if (joinQuery instanceof DatabaseMetaDataImpl.OracleDb) {
                joinQuery.addQuery("layout",
                        "UP_LAYOUT_STRUCT ULS, UP_LAYOUT_PARAM USP WHERE ULS.STRUCT_ID = USP.STRUCT_ID(+) AND ULS.USER_ID = USP.USER_ID(+) AND");
                joinQuery.addQuery("ss_struct",
                        "UP_SS_STRUCT USS, UP_SS_STRUCT_PAR USP WHERE USS.SS_ID=USP.SS_ID(+) AND");
                joinQuery.addQuery("ss_theme", "UP_SS_THEME UTS, UP_SS_THEME_PARM UTP WHERE UTS.SS_ID=UTP.SS_ID(+) AND");
            }
            else {
                throw new RuntimeException("Unknown database driver");
            }
        }

    }
  
    private final SingletonDoubleCheckedCreator<IPerson> systemPersonCreator = new SingletonDoubleCheckedCreator<IPerson>() {
        protected IPerson createSingleton(Object... args) {
            // be sure we only do this once...
            // Load the "system" user id from the database
            final int systemUserId = jdbcOperations.queryForInt("SELECT USER_ID FROM UP_USER WHERE USER_NAME = 'system'");
            log.info("Found user id " + systemUserId + " for the 'system' user.");
            return new SystemUser(systemUserId);
        }
    };

    private final IPerson getSystemUser() {
        return this.systemPersonCreator.get();
    }

  /**
   * Add a user profile
   * @param person
   * @param profile
   * @return userProfile
   * @exception Exception
   */
  public UserProfile addUserProfile (final IPerson person, final IUserProfile profile) {
    final int userId = person.getID();
    // generate an id for this profile

    return this.jdbcOperations.execute(new ConnectionCallback<UserProfile>() {
        @Override
        public UserProfile doInConnection(Connection con) throws SQLException, DataAccessException {
          String sQuery = null;
          PreparedStatement pstmt = con.prepareStatement("INSERT INTO UP_USER_PROFILE " +
          		"(USER_ID,PROFILE_ID,PROFILE_FNAME,PROFILE_NAME,STRUCTURE_SS_ID,THEME_SS_ID," +
          		"DESCRIPTION, LAYOUT_ID) VALUES (?,?,?,?,?,?,?,?)");
          int profileId = getNextKey();
          pstmt.setInt(1, userId);
          pstmt.setInt(2, profileId);
          pstmt.setString(3, profile.getProfileFname());
          pstmt.setString(4, profile.getProfileName());
          pstmt.setInt(5, profile.getStructureStylesheetId());
          pstmt.setInt(6, profile.getThemeStylesheetId());
          pstmt.setString(7, profile.getProfileDescription());
          pstmt.setInt(8, profile.getLayoutId());
          sQuery = "INSERT INTO UP_USER_PROFILE (USER_ID,PROFILE_ID,PROFILE_FNAME,PROFILE_NAME,STRUCTURE_SS_ID,THEME_SS_ID,DESCRIPTION, LAYOUT_ID) VALUES ("
              + userId + ",'" + profileId + ",'" + profile.getProfileFname() + "','" + profile.getProfileName() + "'," + profile.getStructureStylesheetId()
              + "," + profile.getThemeStylesheetId() + ",'" + profile.getProfileDescription() + "', "+profile.getLayoutId()+")";
          if (log.isDebugEnabled())
              log.debug("RDBMUserLayoutStore::addUserProfile(): " + sQuery);
          try {
            pstmt.executeUpdate();
            
            UserProfile newProfile = new UserProfile();
            newProfile.setProfileId(profileId);
            newProfile.setLayoutId(profile.getLayoutId());
            newProfile.setLocaleManager(profile.getLocaleManager());
            newProfile.setProfileDescription(profile.getProfileDescription());
            newProfile.setProfileFname(profile.getProfileFname());
            newProfile.setProfileName(profile.getProfileName());
            newProfile.setStructureStylesheetId(profile.getStructureStylesheetId());
            newProfile.setSystemProfile(false);
            newProfile.setThemeStylesheetId(profile.getThemeStylesheetId());
            
            return newProfile;
    
          } finally {
            pstmt.close();
          }
        }
    });
  }

  private int getNextKey()
  {
      return CounterStoreLocator.getCounterStore().getNextId(PROFILE_TABLE);
  }

  /**
   * Checks if a channel has been approved
   * @param approvedDate
   * @return boolean Channel is approved
   */
   protected static boolean channelApproved(java.util.Date approvedDate) {
      java.util.Date rightNow = new java.util.Date();
      return (approvedDate != null && rightNow.after(approvedDate));
   }

  /**
   * Create a layout
   * @param layoutStructure
   * @param doc
   * @param root
   * @param structId
   * @exception java.sql.SQLException
   */
   protected final void createLayout (HashMap layoutStructure, Document doc,
        Element root, int structId) throws java.sql.SQLException {
      while (structId != 0) {
        LayoutStructure ls = (LayoutStructure) layoutStructure.get(new Integer(structId));
        // replaced with call to method in containing class to allow overriding
        // by subclasses of RDBMUserLayoutStore.
        // Element structure = ls.getStructureDocument(doc);
        Element structure = getStructure(doc, ls);
        root.appendChild(structure);

        String id = structure.getAttribute("ID");
        if (id != null && ! id.equals("")) {
            structure.setIdAttribute("ID", true);
        }

        createLayout(layoutStructure, doc,  structure, ls.getChildId());
        structId = ls.getNextId();
      }
  }

  /**
   * convert true/false into Y/N for database
   * @param value to check
   * @result boolean
   */
  protected static final boolean xmlBool (String value) {
      return (value != null && value.equals("true") ? true : false);
  }

  public void deleteUserProfile(IPerson person, int profileId) {
    int userId = person.getID();
    deleteUserProfile(userId,profileId);
  }

  private void deleteUserProfile(final int userId, final int profileId) {
      this.jdbcOperations.execute(new ConnectionCallback<Object>() {
          @Override
          public Object doInConnection(Connection con) throws SQLException, DataAccessException {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "DELETE FROM UP_USER_PROFILE WHERE USER_ID=" + userId + " AND PROFILE_ID=" + Integer.toString(profileId);
        if (log.isDebugEnabled())
            log.debug("RDBMUserLayoutStore::deleteUserProfile() : " + sQuery);
        stmt.executeUpdate(sQuery);

        // remove profile mappings
        sQuery= "DELETE FROM UP_USER_UA_MAP WHERE USER_ID=" + userId + " AND PROFILE_ID=" + Integer.toString(profileId);
        if (log.isDebugEnabled())
            log.debug("RDBMUserLayoutStore::deleteUserProfile() : " + sQuery);
        stmt.executeUpdate(sQuery);

        // remove parameter information
        sQuery= "DELETE FROM UP_SS_USER_PARM WHERE USER_ID=" + userId + " AND PROFILE_ID=" + Integer.toString(profileId);
        if (log.isDebugEnabled())
            log.debug("RDBMUserLayoutStore::deleteUserProfile() : " + sQuery);
        stmt.executeUpdate(sQuery);

        sQuery= "DELETE FROM UP_SS_USER_ATTS WHERE USER_ID=" + userId + " AND PROFILE_ID=" + Integer.toString(profileId);
        if (log.isDebugEnabled())
            log.debug("RDBMUserLayoutStore::deleteUserProfile() : " + sQuery);
        stmt.executeUpdate(sQuery);

      } finally {
        stmt.close();
      }
      
      return null;
          }
      });
  }

  /**
   * Dump a document tree structure on stdout
   * @param node
   * @param indent
   */
  public static final void dumpDoc (Node node, String indent) {
    if (node == null) {
      return;
    }
    if (node instanceof Element) {
      System.err.print(indent + "element: tag=" + ((Element)node).getTagName() + " ");
    }
    else if (node instanceof Document) {
      System.err.print("document:");
    }
    else {
      System.err.print(indent + "node:");
    }
    System.err.println("name=" + node.getNodeName() + " value=" + node.getNodeValue());
    NamedNodeMap nm = node.getAttributes();
    if (nm != null) {
      for (int i = 0; i < nm.getLength(); i++) {
        System.err.println(indent + " " + nm.item(i).getNodeName() + ": '" + nm.item(i).getNodeValue() + "'");
      }
      System.err.println(indent + "--");
    }
    if (node.hasChildNodes()) {
      dumpDoc(node.getFirstChild(), indent + "   ");
    }
    dumpDoc(node.getNextSibling(), indent);
  }

  /**
   * Return the next available channel structure id for a user
   * @param person
   * @return the next available channel structure id
   */
  public String generateNewChannelSubscribeId (IPerson person) {
    return  getNextStructId(person, channelPrefix);
  }

  /**
   * Return the next available folder structure id for a user
   * @param person
   * @return a <code>String</code> that is the next free structure ID
   * @exception Exception
   */
  public String generateNewFolderId (IPerson person) {
    return  getNextStructId(person, folderPrefix);
  }

  /**
   * Return the next available structure id for a user
   * @param person
   * @param prefix
   * @return next free structure ID
   * @exception Exception
   */
    protected String getNextStructId(final IPerson person, final String prefix) {
        final int userId = person.getID();
        return this.nextStructTransactionOperations.execute(new TransactionCallback<String>() {
            @Override
            public String doInTransaction(TransactionStatus status) {
                return jdbcOperations.execute(new ConnectionCallback<String>() {
                    @Override
                    public String doInConnection(Connection con) throws SQLException, DataAccessException {

                        Statement stmt = con.createStatement();
                        try {
                            String sQuery = "SELECT NEXT_STRUCT_ID FROM UP_USER WHERE USER_ID=" + userId;
                            if (log.isDebugEnabled())
                                log.debug("RDBMUserLayoutStore::getNextStructId(): " + sQuery);
                            ResultSet rs = stmt.executeQuery(sQuery);
                            int currentStructId;
                            try {
                                if (rs.next()) {
                                    currentStructId = rs.getInt(1);
                                }
                                else {
                                    throw new SQLException("no rows returned for query [" + sQuery + "]");
                                }
                            }
                            finally {
                                rs.close();
                            }
                            int nextStructId = currentStructId + 1;
                            String sUpdate = "UPDATE UP_USER SET NEXT_STRUCT_ID=" + nextStructId + " WHERE USER_ID="
                                    + userId + " AND NEXT_STRUCT_ID=" + currentStructId;
                            if (log.isDebugEnabled())
                                log.debug("RDBMUserLayoutStore::getNextStructId(): " + sUpdate);
                            stmt.executeUpdate(sUpdate);
                            return prefix + nextStructId;
                        }
                        finally {
                            stmt.close();
                        }
                    }
                });
            }
        });
    }

  /**
   * Return the Structure ID tag
   * @param  structId
   * @param  chanId
   * @return ID tag
   */
  protected String getStructId(int structId, int chanId) {
    if (chanId == 0) {
      return folderPrefix + structId;
    } else {
      return channelPrefix + structId;
    }
  }

  // private helper modules that retreive information from the DOM structure of the description files
  private String getName (Document descr) {
    NodeList names = descr.getElementsByTagName("name");
    Node name = null;
    for (int i = names.getLength() - 1; i >= 0; i--) {
      name = names.item(i);
      if (name.getParentNode().getNodeName().equals("stylesheetdescription"))
        break;
      else
        name = null;
    }
    if (name != null) {
      return  this.getTextChildNodeValue(name);
    }
    else {
        if (log.isDebugEnabled())
            log.debug("RDBMUserLayoutStore::getName() : no \"name\" element was found under the \"stylesheetdescription\" node!");
      return  null;
    }
  }

  private String getRootElementTextValue (Document descr, String elementName) {
    NodeList names = descr.getElementsByTagName(elementName);
    Node name = null;
    for (int i = names.getLength() - 1; i >= 0; i--) {
      name = names.item(i);

      if (name.getParentNode().getNodeName().equals("stylesheetdescription"))
        break;
      else
        name = null;
    }
    if (name != null) {
      return  this.getTextChildNodeValue(name);
    }
    else {
        if (log.isDebugEnabled())
            log.debug("RDBMUserLayoutStore::getRootElementTextValue() : no \"" + elementName + "\" element was found under the \"stylesheetdescription\" node!");
      return  null;
    }
  }

  private String getDescription (Document descr) {
    NodeList descriptions = descr.getElementsByTagName("description");
    Node description = null;
    for (int i = descriptions.getLength() - 1; i >= 0; i--) {
      description = descriptions.item(i);
      if (description.getParentNode().getNodeName().equals("stylesheetdescription"))
        break;
      else
        description = null;
    }
    if (description != null) {
      return  this.getTextChildNodeValue(description);
    }
    else {
        if (log.isDebugEnabled())
            log.debug("RDBMUserLayoutStore::getDescription() : no \"description\" element was found under the \"stylesheetdescription\" node!");
      return  null;
    }
  }

  private Vector getVectorOfSimpleTextElementValues (Document descr, String elementName) {
    Vector v = new Vector();
    // find "stylesheetdescription" node, take the first one
    Element stylesheetdescriptionElement = (Element)(descr.getElementsByTagName("stylesheetdescription")).item(0);
    if (stylesheetdescriptionElement == null) {
      log.error( "Could not obtain <stylesheetdescription> element");
      return  null;
    }
    NodeList elements = stylesheetdescriptionElement.getElementsByTagName(elementName);
    for (int i = elements.getLength() - 1; i >= 0; i--) {
      v.add(this.getTextChildNodeValue(elements.item(i)));
    }
    return  v;
  }

  private String getTextChildNodeValue (Node node) {
    if (node == null)
      return  null;
    NodeList children = node.getChildNodes();
    for (int i = children.getLength() - 1; i >= 0; i--) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.TEXT_NODE)
        return  child.getNodeValue();
    }
    return  null;
  }

  /**
   *   UserPreferences
   */
  private String getUserBrowserMapping (final IPerson person, final String userAgentArg) {
    final int userId = person.getID();
    return jdbcOperations.execute(new ConnectionCallback<String>() {
        @Override
        public String doInConnection(Connection con) throws SQLException, DataAccessException {
            final String userAgent;
            if (userAgentArg.length() > 255){
                userAgent = userAgentArg.substring(0,254);
                log.debug("userAgent trimmed to 255 characters. userAgent: "+userAgentArg);
            }
            else {
                userAgent = userAgentArg;
            }
            
            
      String sQuery =
        "SELECT PROFILE_FNAME " +
        "FROM UP_USER_UA_MAP LEFT JOIN UP_USER_PROFILE ON " + 
        "UP_USER_UA_MAP.PROFILE_ID=UP_USER_PROFILE.PROFILE_ID WHERE UP_USER_UA_MAP.USER_ID=? AND USER_AGENT=?";
      PreparedStatement pstmt = con.prepareStatement(sQuery);

      try {
        pstmt.setInt(1, userId);
        pstmt.setString(2, userAgent);

        if (log.isDebugEnabled())
            log.debug("RDBMUserLayoutStore::getUserBrowserMapping(): '" + sQuery + "' userId: "
            	+ userId + " userAgent: " + userAgent);
        ResultSet rs = pstmt.executeQuery();
        try {
          if (rs.next()) {
            return rs.getString("PROFILE_FNAME");
          }
        } finally {
          rs.close();
        }
      } finally {
        pstmt.close();
      }
      
      return null;
        }
    });
  }

  protected Document getPersonalUserLayout (final IPerson person, final IUserProfile profile) {
    final LocaleManager localeManager = profile.getLocaleManager();

    return jdbcOperations.execute(new ConnectionCallback<Document>() {
        @Override
        public Document doInConnection(Connection con) throws SQLException, DataAccessException {

      ResultSet rs;
      int userId = person.getID();
      final int realUserId = userId;
      Document doc = DocumentFactory.getThreadDocument();
      Element root = doc.createElement("layout");
      final Statement stmt = con.createStatement();
      // A separate statement is needed so as not to interfere with ResultSet
      // of statements used for queries
      Statement insertStmt = con.createStatement();
      try {
        long startTime = System.currentTimeMillis();
        // eventually, we need to fix template layout implementations so you can just do this:
        //        int layoutId=profile.getLayoutId();
        // but for now:
        int layoutId = getLayoutID(userId, profile.getProfileId());

       if (layoutId == 0) { // First time, grab the default layout for this user
           final Tuple<Integer, Integer> userLayoutIds = transactionOperations.execute(new TransactionCallback<Tuple<Integer, Integer>>() {
               @Override
               public Tuple<Integer, Integer> doInTransaction(TransactionStatus status) {
                   return jdbcOperations.execute(new ConnectionCallback<Tuple<Integer, Integer>>() {
                       @Override
                       public Tuple<Integer, Integer> doInConnection(Connection con) throws SQLException, DataAccessException {
              
              int newLayoutId;
              int newUserId;
              
              String sQuery = "SELECT USER_DFLT_USR_ID, USER_DFLT_LAY_ID FROM UP_USER WHERE USER_ID=" + realUserId;
              if (log.isDebugEnabled())
                  log.debug("RDBMUserLayoutStore::getUserLayout(): " + sQuery);
              ResultSet rs = stmt.executeQuery(sQuery);
              try {
                boolean hasRow = rs.next();
                newUserId = rs.getInt(1);
                newLayoutId = rs.getInt(2);
              } finally {
                rs.close();
              }
    
              // Make sure the next struct id is set in case the user adds a channel
              sQuery = "SELECT NEXT_STRUCT_ID FROM UP_USER WHERE USER_ID=" + newUserId;
              if (log.isDebugEnabled())
                  log.debug("RDBMUserLayoutStore::getUserLayout(): " + sQuery);
              int nextStructId;
              rs = stmt.executeQuery(sQuery);
              try {
                boolean hasRow = rs.next();
                nextStructId = rs.getInt(1);
              } finally {
                rs.close();
              }
    
              int realNextStructId = 0;
    
              if (realUserId != newUserId) {
                // But never make the existing value SMALLER, change it only to make it LARGER
                // (so, get existing value)
                sQuery = "SELECT NEXT_STRUCT_ID FROM UP_USER WHERE USER_ID=" + realUserId;
                if (log.isDebugEnabled())
                    log.debug("RDBMUserLayoutStore::getUserLayout(): " + sQuery);
                rs = stmt.executeQuery(sQuery);
                try {
                  boolean hasRow = rs.next();
                  realNextStructId = rs.getInt(1);
                } finally {
                  rs.close();
                }
              }
    
              if (nextStructId > realNextStructId) {
                sQuery = "UPDATE UP_USER SET NEXT_STRUCT_ID=" + nextStructId + " WHERE USER_ID=" + realUserId;
                if (log.isDebugEnabled())
                    log.debug("RDBMUserLayoutStore::getUserLayout(): " + sQuery);
                stmt.executeUpdate(sQuery);
              }
              
              return new Tuple<Integer, Integer>(newUserId, newLayoutId);
              
                       }
                   });
               }
           });
           
           userId = userLayoutIds.first;
           layoutId = userLayoutIds.second;
        }

        int firstStructId = -1;

        //Flags to enable a default layout lookup if it's needed
        boolean foundLayout = false;
        boolean triedDefault = false;

        //This loop is used to ensure a layout is found for a user. It tries
        //looking up the layout for the current userID. If one isn't found
        //the userID is replaced with the template user ID for this user and
        //the layout is searched for again. This loop should only ever loop once.
        do {
            String sQuery = "SELECT INIT_STRUCT_ID FROM UP_USER_LAYOUT WHERE USER_ID=" + userId + " AND LAYOUT_ID = " + layoutId;
            if (log.isDebugEnabled())
                log.debug("RDBMUserLayoutStore::getUserLayout(): " + sQuery);
            rs = stmt.executeQuery(sQuery);
            try {
              if (rs.next()) {
                firstStructId = rs.getInt(1);
              } else {
                throw new RuntimeException("RDBMUserLayoutStore::getUserLayout(): No INIT_STRUCT_ID in UP_USER_LAYOUT for USER_ID: " + userId + " and LAYOUT_ID: " + layoutId);
              }
            } finally {
              rs.close();
            }

            String sql;
            if (localeAware) {
                // This needs to be changed to get the localized strings
                sql = "SELECT ULS.STRUCT_ID,ULS.NEXT_STRUCT_ID,ULS.CHLD_STRUCT_ID,ULS.CHAN_ID,ULS.NAME,ULS.TYPE,ULS.HIDDEN,"+
              "ULS.UNREMOVABLE,ULS.IMMUTABLE";
            }  else {
                sql = "SELECT ULS.STRUCT_ID,ULS.NEXT_STRUCT_ID,ULS.CHLD_STRUCT_ID,ULS.CHAN_ID,ULS.NAME,ULS.TYPE,ULS.HIDDEN,"+
              "ULS.UNREMOVABLE,ULS.IMMUTABLE";
            }
            if (databaseMetadata.supportsOuterJoins()) {
              sql += ",USP.STRUCT_PARM_NM,USP.STRUCT_PARM_VAL FROM " + databaseMetadata.getJoinQuery().getQuery("layout");
            } else {
              sql += " FROM UP_LAYOUT_STRUCT ULS WHERE ";
            }
            sql += " ULS.USER_ID=" + userId + " AND ULS.LAYOUT_ID=" + layoutId + " ORDER BY ULS.STRUCT_ID";
            if (log.isDebugEnabled())
                log.debug("RDBMUserLayoutStore::getUserLayout(): " + sql);
            rs = stmt.executeQuery(sql);

            //check for rows in the result set
            foundLayout = rs.next();

            if (!foundLayout && !triedDefault && userId == realUserId) {
                //If we didn't find any rows and we haven't tried the default user yet
                triedDefault = true;
                rs.close();

                //Get the default user ID and layout ID
                sQuery = "SELECT USER_DFLT_USR_ID, USER_DFLT_LAY_ID FROM UP_USER WHERE USER_ID=" + userId;
                if (log.isDebugEnabled())
                    log.debug("RDBMUserLayoutStore::getUserLayout(): " + sQuery);
                rs = stmt.executeQuery(sQuery);
                try {
                  rs.next();
                  userId = rs.getInt(1);
                  layoutId = rs.getInt(2);
                } finally {
                  rs.close();
                }
            }
            else {
                //We tried the default or actually found a layout
                break;
            }
        } while (!foundLayout);

        HashMap layoutStructure = new HashMap();
        StringBuffer structChanIds = new StringBuffer();

        try {
          int lastStructId = 0;
          LayoutStructure ls = null;
          String sepChar = "";
          if (foundLayout) {
            int structId = rs.getInt(1);
            // Result Set returns 0 by default if structId was null
            // Except if you are using poolman 2.0.4 in which case you get -1 back
            if (rs.wasNull()) {
              structId = 0;
            }
            readLayout: while (true) {

              int nextId = rs.getInt(2);
              if (rs.wasNull()) {
                nextId = 0;
              }
              int childId = rs.getInt(3);
              if (rs.wasNull()) {
                childId = 0;
              }
              int chanId = rs.getInt(4);
              if (rs.wasNull()) {
                chanId = 0;
              }
              String temp5=rs.getString(5); // Some JDBC drivers require columns accessed in order
              String temp6=rs.getString(6); // Access 5 and 6 now, save till needed.

              // uPortal i18n
              int name_index, value_index;
              if (localeAware) {
                Locale[] locales = localeManager.getLocales();
                String locale = locales[0].toString();
				ls = new LayoutStructure(
                              structId, nextId, childId, chanId, 
                              rs.getString(7),rs.getString(8),rs.getString(9),
                              locale);
                  name_index=10;
                  value_index=11;
              }  else {
                  ls = new LayoutStructure(structId, nextId, childId, chanId, rs.getString(7),rs.getString(8),rs.getString(9));
                  name_index=10;
                  value_index=11;
              }
              layoutStructure.put(new Integer(structId), ls);
              lastStructId = structId;
              if (!ls.isChannel()) {
                ls.addFolderData(temp5, temp6); // Plug in saved column values
              }
              if (databaseMetadata.supportsOuterJoins()) {
                do {
                  String name = rs.getString(name_index);
                  String value = rs.getString(value_index); // Oracle JDBC requires us to do this for longs
                  if (name != null) { // may not be there because of the join
                    ls.addParameter(name, value);
                  }
                  if (!rs.next()) {
                    break readLayout;
                  }
                  structId = rs.getInt(1);
                  if (rs.wasNull()) {
                    structId = 0;
                  }
                } while (structId == lastStructId);
              } else { // Do second SELECT later on for structure parameters
                if (ls.isChannel()) {
                  structChanIds.append(sepChar + ls.getChanId());
                  sepChar = ",";
                }
                if (rs.next()) {
                  structId = rs.getInt(1);
                  if (rs.wasNull()) {
                    structId = 0;
                  }
                } else {
                  break readLayout;
                }
              }
            } // while
          }
        } finally {
          rs.close();
        }

        if (!databaseMetadata.supportsOuterJoins()) { // Pick up structure parameters
          // first, get the struct ids for the channels
          String sql = "SELECT STRUCT_ID FROM UP_LAYOUT_STRUCT WHERE USER_ID=" + userId +
            " AND LAYOUT_ID=" + layoutId +
            " AND CHAN_ID IN (" + structChanIds.toString() + ") ORDER BY STRUCT_ID";

          if (log.isDebugEnabled())
              log.debug("RDBMUserLayoutStore::getUserLayout(): " + sql);
          StringBuffer structIdsSB = new StringBuffer( "" );
          String sep = "";
          rs = stmt.executeQuery(sql);
          try {
            // use the results to build a correct list of struct ids to look for
            while( rs.next()) {
              structIdsSB.append(sep + rs.getString(1));
              sep = ",";
            }// while
          } finally {
            rs.close();
          } // be a good doobie


          sql = "SELECT STRUCT_ID, STRUCT_PARM_NM,STRUCT_PARM_VAL FROM UP_LAYOUT_PARAM WHERE USER_ID=" + userId + " AND LAYOUT_ID=" + layoutId +
            " AND STRUCT_ID IN (" + structIdsSB.toString() + ") ORDER BY STRUCT_ID";
          if (log.isDebugEnabled())
              log.debug("RDBMUserLayoutStore::getUserLayout(): " + sql);
          rs = stmt.executeQuery(sql);
          try {
            if (rs.next()) {
              int structId = rs.getInt(1);
              readParm: while(true) {
                LayoutStructure ls = (LayoutStructure)layoutStructure.get(new Integer(structId));
                int lastStructId = structId;
                do {
                  ls.addParameter(rs.getString(2), rs.getString(3));
                  if (!rs.next()) {
                    break readParm;
                  }
                } while ((structId = rs.getInt(1)) == lastStructId);
              }
            }
          } finally {
            rs.close();
          }
        }

        if (layoutStructure.size() > 0) { // We have a layout to work with
          createLayout(layoutStructure, doc, root, firstStructId);
          layoutStructure.clear();

          if (log.isDebugEnabled()) {
              long stopTime = System.currentTimeMillis();
              log.debug("RDBMUserLayoutStore::getUserLayout(): Layout document for user " + userId + " took " +
                (stopTime - startTime) + " milliseconds to create");
          }

          doc.appendChild(root);
        }
      } finally {
        stmt.close();
        insertStmt.close();
      }
      return  doc;
        }
    });
  }

  public IUserProfile getUserProfileById (final IPerson person, final int profileId) {
    final int userId = person.getID();
    return jdbcOperations.execute(new ConnectionCallback<IUserProfile>() {
        @Override
        public IUserProfile doInConnection(Connection con) throws SQLException, DataAccessException {

      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT USER_ID, PROFILE_ID, PROFILE_FNAME, PROFILE_NAME, DESCRIPTION, LAYOUT_ID, STRUCTURE_SS_ID, THEME_SS_ID FROM UP_USER_PROFILE WHERE USER_ID="
            + userId + " AND PROFILE_ID=" + profileId;
        if (log.isDebugEnabled())
            log.debug("RDBMUserLayoutStore::getUserProfileById(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          if (rs.next()) {
            String temp2 = rs.getString(3);
            String temp3 = rs.getString(4);
            String temp4 = rs.getString(5);
            int layoutId = rs.getInt(6);
            if (rs.wasNull()) {
              layoutId = 0;
            }
            int structSsId = rs.getInt(7);
            if (rs.wasNull()) {
                // This is probably a data issue and probably an export operation;  defer to the system user...
                if (!person.equals(getSystemUser())) {
                    structSsId = getSystemProfileByFname(temp2).getStructureStylesheetId();
                } else {
                    String msg = "The system user profile has no structure stylesheet Id.";
                    throw new IllegalStateException(msg);
                }
            }
            int themeSsId = rs.getInt(8);
            if (rs.wasNull()) {
                // This is probably a data issue and probably an export operation;  defer to the system user...
                if (!person.equals(getSystemUser())) {
                    themeSsId = getSystemProfileByFname(temp2).getThemeStylesheetId();
                } else {
                    String msg = "The system user profile has no theme stylesheet Id.";
                    throw new IllegalStateException(msg);
                }
            }
            IUserProfile userProfile = new UserProfile(profileId, temp2, temp3,temp4, layoutId, structSsId, themeSsId);
            final Locale[] userLocales = localeStore.getUserLocales(person);
            userProfile.setLocaleManager(new LocaleManager(person, userLocales));
            return userProfile;
          }
          else {
            throw new RuntimeException("Unable to find User Profile for user " + userId + " and profile " + profileId);
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
      
        }
    });
  }
  
  private final ThreadLocal<Cache<Tuple<String, String>, UserProfile>> profileCacheHolder = new ThreadLocal<Cache<Tuple<String,String>,UserProfile>>();
  /**
   * Cache used during import/export operations
   */
  public void setProfileImportExportCache(Cache<Tuple<String, String>, UserProfile> profileCache) {
      if (profileCache == null) {
          this.profileCacheHolder.remove();
      }
      else {
          this.profileCacheHolder.set(profileCache);
      }
  }
  private Cache<Tuple<String, String>, UserProfile> getProfileImportExportCache() {
      return this.profileCacheHolder.get();
  }

  public UserProfile getUserProfileByFname (final IPerson person, final String profileFname) {
      Tuple<String, String> key = null;
      final Cache<Tuple<String, String>, UserProfile> profileCache = getProfileImportExportCache();
      if (profileCache != null) {
          key = new Tuple<String, String>(person.getUserName(), profileFname);
          final UserProfile profile = profileCache.getIfPresent(key);
          if (profile != null) {
              return profile;
          }
      }
      
	log.debug("Getting profile " + profileFname + " for user " + person.getID());
    final int userId = person.getID();
    final UserProfile userProfile = jdbcOperations.execute(new ConnectionCallback<UserProfile>() {
        @Override
        public UserProfile doInConnection(Connection con) throws SQLException, DataAccessException {

      String query = "SELECT USER_ID, PROFILE_ID, PROFILE_NAME, DESCRIPTION, " +
      		"LAYOUT_ID, STRUCTURE_SS_ID, THEME_SS_ID FROM UP_USER_PROFILE WHERE " +
      		"USER_ID=? AND PROFILE_FNAME=?";
      PreparedStatement pstmt = con.prepareStatement(query);
      pstmt.setInt(1, userId);
      pstmt.setString(2, profileFname);
      try {
        if (log.isDebugEnabled())
            log.debug("RDBMUserLayoutStore::getUserProfileByFname(): " + query + 
            		" userId: " + userId + " profileFname: " + profileFname);
        ResultSet rs = pstmt.executeQuery();
        try {
          if (rs.next()) {
        	int profileId = rs.getInt(2);
            String profileName = rs.getString(3);
            String profileDesc = rs.getString(4);
            int layoutId = rs.getInt(5);
            if (rs.wasNull()) {
              layoutId = 0;
            }
            int structSsId = rs.getInt(6);
            if (rs.wasNull()) {
                // This is probably a data issue and probably an export operation;  defer to the system user...
                if (!person.equals(getSystemUser())) {
                    structSsId = getSystemProfileByFname(profileFname).getStructureStylesheetId();
                } else {
                    String msg = "The system user profile has no structure stylesheet Id.";
                    throw new IllegalStateException(msg);
                }
            }
            int themeSsId = rs.getInt(7);
            if (rs.wasNull()) {
                // This is probably a data issue and probably an export operation;  defer to the system user...
                if (!person.equals(getSystemUser())) {
                    themeSsId = getSystemProfileByFname(profileFname).getThemeStylesheetId();
                } else {
                    String msg = "The system user profile has no theme stylesheet Id.";
                    throw new IllegalStateException(msg);
                }
            }
            UserProfile userProfile = new UserProfile(profileId, profileFname, profileName, profileDesc, layoutId, structSsId, themeSsId);
            final Locale[] userLocales = localeStore.getUserLocales(person);
            userProfile.setLocaleManager(new LocaleManager(person, userLocales));
            return userProfile;
          }

            /* Try to copy the template profile. */
        	log.debug("Copying template profile " + profileFname + " to user " + person.getID());
        	rs.close();
        	pstmt.close();
        	pstmt = con.prepareStatement("SELECT USER_DFLT_USR_ID FROM UP_USER WHERE USER_ID=?");
        	pstmt.setInt(1, person.getID());
        	rs = pstmt.executeQuery();
        	if(rs.next()) {
        		int defaultProfileUser = rs.getInt(1);
        		if (rs.wasNull()) {
        			throw new RuntimeException("Need to clone the '" + profileFname + "' profile from template user for " + person + " but they have no template user");
        		}

        		IPerson defaultProfilePerson = new PersonImpl();
        		defaultProfilePerson.setID(defaultProfileUser);
        		if(defaultProfilePerson.getID() != person.getID()) {
        			UserProfile templateProfile = getUserProfileByFname(defaultProfilePerson,profileFname);
        			if(templateProfile != null) {
        				UserProfile newUserProfile = new UserProfile(templateProfile);
        			    final Locale[] userLocales = localeStore.getUserLocales(person);
        			    newUserProfile.setLayoutId(0);
        			    newUserProfile = addUserProfile(person,newUserProfile);
        			    
        			    newUserProfile.setLocaleManager(new LocaleManager(person, userLocales));
        			    return newUserProfile;
        			}
        		}
        	}
        	
            throw new RuntimeException("Unable to find User Profile for userId " + userId + " and profile " + profileFname);
        } finally {
          rs.close();
        }
      } finally {
        pstmt.close();
      }
      
        }
    });
    if (profileCache != null && key != null) { 
        profileCache.put(key, userProfile);
    }
    return userProfile;
  }

  public Hashtable getUserProfileList (final IPerson person) {
    final int userId = person.getID();

    return jdbcOperations.execute(new ConnectionCallback<Hashtable>() {
        @Override
        public Hashtable doInConnection(Connection con) throws SQLException, DataAccessException {

      Hashtable<Integer,UserProfile> pv = new Hashtable<Integer,UserProfile>();
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT USER_ID, PROFILE_ID, PROFILE_FNAME, PROFILE_NAME, DESCRIPTION, LAYOUT_ID, STRUCTURE_SS_ID, THEME_SS_ID FROM UP_USER_PROFILE WHERE USER_ID="
            + userId;
        if (log.isDebugEnabled())
            log.debug("RDBMUserLayoutStore::getUserProfileList(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            int layoutId = rs.getInt(6);
            if (rs.wasNull()) {
              layoutId = 0;
            }
            int structSsId = rs.getInt(7);
            if (rs.wasNull()) {
              structSsId = 0;
            }
            int themeSsId = rs.getInt(8);
            if (rs.wasNull()) {
              themeSsId = 0;
            }

            UserProfile upl = new UserProfile(rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5),
                layoutId, structSsId, themeSsId);
            pv.put(new Integer(upl.getProfileId()), upl);
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
      return pv;
      
        }
    });
  }

  protected abstract Element getStructure(Document doc, LayoutStructure ls);

  protected abstract int saveStructure (Node node, PreparedStatement structStmt, PreparedStatement parmStmt) throws SQLException;

  public void setUserBrowserMapping (final IPerson person, final String userAgentArg, final int profileId) {
	  final int userId = person.getID();
	  
	  this.transactionOperations.execute(new TransactionCallback<Object>() {
	      @Override
	      public Object doInTransaction(TransactionStatus status) {
	          return jdbcOperations.execute(new ConnectionCallback<Object>() {
	              @Override
	              public Object doInConnection(Connection con) throws SQLException, DataAccessException {
	                  final String userAgent;
	                  if (userAgentArg.length() > 255){
	                      userAgent = userAgentArg.substring(0,254);
	                      log.debug("userAgent trimmed to 255 characters. userAgent: "+userAgentArg);
	                  }
	                  else {
	                      userAgent = userAgentArg;
	                  }

		  // remove the old mapping and add the new one
			  PreparedStatement ps = null;
			  try{
				  ps = con.prepareStatement("DELETE FROM UP_USER_UA_MAP WHERE USER_ID=? AND USER_AGENT=?");
				  ps.setInt(1,userId);
				  ps.setString(2,userAgent);
				  ps.executeUpdate();
			  }finally{
				  try{
					  ps.close();
				  }catch(Exception e){
					  //ignore
				  }
			  }
			  try{
				  log.debug("writing to UP_USER_UA_MAP: userId: "+userId+", userAgent: "+userAgent+", profileId: "+profileId);
				  ps = con.prepareStatement("INSERT INTO UP_USER_UA_MAP (USER_ID,USER_AGENT,PROFILE_ID) VALUES (?,?,?)");
				  ps.setInt(1,userId);
				  ps.setString(2,userAgent);
				  ps.setInt(3,profileId);
				  ps.executeUpdate();
			  }finally{
				  try{
					  ps.close();
				  }catch(Exception e){
					  //ignore
				  }
			  }
			  
			  return null;
	                   }
                });
            }
        });
  }

  /**
   * Save the user layout.
   * @param person
   * @param profile
   * @param layoutXML
   * @throws Exception
   */
  public void setUserLayout(final IPerson person, final IUserProfile profile, final Document layoutXML, final boolean channelsAdded) {
      final long startTime = System.currentTimeMillis();
      final int userId = person.getID();
      final int profileId = profile.getProfileId();

      this.transactionOperations.execute(new TransactionCallback<Object>() {
          @Override
          public Object doInTransaction(TransactionStatus status) {
              return jdbcOperations.execute(new ConnectionCallback<Object>() {
                  @Override
                  public Object doInConnection(Connection con) throws SQLException, DataAccessException {

          int layoutId = 0;
          ResultSet rs;

          // Eventually we want to be able to just get layoutId from the
          // profile, but because of the template user layouts we have to do this for now ...
          layoutId = getLayoutID(userId, profileId);

          boolean firstLayout = false;
          if (layoutId == 0) {
              // First personal layout for this user/profile
              layoutId = 1;
              firstLayout = true;
          }

          String sql = "DELETE FROM UP_LAYOUT_PARAM WHERE USER_ID=? AND LAYOUT_ID=?";
          PreparedStatement pstmt = con.prepareStatement(sql);
          try {
              pstmt.clearParameters();
              pstmt.setInt(1, userId);
              pstmt.setInt(2, layoutId);
              if (log.isDebugEnabled())
                  log.debug(sql);
              pstmt.executeUpdate();
          } finally {
              pstmt.close();
          }

          sql = "DELETE FROM UP_LAYOUT_STRUCT WHERE USER_ID=? AND LAYOUT_ID=?";
          pstmt = con.prepareStatement(sql);
          try {
              pstmt.clearParameters();
              pstmt.setInt(1, userId);
              pstmt.setInt(2, layoutId);
              if (log.isDebugEnabled())
                log.debug(sql);
              pstmt.executeUpdate();
          } finally {
              pstmt.close();
          }

          PreparedStatement structStmt = con.prepareStatement("INSERT INTO UP_LAYOUT_STRUCT "
                  + "(USER_ID, LAYOUT_ID, STRUCT_ID, NEXT_STRUCT_ID, CHLD_STRUCT_ID,EXTERNAL_ID,CHAN_ID,NAME,TYPE,HIDDEN,IMMUTABLE,UNREMOVABLE) "
                  + "VALUES (" + userId + "," + layoutId + ",?,?,?,?,?,?,?,?,?,?)");

          PreparedStatement parmStmt = con.prepareStatement("INSERT INTO UP_LAYOUT_PARAM "
                  + "(USER_ID, LAYOUT_ID, STRUCT_ID, STRUCT_PARM_NM, STRUCT_PARM_VAL) " + "VALUES (" + userId + "," + layoutId + ",?,?,?)");

          int firstStructId;
          try {
              firstStructId = saveStructure(layoutXML.getFirstChild().getFirstChild(), structStmt, parmStmt);
          } finally {
              structStmt.close();
              parmStmt.close();
          }

          //Check to see if the user has a matching layout
          sql = "SELECT * FROM UP_USER_LAYOUT WHERE USER_ID=? AND LAYOUT_ID=?";
          pstmt = con.prepareStatement(sql);
          try {
              pstmt.clearParameters();
              pstmt.setInt(1, userId);
              pstmt.setInt(2, layoutId);
              if (log.isDebugEnabled())
                  log.debug(sql);
              rs = pstmt.executeQuery();

              try {
                  if (!rs.next()) {
                      // If not, the default user is found and the layout rows from the default user are copied for the current user.
                      int defaultUserId;

                      sql = "SELECT USER_DFLT_USR_ID FROM UP_USER WHERE USER_ID=?";
                      PreparedStatement pstmt2 = con.prepareStatement(sql);
                      try {
                          pstmt2.clearParameters();
                          pstmt2.setInt(1, userId);
                          if (log.isDebugEnabled())
                              log.debug(sql);
                          ResultSet rs2 = null;
                          try {
                              rs2 = pstmt2.executeQuery();
                              rs2.next();
                              defaultUserId = rs2.getInt(1);
                          } finally {
                              rs2.close();
                          }
                      } finally {
                          pstmt2.close();
                      }

                      // Add to UP_USER_LAYOUT
                      sql = "SELECT USER_ID,LAYOUT_ID,LAYOUT_TITLE,INIT_STRUCT_ID FROM UP_USER_LAYOUT WHERE USER_ID=?";
                      pstmt2 = con.prepareStatement(sql);
                      try {
                          pstmt2.clearParameters();
                          pstmt2.setInt(1, defaultUserId);
                          if (log.isDebugEnabled())
                              log.debug(sql);
                          ResultSet rs2 = pstmt2.executeQuery();
                          try {
                              if (rs2.next()) {
                                  // There is a row for this user's template user...
                                  sql = "INSERT INTO UP_USER_LAYOUT (USER_ID, LAYOUT_ID, LAYOUT_TITLE, INIT_STRUCT_ID) VALUES (?,?,?,?)";
                                  PreparedStatement pstmt3 = con.prepareStatement(sql);
                                  try {
                                      pstmt3.clearParameters();
                                      pstmt3.setInt(1, userId);
                                      pstmt3.setInt(2, rs2.getInt("LAYOUT_ID"));
                                      pstmt3.setString(3, rs2.getString("LAYOUT_TITLE"));
                                      pstmt3.setInt(4, rs2.getInt("INIT_STRUCT_ID"));
                                      if (log.isDebugEnabled())
                                          log.debug(sql);
                                      pstmt3.executeUpdate();
                                  } finally {
                                      pstmt3.close();
                                  }
                              } else {
                                  // We can't rely on the template user, but we still need a row...
                                  sql = "INSERT INTO UP_USER_LAYOUT (USER_ID, LAYOUT_ID, LAYOUT_TITLE, INIT_STRUCT_ID) VALUES (?,?,?,?)";
                                  PreparedStatement pstmt3 = con.prepareStatement(sql);
                                  try {
                                      pstmt3.clearParameters();
                                      pstmt3.setInt(1, userId);
                                      pstmt3.setInt(2, layoutId);
                                      pstmt3.setString(3, "default layout");
                                      pstmt3.setInt(4, 1);
                                      if (log.isDebugEnabled())
                                          log.debug(sql);
                                      pstmt3.executeUpdate();
                                  } finally {
                                      pstmt3.close();
                                  }
                              }
                          } finally {
                              rs2.close();
                          }
                      } finally {
                          pstmt2.close();
                      }

                  }
              } finally {
                  rs.close();
              }
          } finally {
              pstmt.close();
          }

          //Update the users layout with the correct inital structure ID
          sql = "UPDATE UP_USER_LAYOUT SET INIT_STRUCT_ID=? WHERE USER_ID=? AND LAYOUT_ID=?";
          pstmt = con.prepareStatement(sql);
          try {
              pstmt.clearParameters();
              pstmt.setInt(1, firstStructId);
              pstmt.setInt(2, userId);
              pstmt.setInt(3, layoutId);
              if (log.isDebugEnabled())
                  log.debug(sql);
              pstmt.executeUpdate();
          } finally {
              pstmt.close();
          }

          // Update the last time the user saw the list of available channels
          if (channelsAdded) {
              sql = "UPDATE UP_USER SET LST_CHAN_UPDT_DT=? WHERE USER_ID=?";
              pstmt = con.prepareStatement(sql);
              try {
                  pstmt.clearParameters();
                  pstmt.setDate(1, new java.sql.Date(System.currentTimeMillis()));
                  pstmt.setInt(2, userId);
                  log.debug(sql);
                  pstmt.executeUpdate();
              } finally {
                  pstmt.close();
              }
          }

          if (firstLayout) {
              int defaultUserId;
              int defaultLayoutId;
              // Have to copy some of data over from the default user
              sql = "SELECT USER_DFLT_USR_ID,USER_DFLT_LAY_ID FROM UP_USER WHERE USER_ID=?";
              pstmt = con.prepareStatement(sql);
              try {
                  pstmt.clearParameters();
                  pstmt.setInt(1, userId);
                  log.debug(sql);
                  rs = pstmt.executeQuery();
                  try {
                      rs.next();
                      defaultUserId = rs.getInt(1);
                      defaultLayoutId = rs.getInt(2);
                  } finally {
                      rs.close();
                  }
              } finally {
                  pstmt.close();
              }

              sql = "UPDATE UP_USER_PROFILE SET LAYOUT_ID=1 WHERE USER_ID=? AND PROFILE_ID=?";
              pstmt = con.prepareStatement(sql);
              try {
                  pstmt.clearParameters();
                  pstmt.setInt(1, userId);
                  pstmt.setInt(2, profileId);
                  log.debug(sql);
                  pstmt.executeUpdate();
              } finally {
                  pstmt.close();
              }
          }
          
          return null;
                  }
              });
          }
      });
      if (log.isDebugEnabled()) {
          long stopTime = System.currentTimeMillis();
          log.debug("RDBMUserLayoutStore::setUserLayout(): Layout document for user " + userId + " took " + (stopTime - startTime) + " milliseconds to save");
      }
  }

  public void updateUserProfile (final IPerson person, final IUserProfile profile) {
    final int userId = person.getID();
    this.transactionOperations.execute(new TransactionCallback<Object>() {
        @Override
        public Object doInTransaction(TransactionStatus status) {
            return jdbcOperations.execute(new ConnectionCallback<Object>() {
                @Override
                public Object doInConnection(Connection con) throws SQLException, DataAccessException {

      String query = "UPDATE UP_USER_PROFILE SET LAYOUT_ID=?,THEME_SS_ID=?,STRUCTURE_SS_ID=?," +
      		"DESCRIPTION=?,PROFILE_NAME=?, PROFILE_FNAME=? WHERE USER_ID=? AND PROFILE_ID=?";
      PreparedStatement pstmt = con.prepareStatement(query);
      pstmt.setInt(1, profile.getLayoutId());
      pstmt.setInt(2, profile.getThemeStylesheetId());
      pstmt.setInt(3, profile.getStructureStylesheetId());
      pstmt.setString(4, profile.getProfileDescription());
      pstmt.setString(5, profile.getProfileName());
      pstmt.setString(6, profile.getProfileFname());
      pstmt.setInt(7, userId);
      pstmt.setInt(8, profile.getProfileId());
      try {
        if (log.isDebugEnabled())
            log.debug("RDBMUserLayoutStore::updateUserProfile() : " + query +
            	" layout_id: " + profile.getLayoutId() + " theme_ss_id: " + profile.getThemeStylesheetId() +
            	" structure_ss_id: " + profile.getStructureStylesheetId() + " description: " +
            	profile.getProfileDescription() + " name: " + profile.getProfileName() +
            	" user_id: " + userId + " fname: " + profile.getProfileFname());
        pstmt.execute();
      } finally {
        pstmt.close();
      }
      
      return null;
                }
            });
        }
    });
  }

  public void setSystemBrowserMapping (String userAgent, int profileId) {
    this.setUserBrowserMapping(this.getSystemUser(), userAgent, profileId);
  }

  private String getSystemBrowserMapping (String userAgent) {
    return  getUserBrowserMapping(this.getSystemUser(), userAgent);
  }

  public IUserProfile getUserProfile (IPerson person, String userAgent) {
    String profileFname = getUserBrowserMapping(person, userAgent);
    if (profileFname == null)
      return  null;
    return  this.getUserProfileByFname(person, profileFname);
  }

  public IUserProfile getSystemProfile (String userAgent) {
    String profileFname = getSystemBrowserMapping(userAgent);
    if (profileFname == null)
      return  null;
    IUserProfile up = this.getUserProfileByFname(this.getSystemUser(), profileFname);
    up.setSystemProfile(true);
    return  up;
  }

  public IUserProfile getSystemProfileById (int profileId) {
    IUserProfile up = this.getUserProfileById(this.getSystemUser(), profileId);
    up.setSystemProfile(true);
    return  up;
  }

  public IUserProfile getSystemProfileByFname (String profileFname) {
	    IUserProfile up = this.getUserProfileByFname(this.getSystemUser(), profileFname);
	    up.setSystemProfile(true);
	    return  up;
	  }

  public Hashtable getSystemProfileList () {
    Hashtable pl = this.getUserProfileList(this.getSystemUser());
    for (Enumeration e = pl.elements(); e.hasMoreElements();) {
      IUserProfile up = (IUserProfile)e.nextElement();
      up.setSystemProfile(true);
    }
    return  pl;
  }

  public void updateSystemProfile (IUserProfile profile) {
    this.updateUserProfile(this.getSystemUser(), profile);
  }

  public IUserProfile addSystemProfile (IUserProfile profile) {
    return  addUserProfile(this.getSystemUser(), profile);
  }

  public void deleteSystemProfile (int profileId) {
    this.deleteUserProfile(this.getSystemUser(), profileId);
  }

    private static class SystemUser implements IPerson {
        private final int systemUserId;

        public SystemUser(int systemUserId) {
            this.systemUserId = systemUserId;
        }

        public void setID(int sID) {
        }

        public int getID() {
            return this.systemUserId;
        }
        
        public String getUserName() {
            return null;
        }

        public void setUserName(String userName) {
            
        }

        public void setFullName(String sFullName) {
        }

        public String getFullName() {
            return "uPortal System Account";
        }

        public Object getAttribute(String key) {
            return null;
        }

        public Object[] getAttributeValues(String key) {
            return null;
        }
        
        public Map<String,List<Object>> getAttributeMap() {
            return null;
        }

        public void setAttribute(String key, Object value) {
        }

        public void setAttribute(String key, List<Object> values) {
        }

        public void setAttributes(Map attrs) {
        }

        public Enumeration getAttributes() {
            return null;
        }

        public Enumeration getAttributeNames() {
            return null;
        }

        public boolean isGuest() {
            return (false);
        }

        public ISecurityContext getSecurityContext() {
            return (null);
        }

        public void setSecurityContext(ISecurityContext context) {
        }

        public EntityIdentifier getEntityIdentifier() {
            return null;
        }

        public void setEntityIdentifier(EntityIdentifier ei) {
        }

        public String getName() {
            return null;
        }
    }

  /**
   * Returns the current layout ID for the user and profile. If the profile doesn't exist or the
   * layout_id field is null 0 is returned.
   *
   * @param userId The userId for the profile
   * @param profileId The profileId for the profile
   * @return The layout_id field or 0 if it does not exist or is null
   * @throws SQLException
   */
  protected int getLayoutID(final int userId, final int profileId) throws SQLException {
      return jdbcOperations.execute(new ConnectionCallback<Integer>() {
          @Override
          public Integer doInConnection(Connection con) throws SQLException, DataAccessException {

          String query =
              "SELECT LAYOUT_ID " +
              "FROM UP_USER_PROFILE " +
              "WHERE USER_ID=? AND PROFILE_ID=?";
    
          int layoutId = 0;

          PreparedStatement pstmt = con.prepareStatement(query);

          try {
              final int u = userId;
            final int p = profileId;
            if (log.isDebugEnabled())
                  log.debug("RDBMUserLayoutStore::getLayoutID(userId=" + u + ", profileId=" + p + " ): " + query);

              pstmt.setInt(1, u);
              pstmt.setInt(2, p);
              ResultSet rs = pstmt.executeQuery();

              try {
                  if (rs.next()) {
                      layoutId = rs.getInt(1);

                      if (rs.wasNull()) {
                          layoutId = 0;
                      }
                  }
                  
                  if (layoutId == 0) {
                	  
                	  // determine the fname for the currently-requested profile
                	  query = "SELECT PROFILE_FNAME FROM UP_USER_PROFILE WHERE " +
                	  		"USER_ID=? AND PROFILE_ID=?";
                	  pstmt = con.prepareStatement(query);
                	  pstmt.setInt(1, u);
                	  pstmt.setInt(2, p);
                	  
                	  rs = pstmt.executeQuery();
                	  String profileFname = null;
                	  if (rs.next()) {
                		  profileFname = rs.getString("PROFILE_FNAME");
                	  }
                	  
                	  // using the fname calculated above, attempt to get the 
                	  // layout id of the default user profile for this fname
                	  query = "SELECT LAYOUT_ID FROM UP_USER_PROFILE LEFT JOIN " +
                	  		"UP_USER ON UP_USER_PROFILE.USER_ID=UP_USER.USER_DFLT_USR_ID " +
                	  		"WHERE UP_USER.USER_ID=? AND UP_USER_PROFILE.PROFILE_FNAME=?";
                	  pstmt = con.prepareStatement(query);
                	  pstmt.setInt(1, u);
                	  pstmt.setString(2, profileFname);
                	  rs = pstmt.executeQuery();
                	  int intendedLayoutId = 0;
                	  if (rs.next()) {
                		  intendedLayoutId = rs.getInt("LAYOUT_ID");
                	  }
                	  
                	  // check to see if another profile for the current user
                	  // has already created the requested layout
                	  query = "SELECT LAYOUT_ID FROM UP_USER_PROFILE WHERE " +
                	  		"USER_ID=? AND LAYOUT_ID=?";
                	  pstmt = con.prepareStatement(query);
                	  pstmt.setInt(1, u);
                	  pstmt.setInt(2, intendedLayoutId);
                	  rs = pstmt.executeQuery();
                	  if (rs.next()) {
                    	  
                    	  // if the layout already exists, update the profile to
                    	  // point to that layout
                    	  query = "UPDATE UP_USER_PROFILE SET LAYOUT_ID=? WHERE " +
                    	  		"USER_ID=? AND PROFILE_ID=?";
                    	  pstmt = con.prepareStatement(query);
                    	  pstmt.setInt(1, intendedLayoutId);
                    	  pstmt.setInt(2, u);
                    	  pstmt.setInt(3, p);
                    	  pstmt.execute();
                    	  
                    	  layoutId = intendedLayoutId;
                		  
                	  }
                      
                  }
                  
              }
              finally {
                  rs.close();
              }
          }
          finally {
              pstmt.close();
          }
          
          return layoutId;
          
          }
      });
  }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.IUserLayoutStore#importLayout(org.dom4j.Element)
     */
    public abstract void importLayout(org.dom4j.Element layout);

}
