/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.utils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PooledCounterStoreTest extends AbstractTransactionalDataSourceSpringContextTests {
    private PooledCounterStore pooledCounterStore;
    private PooledCounterStore pooledCounterStoreTwo;
    
    
    public PooledCounterStoreTest() {
        this.setAutowireMode(AUTOWIRE_BY_NAME);
    }
    
    /**
     * @return the pooledCounterStore
     */
    public PooledCounterStore getPooledCounterStore() {
        return pooledCounterStore;
    }
    /**
     * @param pooledCounterStore the pooledCounterStore to set
     */
    public void setPooledCounterStore(PooledCounterStore pooledCounterStore) {
        this.pooledCounterStore = pooledCounterStore;
    }


    /**
     * @return the pooledCounterStoreTwo
     */
    public PooledCounterStore getPooledCounterStoreTwo() {
        return pooledCounterStoreTwo;
    }
    /**
     * @param pooledCounterStoreTwo the pooledCounterStoreTwo to set
     */
    public void setPooledCounterStoreTwo(PooledCounterStore pooledCounterStoreTwo) {
        this.pooledCounterStoreTwo = pooledCounterStoreTwo;
    }
    
    
    /* (non-Javadoc)
     * @see org.springframework.test.AbstractSingleSpringContextTests#getConfigPath()
     */
    @Override
    protected String getConfigPath() {
        return "/pooledCounterStoreTestApplicationContext.xml";
    }
    
    /* (non-Javadoc)
     * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        this.pooledCounterStore.reset();
        this.pooledCounterStore.setInitialValue(0);

        this.pooledCounterStoreTwo.reset();
        this.pooledCounterStoreTwo.setInitialValue(0);
        
        this.getJdbcTemplate().update("CREATE TABLE UP_SEQUENCE (SEQUENCE_NAME VARCHAR(1000), SEQUENCE_VALUE INTEGER)");
        assertEquals(0, this.countRowsInTable("UP_SEQUENCE"));
    }

    /* (non-Javadoc)
     * @see org.springframework.test.AbstractTransactionalSpringContextTests#onTearDown()
     */
    @Override
    protected void onTearDown() throws Exception {
        this.getJdbcTemplate().update("DROP TABLE UP_SEQUENCE");
        
        super.onTearDown();
    }
    
    /* (non-Javadoc)
     * @see org.springframework.test.ConditionalTestCase#runBare()
     */
    @Override
    public void runBare() throws Throwable {
        this.preventTransaction();
        super.runBare();
    }

    public void testCounterSingleThread() {
        this.pooledCounterStore.setIncrement(3);
        this.pooledCounterStoreTwo.setIncrement(3);
        
        //Get until DB has to increment
        this.getValue(this.pooledCounterStore, "Test1", 1, 2, 0);
        
        this.getValue(this.pooledCounterStoreTwo, "Test1", 1, 5, 3);
        
        this.getValue(this.pooledCounterStore, "Test1", 1, 5, 1);

        this.getValue(this.pooledCounterStoreTwo, "Test1", 1, 5, 4);
        
        this.getValue(this.pooledCounterStore, "Test1", 1, 5, 2);

        this.getValue(this.pooledCounterStoreTwo, "Test1", 1, 5, 5);
        
        this.getValue(this.pooledCounterStore, "Test1", 1, 8, 6);

        this.getValue(this.pooledCounterStoreTwo, "Test1", 1, 11, 9);
        
        
        //Try creating explicitly first
        this.pooledCounterStore.createCounter("Test2");
        assertEquals(2, this.countRowsInTable("UP_SEQUENCE"));
        assertEquals(11, this.getJdbcTemplate().queryForInt("SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME=?", new Object[] { "Test1" }));
        assertEquals(2, this.getJdbcTemplate().queryForInt("SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME=?", new Object[] { "Test2" }));
        
        this.getValue(this.pooledCounterStore, "Test2", 2, 2, 0);
        
        
        //Try Setting the counter
        this.pooledCounterStore.setCounter("Test1", 2);
        assertEquals(2, this.countRowsInTable("UP_SEQUENCE"));
        assertEquals(4, this.getJdbcTemplate().queryForInt("SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME=?", new Object[] { "Test1" }));
        assertEquals(2, this.getJdbcTemplate().queryForInt("SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME=?", new Object[] { "Test2" }));
        
        this.getValue(this.pooledCounterStore, "Test1", 2, 4, 2);
    }
    
    public void testCounterMultiThreadHighContention() throws Exception {
        this.pooledCounterStore.setIncrement(1);
        this.pooledCounterStoreTwo.setIncrement(1);
        
        doMultithreadedTest();
    }
    
    public void testCounterMultiThreadLowContention() throws Exception {
        this.pooledCounterStore.setIncrement(50);
        this.pooledCounterStoreTwo.setIncrement(50);
        
        doMultithreadedTest();
    }

    private void doMultithreadedTest() throws InterruptedException {
        final int threads = 20;
        ExecutorService executorService = Executors.newScheduledThreadPool(threads);
        
        final long start = System.currentTimeMillis() + 50;
        
        for (int thread = 0; thread < threads; thread++) {
            final int tid = thread;
            executorService.submit(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(start - System.currentTimeMillis());
                    }
                    catch (InterruptedException e) {
                        //Ignore
                    }
                    
                    for (int i = 0; i < 20; i++) {
                        final int v;
                        final String pool;
                        if (i % 2 == tid % 2) {
                            v = pooledCounterStore.getIncrementIntegerId("Test1");
                            pool = "one";
                        }
                        else {
                            v = pooledCounterStoreTwo.getIncrementIntegerId("Test1");
                            pool = "two";
                        }
                        if (logger.isDebugEnabled()) {
                            logger.debug(Thread.currentThread().getName() + "  \t" + pool + ": " + v);
                        }
                        
    
                        try {
                            Thread.sleep(2);
                        }
                        catch (InterruptedException e) {
                            //Ignore
                        }
                    }
                }
            });
        }
        
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);
        
        
        final int rowCount = this.countRowsInTable("UP_SEQUENCE");
        if (rowCount > 1) {
            
            final List contents = this.getJdbcTemplate().queryForList("SELECT * FROM UP_SEQUENCE");
            
            fail(rowCount + " rows in UP_SEQUENCE, there should be 1. Contents:\n" + contents);
        }
        assertEquals(399, this.getJdbcTemplate().queryForInt("SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME=?", new Object[] { "Test1" }));
    }
    
    protected void getValue(PooledCounterStore counterStore, String counter, int rows, int tableValue, int counterValue) {
        final int v = counterStore.getIncrementIntegerId(counter);
        assertEquals(rows, this.countRowsInTable("UP_SEQUENCE"));
        assertEquals(tableValue, this.getJdbcTemplate().queryForInt("SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME=?", new Object[] { counter }));
        assertEquals(counterValue, v);
    }
}
