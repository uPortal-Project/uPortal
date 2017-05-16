package com.uportal;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;

import org.hsqldb.server.ServerAcl.AclFormatException;

public class HsqlDb {
	public void startDb( HsqlProperties p) {
		try {
	        System.out.println("Starting Database");
	        Server server = new Server();
	        server.setProperties(p);
	        server.setLogWriter(null); 
	        server.setErrWriter(null); 
	        
	        server.start();
	        
	    } catch (AclFormatException afex) {
	        throw new RuntimeException("Unable to start hsqldb");
	    } catch (IOException ioex) {
	        throw new RuntimeException("Unable to start hsqldb");
	    }
	}
	
	public void stopDb(HsqlProperties p) {
		System.out.println("Preparing for stopping hsqldb database");
		
		  String  driver = "org.hsqldb.jdbcDriver";
		  String url = String.format("jdbc:hsqldb:hsql://localhost:%s/%s",p.getProperty("server.port"), p.getProperty("server.dbname.0"));
        try {
            Class.forName(driver);       // Load the driver

            Connection connection = DriverManager.getConnection(url, "sa",
                "");
            Statement statement = connection.createStatement();

            // can use SHUTDOWN COMPACT or SHUTDOWN IMMEDIATELY
            statement.execute("SHUTDOWN COMPACT");
        } catch (ClassNotFoundException e) {
            System.err.println(e);    // Driver not found
        } catch (SQLException e) {
            System.err.println(e);    // error connection to database
        }
        System.out.println("Hsqldb database was stopped successfully");
    }
		
				

}
