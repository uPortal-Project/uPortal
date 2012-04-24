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

package org.jasig.portal.spring;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import java.util.Collections;
import java.util.Set;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.google.common.collect.MapMaker;

/**
 * Factory for creating Mockito objects as beans.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MockitoFactoryBean<T> extends AbstractFactoryBean<T> {
    private static final Set<Object> MOCK_CACHE = Collections.newSetFromMap(new MapMaker().weakKeys().<Object, Boolean>makeMap());
    
    public static void resetAllMocks() {
        reset(MOCK_CACHE.toArray());
    }
    
    private final Class<? extends T> type;

    public MockitoFactoryBean(Class<? extends T> type) {
        this.type = type;
    }

    @Override
    public Class<? extends T> getObjectType() {
        return this.type;
    }

    @Override
    protected T createInstance() throws Exception {
        final T mock = mock(this.type);
        MOCK_CACHE.add(mock);
        return mock;
    }

    @Override
    protected void destroyInstance(T instance) throws Exception {
        MOCK_CACHE.remove(instance);
    }
    
}
