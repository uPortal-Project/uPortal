package org.jasig.portal.security.provider;

/**
 * Copyright ©  2001, 2002 The JA-SIG Collaborative.  All rights reserved.
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jasig.portal.AuthorizationException;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Reference implementation of IPermissionStore.  Performs CRUD operations
 * on the UP_Permission table.
 * @author Dan Ellentuck (de3@columbia.edu)
 * @version $Revision$
 */
public class RDBMPermissionImpl implements IPermissionStore {

    private static final Log log = LogFactory.getLog(RDBMPermissionImpl.class);
    
    private static RDBMPermissionImpl singleton;

    // Prior to jdk 1.4, java.sql.Timestamp.getTime() truncated milliseconds.
    private static boolean timestampHasMillis;

    // sql Strings:
    private static String PERMISSION_TABLE = "UP_PERMISSION";
    private static String OWNER_COLUMN =     "OWNER";
    private static String PRINCIPAL_TYPE_COLUMN = "PRINCIPAL_TYPE";
    private static String PRINCIPAL_KEY_COLUMN = "PRINCIPAL_KEY";
    private static String ACTIVITY_COLUMN =  "ACTIVITY";
    private static String TARGET_COLUMN =    "TARGET";
    private static String TYPE_COLUMN =      "PERMISSION_TYPE";
    private static String EFFECTIVE_COLUMN = "EFFECTIVE";
    private static String EXPIRES_COLUMN =   "EXPIRES";
    private static String allPermissionColumnsSql;
    private static String deletePermissionSql;
    private static String findPermissionSql;
    private static String insertPermissionSql;
    private static String selectPermissionSql;
    private static String updatePermissionSql;
    
    private static String PRINCIPAL_SEPARATOR = ".";
/**
 * RDBMReferencePermission constructor comment.
 */
public RDBMPermissionImpl() {
    super();
    Date testDate = new Date();
    Timestamp testTimestamp = new Timestamp(testDate.getTime());
    timestampHasMillis = (testDate.getTime() == testTimestamp.getTime());
}
/**
 * Add the IPermissions to the store.
 * @param perms org.jasig.portal.security.IPermission[]
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
public void add(IPermission[] perms) throws AuthorizationException
{
    if ( perms.length > 0 )
    {
        try
        {
            primAdd(perms);
        }
        catch (Exception ex)
        {
            log.error( ex);
            throw new AuthorizationException(ex.getMessage());
        }
    }
}
/**
 * Add the IPermission to the store.
 * @param perm org.jasig.portal.security.IPermission
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
public void add(IPermission perm) throws AuthorizationException

{
    Connection conn = null;
    int rc = 0;

    try
    {
        conn = RDBMServices.getConnection();
        String sQuery = getInsertPermissionSql();
        RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn, sQuery);
        try
        {
            primAdd(perm, ps);
            log.debug("RDBMPermissionImpl.add(): " + ps);
            rc = ps.executeUpdate();
            if ( rc != 1 )
                { throw new AuthorizationException("Problem adding Permission " + perm); }
        }
        finally
            { ps.close(); }
    }
    catch (Exception ex)
    {
        log.error( ex.getMessage());
        throw new AuthorizationException("Problem adding Permission " + perm);
    }
    finally
        { RDBMServices.releaseConnection(conn); }
}
/**
 * Delete the IPermissions from the store.
 * @param perms org.jasig.portal.security.IPermission[]
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
public void delete(IPermission[] perms) throws AuthorizationException
{
    if ( perms.length > 0 )
    {
        try
        {
            primDelete(perms);
        }
        catch (Exception ex)
        {
            log.error( ex);
            throw new AuthorizationException(ex.getMessage());
        }
    }
}
/**
 * Delete a single IPermission from the store.
 * @param perm org.jasig.portal.security.IPermission
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
public void delete(IPermission perm) throws AuthorizationException

{
    Connection conn = null;
    try
    {
        conn = RDBMServices.getConnection();
        String sQuery = getDeletePermissionSql();
        RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn, sQuery);
        try
            { primDelete(perm, ps); }
        finally
            { ps.close(); }
    }
    catch (Exception ex)
    {
        log.error( ex.getMessage());
        throw new AuthorizationException("Problem deleting Permission " + perm);
    }
    finally
        { RDBMServices.releaseConnection(conn); }
}
/**
 * Answer if this entity exists in the database.
 * @return boolean
 * @param perm org.jasig.portal.security.IPermission
 * @exception java.sql.SQLException
 */
public boolean existsInDatabase(IPermission perm) throws AuthorizationException, SQLException
{
    Connection conn = null;
    try
    {
        conn = RDBMServices.getConnection();
        String sQuery = getFindPermissionSql();
        RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn, sQuery);
        try
        {
            ps.setString(1, perm.getOwner());
            ps.setInt   (2, getPrincipalType(perm));
            ps.setString(3, getPrincipalKey(perm));
            ps.setString(4, perm.getActivity());
            ps.setString(5, perm.getTarget());
            log.debug("RDBMPermissionImpl.existsInDatabase(): " + ps);
            ResultSet rs = ps.executeQuery();
            try {
              return ( rs.next() );
            } finally {
              rs.close();
            }
        }
        finally
            { ps.close(); }
    }
    catch (Exception ex)
    {
        log.error( ex);
        throw new AuthorizationException("RDBMPermissionImpl.existsInDatabase(): " + ex);
    }
    finally
    {
        RDBMServices.releaseConnection(conn);
    }
}
/**
 * @return java.lang.String
 */
private static String getAllPermissionColumnsSql()
{
    if ( allPermissionColumnsSql == null )
    {
        StringBuffer sqlBuff = new StringBuffer(200);
        sqlBuff.append(OWNER_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(PRINCIPAL_TYPE_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(PRINCIPAL_KEY_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(ACTIVITY_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(TARGET_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(TYPE_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(EFFECTIVE_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(EXPIRES_COLUMN);
        allPermissionColumnsSql = sqlBuff.toString();
    }
    return allPermissionColumnsSql;
}
/**
 * @return java.lang.String
 */
private static String getDeletePermissionSql()
{
    if ( deletePermissionSql == null )
    {
        StringBuffer sqlBuff = new StringBuffer(200);
        sqlBuff.append("DELETE FROM ");
        sqlBuff.append(PERMISSION_TABLE);
        sqlBuff.append(" WHERE ");
        sqlBuff.append(OWNER_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(PRINCIPAL_TYPE_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(PRINCIPAL_KEY_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(ACTIVITY_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(TARGET_COLUMN);
        sqlBuff.append(" = ? ");
        deletePermissionSql = sqlBuff.toString();
    }
    return deletePermissionSql;
}
/**
 * Insert the method's description here.
 * Creation date: (11/6/01 5:19:57 PM)
 * @return java.lang.String
 */
private static java.lang.String getFindPermissionSql()
{
    if ( findPermissionSql == null )
    {
        StringBuffer sqlBuff = new StringBuffer(getSelectPermissionSql());
        sqlBuff.append("WHERE ");
        sqlBuff.append(OWNER_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(PRINCIPAL_TYPE_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(PRINCIPAL_KEY_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(ACTIVITY_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(TARGET_COLUMN);
        sqlBuff.append(" = ? ");
        sqlBuff.append(TYPE_COLUMN);
        sqlBuff.append(" = ? ");
        findPermissionSql = sqlBuff.toString();
    }
    return findPermissionSql;
}
/**
 * @return java.lang.String
 */
private static String getInsertPermissionSql()
{
    if ( insertPermissionSql == null )
    {
        StringBuffer sqlBuff = new StringBuffer(200);
        sqlBuff.append("INSERT INTO ");
        sqlBuff.append(PERMISSION_TABLE);
        sqlBuff.append(" (");
        sqlBuff.append(getAllPermissionColumnsSql());
        sqlBuff.append(") VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        insertPermissionSql = sqlBuff.toString();
    }
    return insertPermissionSql;
}
/**
 * Returns the principal key portion of the IPermission principal.
 * @return String
 * @param principalString
 */
private String getPrincipalKey(String principalString)
{
    return principalString.substring(principalString.indexOf(PRINCIPAL_SEPARATOR)+1);
}
/**
 * Returns the principal key portion of the IPermission principal.
 * @return String
 * @param perm org.jasig.portal.security.IPermission
 * @exception AuthorizationException
 */
private String getPrincipalKey(IPermission perm) throws AuthorizationException
{
    return getPrincipalKey(perm.getPrincipal());
}
/**
 * Returns the principal type portion of the principal.
 * @return int
 * @param principalString
 */
private int getPrincipalType(String principalString)
{
    return Integer.parseInt(principalString.substring(0,principalString.indexOf(PRINCIPAL_SEPARATOR)));
}
/**
 * Returns the principal type portion of the IPermission principal.
 * @return int
 * @param perm org.jasig.portal.security.IPermission
 * @exception AuthorizationException
 */
private int getPrincipalType(IPermission perm) throws AuthorizationException
{
    return getPrincipalType(perm.getPrincipal());
}
/**
 * @return java.lang.String
 */
private static String getSelectPermissionSql()
{
    if ( selectPermissionSql == null )
    {
        StringBuffer sqlBuff = new StringBuffer(200);
        sqlBuff.append("SELECT ");
        sqlBuff.append(getAllPermissionColumnsSql());
        sqlBuff.append(" FROM ");
        sqlBuff.append(PERMISSION_TABLE);
        sqlBuff.append(" ");
        selectPermissionSql = sqlBuff.toString();
    }
    return selectPermissionSql;
}
/**
 * @return java.lang.String
 */
private static String getUpdatePermissionSql()
{
    if ( updatePermissionSql == null )
    {
        StringBuffer sqlBuff = new StringBuffer(300);
        sqlBuff.append("UPDATE ");
        sqlBuff.append(PERMISSION_TABLE);
        sqlBuff.append(" SET ");
        sqlBuff.append(TYPE_COLUMN);
        sqlBuff.append(" = ?, ");
        sqlBuff.append(EFFECTIVE_COLUMN);
        sqlBuff.append(" = ?, ");
        sqlBuff.append(EXPIRES_COLUMN);
        sqlBuff.append(" = ? WHERE ");
        sqlBuff.append(OWNER_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(PRINCIPAL_TYPE_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(PRINCIPAL_KEY_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(ACTIVITY_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(TARGET_COLUMN);
        sqlBuff.append(" = ? ");
        updatePermissionSql = sqlBuff.toString();
    }
    return updatePermissionSql;
}
/**
 * @return org.jasig.portal.security.IPermission
 * @param rs java.sql.ResultSet
 */
private IPermission instanceFromResultSet(ResultSet rs) throws  SQLException
{
    Timestamp ts = null;

    IPermission perm = newInstance(rs.getString(OWNER_COLUMN));
    perm.setPrincipal(rs.getString(PRINCIPAL_TYPE_COLUMN) + "." + rs.getString(PRINCIPAL_KEY_COLUMN) );
    perm.setActivity(rs.getString(ACTIVITY_COLUMN));
    perm.setTarget(rs.getString(TARGET_COLUMN));
    perm.setType(rs.getString(TYPE_COLUMN));

    ts = rs.getTimestamp(EFFECTIVE_COLUMN);
    if ( ts != null )
        { perm.setEffective(new Date(getTimestampMillis(ts))); }

    ts = rs.getTimestamp(EXPIRES_COLUMN);
    if ( ts != null )
        { perm.setExpires(new Date(getTimestampMillis(ts))); }

    return perm;
}
/**
 * Factory method for IPermissions
 */
public IPermission newInstance(String owner)
{
    return new PermissionImpl(owner);
}
/**
 * Add the IPermissions to the store.
 * @param perms org.jasig.portal.security.IPermission[]
 * @exception Exception
 */
private void primAdd(IPermission[] perms) throws Exception
{
    Connection conn = null;
    int rc = 0;

    try
    {
        conn = RDBMServices.getConnection();
        String sQuery = getInsertPermissionSql();
        RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn, sQuery);
        try
        {
            RDBMServices.setAutoCommit(conn, false);

            for ( int i=0; i<perms.length; i++ )
            {
                primAdd(perms[i], ps);
                log.debug("RDBMPermissionImpl.primAdd(): " + ps);
                rc = ps.executeUpdate();

                if ( rc != 1 )
                {
                    String errMsg = "Problem adding " + perms[i] + " RC: " + rc;
                    log.error( errMsg);
                    RDBMServices.rollback(conn);
                    throw new AuthorizationException(errMsg);
                }
            }
        }
        finally
            { ps.close(); }

        RDBMServices.commit(conn);

    }
    catch (Exception ex)
    {
        log.error( ex);
        RDBMServices.rollback(conn);
        throw ex;
    }
    finally
    {
        try 
            { RDBMServices.setAutoCommit(conn, true); }
        finally
            { RDBMServices.releaseConnection(conn); }
    }
}
/**
 * Set the params on the PreparedStatement and execute the insert.
 * @param perm org.jasig.portal.security.IPermission
 * @param ps java.sql.PreparedStatement - the PreparedStatement for inserting a Permission row.
 * @exception Exception
 */
private void primAdd(IPermission perm, RDBMServices.PreparedStatement ps) throws Exception
{
    java.sql.Timestamp ts = null;

    // NON-NULL COLUMNS:
    ps.clearParameters();
    ps.setString(1, perm.getOwner());
    ps.setInt(   2, getPrincipalType(perm));
    ps.setString(3, getPrincipalKey(perm));
    ps.setString(4, perm.getActivity());
    ps.setString(5, perm.getTarget());
    // TYPE:
    if ( perm.getType() == null )
        { ps.setNull(6, Types.VARCHAR); }
    else
        { ps.setString(6, perm.getType()); }
    // EFFECTIVE:
    if ( perm.getEffective() == null )
        { ps.setNull(7, Types.TIMESTAMP); }
    else
        {
            ts = new java.sql.Timestamp(perm.getEffective().getTime());
            ps.setTimestamp(7, ts);
        }
    // EXPIRES:
    if ( perm.getExpires() == null )
        { ps.setNull(8, Types.TIMESTAMP); }
    else
        {
            ts = new java.sql.Timestamp(perm.getExpires().getTime());
            ps.setTimestamp(8, ts);
        }
}
/**
 * Delete the IPermissions from the store.
 * @param perms org.jasig.portal.security.IPermission[]
 * @exception Exception
 */
private void primDelete(IPermission[] perms) throws Exception
{
    Connection conn = null;

    try
    {
        conn = RDBMServices.getConnection();
        String sQuery = getDeletePermissionSql();
        RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn, sQuery);
        try
        {
            RDBMServices.setAutoCommit(conn, false);

            for ( int i=0; i<perms.length; i++ )
                { primDelete(perms[i], ps); }
        }
        finally
            { ps.close(); }

        RDBMServices.commit(conn);

    }
    catch (Exception ex)
    {
        log.error( ex);
        RDBMServices.rollback(conn);
        throw ex;
    }
    finally
    {
        try 
            { RDBMServices.setAutoCommit(conn, true); }
        finally 
            { RDBMServices.releaseConnection(conn); }
    }
}
/**
 * Set the params on the PreparedStatement and execute the delete.
 * @param perm org.jasig.portal.security.IPermission
 * @param ps java.sql.PreparedStatement - the PreparedStatement for deleting a Permission row.
 * @return int - the return code from the PreparedStatement
 * @exception Exception
 */
private int primDelete(IPermission perm, RDBMServices.PreparedStatement ps) throws Exception
{
    ps.clearParameters();
    ps.setString(1, perm.getOwner());
    ps.setInt(   2, getPrincipalType(perm));
    ps.setString(3, getPrincipalKey(perm));
    ps.setString(4, perm.getActivity());
    ps.setString(5, perm.getTarget());
    log.debug("RDBMPermissionImpl.primDelete(): " + ps);

    return ps.executeUpdate();
}
/**
 * Update the IPermissions in the store.
 * @param perms org.jasig.portal.security.IPermission[]
 * @exception Exception
 */
private void primUpdate(IPermission[] perms) throws Exception
{
    Connection conn = null;

    try
    {
        conn = RDBMServices.getConnection();
        String sQuery = getUpdatePermissionSql();
        RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn, sQuery);
        try
        {
            RDBMServices.setAutoCommit(conn, false);

            for ( int i=0; i<perms.length; i++ )
                { primUpdate(perms[i], ps); }
        }
        finally
            { ps.close(); }

        RDBMServices.commit(conn);

    }
    catch (Exception ex)
    {
        log.error( ex);
        RDBMServices.rollback(conn);
        throw ex;
    }
    finally
    {
        try 
            { RDBMServices.setAutoCommit(conn, true); }
        finally 
            { RDBMServices.releaseConnection(conn); }
    }
}
/**
 * Set the params on the PreparedStatement and execute the update.
 * @param perm org.jasig.portal.security.IPermission
 * @param ps java.sql.PreparedStatement - the PreparedStatement for updating a Permission row.
 * @return int - the return code from the PreparedStatement
 * @exception Exception
 */
private int primUpdate(IPermission perm, RDBMServices.PreparedStatement ps) throws Exception
{
    java.sql.Timestamp ts = null;

    // UPDATE COLUMNS:

    ps.clearParameters();
    // TYPE:
    if ( perm.getType() == null )
        { ps.setNull(1, Types.VARCHAR); }
    else
        { ps.setString(1, perm.getType()); }
    // EFFECTIVE:
    if ( perm.getEffective() == null )
        { ps.setNull(2, Types.TIMESTAMP); }
    else
    {
        ts = new java.sql.Timestamp(perm.getEffective().getTime());
        ps.setTimestamp(2, ts);
    }
    // EXPIRES:
    if ( perm.getExpires() == null )
        { ps.setNull(3, Types.TIMESTAMP); }
    else
    {
        ts = new java.sql.Timestamp(perm.getExpires().getTime());
        ps.setTimestamp(3, ts);
    }
    // WHERE COLUMNS:
    ps.setString(4, perm.getOwner());
    ps.setInt(   5, getPrincipalType(perm));
    ps.setString(6, getPrincipalKey(perm));
    ps.setString(7, perm.getActivity());
    ps.setString(8, perm.getTarget());
    log.debug("RDBMPermissionImpl.primUpdate(): " + ps);


    return ps.executeUpdate();
}
/**
 * Select the Permissions from the store.
 * @param owner String - the Permission owner
 * @param principal String - the Permission principal
 * @param activity String - the Permission activity
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
public IPermission[] select
    (String owner,
    String principal,
    String activity,
    String target,
    String type)
throws AuthorizationException
{
    Connection conn = null;
    Statement stmnt = null;
    ResultSet rs = null;
    List perms = new ArrayList();

    StringBuffer sqlQuery = new StringBuffer(getSelectPermissionSql());
    sqlQuery.append(" WHERE ");

    if ( owner != null )
    {
        sqlQuery.append(OWNER_COLUMN);
        sqlQuery.append(" = '");
        sqlQuery.append(RDBMServices.sqlEscape(owner));
        sqlQuery.append("' ");
    }
    else
    {
        sqlQuery.append("1 = 1 ");
    }

    if ( principal != null )
        {
            sqlQuery.append("AND ");
            sqlQuery.append(PRINCIPAL_TYPE_COLUMN);
            sqlQuery.append(" = ");
            sqlQuery.append(getPrincipalType(principal));
            sqlQuery.append(" ");
            sqlQuery.append("AND ");
            sqlQuery.append(PRINCIPAL_KEY_COLUMN);
            sqlQuery.append(" = '");
            sqlQuery.append(getPrincipalKey(principal));
            sqlQuery.append("' ");
        }

    if ( activity != null )
        {
            sqlQuery.append("AND ");
            sqlQuery.append(ACTIVITY_COLUMN);
            sqlQuery.append(" = '");
            sqlQuery.append(RDBMServices.sqlEscape(activity));
            sqlQuery.append("' ");
        }

    if ( target != null )
        {
            sqlQuery.append("AND ");
            sqlQuery.append(TARGET_COLUMN);
            sqlQuery.append(" = '");
            sqlQuery.append(RDBMServices.sqlEscape(target));
            sqlQuery.append("' ");
        }

    if ( type != null )
        {
            sqlQuery.append("AND ");
            sqlQuery.append(TYPE_COLUMN);
            sqlQuery.append(" = '");
            sqlQuery.append(type);
            sqlQuery.append("' ");
        }

    log.debug("RDBMPermissionImpl.select(): " + sqlQuery.toString());

    try
    {
        conn = RDBMServices.getConnection();
        stmnt = conn.createStatement();
        try
        {
            rs = stmnt.executeQuery(sqlQuery.toString());
            try
            {
                while ( rs.next() )
                    { perms.add(instanceFromResultSet(rs)); }
            }
            finally
                { rs.close(); }
        }
        finally
            { stmnt.close(); }
    }
    catch (SQLException sqle)
    {
        log.error( sqle);
        throw new AuthorizationException("Problem retrieving Permissions " + sqle.getMessage());
    }
    finally
        { RDBMServices.releaseConnection(conn); }

    return ((IPermission[])perms.toArray(new IPermission[perms.size()]));
}
/**
 * @return org.jasig.portal.security.provider.RDBMPermissionImpl
 */
public static synchronized RDBMPermissionImpl singleton()
{
    if ( singleton == null )
        { singleton = new RDBMPermissionImpl(); }
    return singleton;
}
/**
 * Update the IPermissions in the store.
 * @param perms org.jasig.portal.security.IPermission[]
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
public void update(IPermission[] perms) throws AuthorizationException
{
    if ( perms.length > 0 )
    {
        try
        {
            primUpdate(perms);
        }
        catch (Exception ex)
        {
            log.error( ex);
            throw new AuthorizationException(ex.getMessage());
        }
    }
}
/**
 * Update a single IPermission in the store.
 * @param perm org.jasig.portal.security.IPermission
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
public void update(IPermission perm) throws AuthorizationException

{
    Connection conn = null;
    try
    {
        conn = RDBMServices.getConnection();
        String sQuery = getUpdatePermissionSql();
        log.debug("RDBMPermissionImpl.update(): " + sQuery);
        RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn, sQuery);
        try
            { primUpdate(perm, ps); }
        finally
            { ps.close(); }
    }
    catch (Exception ex)
    {
        log.error( ex.getMessage());
        throw new AuthorizationException("Problem updating Permission " + perm);
    }
    finally
        { RDBMServices.releaseConnection(conn); }
}
/**
 * @return long
 */
private static long getTimestampMillis(Timestamp ts)
{
    if ( timestampHasMillis )
        { return ts.getTime(); }
    else
        { return (ts.getTime() + ts.getNanos() / 1000000); }
}
}
