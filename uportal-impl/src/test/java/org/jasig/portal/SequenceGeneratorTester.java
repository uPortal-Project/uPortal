/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * Tests the sequence generator.
 * @author: Dan Ellentuck
 */
public class SequenceGeneratorTester extends TestCase {
    private int numTestCounters;
    private String[] testCounterNames;
	private ReferenceSequenceGenerator generator;
	
    protected class Tester implements Runnable 
    {
	    protected ReferenceSequenceGenerator generator = null;
	    protected int numTests = 0;
	    protected String[] counterValues = null;
	    
        protected Tester(ReferenceSequenceGenerator gen, int tests) 
        {
            super();
            generator = gen;
            numTests = tests;
            counterValues = new String[numTests];
        }
        public void run() {
            for (int i=0; i<numTests; i++) 
            {
                String ctrValue = null;
                for (int j=0; ctrValue == null && j<10; j++)
                {
                    try { ctrValue = generator.getNext(testCounterNames[0]); }
                    catch (Exception e) 
                        { print("SequenceGeneratorTester$Tester: Caught Exception: " + e.getMessage()); }
                }
	            counterValues[i] = ctrValue;
            }
        }
    } 
	
/**
 * EntityLockTester constructor comment.
 */
public SequenceGeneratorTester(String name) {
	super(name);
}
/**
 * @return org.jasig.portal.concurrency.locking.IEntityLockStore
 */
private ReferenceSequenceGenerator getGenerator() {
    if (generator == null)
        { generator = new ReferenceSequenceGenerator(); }
    return generator;
}
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) throws Exception
{
	String[] mainArgs = {"org.jasig.portal.SequenceGeneratorTester"};
    print("START TESTING SEQUENCE GENERATOR");
    printBlankLine();
    TestRunner.main(mainArgs);
    printBlankLine();    
    print("END TESTING SEQUENCE GENERATOR");
    
}
/**
 * @param msg java.lang.String
 */
private static void print(String msg) 
{
    java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
    System.out.println(ts + " : " + msg);
}
/**
 */
private static void printBlankLine() 
{
    System.out.println("");
}
/**
 */
protected void setUp() 
{
	Connection conn = null;
	Statement  stmt = null;
	
    try {
        String sql;
        int idx;
        numTestCounters = 5;
        testCounterNames = new String[numTestCounters];
        
        print("Creating test counters.");
        
        for (idx=0; idx<numTestCounters; idx++)
            { testCounterNames[idx] = "TEST_COUNTER_" + idx; }

        conn = RDBMServices.getConnection();
        stmt = conn.createStatement();
        
        stmt.execute("CREATE TABLE UP_SEQUENCE(SEQUENCE_NAME VARCHAR(255) NOT NULL PRIMARY KEY,SEQUENCE_VALUE INTEGER)");
        
        // Delete any left over counters that could interfere. 
        for (idx=0; idx<testCounterNames.length; idx++)
        {
            sql = "DELETE FROM UP_SEQUENCE WHERE SEQUENCE_NAME = " + 
              "'" + testCounterNames[idx] + "'";
            stmt.executeUpdate(sql);
        }
        // create some test counters:
        for (idx=0; idx<numTestCounters; idx++)
        {
            sql = "INSERT INTO UP_SEQUENCE (SEQUENCE_NAME, SEQUENCE_VALUE) " + 
		          "VALUES (" + "'" + testCounterNames[idx] + "', 0)";
            stmt.executeUpdate(sql);
        }
    }
    catch (Exception ex) { print("SequenceGeneratorTester.setUp(): " + ex.getMessage());}
    finally
    {
        if (stmt != null) { try { stmt.close(); } catch (SQLException sqle) {} }
        if (conn != null) { RDBMServices.releaseConnection(conn); }
    }
 }
/**
 */
protected void tearDown() 
{
	Connection conn = null;
	Statement stmt = null;
    try {
        // delete the test counters:
        print("Deleting test counters.");
        String sql;
        int idx;
        conn = RDBMServices.getConnection();
        stmt = conn.createStatement();

        stmt.execute("DROP TABLE UP_SEQUENCE");
    }
    catch (Exception ex) { print("SequenceGeneratorTester.tearDown(): " + ex.getMessage());}
    finally
    {
        if (stmt != null) { try {stmt.close();} catch (SQLException sqle) {} }
        if (conn != null) { RDBMServices.releaseConnection(conn); }
    }
}
/**
 */
public void testConcurrentAccess() throws Exception
{
    ReferenceSequenceGenerator gen = new ReferenceSequenceGenerator();

    int numTests = 50;
    int numThreads = 5;
    String msg = null;

    print("Setting up testing Threads.");
    Tester[] testers = new Tester[numThreads];
    for (int i=0; i<numThreads; i++)
    {
    	testers[i] = new Tester(gen, numTests);
    	Thread thread = new Thread(testers[i]);
    	thread.start(); 
    }
   

    long millis = numTests * numThreads * 10;    
    print("Now sleeping for " + (millis/1000) + " seconds.");
    Thread.sleep(millis);
    
    msg = "Checking counter values for uniqueness.";
    print(msg);

    String testValue;
    Set testValues = new HashSet();
    boolean testResult = false;
     
    for (int testerIdx=0; testerIdx<numThreads; testerIdx++)
    { 
    	for ( int valueIdx = 0; valueIdx <numTests; valueIdx++ )
        {
        	testValue = testers[testerIdx].counterValues[valueIdx]; 
            assertNotNull( msg, testValue );
            assertTrue( msg, ! testValues.contains(testValue));
            testValues.add(testValue);
        }
    }
        
}
/**
 */
public void testCreateNewCounters() throws Exception
{
	    String msg = null;
        int numNewCounters = 5;
        int idx;
        int counterValue;

        String[] counterNames = new String[numNewCounters];
        for (idx=0; idx<numNewCounters; idx++)
            { counterNames[idx] = "NEW_CTR_" + idx; }

        print("Creating new counter(s)."); 
        for (idx=0; idx<numNewCounters; idx++)
        {
            getGenerator().createCounter(counterNames[idx]);
        }
        
        msg = "Getting sequence value from new counter(s).";
        print(msg);
        for (idx=0; idx<numNewCounters; idx++)
        {
            counterValue = getGenerator().getNextInt(counterNames[idx]);
            assertEquals(msg, 1, counterValue);
        }
        

        print("Deleting new counter(s).");
	    Connection conn = null;
	    Statement stmt = null;
        try {
            String sql;
            conn = RDBMServices.getConnection();
            stmt = conn.createStatement();
            for (idx=0; idx<counterNames.length; idx++)
            {
                sql = "DELETE FROM UP_SEQUENCE WHERE SEQUENCE_NAME = " + 
                  "'" + counterNames[idx] + "'";
                stmt.executeUpdate(sql);
            }
        }
        catch (Exception ex) { print("SequenceGeneratorTester.testCreateNewCounters(): " + ex.getMessage());}
        finally
        {
            if (stmt != null) { try {stmt.close();} catch (SQLException sqle) {} }
            if (conn != null) { RDBMServices.releaseConnection(conn); }
        }

}
/**
 */
public void testGetUniqueSequenceNumbers() throws Exception
{
    String msg = null;
    int numTestValues = 10;
    int idx;
    String[][] testValues = new String[numTestCounters][numTestValues];

    print("Getting sequence values."); 
    for (idx=0; idx<numTestCounters; idx++)
    {
        for(int i=0; i<numTestValues; i++)
        {
            testValues[idx][i] = getGenerator().getNext(testCounterNames[idx]);
        }

    }
        
    msg = "Testing sequence values for uniqueness.";
    print(msg);
    boolean assertionValue;
    for (idx=0; idx<numTestCounters; idx++)
    {
        for(int i=1; i<numTestValues; i++)
        {
            assertionValue = testValues[idx][i-1].equals( testValues[idx][i] );
            assertTrue(msg, ! assertionValue);
        }

    }
}
/**
 */
public void testSetCounterValues() throws Exception
{
    int idx, testValue, nextCounterValue;
        
    print("Setting sequence values."); 
    for (idx=0; idx<numTestCounters; idx++)
    {
        testValue = idx * 999;
        getGenerator().setCounter(testCounterNames[idx], testValue);
        nextCounterValue = getGenerator().getNextInt(testCounterNames[idx]);
        nextCounterValue--;
        assertEquals(testValue, nextCounterValue);
    }
        
}
}
