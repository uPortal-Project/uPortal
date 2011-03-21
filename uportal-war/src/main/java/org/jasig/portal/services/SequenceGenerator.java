/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.services;

import org.jasig.portal.spring.locator.CounterStoreLocator;
import org.jasig.portal.utils.ICounterStore;
import org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator;

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
        return CounterStoreLocator.getCounterStore();
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
    public int getNextInt(String name) {
        final ICounterStore counterStore = this.getCounterStore();
        return counterStore.getIncrementIntegerId(name);
    }

    /**
     * @param name java.lang.String
     * @param newValue int
     */
    public void setCounter(String name, int newValue) {
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
