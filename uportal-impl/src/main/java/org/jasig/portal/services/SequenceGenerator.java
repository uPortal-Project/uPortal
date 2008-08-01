/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services;

import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.jasig.portal.utils.ICounterStore;
import org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator;
import org.springframework.context.ApplicationContext;

/**
 * @author Dan Ellentuck 
 * @version $Revision$\
 * @deprecated Use {@link ICounterStore} instead.
 */
@Deprecated
public class SequenceGenerator {
    public static String DEFAULT_COUNTER_NAME = "DEFAULT";
    
    private final static SingletonDoubleCheckedCreator<SequenceGenerator> INSTANCE = new SingletonDoubleCheckedCreator<SequenceGenerator>() {
        /* (non-Javadoc)
         * @see org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator#createSingleton(java.lang.Object[])
         */
        @Override
        protected SequenceGenerator createSingleton(Object... args) {
            return new SequenceGenerator();
        }
    };

    private SequenceGenerator() {
    }
    
    private ICounterStore getCounterStore() {
        final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
        final ICounterStore counterStore = (ICounterStore)applicationContext.getBean("counterStore", ICounterStore.class);
        return counterStore;
    }

    /**
     * @param name String
     */
    public void createCounter(String name) throws Exception {
        final ICounterStore counterStore = this.getCounterStore();
        counterStore.createCounter(name);
    }

    /**
     * @return String
     */
    public String getNext() throws Exception {
        return this.getNext(DEFAULT_COUNTER_NAME);
    }

    /**
     * @param name String
     * @return String
     */
    public String getNext(String name) throws Exception {
        final ICounterStore counterStore = this.getCounterStore();
        final int next = counterStore.getIncrementIntegerId(name);
        return Integer.toString(next);
    }

    /**
     * @return int
     */
    public int getNextInt() throws Exception {
        return this.getNextInt(DEFAULT_COUNTER_NAME);
    }

    /**
     * @param name String
     * @return int
     */
    public int getNextInt(String name) throws Exception {
        final ICounterStore counterStore = this.getCounterStore();
        return counterStore.getIncrementIntegerId(name);
    }

    /**
     * @param name java.lang.String
     * @param newValue int
     */
    public void setCounter(String name, int newValue) throws Exception {
        final ICounterStore counterStore = this.getCounterStore();
        counterStore.setCounter(name, newValue);
    }

    /**
     * @return SequenceGenerator
     */
    public final static SequenceGenerator instance() {
        return INSTANCE.get();
    }
}
