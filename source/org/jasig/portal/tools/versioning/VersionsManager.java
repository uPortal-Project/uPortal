/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.versioning;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;

import org.jasig.portal.RDBMServices;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides access to and persistence of version information for pieces of 
 * code installed in the portal. Identification of pieces of code is by 
 * functional name. Version is represented by three integers. In most 
 * significant order these are Major, Minor, and Micro.
 * 
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $Revision$
 */
public class VersionsManager
{
    private static final Log log = LogFactory.getLog(VersionsManager.class);
    private static final Version[] VERSIONS_ARRAY_TYPE = new Version[]{};
    private static final VersionsManager instance = new VersionsManager();
    private static LinkedList versions = loadVersions();
    
    private VersionsManager()
    {
    }
    
    /**
     * Loads all version information from the back end database and returns it
     * as a LinkedList containing Version objects.
     *  
     * @return java.util.LinkedList
     */
    private static LinkedList loadVersions()
    {
        LinkedList list = new LinkedList();
        Connection con = null;
        try
        {
            con = RDBMServices.getConnection();
            Statement sql = con.createStatement();
            sql.execute(
                "SELECT FNAME, DESCRIPTION, MAJOR, MINOR, MICRO FROM UP_VERSIONS");
            ResultSet rs = sql.getResultSet();
            while(rs.next())
            {
                list.add(new Version( 
                rs.getString(1),
                rs.getString(2),
                rs.getInt(3),
                rs.getInt(4),
                rs.getInt(5)));
            }
        }
        catch (Exception e)
        {
            log.error( 
                "Unable to load Version information for uPortal objects.", e);
        }
        finally
        {
            RDBMServices.releaseConnection(con);
        }
        
        return list;
    }
    
    /**
     * Updates the cached version information from the database.
     *
     */
    private static synchronized void updateVersions()
    {
        versions = loadVersions();
    }

    /**
     * Returns the singleton instance of the VersionsManager.
     * 
     * @return VersionManager
     */
    public static final VersionsManager getInstance()
    {
        return instance;
    }
    
    /**
     * Returns an array of Versions representing all version information 
     * registered with the VersionsManager.
     * 
     * @return Version[]
     */
    public Version[] getVersions()
    {
        return (Version[]) versions.toArray(VERSIONS_ARRAY_TYPE);
    }
    
    /**
     * Returns the version for a specified functional name or null if no 
     * version information is available.
     * 
     * @param fname java.lang.String
     * @return Version
     */
    public Version getVersion(String fname)
    {
        
        for(Iterator i=versions.iterator(); i.hasNext();)
        {
            Version v = (Version) i.next();
            if (v.getFname().equals(fname))
                return v;
        }
        return null;
    }
    
    /**
     * Removes version information for the specified functional name. Returns
     * true if version information existed and was removed. 
     * 
     * @param fname java.lang.String
     * @return boolean
     *
     */
    public synchronized boolean removeVersion(String fname)
    {
        Version v = getVersion(fname);
        
        if ( v == null )
            return false;
        boolean changed = remove(v);
        
        updateVersions();
        return changed;
    }

    /**
     * Removes this version from the backend database. Returns true if version
     * information was indeed removed.
     * 
     * @param v
     * @return boolean
     */
    private boolean remove(Version v)
    {
        Connection con = null;
        try
        {
            con = RDBMServices.getConnection();
            PreparedStatement sql = con.prepareStatement(
                "DELETE FROM UP_VERSIONS " +
                "WHERE FNAME=? AND MAJOR=? AND MINOR=? AND MICRO=?");
            sql.setString(1, v.getFname());
            sql.setInt(2,v.getMajor());    
            sql.setInt(3,v.getMinor());    
            sql.setInt(4,v.getMicro());
            int modifiedCount = sql.executeUpdate();
            
            return modifiedCount >= 1;
        }
        catch (Exception e)
        {
            log.error( 
                "Unable to remove Version information for " + v.toString(), e);
        }
        finally
        {
            RDBMServices.releaseConnection(con);
        }
        return false;
    }
    
    /**
     * Updates the version information for the indicated functional name to
     * the passed in values only if:
     * 
     * a) a version already exists and the values in the database match those
     * obtained via getVersion(fname). A database update is performed in this
     * case.
     * 
     * b) a version does not already exist for this functional name. A database
     * insert is performed in this case.
     * 
     * Returns true if this call resulted in a database change. Use of primary 
     * keys in the database table is critical for (b) in the scenario where 
     * more than one portal is running against the same database to prevent two 
     * inserts to the table from succeeding and masking which portal 
     * successfully changed the version. 
     * 
     * @param fname
     * @param major
     * @param minor
     * @param micro
     * @return boolean
     */
    public synchronized boolean setVersion(
        String fname,
        String description,
        int major,
        int minor,
        int micro)
    {
        Version v = getVersion(fname);
        boolean changed = false;
        
        if (v == null)
            changed = insertVersion(fname, description, major, minor, micro);
        else    
            changed =
                updateVersion( v,
                    new Version(fname, description, major, minor, micro));
        updateVersions();
        return changed;
    }

    /**
     * Attempts to update version information in the database. Returns
     * true if the update was successful, false otherwise. If multiple portals
     * share the same database two portals may try this at the same time and
     * one should fail since the major, minor, and/or micro versions had in 
     * cache will not map to those now in the database.
     * 
     * @param old
     * @param next
     * @return boolean
     */
    private boolean updateVersion(Version old, Version next)
    {
        Connection con = null;
        try
        {
            con = RDBMServices.getConnection();
            PreparedStatement sql = con.prepareStatement(
                "UPDATE UP_VERSIONS SET MAJOR=?, MINOR=?, MICRO=?, DESCRIPTION=? " +
                "WHERE FNAME=? AND MAJOR=? AND MINOR=? AND MICRO=?");
            sql.setInt(1,next.getMajor());    
            sql.setInt(2,next.getMinor());    
            sql.setInt(3,next.getMicro());    
            sql.setString(4,next.getDescription());    
            sql.setString(5, old.getFname());
            sql.setInt(6,old.getMajor());    
            sql.setInt(7,old.getMinor());    
            sql.setInt(8,old.getMicro());
            int modifiedCount = sql.executeUpdate();
            
            if (modifiedCount > 1)
                log.error( 
                    "Warning: Multiple version entries detected in UP_VERSION"
                    + " table. Primary keys must be used in this table when "
                    + "coordinating version information from multiple portals "
                    + "running against the same database.");
            return modifiedCount >= 1;
        }
        catch (Exception e)
        {
            log.error( 
                "Unable to update version information for " + old.toString() +
                ". Update from external source assumed. Abandoning update.", e);
        }
        finally
        {
            RDBMServices.releaseConnection(con);
        }
        return false;
    }

    /**
     * Attempts to insert new version information into the database. Returns
     * true if the insert was successful, false otherwise. If multiple portals
     * share the same database two portals may try this at the same time and
     * one should fail provided primary keys have been specified for the table.
     * 
     * @param fname
     * @param major
     * @param minor
     * @param micro
     * @return boolean
     */
    private boolean insertVersion(
        String fname,
        String description,
        int major,
        int minor,
        int micro)
    {
        Connection con = null;
        Version v = new Version(fname, description, major, minor, micro);
        
        try
        {
            con = RDBMServices.getConnection();
            PreparedStatement sql =
                con.prepareStatement(
                    "INSERT INTO UP_VERSIONS (FNAME, DESCRIPTION, MAJOR, MINOR, MICRO) "
                        + "VALUES (?,?,?,?,?)");
            sql.setString(1, fname);
            sql.setString(2, description);
            sql.setInt(3, major);
            sql.setInt(4, minor);
            sql.setInt(5, micro);
            int modifiedCount = sql.executeUpdate();

            return modifiedCount == 1;
        }
        catch( Exception e)
        {
            log.error( 
                "Unable to insert version information for " + v.toString() +
                ". Abandoning insert.", e);
        }
        finally
        {
            RDBMServices.releaseConnection(con);
        }
        return false;
    }
}
