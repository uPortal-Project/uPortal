package org.jasig.portal;


/**
 * A class to keep track and generate incremental 
 * IDs for various tables. 
 * @author Peter Kharchenko
 * @version $Revision$
 */

import org.w3c.dom.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.jasig.portal.utils.DTDResolver;


public class DBCounterImpl {

    private static RdbmServices rdbmService = new RdbmServices ();
    private static Connection connection=null;

    public DBCounterImpl() {
    }

    private Connection getConnection() {
	if(connection==null) 
	    connection=rdbmService.getConnection();
	return connection;
    }
    
    /*
     * get&increment method.
     */
    public synchronized Integer getIncrementIntegerId(String tableName) {
	Connection con=null;
	Integer id=null;
        try {
	    con=this.getConnection();
            Statement stmt = con.createStatement ();
	    String str_id=null;
	    
            String sQuery = "SELECT ID FROM UP_COUNTERS WHERE TABLE_NAME='" + tableName + "'";
            Logger.log (Logger.DEBUG, sQuery);
            ResultSet rs = stmt.executeQuery (sQuery);
            if (rs.next ()) {
                    str_id = rs.getString ("ID");
	    }
	    if(str_id!=null) {
		id=new Integer(Integer.parseInt(str_id)+1);
		sQuery = "UPDATE UP_COUNTERS SET ID="+id.intValue()+"WHERE TABLE_NAME='" + tableName + "'";
		stmt.executeUpdate(sQuery);
	    }
	} catch (Exception e) {
	    Logger.log(Logger.ERROR,e);
	} finally {
            rdbmService.releaseConnection (con);
        }
	return id;
    }

    public synchronized void createCounter(String tableName) {
	Connection con=null;
	try {
	    con=this.getConnection();
            Statement stmt = con.createStatement ();
            String sQuery = "INSERT INTO UP_COUNTERS ('TABLE_NAME,ID') VALUES ('"+tableName+"',0)";
            Logger.log (Logger.DEBUG, sQuery);
            ResultSet rs = stmt.executeQuery (sQuery);
	} catch (Exception e) {
	    Logger.log(Logger.ERROR,e);
	} finally {
            rdbmService.releaseConnection (con);
        }
    }

    public synchronized void setCounter(String tableName,int value) {
	Connection con=null;
	try {
	    con=this.getConnection();
            Statement stmt = con.createStatement ();
            String sQuery = "UPDATE UP_COUNTERS SET ID="+value+"WHERE TABLE_NAME='" + tableName + "'";
            Logger.log (Logger.DEBUG, sQuery);
            stmt.executeUpdate (sQuery);
	} catch (Exception e) {
	    Logger.log(Logger.ERROR,e);
	} finally {
            rdbmService.releaseConnection (con);
        }
    }
}

