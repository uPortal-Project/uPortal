package org.jasig.portal.groups;

import java.sql.*;
import java.util.*;
import org.jasig.portal.*;
import org.jasig.portal.services.LogService;

/**
 * This class provides access to the entity types used by <code>IEntityGroups</code>.  
 * Type, to a group, is like <code>Class</code> to an <code>Array</code>.  It specifies 
 * what kind of entities the <code>IEntityGroup</code> contains.
 * <p>
 * Each type is associated with an <code>Integer</code> which is used to store the type in
 * the portal data store.  This class translates between the <code>Integer</code> and 
 * <code>Class</code> values.  
 *
 * @author Dan Ellentuck
 * @version 1.0, 12/5/01  
 */
public class EntityTypes {

	private static EntityTypes singleton;

	// Caches for entityType classes.
	private Map entityTypesByID;
	private Map entityIDsByType;
	
	// Constant strings for ENTITY TYPE table:
	private static String ENTITY_TYPE_TABLE = "UP_GROUP_ENTITY_TYPE";
	private static String TYPE_ID_COLUMN = "ENTITY_TYPE_ID";
	private static String TYPE_NAME_COLUMN = "ENTITY_TYPE_NAME";

	// SQL strings for ENTITY TYPE crud:
	private static String selectEntityTypesSql; 
	
/**
 * EntityTypes constructor comment.
 */
public EntityTypes()
{
	super();
	initialize();
}
/**
 * Interface to the entity types cache.
 * @return java.lang.Integer
 */
public Integer getEntityIDFromType(Class type) 
{
	return (Integer)getEntityIDsByType().get(type);
}
/**
 * @return java.util.Map
 */
private java.util.Map getEntityIDsByType() 
{
	if ( entityIDsByType == null )
		entityIDsByType = new HashMap(5);	
	return entityIDsByType;
}
/**
 * Interface to the entity types cache.
 * @return java.lang.Class
 */
public static Class getEntityType(Integer typeID) 
{
	return singleton().getEntityTypeFromID(typeID);
}
/**
 * Interface to the entity types cache.
 * @return java.lang.Class
 */
public Class getEntityTypeFromID(Integer typeID) 
{
	return (Class)getEntityTypesByID().get(typeID);
}
/**
 * Interface to the entity types cache.
 * @return java.lang.Class
 */
public static Integer getEntityTypeID(Class type) 
{
	return singleton().getEntityIDFromType(type);
}
/**
 * @return java.util.Map
 */
private java.util.Map getEntityTypesByID() 
{
	if ( entityTypesByID == null )
		entityTypesByID = new HashMap(5);	
	return entityTypesByID;
}
/**
 * @return java.lang.String
 */
private static java.lang.String getSelectEntityTypesSql()
{
	if ( selectEntityTypesSql == null )
	{
		StringBuffer buff = new StringBuffer(100);
		buff.append("SELECT ");
		buff.append(TYPE_ID_COLUMN);
		buff.append(", ");
		buff.append(TYPE_NAME_COLUMN);
		buff.append(" FROM ");
		buff.append(ENTITY_TYPE_TABLE);
		
		selectEntityTypesSql = buff.toString();
	}	
	return selectEntityTypesSql;
}
/**
 * Cache entityTypes.
 */
private void initialize()
{
	Connection conn = null;
	Integer typeID = null;
	Class entityType = null;

	try
	{
		conn = RdbmServices.getConnection();	
		Statement stmnt = conn.createStatement();
		ResultSet rs = stmnt.executeQuery(getSelectEntityTypesSql());
		while (rs.next())
		{ 
			typeID = new Integer(rs.getInt(1));
			entityType = Class.forName(rs.getString(2));
			getEntityIDsByType().put(entityType, typeID);
			getEntityTypesByID().put(typeID, entityType);
		}
	}
	catch (Exception ex)
		{ 
			LogService.log (LogService.ERROR, ex);
		}
	finally
		{ RdbmServices.releaseConnection(conn); }	
}
/**
 * @return org.jasig.portal.groups.EntityTypes
 */
public static synchronized EntityTypes singleton()
{
	if ( singleton == null )
		{ singleton = new EntityTypes(); }
	return singleton;
}
}
