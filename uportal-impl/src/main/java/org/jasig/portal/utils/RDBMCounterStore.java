/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.RDBMServices;

/**
 * A reference implementation for the counter store
 *
 * @author George Lindholm, george.lindholm@ubc.ca
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$
 * @deprecated Use {@link PooledCounterStore} instead
 */
@Deprecated
public class RDBMCounterStore implements ICounterStore {
    
    private static final Log log = LogFactory.getLog(RDBMCounterStore.class);
    
    /**
     * Creates a new counter with an initial value of 0. Does not check to
     * see if the counter already exists.
     * 
     * @see org.jasig.portal.utils.ICounterStore#createCounter(java.lang.String)
     */
    public synchronized void createCounter (String counterName) throws Exception {
        Connection con = RDBMServices.getConnection();
        PreparedStatement createCounterPstmt = null;
        
        try {
            RDBMServices.setAutoCommit(con, false);
            
            String createCounterInsert =
                "INSERT INTO UP_SEQUENCE (SEQUENCE_NAME, SEQUENCE_VALUE) " +
                "VALUES (?, 0)";
            
            createCounterPstmt = con.prepareStatement(createCounterInsert);
            createCounterPstmt.setString(1, counterName);
            
            if (log.isDebugEnabled())
                log.debug("RDBMCounterStore::createCounter(" + counterName + 
                        "): " + createCounterInsert);
            int updateCount = createCounterPstmt.executeUpdate();
            
            if (updateCount <= 0) {
                PortalException pe = new PortalException("RDBMCounterStore::createCounter(): An error occured while creating the counter named: " + counterName + ".\nNo rows were created.");
                log.error("RDBMCounterStore::createCounter(): " +
                        "An error occured while creating the counter named: " 
                        + counterName + ".\nNo rows were created.", pe); 
                throw pe;
            }
            
            RDBMServices.commit(con);
        }
        catch (SQLException sqle) {
            RDBMServices.rollback(con);
            
            PortalException pe = 
                new PortalException("RDBMCounterStore::createCounter(): " +
                        "An error occured while creating the counter named: " 
                        + counterName, sqle);
            log.error("RDBMCounterStore::createCounter(): " +
                    "An error occured while creating the counter named: " 
                    + counterName, pe); 
            throw pe;
        } 
        finally {
            try { createCounterPstmt.close(); } catch (Exception e) {};
       
            RDBMServices.releaseConnection(con);
        }
    }


    /**
     * Sets the counter to the specified value. Does not check to make
     * sure the counter already exists.
     * 
     * @see org.jasig.portal.utils.ICounterStore#setCounter(java.lang.String, int)
     */
    public synchronized void setCounter (String counterName, int value) throws Exception {
        Connection con = RDBMServices.getConnection();
        
        PreparedStatement setCounterPstmt = null;
        
        try {
            RDBMServices.setAutoCommit(con, false);
            
            String setCounterUpdate = 
                "UPDATE UP_SEQUENCE " +
                "SET SEQUENCE_VALUE=? " +
                "WHERE SEQUENCE_NAME=?";
            
            setCounterPstmt = con.prepareStatement(setCounterUpdate);
            setCounterPstmt.setInt(1, value);
            setCounterPstmt.setString(2, counterName);
            if (log.isDebugEnabled())
                log.debug("RDBMCounterStore::setCounter(" + counterName + ", " + 
                        value + "): " + setCounterUpdate);
            int updateCount = setCounterPstmt.executeUpdate();
            
            if (updateCount <= 0) {
                PortalException pe = 
                    new PortalException("RDBMCounterStore::setCounter(): " +
                            "An error occured while setting the counter named: " 
                            + counterName + ".\nNo rows were updated.");
                log.error(pe, pe); 
                throw pe;
            }            
            
            RDBMServices.commit(con);
        } 
        catch (SQLException sqle) {
            RDBMServices.rollback(con);
            
            PortalException pe = 
                new PortalException("RDBMCounterStore::createCounter(): " +
                        "An error occured while creating the counter named: " 
                        + counterName, sqle);
            log.error(pe, pe); 
            throw pe;
            
        }
        finally {
            try { setCounterPstmt.close(); } catch (Exception e) {};
            
            RDBMServices.releaseConnection(con);
        }
    }

    /**
     * Gets the next number in the sequence. If the counter does not exist
     * it is first created.
     * 
     * @see org.jasig.portal.utils.ICounterStore#getIncrementIntegerId(java.lang.String)
     */
    public synchronized int getIncrementIntegerId (String counterName) throws Exception {
        Connection con = RDBMServices.getConnection();
        
        PreparedStatement getCounterPstmt = null;
        PreparedStatement updateCounterPstmt = null;
        ResultSet rs = null;
        
        try {
            String getCounterQuery =
                "SELECT SEQUENCE_VALUE " +
                "FROM UP_SEQUENCE " +
                "WHERE SEQUENCE_NAME=?";
            
            String updateCounterQuery =
                "UPDATE UP_SEQUENCE " +
                "SET SEQUENCE_VALUE=? " +
                "WHERE SEQUENCE_NAME=? AND SEQUENCE_VALUE=?";
            
            getCounterPstmt = con.prepareStatement(getCounterQuery);
            getCounterPstmt.setString(1, counterName);
            
            updateCounterPstmt = con.prepareStatement(updateCounterQuery);
            updateCounterPstmt.setString(2, counterName);
            
            for (int i = 0; i < 25; i++) 
            {
                if (log.isDebugEnabled())
                    log.debug("RDBMCounterStore::getIncrementInteger(" 
                        + counterName + "): " + getCounterQuery);
                rs = getCounterPstmt.executeQuery();
                
                if (!rs.next()) {
                    try {
                        createCounter(counterName);
                    }
                    catch (Exception e) {
                        throw 
                        new PortalException("RDBMCounterStore::getIncrementInteger(): " +
                                "Could not create new counter for name: " + counterName, e);
                    }
                    
                    rs = getCounterPstmt.executeQuery();
                    
                    if (!rs.next()) {
                        throw new PortalException("RDBMCounterStore::getIncrementInteger(): Counter should have been created but was not found, name: " + counterName);
                    }
                }
                
                int origId = rs.getInt(1);
                int nextId = origId + 1;
                
                updateCounterPstmt.setInt(1, nextId);
                updateCounterPstmt.setInt(3, origId);
                
                if (log.isDebugEnabled())
                    log.debug("RDBMCounterStore::getIncrementInteger(" + counterName + ", " + nextId + ", " + origId + "): " + updateCounterQuery);
                int rowsUpdated = updateCounterPstmt.executeUpdate();
                
                if (rowsUpdated > 0) { 
                    return nextId;
                }
                else {
                    // Assume concurrent update (from other server). Try again after some random amount of milliseconds. 
                    Thread.sleep(java.lang.Math.round(java.lang.Math.random()* 3 * 1000)); // Retry in up to 3 seconds
                }
            }          // end try 
        } 
        catch (SQLException sqle) {
            PortalException pe = new PortalException("RDBMCounterStore::getIncrementInteger(): An error occured while updating the counter, name: " + counterName, sqle);
            log.error(pe, pe);
            throw pe;
        } 
        finally {
            try { getCounterPstmt.close(); } catch (Exception e) {}
            try { updateCounterPstmt.close(); } catch (Exception e) {}
            try { rs.close(); } catch (Exception e) {}
            
            RDBMServices.releaseConnection(con);
        }
        
        PortalException pe = new PortalException("RDBMCounterStore::getIncrementInteger(): Unable to increment counter for " + counterName);
        log.error(pe, pe);
        throw pe;
    }

}
