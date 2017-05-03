/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.shell;

import groovy.lang.Binding;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.springframework.context.ApplicationContext;

/**
 */
public class SpringBinding extends Binding implements Map<String, Object> {
    private final ApplicationContext context;

    public SpringBinding(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void setVariable(String name, Object value) {
        if (this.context.containsBean(name)) {
            throw new IllegalArgumentException("Can't bind variable to key named '" + name + "'.");
        }

        super.setVariable(name, value);
    }

    @Override
    public Object getVariable(String name) {
        if (this.context.containsBean(name)) {
            return this.context.getBean(name);
        }

        return super.getVariable(name);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map getVariables() {
        return this;
    }

    //******************** MAP INTERFACE IMPL ********************//

    @Override
    public int size() {
        return super.getVariables().size() + context.getBeanDefinitionCount();
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return context.containsBean((String) key) || super.getVariables().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(Object key) {
        final String name = (String) key;
        if (context.containsBean(name)) {
            return context.getBean(name);
        }

        return super.getVariables().get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object put(String key, Object value) {
        if (context.containsBean(key)) {
            throw new IllegalArgumentException("Can't bind variable to key named '" + key + "'.");
        }

        return super.getVariables().put(key, value);
    }

    @Override
    public Object remove(Object key) {
        final String name = (String) key;
        if (context.containsBean(name)) {
            throw new IllegalArgumentException("Can't remove variable to named '" + name + "'.");
        }

        return super.getVariables().remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        for (Map.Entry<? extends String, ? extends Object> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        super.getVariables().clear();
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Object> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
