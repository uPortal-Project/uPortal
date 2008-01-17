/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletPreferences;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.rdbm.DatabaseMetaDataImpl;
import org.jasig.portal.rdbm.IDatabaseMetadata;
import org.jasig.portal.rdbm.IJoinQueryString;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.EntityCachingService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.tools.versioning.VersionsManager;
import org.jasig.portal.utils.CounterStoreFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;

/**
 * Reference implementation of IChannelRegistryStore.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class RDBMChannelRegistryStore implements IChannelRegistryStore, InitializingBean {
    
    public static final String PORTLET_CHANNEL_PARAM_PREFIX = "PORTLET.";

	private static final Log log = LogFactory.getLog(RDBMChannelRegistryStore.class);

	// I18n property
	protected static final boolean localeAware = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.i18n.LocaleManager.locale_aware", LocaleManager.DEFAULT_LOCALE_AWARE);
	
	private IDatabaseMetadata databaseMetadata;
	private IPortletDefinitionRegistry portletDefinitionRegistry;
	

    /**
     * @return the portletDefinitionRegistry
     */
    public IPortletDefinitionRegistry getPortletDefinitionRegistry() {
        return portletDefinitionRegistry;
    }
    /**
     * @param portletDefinitionRegistry the portletDefinitionRegistry to set
     */
	@Required
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
	    Validate.notNull(portletDefinitionRegistry);
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    /**
     * @return the databaseMetadata
     */
    public IDatabaseMetadata getDatabaseMetadata() {
        return databaseMetadata;
    }
	/**
	 * @param databaseMetadata
	 */
	@Required
	public void setDatabaseMetadata(IDatabaseMetadata databaseMetadata) {
	    Validate.notNull(databaseMetadata);
	    this.databaseMetadata = databaseMetadata;
	}

	/* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        //Add join queries for databases that are known to support them
        if (this.databaseMetadata.supportsOuterJoins()) {
            final IJoinQueryString joinQuery = this.databaseMetadata.getJoinQuery();
            if (joinQuery instanceof DatabaseMetaDataImpl.JdbcDb) {
                joinQuery.addQuery("channel", "{oj UP_CHANNEL UC LEFT OUTER JOIN UP_CHANNEL_PARAM UCP ON UC.CHAN_ID = UCP.CHAN_ID} WHERE");
            }
            else if (joinQuery instanceof DatabaseMetaDataImpl.PostgreSQLDb) {
                joinQuery.addQuery("channel", "UP_CHANNEL UC LEFT OUTER JOIN UP_CHANNEL_PARAM UCP ON UC.CHAN_ID = UCP.CHAN_ID WHERE");
            }
            else if (joinQuery instanceof DatabaseMetaDataImpl.OracleDb) {
                joinQuery.addQuery("channel", "UP_CHANNEL UC, UP_CHANNEL_PARAM UCP WHERE UC.CHAN_ID = UCP.CHAN_ID(+) AND");
            }
            else {
                throw new Exception("Unknown database driver");
            }
        }
    }



    /**
	 * Create a new ChannelType object.
	 * @return channelType, the new channel type
	 * @throws java.lang.Exception
	 */
	public ChannelType newChannelType() throws Exception {
		int nextChanTypeId = CounterStoreFactory.getCounterStoreImpl().getIncrementIntegerId("UP_CHAN_TYPE");
		return new ChannelType(nextChanTypeId);
	}

	/**
	 * Get the channel type associated with a particular identifier.
	 * @param channelTypeId the channel type identifier
	 * @return channelType the channel type
	 * @throws java.sql.SQLException
	 */
	public ChannelType getChannelType(int channelTypeId) throws SQLException {
		ChannelType channelType = null;
		Connection con = RDBMServices.getConnection();

		try {
			Statement stmt = con.createStatement();
			try {
				String query = "SELECT * FROM UP_CHAN_TYPE WHERE TYPE_ID=" + channelTypeId;
				if (log.isDebugEnabled())
					log.debug("RDBMChannelRegistryStore.getChannelType(): " + query);
				ResultSet rs = stmt.executeQuery(query);
				try {
					if (rs.next()) {
						String javaClass = rs.getString("TYPE");
						String name = rs.getString("TYPE_NAME");
						String descr = rs.getString("TYPE_DESCR");
						String cpdUri = rs.getString("TYPE_DEF_URI");

						channelType = new ChannelType(channelTypeId);
						channelType.setJavaClass(javaClass);
						channelType.setName(name);
						channelType.setDescription(descr);
						channelType.setCpdUri(cpdUri);
					}
				} finally {
					rs.close();
				}
			} finally {
				stmt.close();
			}
		} finally {
			RDBMServices.releaseConnection(con);
		}
		return channelType;
	}

	/**
	 * Get channel types.
	 * @return types, the channel types
	 * @throws java.sql.SQLException
	 */
	public ChannelType[] getChannelTypes() throws SQLException {
		ChannelType[] channelTypes = null;
		Connection con = RDBMServices.getConnection();

		try {
			Statement stmt = con.createStatement();
			try {
				String query = "SELECT TYPE_ID, TYPE, TYPE_NAME, TYPE_DESCR, TYPE_DEF_URI FROM UP_CHAN_TYPE";
				if (log.isDebugEnabled())
					log.debug("RDBMChannelRegistryStore.getChannelTypes(): " + query);
				ResultSet rs = stmt.executeQuery(query);
				try {
					List channelTypesList = new ArrayList();
					while (rs.next()) {
						int channelTypeId = rs.getInt(1);
						String javaClass = rs.getString(2);
						String name = rs.getString(3);
						String descr = rs.getString(4);
						String cpdUri = rs.getString(5);

						ChannelType channelType = new ChannelType(channelTypeId);
						channelType.setJavaClass(javaClass);
						channelType.setName(name);
						channelType.setDescription(descr);
						channelType.setCpdUri(cpdUri);
						channelTypesList.add(channelType);
					}
					channelTypes = (ChannelType[])channelTypesList.toArray(new ChannelType[0]);
				} finally {
					rs.close();
				}
			} finally {
				stmt.close();
			}
		} finally {
			RDBMServices.releaseConnection(con);
		}
		return channelTypes;
	}

	/**
	 * Persists a channel type.
	 * @param chanType a channel type
	 * @throws java.sql.SQLException
	 */
	public void saveChannelType(ChannelType chanType) throws SQLException {
		Connection con = null;

		// Check if channel type exists.  If it doesn't exist, do an insert.
		// Otherwise, do an update.
		ChannelType chanTypeInStore = getChannelType(chanType.getId());
		int chanTypeId = chanType.getId();
		String javaClass = chanType.getJavaClass();
		String name = chanType.getName();
		String descr = chanType.getDescription();
		String cpdUri = chanType.getCpdUri();
		con = RDBMServices.getConnection();

		if (chanTypeInStore == null) {
			try {
				String insert = "INSERT INTO UP_CHAN_TYPE VALUES (" +
				"?, ?, ?, ?, ?)";
				PreparedStatement pstmt = con.prepareStatement(insert);
				try {
					pstmt.setInt(1, chanTypeId);
					pstmt.setString(2, javaClass);
					pstmt.setString(3, name);
					pstmt.setString(4, descr);
					pstmt.setString(5, cpdUri);

					if (log.isDebugEnabled())
						log.debug("Save Channel Type SQL('" + chanTypeId + "', " +
								"'" + javaClass + "', " + "'" + name + "', " +
								"'" + descr + "', " + "'" + cpdUri + "'): " + insert );
					pstmt.executeUpdate();
				} finally {
					pstmt.close();
				}
			} finally {
				RDBMServices.releaseConnection(con);
			}
		} else {
			// The channel type exists, so do an update
			try {
				String update = "UPDATE UP_CHAN_TYPE SET TYPE=?, TYPE_NAME=?, " +
				"TYPE_DESCR=?, TYPE_DEF_URI=? WHERE TYPE_ID=?";
				PreparedStatement pstmt = con.prepareStatement(update);
				try {
					pstmt.setString(1, javaClass);
					pstmt.setString(2, name);
					pstmt.setString(3, descr);
					pstmt.setString(4, cpdUri);
					pstmt.setInt(5, chanTypeId);
					if (log.isDebugEnabled())
						log.debug("Save Channel Type SQL('" + javaClass + "', " +
								"'" + name + "', " + "'" + descr + "', " +
								"'" + cpdUri + "'" + chanTypeId + "', " +
								"'): " + update );
					pstmt.executeUpdate();
				} finally {
					pstmt.close();
				}
			} finally {
				RDBMServices.releaseConnection(con);
			}
		}
	}

	/**
	 * Deletes a channel type.  The deletion will only succeed if no existing
	 * channels reference the channel type.
	 * @param chanType a channel type
	 * @throws java.sql.SQLException
	 */
	public void deleteChannelType(ChannelType chanType) throws SQLException {
		Connection con = null;

		try {
			con = RDBMServices.getConnection();

			// Set autocommit false for the connection
			RDBMServices.setAutoCommit(con, false);
			Statement stmt = con.createStatement();

			try {
				// First check to see if any channels are still referencing this channel type
				int chanTypeId = chanType.getId();
				String select = "SELECT * FROM UP_CHANNEL WHERE CHAN_TYPE_ID=" + chanTypeId;
				if (log.isDebugEnabled())
					log.debug("RDBMChannelRegistryStore.deleteChannelType(): " + select);
				ResultSet rs = stmt.executeQuery(select);

				// If there are channels referencing this channel type, throw an exception
				if (rs.next()) {
					String message = "Cannot delete channel type " + chanTypeId + ".  It is still in use by channels ";
					do {
						int channelPublishId = rs.getInt("CHAN_ID");
						message += channelPublishId + " ";
					} while (rs.next());
					throw new SQLException(message);
					// Otherwise delete the channel type
				} else {
					String delete = "DELETE FROM UP_CHAN_TYPE WHERE TYPE_ID=" + chanTypeId;
					if (log.isDebugEnabled())
						log.debug("RDBMChannelRegistryStore.deleteChannelType(): " + delete);
					stmt.executeUpdate(delete);
				}

				// Commit the transaction
				RDBMServices.commit(con);
			} catch (SQLException sqle) {
				// Roll back the transaction
				RDBMServices.rollback(con);
				throw sqle;
			} finally {
				stmt.close();
			}
		} finally {
			RDBMServices.releaseConnection(con);
		}
	}

	/**
	 * Create a new ChannelDefinition object.
	 * @return channelDefinition, the new channel definition
	 * @throws java.lang.Exception
	 */
	public ChannelDefinition newChannelDefinition() throws Exception {
		int nextChanDefId = CounterStoreFactory.getCounterStoreImpl().getIncrementIntegerId("UP_CHANNEL");
		return new ChannelDefinition(nextChanDefId);
	}

	/**
     * Create a new ChannelDefinition object.
     * @return channelDefinition, the new channel definition
     * @throws java.lang.Exception
     */
    public ChannelDefinition newChannelDefinition(final int id) throws Exception {
        return new ChannelDefinition(id);
    }

	/**
	 * Get a channel definition.
	 * @param channelPublishId a channel publish ID
	 * @return channelDefinition, a definition of the channel or <code>null</code>
	 *   if no matching channel definition can be found
	 * @throws java.sql.SQLException
	 * @throws java.lang.IllegalArgumentException
	 */
	public ChannelDefinition getChannelDefinition(int channelPublishId) throws SQLException {
		if (channelPublishId < 1) {
			throw new IllegalArgumentException("Invalid channel Id requested: " + channelPublishId);
		}

		ChannelDefinition channelDef = null;

		// Check the cache
		try {
			channelDef = (ChannelDefinition)EntityCachingService.instance().get(ChannelDefinition.class, String.valueOf(channelPublishId));
		} catch (Exception e) {
			log.error("Error checking cache for definition of channel with publish id "
					+ channelPublishId, e);
		}

		// If not found in cache, get it from the store and cache it, otherwise return it
		if (channelDef == null) {
			Connection con = null;
			PreparedStatement pstmtChannel = null;
			PreparedStatement pstmtChannelParam = null;
			PreparedStatement pstmtChannelMdata = null;
			ResultSet rs = null;

			try {
				con = RDBMServices.getConnection();
				pstmtChannel = getChannelPstmt(con);
				pstmtChannelParam = getChannelParamPstmt(con);
				pstmtChannelMdata = getChannelMdataPstmt(con);
				pstmtChannel.setInt(1, channelPublishId);
				if (log.isDebugEnabled())
					log.debug("RDBMChannelRegistryStore.getChannelDefinition(): " +
							pstmtChannel);
				rs = pstmtChannel.executeQuery();

				if (rs.next()) {
					int chanType = rs.getInt(4);
					if (rs.wasNull()) {
						chanType = 0;
					}
					int publisherId = rs.getInt(5);
					if (rs.wasNull()) {
						publisherId = 0;
					}
					int approverId = rs.getInt(6);
					if (rs.wasNull()) {
						approverId = 0;
					}
					int timeout = rs.getInt(9);
					if (rs.wasNull()) {
						timeout = 0;
					}
					channelDef = new ChannelDefinition(channelPublishId);
					channelDef.setTitle(rs.getString(1));
					channelDef.setDescription(rs.getString(2));
					channelDef.setJavaClass(rs.getString(3));
					channelDef.setTypeId(chanType);
					channelDef.setPublisherId(publisherId);
					channelDef.setApproverId(approverId);
					channelDef.setPublishDate(rs.getTimestamp(7));
					channelDef.setApprovalDate(rs.getTimestamp(8));
					channelDef.setTimeout(timeout);
					channelDef.setEditable(RDBMServices.dbFlag(rs.getString(10)));
					channelDef.setHasHelp(RDBMServices.dbFlag(rs.getString(11)));
					channelDef.setHasAbout(RDBMServices.dbFlag(rs.getString(12)));
					channelDef.setName(rs.getString(13));
					channelDef.setFName(rs.getString(14));
					channelDef.setIsSecure(RDBMServices.dbFlag(rs.getString(15)));

					// Don't use the following line to attain DB compatibility
					// channelDef.setLocale("en_US");

					int dbOffset = 0;
					if (pstmtChannelParam == null) { // we are using a join statement so no need for a new query
						dbOffset = 15;
					} else {
						rs.close();
						pstmtChannelParam.clearParameters();
						pstmtChannelParam.setInt(1, channelPublishId);
						if (log.isDebugEnabled())
							log.debug("RDBMChannelRegistryStore.getChannelDefinition(): " +
									pstmtChannelParam);
						rs = pstmtChannelParam.executeQuery();
					}

					while (true) {
						if (pstmtChannelParam != null && !rs.next()) {
							break;
						}
						String name = rs.getString(dbOffset + 1);
						String value = rs.getString(dbOffset + 2);
						String override = rs.getString(dbOffset + 3);
						if (name != null) {
							channelDef.addParameter(name, value, override);
						}
						if (pstmtChannelParam == null && !rs.next()) {
							break;
						}
					}

					if (localeAware) {
						// Read UP_CHANNEL_MDATA
						rs.close();
						pstmtChannelMdata.clearParameters();
						pstmtChannelMdata.setInt(1, channelPublishId);
						if (log.isDebugEnabled())
							log.debug("RDBMChannelRegistryStore.getChannelDefinition(): " +
									pstmtChannelMdata);
						try {
							rs = pstmtChannelMdata.executeQuery();

							String locale;
							while (true) {
								if (pstmtChannelMdata != null && !rs.next()) {
									break;
								}
								locale = rs.getString(1);
								channelDef.putChanTitles(locale, rs.getString(2));
								channelDef.putChanDescs(locale, rs.getString(3));
								channelDef.putChanNames(locale, rs.getString(4));

								if (pstmtChannelMdata == null && !rs.next()) {
									break;
								}
							}
						}  catch (SQLException e) {
							log.error( "RDBMChannelRegistryStore.getChannelDefinition(): " +
									"Database being used is not internationalized. " +
									"Execute `ant i18n-db' for internationalized database setting.",e);
						}
					}
				}

                //TODO add a portletPreferences field to the ChannelDefinition object to support multivalued prefs
                if (channelDef.isPortlet()) {
                    final IPortletDefinition portletDefinition = portletDefinitionRegistry.getPortletDefinition(channelPublishId);
                    final IPortletPreferences portletPreferences = portletDefinition.getPortletPreferences();
                    
                    for (final IPortletPreference portletPreference : portletPreferences.getPortletPreferences()) {
                        final String name = portletPreference.getName();
                        final String[] values = portletPreference.getValues();
                        final boolean readOnly = portletPreference.isReadOnly();
                        
                        final String value;
                        if (values == null || values.length == 0) {
                            value = null;
                        }
                        else {
                            value = values[0];
                        }
                        
                        channelDef.addParameter(PORTLET_CHANNEL_PARAM_PREFIX + name, value, !readOnly);
                    }
                }

				if (log.isDebugEnabled())
					log.debug("RDBMChannelRegistryStore.getChannelDefinition(): Read channel " +
							channelPublishId + " from the store");

				// Add the channel definition to the cache
				try {
          if (channelDef != null) {
            EntityCachingService.instance().add(channelDef);
          }
				} catch (Exception e) {
					log.error("Error caching channel definition " + channelDef, e);
				}

			} finally {
				try { rs.close(); } catch (Exception e) {}
				try { pstmtChannel.close(); } catch (Exception e) {}
				try { pstmtChannelParam.close(); } catch (Exception e) {}
				try { pstmtChannelMdata.close(); } catch (Exception e) {}
				try { RDBMServices.releaseConnection(con); } catch (Exception e) {}
			}
		}
		return channelDef;
	}

	/**
	 * Get a channel definition.  If there is more than one channel definition
	 * with the given functional name, then the one with the most recent
	 * approval date will be returned.
	 * @param channelFunctionalName a channel functional name
	 * @return channelDefinition, a definition of the channel or <code>null</code>
	 *   if no matching channel definition can be found
	 * @throws java.sql.SQLException
	 */
	public ChannelDefinition getChannelDefinition(String channelFunctionalName) throws SQLException {
		ChannelDefinition channelDef = null;
		Connection con = RDBMServices.getConnection();

		try {
			String query = "SELECT CHAN_ID FROM UP_CHANNEL WHERE CHAN_FNAME=? ORDER BY CHAN_APVL_DT DESC";
			PreparedStatement pstmt = con.prepareStatement(query);
			try {
				if (log.isDebugEnabled())
					log.debug("Executing '" + query + "' with CHAN_FNAME=" +
							channelFunctionalName);

				pstmt.setString(1, channelFunctionalName);
				ResultSet rs = pstmt.executeQuery();
				try {
					if (rs.next()) {
						channelDef = getChannelDefinition(rs.getInt("CHAN_ID"));
					}
				} finally {
					rs.close();
				}
			} finally {
				pstmt.close();
			}
		} finally {
			RDBMServices.releaseConnection(con);
		}
		return channelDef;
	}

	/**
	 * Get all channel definitions including ones that haven't been approved.
	 * @return channelDefs, the channel definitions
	 * @throws java.sql.SQLException
	 */
	public ChannelDefinition[] getChannelDefinitions() throws SQLException {
		ChannelDefinition[] channelDefs = null;
		Connection con = RDBMServices.getConnection();

		try {
			Statement stmt = con.createStatement();
			try {
				String query = "SELECT CHAN_ID FROM UP_CHANNEL";
				if (log.isDebugEnabled())
					log.debug("RDBMChannelRegistryStore.getChannelDefinitions(): " + query);
				ResultSet rs = stmt.executeQuery(query);
				try {
					List channelDefsList = new ArrayList();
					while (rs.next()) {
						ChannelDefinition channelDef = getChannelDefinition(rs.getInt("CHAN_ID"));
						channelDefsList.add(channelDef);
					}
					channelDefs = (ChannelDefinition[])channelDefsList.toArray(new ChannelDefinition[0]);
				} finally {
					rs.close();
				}
			} finally {
				stmt.close();
			}
		} finally {
			RDBMServices.releaseConnection(con);
		}
		return channelDefs;
	}

	/**
	 * Persists a channel definition.
	 * @param channelDef the channel definition
	 * @throws java.sql.SQLException
	 */
	public void saveChannelDefinition (ChannelDefinition channelDef) throws Exception {
		Connection con = RDBMServices.getConnection();
		try {
			int channelPublishId = channelDef.getId();

			// Set autocommit false for the connection
			RDBMServices.setAutoCommit(con, false);
			try {
				saveChannelDef(con, channelDef);
				
				deleteChannelParams(con, channelPublishId);
				ChannelParameter[] parameters = channelDef.getParameters();

                // Keep track of any portlet preferences
                final boolean isPortlet = channelDef.isPortlet();
                final LinkedHashMap<String, List<String>> portletPreferencesBuilder = new LinkedHashMap<String, List<String>>();
                final Map<String, Boolean> portletPreferencesReadOnly = new HashMap<String, Boolean>();
				
				if (parameters != null) {
					for (int i = 0; i < parameters.length; i++) {
						String paramName = parameters[i].getName();
						String paramValue = parameters[i].getValue();
						boolean paramOverride = parameters[i].getOverride();

						if (paramName == null && paramValue == null) {
							throw new RuntimeException("Invalid parameter node");
						}

						//TODO Add a ChannelDefinition.getPortletPreferences() API to store/track definition level portlet preferences
						if (isPortlet && paramName.startsWith(PORTLET_CHANNEL_PARAM_PREFIX)) {
                            // We have a portlet preference
                            final String prefName = paramName.substring(PORTLET_CHANNEL_PARAM_PREFIX.length());
                            final String prefValue = paramValue;
                            
                            List<String> prefValues = portletPreferencesBuilder.get(prefName);
                            if (prefValues == null) {
                                prefValues = new LinkedList<String>();
                                portletPreferencesBuilder.put(prefName, prefValues);
                                
                                portletPreferencesReadOnly.put(prefName, !paramOverride);
                            }
                            
                            prefValues.add(prefValue);
                        }
                        else {
                            insertChannelParam(con, channelPublishId, paramName, paramValue, paramOverride);
                        }
					}
				}

				// Commit the transaction
				RDBMServices.commit(con);
				
				// Notify the cache
				try {
					EntityCachingService.instance().update(channelDef);
				} catch (Exception e) {
					log.error("Error updating cache for channel definition " + channelDef, e);
				}
				
				//TODO There isn't a good way to tie the success of the portlet commit and the channel definition commit
				//     together since the portlet definition commit needs the IChannelRegistryStore to know about the channel
				//     the definition is for during the update.
                if (isPortlet) {
                    try {
                        //Get or Create the portlet definition
                        final IPortletDefinition portletDefinition = portletDefinitionRegistry.getOrCreatePortletDefinition(channelPublishId);
                        
                        //Convert the Lists into actual IPortletPreference objects
                        final List<IPortletPreference> portletPreferencesList = new ArrayList<IPortletPreference>(portletPreferencesBuilder.size());
                        for (final Entry<String, List<String>> prefEntry : portletPreferencesBuilder.entrySet()) {
                            final String prefName = prefEntry.getKey();
                            final List<String> prefValues = prefEntry.getValue();
                            final Boolean readOnly = portletPreferencesReadOnly.get(prefName);
                            
                            final IPortletPreference portletPreference = new PortletPreferenceImpl(prefName, readOnly != null ? readOnly : false, prefValues.toArray(new String[prefValues.size()]));
                            portletPreferencesList.add(portletPreference);
                        }
                        
                        //Update the preferences of the portlet definition
                        final IPortletPreferences portletPreferences = portletDefinition.getPortletPreferences();
                        portletPreferences.setPortletPreferences(portletPreferencesList);
                        portletDefinitionRegistry.updatePortletDefinition(portletDefinition);
                    }
                    catch (DataAccessException dae) {
                        log.error("Exception persisting IPortletDefinition, the stored IPortletDefinition will not match the corresponding ChannelDefinition.", dae);
                    }
                }

			} catch (SQLException sqle) {
				log.error("Exception saving channel definition " + channelDef, sqle);
				RDBMServices.rollback(con);
				throw sqle;
			}
		} finally {
			RDBMServices.releaseConnection(con);
		}
	}

	private void saveChannelDef(Connection con, ChannelDefinition channelDef) throws SQLException {
		int channelPublishId = channelDef.getId();
		String sqlTitle = RDBMServices.sqlEscape(channelDef.getTitle());
		String sqlDescription = RDBMServices.sqlEscape(channelDef.getDescription());
		String sqlClass = channelDef.getJavaClass();
		int sqlTypeID = channelDef.getTypeId();
		int chanPublisherId = channelDef.getPublisherId();
		String chanPublishDate = this.databaseMetadata.sqlTimeStamp(channelDef.getPublishDate());
		int chanApproverId = channelDef.getApproverId();
		String chanApprovalDate = this.databaseMetadata.sqlTimeStamp(channelDef.getApprovalDate());
		int sqlTimeout = channelDef.getTimeout();
		String sqlEditable = RDBMServices.dbFlag(channelDef.isEditable());
		String sqlHasHelp = RDBMServices.dbFlag(channelDef.hasHelp());
		String sqlHasAbout = RDBMServices.dbFlag(channelDef.hasAbout());
		String sqlName = RDBMServices.sqlEscape(channelDef.getName());
		String sqlFName = RDBMServices.sqlEscape(channelDef.getFName());
		String sqlIsSecure = RDBMServices.dbFlag(channelDef.isSecure());

		// If channel is already there, do an update, otherwise do an insert
		String sql;
		if (channelExists(con, channelPublishId)) {
			sql = "UPDATE UP_CHANNEL SET " +
			"CHAN_TITLE='" + sqlTitle + "', " +
			"CHAN_DESC='" + sqlDescription + "', " +
			"CHAN_CLASS='" + sqlClass + "', " +
			"CHAN_TYPE_ID=" + sqlTypeID + ", " +
			"CHAN_PUBL_ID=" + chanPublisherId + ", " +
			"CHAN_PUBL_DT=" + chanPublishDate + ", " +
			"CHAN_APVL_ID=" + chanApproverId + ", " +
			"CHAN_APVL_DT=" + chanApprovalDate + ", " +
			"CHAN_TIMEOUT=" + sqlTimeout + ", " +
			"CHAN_EDITABLE='" + sqlEditable + "', " +
			"CHAN_HAS_HELP='" + sqlHasHelp + "', " +
			"CHAN_HAS_ABOUT='" + sqlHasAbout + "', " +
			"CHAN_NAME='" + sqlName + "', " +
			"CHAN_FNAME='" + sqlFName + "', " +
			"CHAN_SECURE='" + sqlIsSecure + "' " +
			"WHERE CHAN_ID=" + channelPublishId;
		} else {
			sql = "INSERT INTO UP_CHANNEL (CHAN_ID, CHAN_TITLE, CHAN_DESC, CHAN_CLASS, CHAN_TYPE_ID, CHAN_PUBL_ID, CHAN_PUBL_DT, "
				+ "CHAN_APVL_ID, CHAN_APVL_DT, CHAN_TIMEOUT, CHAN_EDITABLE, CHAN_HAS_HELP, CHAN_HAS_ABOUT, CHAN_NAME, CHAN_FNAME, CHAN_SECURE) ";
			sql += "VALUES (" + channelPublishId + ", '" + sqlTitle + "', '" + sqlDescription + "', '" + sqlClass + "', " + sqlTypeID + ", "
			+ chanPublisherId + ", " + chanPublishDate + ", " + chanApproverId + ", " + chanApprovalDate + ", " + sqlTimeout
			+ ", '" + sqlEditable + "', '" + sqlHasHelp + "', '" + sqlHasAbout
			+ "', '" + sqlName + "', '" + sqlFName + "', '" + sqlIsSecure + "')";
		}
		if (log.isDebugEnabled())
			log.debug(sql);
		Statement stmt = con.createStatement();
		try{
			stmt.executeUpdate(sql);
		}finally{
			stmt.close();
		}
	}

	private static boolean channelExists(Connection con, int channelPublishId) throws SQLException {
		String query = "SELECT CHAN_ID FROM UP_CHANNEL WHERE CHAN_ID=" + channelPublishId;
		if (log.isDebugEnabled())
			log.debug(query);
		Statement stmt = con.createStatement();
		boolean doUpdate = false;
		try{
			ResultSet rs = stmt.executeQuery(query);
			doUpdate = rs.next();
		}finally{
			stmt.close();
		}
		return doUpdate;
	}

	private static void insertChannelParam(Connection con, int channelPublishId, String paramName, String paramValue, boolean paramOverride) throws SQLException {
		Statement stmt2 = con.createStatement();
		try{
			// We have a normal channel parameter
			String sql = "INSERT INTO UP_CHANNEL_PARAM (CHAN_ID, CHAN_PARM_NM, CHAN_PARM_VAL, CHAN_PARM_OVRD) VALUES (" + channelPublishId +
			",'" + paramName + "','" + paramValue + "', '" + (paramOverride ? "Y" : "N") + "')";
			if (log.isDebugEnabled())
				log.debug(sql);
			stmt2.executeUpdate(sql);
		}finally{
			stmt2.close();
		}
	}

	private static void deleteChannelParams(Connection con, int channelPublishId) throws SQLException {
		Statement stmt2 = con.createStatement();
		try{
			// First delete existing parameters for this channel
			String sql = "DELETE FROM UP_CHANNEL_PARAM WHERE CHAN_ID=" + channelPublishId;
			if (log.isDebugEnabled())
				log.debug(sql);
			stmt2.executeUpdate(sql);
		}finally{
			stmt2.close();
		}
	}

	/**
	 * Permanently deletes a channel definition from the store.
	 * All references to this channel definition are also deleted.
	 * @param channelDef the channel definition
	 * @throws java.sql.SQLException
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public void deleteChannelDefinition(ChannelDefinition channelDef) throws SQLException, GroupsException {
		Connection con = RDBMServices.getConnection();
		try {
			Statement stmt = con.createStatement();
			try {
				int channelPublishId = channelDef.getId();

				// Remove versioning information for channel if found.
				VersionsManager mgr = VersionsManager.getInstance();
				mgr.removeVersion(channelDef.getFName());

				// Delete from UP_CHANNEL
				String delete = "DELETE FROM UP_CHANNEL WHERE CHAN_ID=" + channelPublishId;
				if (log.isDebugEnabled())
					log.debug("RDBMChannelRegistryStore.deleteChannelDefinition(): " + delete);
				stmt.executeUpdate(delete);

				// Delete from UP_CHANNEL_PARAM
				delete = "DELETE FROM UP_CHANNEL_PARAM WHERE CHAN_ID=" + channelPublishId;
				if (log.isDebugEnabled())
					log.debug("RDBMChannelRegistryStore.deleteChannelDefinition(): " + delete);
				stmt.executeUpdate(delete);

				// Delete from UP_PERMISSION
				// This needs to be updated to work with permission interfaces!!!
				delete = "DELETE FROM UP_PERMISSION WHERE OWNER='CHAN_ID." + channelPublishId + "' OR TARGET='CHAN_ID." + channelPublishId + "'";
				if (log.isDebugEnabled())
					log.debug("RDBMChannelRegistryStore.deleteChannelDefinition(): " + delete);
				stmt.executeUpdate(delete);

				// Delete from UPC_KEYWORD
				delete = "DELETE FROM UPC_KEYWORD WHERE CHAN_ID=" + channelPublishId;
				if (log.isDebugEnabled())
					log.debug("RDBMChannelRegistryStore.deleteChannelDefinition(): " + delete);
				stmt.executeUpdate(delete);

				// Disassociate from parent categories (delete from UP_GROUP_MEMBERSHIP)
				IEntity channelDefEntity = GroupService.getEntity(String.valueOf(channelPublishId), ChannelDefinition.class);
				Iterator iter = channelDefEntity.getContainingGroups();
				while (iter.hasNext()) {
					IEntityGroup parentGroup = (IEntityGroup)iter.next();
					parentGroup.removeMember(channelDefEntity);
					parentGroup.updateMembers();
				}

				// Notify the cache
				try {
					EntityCachingService.instance().remove(channelDef);
				} catch (Exception e) {
					log.error("Error removing channel definition "
							+ channelDef + " from cache.", e);
				}

			} finally {
				stmt.close();
			}
		} finally {
			RDBMServices.releaseConnection(con);
		}
	}

	/**
	 * Sets a channel definition as "approved".  This effectively makes a
	 * channel definition available in the channel registry, making the channel
	 * available for subscription to those authorized to subscribe to it.
	 * This method is a convenience method. As an alternative to calling
	 * this method, one could simply set the approver ID and approval date
	 * and then call saveChannelDefinition(ChannelDefinition chanDef).
	 * @param channelDef the channel definition to approve
	 * @param approver the user that approves this channel definition
	 * @param approveDate the date when the channel definition should be approved (can be future dated)
	 * @throws Exception
	 */
	public void approveChannelDefinition(ChannelDefinition channelDef, IPerson approver, Date approveDate) throws Exception {
		channelDef.setApproverId(approver.getID());
		channelDef.setApprovalDate(approveDate);
		saveChannelDefinition(channelDef);
	}

	/**
	 * Removes a channel from the channel registry by changing
	 * its status from "approved" to "unapproved".  Afterwards, no one
	 * will be able to subscribe to or render the channel.
	 * This method is a convenience method. As an alternative to calling
	 * this method, one could simply set the approver ID and approval date
	 * to NULL and then call saveChannelDefinition(ChannelDefinition chanDef).
	 * @param channelDef the channel definition to disapprove
	 * @throws Exception
	 */
	public void disapproveChannelDefinition(ChannelDefinition channelDef) throws Exception {
		channelDef.setApproverId(-1);
		channelDef.setApprovalDate(null);
		saveChannelDefinition(channelDef);
	}

	/**
	 * Creates a new channel category.
	 * @return channelCategory the new channel category
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public ChannelCategory newChannelCategory() throws GroupsException {
		IEntityGroup categoryGroup = GroupService.newGroup(ChannelDefinition.class);
		categoryGroup.setName(""); // name cannot be null
		categoryGroup.setCreatorID(""); // creatorId cannot be null
		categoryGroup.update();
		String id = categoryGroup.getKey();
		return new ChannelCategory(id);
	}

	/**
	 * Creates a new channel category with the specified values.
	 * @param name the name of the category
	 * @param description the name of the description
	 * @param creatorId the id of the creator or system
	 * @return channelCategory the new channel category
	 * @throws GroupsException
	 */
	public ChannelCategory newChannelCategory( String name,
			String description,
			String creatorId )
	throws GroupsException
	{
		IEntityGroup categoryGroup = GroupService.newGroup(ChannelDefinition.class);
		categoryGroup.setName( name ); // name cannot be null
		categoryGroup.setCreatorID( creatorId ); // creatorId cannot be null
		categoryGroup.setDescription( description );
		categoryGroup.update();
		String id = categoryGroup.getKey();
		ChannelCategory cat = new ChannelCategory(id);
		cat.setName( name );
		cat.setDescription( description );
		cat.setCreatorId( creatorId );
		return cat;
	}

	/**
	 * Gets an existing channel category.
	 * @param channelCategoryId the id of the category to get
	 * @return channelCategory the channel category
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public ChannelCategory getChannelCategory(String channelCategoryId) throws GroupsException {
		IEntityGroup categoryGroup = GroupService.findGroup(channelCategoryId);
		ChannelCategory category = new ChannelCategory(channelCategoryId);
		category.setName(categoryGroup.getName());
		category.setDescription(categoryGroup.getDescription());
		category.setCreatorId(categoryGroup.getCreatorID());
		return category;
	}

	/**
	 * Gets top level channel category
	 * @return channelCategories the new channel category
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public ChannelCategory getTopLevelChannelCategory() throws GroupsException {
		IEntityGroup categoryGroup = GroupService.getDistinguishedGroup(IGroupConstants.CHANNEL_CATEGORIES);
		return getChannelCategory(categoryGroup.getKey());
	}

	/**
	 * Gets all child channel categories for a parent category.
	 * @return channelCategories the children categories
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public ChannelCategory[] getChildCategories(ChannelCategory parent) throws GroupsException {
		String parentKey = String.valueOf(parent.getId());
		IEntityGroup parentGroup = GroupService.findGroup(parentKey);
		List categories = new ArrayList();
		Iterator iter = parentGroup.getMembers();
		while (iter.hasNext()) {
			IGroupMember gm = (IGroupMember)iter.next();
			if (gm.isGroup()) {
				String categoryId = gm.getKey();
				categories.add(getChannelCategory(categoryId));
			}
		}
		return (ChannelCategory[])categories.toArray(new ChannelCategory[0]);
	}

	/**
	 * Gets all child channel definitions for a parent category.
	 * @return channelDefinitions the children channel definitions
	 * @throws java.sql.SQLException
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public ChannelDefinition[] getChildChannels(ChannelCategory parent) throws SQLException, GroupsException {
		String parentKey = String.valueOf(parent.getId());
		IEntityGroup parentGroup = GroupService.findGroup(parentKey);
		List channelDefs = new ArrayList();
		Iterator iter = parentGroup.getMembers();
		while (iter.hasNext()) {
			IGroupMember gm = (IGroupMember)iter.next();
			if (gm.isEntity()) {
				int channelPublishId = Integer.parseInt(gm.getKey());
				channelDefs.add(getChannelDefinition(channelPublishId));
			}
		}
		return (ChannelDefinition[])channelDefs.toArray(new ChannelDefinition[0]);
	}

	/**
	 * Gets the immediate parent categories of this category.
	 * @return parents, the parent categories.
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public ChannelCategory[] getParentCategories(ChannelCategory child) throws GroupsException {
		String childKey = String.valueOf(child.getId());
		IEntityGroup childGroup = GroupService.findGroup(childKey);
		List parents = new ArrayList();
		Iterator iter = childGroup.getContainingGroups();
		while (iter.hasNext()) {
			IGroupMember gm = (IGroupMember)iter.next();
			if (gm.isGroup()) {
				String categoryId = gm.getKey();
				parents.add(getChannelCategory(categoryId));
			}
		}
		return (ChannelCategory[])parents.toArray(new ChannelCategory[0]);
	}

	/**
	 * Gets the immediate parent categories of this channel definition.
	 * @return parents, the parent categories.
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public ChannelCategory[] getParentCategories(ChannelDefinition child) throws GroupsException {
		String childKey = String.valueOf(child.getId());
		IEntity childEntity = GroupService.getEntity(childKey, ChannelDefinition.class);
		List parents = new ArrayList();
		Iterator iter = childEntity.getContainingGroups();
		while (iter.hasNext()) {
			IGroupMember gm = (IGroupMember)iter.next();
			if (gm.isGroup()) {
				String categoryId = gm.getKey();
				parents.add(getChannelCategory(categoryId));
			}
		}
		return (ChannelCategory[])parents.toArray(new ChannelCategory[0]);
	}

	/**
	 * Persists a channel category.
	 * @param category the channel category to persist
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public void saveChannelCategory(ChannelCategory category) throws GroupsException {
		IEntityGroup categoryGroup = GroupService.findGroup(category.getId());
		categoryGroup.setName(category.getName());
		categoryGroup.setDescription(category.getDescription());
		categoryGroup.setCreatorID(category.getCreatorId());
		categoryGroup.update();
	}

	/**
	 * Deletes a channel category.
	 * @param category the channel category to delete
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public void deleteChannelCategory(ChannelCategory category) throws GroupsException {
		String key = String.valueOf(category.getId());
		ILockableEntityGroup categoryGroup = GroupService.findLockableGroup(key,"UP_FRAMEWORK");
		categoryGroup.delete();
	}

	/**
	 * Makes one category a child of another.
	 * @param child the source category
	 * @param parent the destination category
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public void addCategoryToCategory(ChannelCategory child, ChannelCategory parent) throws GroupsException {
		String childKey = String.valueOf(child.getId());
		IEntityGroup childGroup = GroupService.findGroup(childKey);
		String parentKey = String.valueOf(parent.getId());
		IEntityGroup parentGroup = GroupService.findGroup(parentKey);
		parentGroup.addMember(childGroup);
		parentGroup.updateMembers();
	}

	/**
	 * Makes one category a child of another.
	 * @param child the category to remove
	 * @param parent the category to remove from
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public void removeCategoryFromCategory(ChannelCategory child, ChannelCategory parent) throws GroupsException {
		String childKey = String.valueOf(child.getId());
		IEntityGroup childGroup = GroupService.findGroup(childKey);
		String parentKey = String.valueOf(parent.getId());
		IEntityGroup parentGroup = GroupService.findGroup(parentKey);
		parentGroup.removeMember(childGroup);
		parentGroup.updateMembers();
	}

	/**
	 * Associates a channel definition with a category.
	 * @param channelDef the channel definition
	 * @param category the channel category to which to associate the channel definition
	 * @throws org.jasig.portal.PortalException
	 */
	public void addChannelToCategory(ChannelDefinition channelDef, ChannelCategory category) throws PortalException {
		String channelDefKey = String.valueOf(channelDef.getId());
		IEntity channelDefEntity = GroupService.getEntity(channelDefKey, ChannelDefinition.class);
		IEntityGroup categoryGroup = GroupService.findGroup(category.getId());
		categoryGroup.addMember(channelDefEntity);
		categoryGroup.updateMembers();
	}

	/**
	 * Disassociates a channel definition from a category.
	 * @param channelDef the channel definition
	 * @param category the channel category from which to disassociate the channel definition
	 * @throws org.jasig.portal.PortalException
	 */
	public void removeChannelFromCategory(ChannelDefinition channelDef, ChannelCategory category) throws PortalException {
		String channelDefKey = String.valueOf(channelDef.getId());
		IEntity channelDefEntity = GroupService.getEntity(channelDefKey, ChannelDefinition.class);
		String categoryKey = String.valueOf(category.getId());
		IEntityGroup categoryGroup = GroupService.findGroup(categoryKey);
		categoryGroup.removeMember(channelDefEntity);
		categoryGroup.updateMembers();
	}


	protected final PreparedStatement getChannelPstmt(Connection con) throws SQLException {
		String sql = "SELECT UC.CHAN_TITLE, UC.CHAN_DESC, UC.CHAN_CLASS, UC.CHAN_TYPE_ID, " +
		"UC.CHAN_PUBL_ID, UC.CHAN_APVL_ID, UC.CHAN_PUBL_DT, UC.CHAN_APVL_DT, " +
		"UC.CHAN_TIMEOUT, UC.CHAN_EDITABLE, UC.CHAN_HAS_HELP, UC.CHAN_HAS_ABOUT, " +
		"UC.CHAN_NAME, UC.CHAN_FNAME, UC.CHAN_SECURE";

		if (this.databaseMetadata.supportsOuterJoins()) {
			sql += ", CHAN_PARM_NM, CHAN_PARM_VAL, CHAN_PARM_OVRD, CHAN_PARM_DESC FROM " + this.databaseMetadata.getJoinQuery().getQuery("channel");
		} else {
			sql += " FROM UP_CHANNEL UC WHERE";
		}

		sql += " UC.CHAN_ID=?";

		return con.prepareStatement(sql);
	}

	protected final PreparedStatement getChannelParamPstmt(Connection con) throws SQLException {
		if (this.databaseMetadata.supportsOuterJoins()) {
			return null;
		} else {
			return con.prepareStatement("SELECT CHAN_PARM_NM, CHAN_PARM_VAL,CHAN_PARM_OVRD,CHAN_PARM_DESC FROM UP_CHANNEL_PARAM WHERE CHAN_ID=?");
		}
	}

	protected final PreparedStatement getChannelMdataPstmt(Connection con) throws SQLException {
		return con.prepareStatement("SELECT LOCALE, CHAN_TITLE, CHAN_DESC, CHAN_NAME FROM UP_CHANNEL_MDATA WHERE CHAN_ID=?");
	}

}
