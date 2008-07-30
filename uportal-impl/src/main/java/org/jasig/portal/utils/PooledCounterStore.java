/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.utils;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PooledCounterStore implements ICounterStore {
    private static final String GET_NEXT_VALUE =
        "SELECT SEQUENCE_VALUE " +
        "FROM UP_SEQUENCE " +
        "WHERE SEQUENCE_NAME=?";
    
    private static final String UPDATE_COUNTER_VALUE =
        "UPDATE UP_SEQUENCE " +
        "SET SEQUENCE_VALUE=? " +
        "WHERE SEQUENCE_NAME=? AND SEQUENCE_VALUE=?";
    
    private static final String CREATE_SEQUENCE =
        "INSERT INTO UP_SEQUENCE (SEQUENCE_NAME, SEQUENCE_VALUE) " +
        "VALUES (?, ?)";
    
    private static final String FORCED_UPDATE_COUNTER_VALUE =
        "UPDATE UP_SEQUENCE " +
        "SET SEQUENCE_VALUE=? " +
        "WHERE SEQUENCE_NAME=?";

    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final ConcurrentHashMap<String, CounterPool> counterPools = new ConcurrentHashMap<String, CounterPool>();
    private final Random random = new Random();

    private TransactionTemplate transactionTemplate;
    private SimpleJdbcTemplate simpleJdbcTemplate;
    private int initialValue = 1;
    private int increment = 50;
    private int retryCount = 10;
    private int minWait = 1;
    private int maxWait = 10;
    
    /**
     * The DataSource to use for counter generation
     */
    @Required
    public void setDataSource(DataSource dataSource) {
        Validate.notNull(dataSource);
        this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }
    
    /**
     * The transaction manager to use for updates to the counter table
     */
    @Required
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        Validate.notNull(transactionManager);
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }
    
    
    public int getInitialValue() {
        return initialValue;
    }
    /**
     * Initial value to set for a counter when creating it
     */
    public void setInitialValue(int initialValue) {
        this.initialValue = initialValue;
    }

    public int getIncrement() {
        return increment;
    }
    /**
     * In-memory counter value pool size. The higher this value the fewer database access will be needed but more
     * values may be lost at app shutdown
     */
    public void setIncrement(int increment) {
        this.increment = increment;
    }

    public int getRetryCount() {
        return retryCount;
    }
    /**
     * Number of attempts to make when a database update fails to to concurrent update from another machine
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getMinWait() {
        return minWait;
    }
    /**
     * Minimum number of milliseconds to wait between retries
     */
    public void setMinWait(int minWait) {
        this.minWait = minWait;
    }

    public int getMaxWait() {
        return maxWait;
    }
    /**
     * Maximum number of milliseconds to wait between retries
     */
    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.ICounterStore#createCounter(java.lang.String)
     */
    public void createCounter(final String counterName) {
        final CounterPool counterPool = this.getCounterPool(counterName);
        
        synchronized (counterPool) {
            if (!counterPool.initialized.get()) {
                for (int attempt = 0; attempt < this.retryCount; attempt++) {
                    final boolean counterCreated = (Boolean)this.transactionTemplate.execute(counterPool.createCounterCallback);
                    if (counterCreated) {
                        break;
                    }
                    
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("Failed to create counter '" + counterName + "' waiting to retry");
                    }
                    this.waitToRetry();
                }
            }
            else {
                this.logger.warn("Requested creation of counter '" + counterName + "' but the corresponding CounterPool is initialized, assuming it exists");
            }
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.ICounterStore#getIncrementIntegerId(java.lang.String)
     */
    public int getIncrementIntegerId(final String counterName) {
        final CounterPool counterPool = this.getCounterPool(counterName);
        
        synchronized (counterPool) {
            if (counterPool.needsUpdate()) {
                for (int attempt = 0; attempt < this.retryCount; attempt++) {
                    final boolean counterUpdated = (Boolean)this.transactionTemplate.execute(counterPool.incrementCounterCallback);
                    if (counterUpdated) {
                        break;
                    }
                    
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("Failed to update counter '" + counterName + "' waiting to retry");
                    }
                    this.waitToRetry();
                }
            }
            
            return counterPool.nextValue.getAndIncrement();
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.ICounterStore#setCounter(java.lang.String, int)
     */
    public void setCounter(String counterName, int value) {
        final CounterPool counterPool = this.getCounterPool(counterName);
        
        synchronized (counterPool) {
            final ForceUpdateCounterCallback forceUpdateCounterCallback = new ForceUpdateCounterCallback(counterPool, this.increment, value, this.simpleJdbcTemplate);
            
            for (int attempt = 0; attempt < this.retryCount; attempt++) {
                final boolean counterSet = (Boolean)this.transactionTemplate.execute(forceUpdateCounterCallback);
                if (counterSet) {
                    break;
                }
                
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Failed to set counter '" + counterName + "' waiting to retry");
                }
                this.waitToRetry();
            }
        }
    }
    
    public void reset() {
        this.counterPools.clear();
    }

    /**
     * Get the unique CounterPool instance for the specified counterName
     */
    protected CounterPool getCounterPool(String counterName) {
        CounterPool counterPool = this.counterPools.get(counterName);
        if (counterPool == null) {
            final CounterPool newCounterPool = new CounterPool(counterName, this.increment, this.initialValue, this.simpleJdbcTemplate);
            counterPool = this.counterPools.putIfAbsent(counterName, newCounterPool);
            
            if (counterPool == null) {
                counterPool = newCounterPool;
            }
        }

        return counterPool;
    }
    
    /**
     * Blocks for the number of milliseconds computed via the minWait and maxWait properties
     */
    protected void waitToRetry() {
        final int waitTime;
        if (this.minWait >= this.maxWait) {
            waitTime = this.minWait;
        }
        else {
            waitTime = random.nextInt(this.maxWait - this.minWait) + this.minWait;
        }
        
        if (this.logger.isDebugEnabled()) {
            this.logger.warn("Waiting " + waitTime);
        }
        
        if (waitTime <= 0) {
            return;
        }
        
        try {
            Thread.sleep(waitTime);
        }
        catch (InterruptedException e) {
            //Ignore and continue
        }
    }



    /**
     * Represents a named counter, used to track the in-memory pool of values
     */
    private static class CounterPool {
        public final String name;
        public final AtomicInteger nextValue = new AtomicInteger(0);
        public final AtomicInteger maxValue = new AtomicInteger(0);
        public final AtomicBoolean initialized = new AtomicBoolean(false);
        
        public final IncrementCounterCallback incrementCounterCallback;
        public final CreateCounterCallback createCounterCallback;
        
        public CounterPool(String name, int increment, int initialValue, SimpleJdbcTemplate simpleJdbcTemplate) {
            this.name = name;
            this.incrementCounterCallback = new IncrementCounterCallback(this, increment, initialValue, simpleJdbcTemplate);
            this.createCounterCallback = new CreateCounterCallback(this, increment, initialValue, simpleJdbcTemplate);
        }
        
        public boolean needsUpdate() {
            return !this.initialized.get() || this.nextValue.get() > this.maxValue.get();
        }
    }
    
    /**
     * Updates a counter entry using the specified increment, creating it first if nessesary. The passed
     * {@link CounterPool} is updated to reflect the query results if successful.
     * 
     * Returns true if a counter entry was updated or created. False if the update statement returns
     * 0 rows affected (concurrent updates), throws {@link org.springframework.dao.DataAccessException}
     * in any other case.
     */
    private static final class IncrementCounterCallback extends BaseCounterCallback {
        private final CounterPool counterPool;
        private final SimpleJdbcTemplate simpleJdbcTemplate;
        private final int increment;

        public IncrementCounterCallback(CounterPool counterPool, int increment, int initialValue, SimpleJdbcTemplate simpleJdbcTemplate) {
            super(counterPool, increment, initialValue, simpleJdbcTemplate);
            this.counterPool = counterPool;
            this.increment = increment;
            this.simpleJdbcTemplate = simpleJdbcTemplate;
        }

        /* (non-Javadoc)
         * @see org.springframework.transaction.support.TransactionCallback#doInTransaction(org.springframework.transaction.TransactionStatus)
         */
        public Object doInTransaction(TransactionStatus status) {
            int lastValue;
            try {
                lastValue = this.simpleJdbcTemplate.queryForInt(GET_NEXT_VALUE, this.counterPool.name);
            }
            catch (IncorrectResultSizeDataAccessException irsdae) {
                //Only know how to handle no counter existing yet
                if (irsdae.getActualSize() != 0) {
                    throw irsdae;
                }
                
                return this.createCounter(status);
            }
            
            final int nextEndValue = lastValue + this.increment;
            final int updateCount = this.simpleJdbcTemplate.update(UPDATE_COUNTER_VALUE, nextEndValue, this.counterPool.name, lastValue);
            
            //Counter updated, update the counterPool and return true
            if (updateCount == 1) {
                this.counterPool.initialized.set(true);
                this.counterPool.nextValue.set(lastValue + 1);
                this.counterPool.maxValue.set(nextEndValue);
                return true;
            }
            
            //Failed to update counter for some reason, return false
            return false;
        }
    }
    
    /**
     * Creates a new counter entry in the sequence table if one doesn't already exist. The passed
     * {@link CounterPool} is updated to reflect the query results if successful.
     * 
     * Returns true if a counter entry exists or is created. False if the insert statement returns
     * 0 rows affected, throws {@link org.springframework.dao.DataAccessException} in any other case.
     */
    private static final class CreateCounterCallback extends BaseCounterCallback {
        private final CounterPool counterPool;
        private final SimpleJdbcTemplate simpleJdbcTemplate;

        public CreateCounterCallback(CounterPool counterPool, int increment, int initialValue, SimpleJdbcTemplate simpleJdbcTemplate) {
            super(counterPool, increment, initialValue, simpleJdbcTemplate);
            this.counterPool = counterPool;
            this.simpleJdbcTemplate = simpleJdbcTemplate;
        }

        /* (non-Javadoc)
         * @see org.springframework.transaction.support.TransactionCallback#doInTransaction(org.springframework.transaction.TransactionStatus)
         */
        public Object doInTransaction(TransactionStatus status) {
            try {
                this.simpleJdbcTemplate.queryForInt(GET_NEXT_VALUE, this.counterPool.name);
                //No exception so the counter must exist, return true
                return true;
            }
            catch (IncorrectResultSizeDataAccessException irsdae) {
                //Only know how to handle no counter existing yet
                if (irsdae.getActualSize() != 0) {
                    throw irsdae;
                }
                
                return this.createCounter(status);
            }
        }
    }
    
    /**
     * Force the value of the counter to the specified value. The passed
     * {@link CounterPool} is updated to reflect the query results if successful.
     * 
     * Returns true if a counter entry is set or created. False if doesn't exist and create fails,
     * throws {@link org.springframework.dao.DataAccessException} in any other case.
     */
    private static final class ForceUpdateCounterCallback extends BaseCounterCallback {
        private final CounterPool counterPool;
        private final SimpleJdbcTemplate simpleJdbcTemplate;
        private final int forcedValue;
        private final int increment;

        public ForceUpdateCounterCallback(CounterPool counterPool, int increment, int forcedValue, SimpleJdbcTemplate simpleJdbcTemplate) {
            super(counterPool, increment, forcedValue, simpleJdbcTemplate);
            this.counterPool = counterPool;
            this.increment = increment;
            this.forcedValue = forcedValue;
            this.simpleJdbcTemplate = simpleJdbcTemplate;
        }

        /* (non-Javadoc)
         * @see org.springframework.transaction.support.TransactionCallback#doInTransaction(org.springframework.transaction.TransactionStatus)
         */
        public Object doInTransaction(TransactionStatus status) {
            final int maxValue = this.forcedValue + this.increment - 1;
            final int updateCount = this.simpleJdbcTemplate.update(FORCED_UPDATE_COUNTER_VALUE, maxValue, this.counterPool.name);
            
            //Counter set, update the counterPool and return true
            if (updateCount == 1) {
                this.counterPool.initialized.set(true);
                this.counterPool.nextValue.set(this.forcedValue);
                this.counterPool.maxValue.set(maxValue);
                return true;
            }
            
            //Failed to set counter, must not exist so create it
            return this.createCounter(status);
        }
    }
    
    /**
     * Base logic for creating a counter that doesn't already exists
     */
    private static abstract class BaseCounterCallback implements TransactionCallback {
        private final CounterPool counterPool;
        private final SimpleJdbcTemplate simpleJdbcTemplate;
        private final int initialValue;
        private final int increment;

        public BaseCounterCallback(CounterPool counterPool, int increment, int initialValue, SimpleJdbcTemplate simpleJdbcTemplate) {
            this.counterPool = counterPool;
            this.increment = increment;
            this.initialValue = initialValue;
            this.simpleJdbcTemplate = simpleJdbcTemplate;
        }

        /**
         * Assumes the caller has already checked that the counter doesn't exist.
         */
        protected boolean createCounter(TransactionStatus status) {
            //No counter yet
            final int maxValue = this.initialValue + this.increment - 1;
            final int updateCount = this.simpleJdbcTemplate.update(CREATE_SEQUENCE, this.counterPool.name, maxValue);
        
            //Counter created, update the counterPool and return true
            if (updateCount == 1) {
                this.counterPool.initialized.set(true);
                this.counterPool.nextValue.set(this.initialValue);
                this.counterPool.maxValue.set(maxValue);
                return true;
            }
            
            //Failed to create counter for some reason, return false
            return false;
        }
    }
}