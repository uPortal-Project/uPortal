package org.jasig.portal.groups;

/**
 * Copyright (c) 2001 The JA-SIG Collaborative.  All rights reserved.
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

import java.sql.*;
import java.util.*;
import org.jasig.portal.*;
import org.jasig.portal.services.LogService;
import org.jasig.portal.services.SequenceGenerator;
import org.jasig.portal.utils.SqlTransaction;

/**
 * Factory for <code>EntityGroupImpl</code>.
 * @author Dan Ellentuck
 * @version 1.0, 11/29/01  
 */
public class EntityGroupStoreRDBM implements IEntityGroupStore {
	private static EntityGroupStoreRDBM singleton;
	
	// Constant strings for GROUP table:
	private static String GROUP_TABLE = "UP_GROUP";
	private static String GROUP_TABLE_ALIAS = "T1";
	private static String GROUP_ID_COLUMN = "GROUP_ID";
	private static String GROUP_CREATOR_COLUMN = "CREATOR_ID";
	private static String GROUP_TYPE_COLUMN = "ENTITY_TYPE_ID";
	private static String GROUP_NAME_COLUMN = "GROUP_NAME";
	private static String GROUP_DESCRIPTION_COLUMN = "DESCRIPTION";

	// SQL strings for GROUP crud:
	private static String allGroupColumns;
	private static String allGroupColumnsWithTableAlias;
	private static String findContainingGroupsSql;
	private static String findGroupSql;
	private static String findGroupsByCreatorSql;
	private static String findMemberGroupSql;
	private static String findMemberGroupsSql;
	private static String insertGroupSql;
	private static String updateGroupSql;
	
	// Constant strings for MEMBERS table:
	private static String MEMBER_TABLE = "UP_GROUP_MEMBERSHIP";
	private static String MEMBER_TABLE_ALIAS = "T2";
	private static String MEMBER_GROUP_ID_COLUMN = "GROUP_ID";
	private static String MEMBER_MEMBER_KEY_COLUMN = "MEMBER_KEY";
	private static String MEMBER_IS_GROUP_COLUMN = "MEMBER_IS_GROUP";
	private static String MEMBER_IS_ENTITY = "F";
	private static String MEMBER_IS_GROUP = "T";
	
	// SQL strings for group MEMBERS crud:
	private static String allMemberColumns;
	private static String deleteMembersInGroupSql;
	private static String deleteMemberSql;
	private static String insertMemberSql;
	
/**
 * EntityGroupStoreRDBM constructor.
 */
public EntityGroupStoreRDBM() 
{
	super();
}
/**
 * @param conn java.sql.Connection
 * @exception java.sql.SQLException
 */
protected static void commit(Connection conn) throws java.sql.SQLException 
{
	SqlTransaction.commit(conn);
}
/**
 * If this entity exists, delete it.
 * @param group org.jasig.portal.groups.IEntityGroup
 */
public void delete(IEntityGroup group) throws GroupsException
{
	if ( existsInDatabase(group) )
	{
		try
			{ primDelete(group); }
		catch (SQLException sqle)
			{ throw new GroupsException("Problem deleting " + group + ": " + sqle.getMessage()); }
	}
}
/**
 * Answer if the IEntityGroup entity exists in the database.
 * @return boolean
 * @param group IEntityGroup
 */
private boolean existsInDatabase(IEntityGroup group) throws GroupsException
{
	IEntityGroup ug = this.find(group.getKey());
	return ug != null;
}
/**
 * Find and return an instance of the group.
 * @return org.jasig.portal.groups.IEntityGroup
 * @param key java.lang.Object
 */
public IEntityGroup find(String groupID) throws GroupsException
{
	IEntityGroup ug = null;
	java.sql.Connection conn = null;
	try
	{
		conn = RdbmServices.getConnection();
		java.sql.PreparedStatement ps = conn.prepareStatement(getFindGroupSql());
		ps.setString(1, groupID);
		java.sql.ResultSet rs = ps.executeQuery();
		while (rs.next())
			{ ug = instanceFromResultSet(rs); }
	}
	catch (Exception e)
		{ 
			LogService.log (LogService.ERROR, e);
			throw new GroupsException("Error retrieving " + groupID + ": " + e);
		}
	finally
		{ RdbmServices.releaseConnection(conn); }

	return ug;
}
/**
 * Find the groups associated with this member key.
 * @return java.util.Iterator
 * @param String memberKey 
 */
private java.util.Iterator findContainingGroups(String memberKey, int type) throws GroupsException
{
	java.sql.Connection conn = null;
	Collection groups = new ArrayList();
	IEntityGroup eg = null;

	try
	{
		conn = RdbmServices.getConnection();
		java.sql.PreparedStatement ps = conn.prepareStatement(getFindContainingGroupsSql());
		ps.setString(1, memberKey);
		ps.setInt(2, type);
		java.sql.ResultSet rs = ps.executeQuery();
		while (rs.next())
		{ 
			eg = instanceFromResultSet(rs);
			groups.add(eg);
		}
	}
	catch (Exception e)
		{ 
			LogService.log (LogService.ERROR, e);
			throw new GroupsException("Problem retrieving containing groups: " + e);
		}
		
	finally
		{ RdbmServices.releaseConnection(conn); }

	return groups.iterator();
}
/**
 * Find the groups that this group member belongs to.
 * @return java.util.Iterator
 * @param group org.jasig.portal.groups.IGroupMember
 */
public java.util.Iterator findContainingGroups(IGroupMember gm) throws GroupsException
{
	String memberKey = gm.getKey();
	Integer type = EntityTypes.getEntityTypeID(gm.getEntityType());
	return findContainingGroups(memberKey, type.intValue());
}
/**
 * Find the groups with this creatorID.
 * @return java.util.Iterator
 * @param String creatorID 
 */
public java.util.Iterator findGroupsByCreator(String creatorID) throws GroupsException
{
	java.sql.Connection conn = null;
	Collection groups = new ArrayList();
	IEntityGroup eg = null;
	
	try
	{
		conn = RdbmServices.getConnection();
		PreparedStatement ps = conn.prepareStatement(getFindGroupsByCreatorSql());
		ps.setString(1, creatorID);
		ResultSet rs = ps.executeQuery();
		while (rs.next())
		{ 
			eg = instanceFromResultSet(rs);
			groups.add(eg);
		}
	}
	catch (Exception e)
		{ 
			LogService.log (LogService.ERROR, e);
			throw new GroupsException("Problem retrieving groups: " + e);
		}
		
	finally
		{ RdbmServices.releaseConnection(conn); }

	return groups.iterator();
}
/**
 * Find the IUserGroups that are members of the group.
 * @return java.util.Iterator
 * @param group org.jasig.portal.groups.IEntityGroup
 */
public Iterator findMemberGroups(IEntityGroup group) throws GroupsException
{
	java.sql.Connection conn = null;
	Collection groups = new ArrayList();
	IEntityGroup eg = null;

	try
	{
		conn = RdbmServices.getConnection();
		java.sql.PreparedStatement ps = conn.prepareStatement(getFindMemberGroupsSql());
		ps.setString(1, group.getKey());
		java.sql.ResultSet rs = ps.executeQuery();
		while (rs.next())
		{ 
			eg = instanceFromResultSet(rs);
			groups.add(eg);
		}
	}
	catch (Exception sqle)
		{ 
			LogService.log (LogService.ERROR, sqle);
			throw new GroupsException("Problem retrieving member groups: " + sqle);
		}
	finally
		{ RdbmServices.releaseConnection(conn); }

	return groups.iterator();
}
/**
 * @return java.lang.String
 */
private static java.lang.String getAllGroupColumns()
{
	
	if ( allGroupColumns == null )
	{
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
/**
 * @return java.lang.String
 */
private static java.lang.String getAllGroupColumnsWithTableAlias()
{
	
	if ( allGroupColumnsWithTableAlias == null )
	{
		StringBuffer buff = new StringBuffer(100);
		buff.append(prependGroupTableAlias(GROUP_ID_COLUMN));
		buff.append(", ");
		buff.append(prependGroupTableAlias(GROUP_CREATOR_COLUMN));
		buff.append(", ");
		buff.append(prependGroupTableAlias(GROUP_TYPE_COLUMN));
		buff.append(", ");
		buff.append(prependGroupTableAlias(GROUP_NAME_COLUMN));
		buff.append(", ");
		buff.append(prependGroupTableAlias(GROUP_DESCRIPTION_COLUMN));
		
		allGroupColumnsWithTableAlias = buff.toString();
	}	
	return allGroupColumnsWithTableAlias;
}
/**
 * @return java.lang.String
 */
private static java.lang.String getAllMemberColumns()
{
	if ( allMemberColumns == null )
	{
		StringBuffer buff = new StringBuffer(100);
		
		buff.append(MEMBER_GROUP_ID_COLUMN);
		buff.append(", ");
		buff.append(MEMBER_MEMBER_KEY_COLUMN);
		buff.append(", ");
		buff.append(MEMBER_IS_GROUP_COLUMN);
		
		allMemberColumns = buff.toString();
	}	
	return allMemberColumns;
}
/**
 * @return java.lang.String
 */
private static java.lang.String getDeleteGroupSql(IEntityGroup group) 
{
	StringBuffer buff = new StringBuffer(100);
	buff.append("DELETE ");
	buff.append(GROUP_TABLE);
	buff.append(" WHERE ");
	buff.append(GROUP_ID_COLUMN);
	buff.append(" = '");	
	buff.append(group.getKey());
	buff.append("'");	
	return buff.toString();
}
/**
 * @return java.lang.String
 */
private static java.lang.String getDeleteMembersInGroupSql()
{
	if ( deleteMembersInGroupSql == null )
	{
		StringBuffer buff = new StringBuffer(100);
		buff.append("DELETE ");
		buff.append(MEMBER_TABLE);
		buff.append(" WHERE ");
		buff.append(GROUP_ID_COLUMN);
		buff.append(" = ");
		
		deleteMembersInGroupSql = buff.toString();
	}	
	return deleteMembersInGroupSql;
}
/**
 * @return java.lang.String
 */
private static java.lang.String getDeleteMembersInGroupSql(IEntityGroup group) 
{
	StringBuffer buff = new StringBuffer(getDeleteMembersInGroupSql());
	buff.append("'");
	buff.append(group.getKey());
	buff.append("'");	
	return buff.toString();
}
/**
 * @return java.lang.String
 */
private static java.lang.String getDeleteMemberSql()
{
	if ( deleteMemberSql == null )
	{
		StringBuffer buff = new StringBuffer(100);
		buff.append("DELETE ");
		buff.append(MEMBER_TABLE);
		buff.append(" WHERE ");
		buff.append(MEMBER_GROUP_ID_COLUMN);
		buff.append(" = ? AND ");
		buff.append(MEMBER_MEMBER_KEY_COLUMN);
		buff.append(" = ? AND ");
		buff.append(MEMBER_IS_GROUP_COLUMN);
		buff.append(" = ? ");
		
		
		deleteMemberSql = buff.toString();
	}	
	return deleteMemberSql;
}
/**
 * @return java.lang.String
 */
private static java.lang.String getFindContainingGroupsSql() 
{
	if ( findContainingGroupsSql == null)
	{
		StringBuffer buff = new StringBuffer(500);
		buff.append("SELECT ");
		buff.append(getAllGroupColumnsWithTableAlias());
		buff.append(" FROM ");
		buff.append(GROUP_TABLE + " " + GROUP_TABLE_ALIAS);
		buff.append(", ");
		buff.append(MEMBER_TABLE + " " + MEMBER_TABLE_ALIAS);
		buff.append(" WHERE ");
		buff.append(prependGroupTableAlias(GROUP_ID_COLUMN));
		buff.append(" = ");
		buff.append(prependMemberTableAlias(MEMBER_GROUP_ID_COLUMN));
		buff.append(" AND ");
		buff.append(prependMemberTableAlias(MEMBER_MEMBER_KEY_COLUMN));
		buff.append(" = ? AND ");
		buff.append(prependGroupTableAlias(GROUP_TYPE_COLUMN));
		buff.append(" = ? ");

		findContainingGroupsSql = buff.toString();
	}
	return findContainingGroupsSql;
}
/**
 * @return java.lang.String
 */
private static java.lang.String getFindGroupsByCreatorSql()
{	
	if ( findGroupsByCreatorSql == null )
	{
		StringBuffer buff = new StringBuffer(200);
		buff.append("SELECT ");
		buff.append(getAllGroupColumns());
		buff.append(" FROM ");
		buff.append(GROUP_TABLE);
		buff.append(" WHERE ");
		buff.append(GROUP_CREATOR_COLUMN);
		buff.append(" = ? ");
		
		findGroupsByCreatorSql = buff.toString();
	}	
	return findGroupsByCreatorSql;
}
/**
 * @return java.lang.String
 */
private static java.lang.String getFindGroupSql()
{
	
	if ( findGroupSql == null )
	{
		StringBuffer buff = new StringBuffer(200);
		buff.append("SELECT ");
		buff.append(getAllGroupColumns());
		buff.append(" FROM ");
		buff.append(GROUP_TABLE);
		buff.append(" WHERE ");
		buff.append(GROUP_ID_COLUMN);
		buff.append(" = ? ");
		
		findGroupSql = buff.toString();
	}	
	return findGroupSql;
}
/**
 * @return java.lang.String
 */
private static java.lang.String getFindMemberGroupSql() 
{
	if ( findMemberGroupSql == null )
	{
		StringBuffer buff = new StringBuffer(getFindMemberGroupsSql());
		buff.append("AND ");
		buff.append(GROUP_TABLE_ALIAS);
		buff.append(".");
		buff.append(GROUP_NAME_COLUMN);
		buff.append(" = ?");
		findMemberGroupSql = buff.toString(); 
	}
	return findMemberGroupSql;
}
/**
 * @return java.lang.String
 */
private static java.lang.String getFindMemberGroupsSql() 
{
	if (findMemberGroupsSql == null)
	{
		StringBuffer buff = new StringBuffer(500);
		buff.append("SELECT ");
		buff.append(getAllGroupColumnsWithTableAlias());
		buff.append(" FROM ");
		buff.append(GROUP_TABLE + " " + GROUP_TABLE_ALIAS);
		buff.append(", ");
		buff.append(MEMBER_TABLE + " " + MEMBER_TABLE_ALIAS);
		buff.append(" WHERE ");
		buff.append(prependGroupTableAlias(GROUP_ID_COLUMN));
		buff.append(" = ");
		buff.append(prependMemberTableAlias(MEMBER_MEMBER_KEY_COLUMN));
		buff.append(" AND ");
		buff.append(prependMemberTableAlias(MEMBER_IS_GROUP_COLUMN));
		buff.append(" = '");
		buff.append(MEMBER_IS_GROUP);
		buff.append("' AND ");
		buff.append(prependMemberTableAlias(MEMBER_GROUP_ID_COLUMN));
		buff.append(" = ? ");
		
		findMemberGroupsSql = buff.toString();
	}
	return findMemberGroupsSql;
}
/**
 * @return java.lang.String
 */
private static java.lang.String getInsertGroupSql()
{
	if ( insertGroupSql == null )
	{
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
/**
 * @return java.lang.String
 */
private static java.lang.String getInsertMemberSql()
{
	if ( insertMemberSql == null )
	{
		StringBuffer buff = new StringBuffer(200);
		buff.append("INSERT INTO ");
		buff.append(MEMBER_TABLE);
		buff.append(" (");
		buff.append(getAllMemberColumns());
		buff.append(") VALUES (?, ?, ? )");
		
		insertMemberSql = buff.toString();
	}	
	return insertMemberSql;
}
/**
 * @return java.lang.String
 * @exception java.lang.Exception 
 */
private String getNextKey() throws java.lang.Exception 
{
	return SequenceGenerator.instance().getNext(GROUP_TABLE);
}
/**
 * @return java.lang.String
 */
private static java.lang.String getUpdateGroupSql()
{
	if ( updateGroupSql == null )
	{
		StringBuffer buff = new StringBuffer(200);
		buff.append("UPDATE ");
		buff.append(GROUP_TABLE);
		buff.append(" SET ");
		buff.append(GROUP_CREATOR_COLUMN);
		buff.append(" = ?, ");
		buff.append(GROUP_TYPE_COLUMN);
		buff.append(" = ?, ");
		buff.append(GROUP_NAME_COLUMN);
		buff.append(" = ?, ");
		buff.append(GROUP_DESCRIPTION_COLUMN);
		buff.append(" = ? WHERE ");
		buff.append(GROUP_ID_COLUMN);
		buff.append(" = ? ");
		
		updateGroupSql = buff.toString();
	}	
	return updateGroupSql;
}
/**
 * Find and return an instance of the group.
 * @return org.jasig.portal.groups.IEntityGroup
 * @param key java.lang.Object
 */
private IEntityGroup instanceFromResultSet(java.sql.ResultSet rs) 
throws  SQLException, 
		GroupsException
{
	IEntityGroup eg = null;

	String key = rs.getString(1);
	String creatorID = rs.getString(2);
	Integer entityTypeID = new Integer(rs.getInt(3));
	Class entityType = EntityTypes.getEntityType(entityTypeID);
	String groupName = rs.getString(4);
	String description = rs.getString(5);

	if ( key != null )
		{ eg = newInstance(key, entityType, creatorID, groupName, description); }
		
	return eg;
}
/**
 *
 */
protected static void logNoTransactionWarning()
{
	String msg = "You are running the portal on a database that does not support transactions.  " +
				 "This is not a supported production environment for uPortal.  " + 
				 "Sooner or later, your database will become corrupt.";
	LogService.instance().log(LogService.WARN, msg);
}
/**
 * @return org.jasig.portal.groups.IEntityGroup
 */
public IEntityGroup newInstance(Class type) throws GroupsException
{
	try
		{ return new EntityGroupImpl(getNextKey(), type); }
	catch ( Exception ex )
		{ throw new GroupsException("Could not create new group: " + ex.getMessage()); }
}
/**
 * @return org.jasig.portal.groups.IEntityGroup
 */
private IEntityGroup newInstance
	(String newKey, 
	Class newType, 
	String newCreatorID, 
	String newName, 
	String newDescription)
	throws GroupsException
{
	EntityGroupImpl egi = new EntityGroupImpl(newKey, newType);
	egi.setCreatorID(newCreatorID);
	egi.primSetName(newName);
	egi.setDescription(newDescription);
	return egi;
}
/**
 * @return java.lang.String
 */
private static java.lang.String prependGroupTableAlias(String column) 
{
	return GROUP_TABLE_ALIAS + "." + column;
}
/**
 * @return java.lang.String
 */
private static java.lang.String prependMemberTableAlias(String column) 
{
	return MEMBER_TABLE_ALIAS + "." + column;
}
/**
 * Insert the entity into the database.
 * @param group org.jasig.portal.groups.IEntityGroup
 */
private void primAdd(IEntityGroup group) throws SQLException, GroupsException
{
	java.sql.Connection conn = null;
	
	try
	{
		
		conn = RdbmServices.getConnection();
		java.sql.PreparedStatement ps = conn.prepareStatement(getInsertGroupSql());
		Integer typeID = EntityTypes.getEntityTypeID(group.getEntityType());
		ps.setString(1, group.getKey());
		ps.setString(2, group.getCreatorID());
		ps.setInt   (3, typeID.intValue());
		ps.setString(4, group.getName());
		ps.setString(5, group.getDescription());

		int rc = ps.executeUpdate();
		
		if ( rc != 1 )
		{
			String errString = "Problem adding " + group;
			LogService.log (LogService.ERROR, errString);
			throw new GroupsException(errString);
		}
	}
	catch (java.sql.SQLException sqle)
	{
		LogService.log (LogService.ERROR, sqle);
		throw sqle;
	}
	finally
	{
		RdbmServices.releaseConnection(conn); 
	}
}
/**
 * Delete this entity from the database after first deleting
 * its memberships.
 * Exception java.sql.SQLException - if we catch a SQLException,
 * we rollback and re-throw it.  
 * @param group org.jasig.portal.groups.IEntityGroup
 */
private void primDelete(IEntityGroup group) throws SQLException
{
	java.sql.Connection conn = null;
	String groupID = group.getKey();
	String deleteGroupSql = getDeleteGroupSql(group);
	String deleteMembershipSql = getDeleteMembersInGroupSql(group);
	
	try
	{
		conn = RdbmServices.getConnection();
		Statement stmnt = conn.createStatement();
		setAutoCommit(conn, false);
		
		stmnt.executeUpdate(deleteMembershipSql);
		stmnt.executeUpdate(deleteGroupSql);
		
		commit(conn);

	}
	catch (SQLException sqle)
	{ 
		rollback(conn); 
		throw sqle;
	}
	finally
	{
		setAutoCommit(conn, true);
		RdbmServices.releaseConnection(conn); 
	}
}
/**
 * Update the entity in the database.
 * @param group org.jasig.portal.groups.IEntityGroup
 */
private void primUpdate(IEntityGroup group) throws SQLException, GroupsException
{
	java.sql.Connection conn = null;
	
	try
	{
		conn = RdbmServices.getConnection();
		java.sql.PreparedStatement ps = conn.prepareStatement(getUpdateGroupSql());
		
		Integer typeID = EntityTypes.getEntityTypeID(group.getEntityType());

		ps.setString(1, group.getCreatorID());
		ps.setInt(2, typeID.intValue());
		ps.setString(3, group.getName());
		ps.setString(4, group.getDescription());
		ps.setString(5, group.getKey());	

		int rc = ps.executeUpdate();
		
		if ( rc != 1 )
		{
			String errString = "Problem updating " + group;
			LogService.log (LogService.ERROR, errString);
			throw new GroupsException(errString);
		}
	}
	catch (java.sql.SQLException sqle)
	{
		LogService.log (LogService.ERROR, sqle);
		throw sqle;
	}
	finally
	{
		RdbmServices.releaseConnection(conn); 
	}
}
/**
 * Insert and delete group membership rows inside a transaction.  
 * @param group org.jasig.portal.groups.EntityGroupImpl
 */
private void primUpdateMembers(EntityGroupImpl egi) throws java.sql.SQLException
{
	String groupKey = egi.getKey();
	String memberKey = null;
	String isGroup = null;
	Connection conn = null;
		
	try
	{
		conn = RdbmServices.getConnection();
		setAutoCommit(conn, false);

		if ( egi.hasDeletes() )
		{
			PreparedStatement psDelete = conn.prepareStatement(getDeleteMemberSql());
			Iterator deletes = egi.getRemovedMembers().values().iterator();
			while ( deletes.hasNext() )
			{
				GroupMemberImpl removedGM = (GroupMemberImpl) deletes.next();
				memberKey = removedGM.getKey();
				isGroup = removedGM.isGroup() ? MEMBER_IS_GROUP : MEMBER_IS_ENTITY;
				psDelete.setString(1, groupKey);
				psDelete.setString(2, memberKey);
				psDelete.setString(3, isGroup);
				psDelete.executeUpdate();
			}
		}

		if ( egi.hasAdds() )
		{
			PreparedStatement psAdd = conn.prepareStatement(getInsertMemberSql());
			Iterator adds = egi.getAddedMembers().values().iterator();
			while ( adds.hasNext() )
			{
				GroupMemberImpl addedGM = (GroupMemberImpl) adds.next();
				memberKey = addedGM.getKey();
				isGroup = addedGM.isGroup() ? MEMBER_IS_GROUP : MEMBER_IS_ENTITY;
				psAdd.setString(1, groupKey);
				psAdd.setString(2, memberKey);
				psAdd.setString(3, isGroup);
				psAdd.executeUpdate();
			}
		}

		commit(conn);

	}
	catch (SQLException sqle)
	{
		
		rollback(conn);

		LogService.log (LogService.ERROR, sqle);
		throw sqle;
	}

	finally
	{
		setAutoCommit(conn, true);
		RdbmServices.releaseConnection(conn); 
	}
}
/**
 * @param conn java.sql.Connection
 * @exception java.sql.SQLException
 */
protected static void rollback(Connection conn) throws java.sql.SQLException 
{
	SqlTransaction.rollback(conn);
}
/**
 * @param conn java.sql.Connection
 * @param newValue boolean
 * @exception java.sql.SQLException The exception description.
 */
protected static void setAutoCommit(Connection conn, boolean newValue) throws java.sql.SQLException 
{
	SqlTransaction.setAutoCommit(conn, newValue);
}
/**
 * @return org.jasig.portal.groups.EntityGroupStoreRDBM
 */
public static synchronized EntityGroupStoreRDBM singleton()
throws GroupsException
{
	if ( singleton == null )
		{ singleton = new EntityGroupStoreRDBM(); }
	return singleton;
}
/**
 * Commit this entity to the backing store.
 * @param group org.jasig.portal.groups.IEntityGroup
 */
public void update(IEntityGroup group) throws GroupsException 
{
	try
	{
		if ( existsInDatabase(group) )
			{ primUpdate(group); }
		else
			{ primAdd(group); }
	}
	catch (Exception ex)
		{ throw new GroupsException("Problem updating " + this + ex); }
}
/**
 * Insert and delete group membership rows inside a transaction.  
 * @param group org.jasig.portal.groups.IEntityGroup
 */
public void updateMembers(IEntityGroup eg) throws GroupsException
{
	EntityGroupImpl egi = (EntityGroupImpl) eg;
	if ( egi.isDirty() )
	{
		try
			{ primUpdateMembers(egi); }
		catch ( SQLException sqle )
			{ throw new GroupsException("Problem updating memberships for " + egi + " " + sqle.getMessage()); }
	}
}
}
