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

package org.jasig.portal.url;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;

/**
 * Builds a URL.
 * 
 * This class is not thread safe.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class UrlStringBuilder extends BaseEncodedStringBuilder {
    private static final long serialVersionUID = 1L;

    private final String protocol;
    private final String host;
    private final Integer port;
    private final String context;
    private final List<String> path = new LinkedList<String>();
    private final Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
    
    /**
     * Creates a URL with no host, protocol or port. The URL will start with a /
     * 
     * @param encoding The encoding to use for parameters
     */
    public UrlStringBuilder(String encoding, String context) {
        super(encoding);

        this.protocol = null;
        this.host = null;
        this.port = null;
        this.context = context;
    }
    
    /**
     * Creates a URL with no host, protocol or port. The URL will start with protocol://host
     * 
     * @param encoding The encoding to use for parameters
     */
    public UrlStringBuilder(String encoding, String protocol, String host) {
        this(encoding, protocol, host, null);
    }
    
    /**
     * Creates a URL with no host, protocol or port. The URL will start with protocol://host:port
     * 
     * @param encoding The encoding to use for parameters
     */
    public UrlStringBuilder(String encoding, String protocol, String host, Integer port) {
        super(encoding);
        
        Validate.notNull(protocol, "protocol can not be null");
        Validate.notNull(host, " host can not be null");

        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.context = null;
    }
    
    /**
     * Copy constructor
     */
    public UrlStringBuilder(UrlStringBuilder urlBuilder) {
        super(urlBuilder.getEncoding());

        this.host = urlBuilder.host;
        this.protocol = urlBuilder.protocol;
        this.port = urlBuilder.port;
        this.context = urlBuilder.context;
        this.path.addAll(urlBuilder.path);
        for (final Map.Entry<String, List<String>> paramEntry : urlBuilder.parameters.entrySet()) {
            final String key = paramEntry.getKey();
            List<String> value = paramEntry.getValue();
            if (value != null) {
                value = new ArrayList<String>(value);
            }
            this.parameters.put(key, value);
        }
    }

    protected <T> List<T> copy(List<T> l) {
        return l == null ? null : new ArrayList<T>(l);
    }
    
    protected <T> List<T> copy(T[] t) {
        return t == null ? null : new ArrayList<T>(Arrays.asList(t));
    }
    
    /**
     * Sets a URL parameter, replacing any existing parameter with the same name.
     * 
     * @param name Parameter name, can not be null
     * @param values Values for the parameter, null is valid
     * @return this
     */
    public UrlStringBuilder setParameter(String name, String... values) {
        this.setParameter(name, values != null ? Arrays.asList(values) : null);
        return this;
    }
    
    /**
     * @see #setParameter(String, String...)
     */
    public UrlStringBuilder setParameter(String name, List<String> values) {
        Validate.notNull(name, "parameter name cannot be null");
        
        this.parameters.put(name, this.copy(values));
        return this;
    }
    
    /**
     * Adds to a URL parameter, if a parameter with the same name already exists its values are
     * added to
     * 
     * @param name Parameter name, can not be null
     * @param values Values for the parameter, null is valid
     * @return this
     */
    public UrlStringBuilder addParameter(String name, String... values) {
        this.addParameter(name, values != null ? Arrays.asList(values) : null);
        return this;
    }
    
    /**
     * @see #addParameter(String, List)
     */
    public UrlStringBuilder addParameter(String name, List<String> values) {
        Validate.notNull(name, "parameter name cannot be null");
        
        List<String> existingValues = this.parameters.get(name);
        if (existingValues == null) {
            existingValues = new ArrayList<String>(values != null ? values.size() : 0);
            this.parameters.put(name, existingValues);
        }
        if (values != null) {
            existingValues.addAll(values);
        }
        return this;
    }
    
    /**
     * Calls {@link #setParameters(String, Map)} with "" for the namespace
     * @see #setParameters(String, Map)
     */
    public UrlStringBuilder setParameters(Map<String, List<String>> parameters) {
        this.setParameters("", parameters);
        return this;
    }
    
    /**
     * Removes all existing parameters and sets the contents of the specified Map
     * as the parameters.
     * 
     * @param namespace String to prepend to each parameter name in the Map
     * @param parameters Map of parameters to set
     * @return this
     */
    public UrlStringBuilder setParameters(String namespace, Map<String, List<String>> parameters) {
        for (final String name : parameters.keySet()) {
            Validate.notNull(name, "parameter map cannot contain any null keys");
        }
        
        this.parameters.clear();
        this.addParameters(namespace, parameters);
        return this;
    }
    
    /**
     * Calls {@link #addParameters(String, Map)} with "" for the namespace
     * @see #addParameters(String, Map)
     */
    public UrlStringBuilder addParameters(Map<String, List<String>> parameters) {
        this.addParameters("", parameters);
        return this;
    }
    
    /**
     * Adds the contents of the specified Map as the parameters, the values of the Map are
     * List<String>
     * 
     * @param namespace String to prepend to each parameter name in the Map
     * @param parameters Map of parameters to set
     * @return this
     */
    public UrlStringBuilder addParameters(String namespace, Map<String, List<String>> parameters) {
        for (final String name : parameters.keySet()) {
            Validate.notNull(name, "parameter map cannot contain any null keys");
        }
        
        for (final Map.Entry<String, List<String>> newParamEntry : parameters.entrySet()) {
            final String name = newParamEntry.getKey();
            final List<String> values = this.copy(newParamEntry.getValue());
            
            this.parameters.put(namespace + name, values);
        }
        return this;
    }
    
    /**
     * Adds the contents of the specified Map as the parameters, the values of the Map are
     * String[]
     * 
     * @param namespace String to prepend to each parameter name in the Map
     * @param parameters Map of parameters to set
     * @return this
     */
    public UrlStringBuilder addParametersArray(String namespace, Map<String, String[]> parameters) {
        for (final String name : parameters.keySet()) {
            Validate.notNull(name, "parameter map cannot contain any null keys");
        }
        
        for (final Map.Entry<String, String[]> newParamEntry : parameters.entrySet()) {
            final String name = newParamEntry.getKey();
            final List<String> values = this.copy(newParamEntry.getValue());
            
            this.parameters.put(namespace + name, values);
        }
        return this;
    }
    
    /**
     * Removes any existing path elements and sets the provided elements as the path
     * 
     * @param elements Path elements to set
     * @return this
     */
    public UrlStringBuilder setPath(String... elements) {
        Validate.noNullElements(elements, "elements cannot be null");
        
        this.path.clear();
        this.addPath(elements);
        return this;
    }
    
    /**
     * Adds a single element to the path.
     * 
     * @param element The element to add
     * @return this
     */
    public UrlStringBuilder addPath(String element) {
        Validate.notNull(element, "element cannot be null");
        this.path.add(element);
        return this;
    }

    /**
     * Adds the provided elements to the path
     * 
     * @param elements Path elements to add
     * @return this
     */
    public UrlStringBuilder addPath(String... elements) {
        Validate.noNullElements(elements, "elements cannot be null");
        
        for (final String element : elements) {
            this.path.add(element);
        }
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        return new UrlStringBuilder(this);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!this.getClass().isInstance(obj)) {
            return false;
        }
        
        return this.toString().equals(obj.toString());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder url = new StringBuilder();
        
        //Add protocol://host:port if they are set
        if (this.host != null) {
            url.append(this.protocol).append("://").append(this.host);
            
            if (this.port != null) {
                url.append(":").append(this.port);
            }
        }
        
        if (this.context != null) {
            url.append("/").append(context);
        }
        
        //If no host/port/context and no path start with a /
        else if (this.path.size() == 0) {
            url.append("/");
        }

        //Add the path
        for (final String element : this.path) {
            url.append("/").append(this.encode(element));
        }
        
        //Add parameters
        if (this.parameters.size() > 0) {
            url.append("?");

            for (final Iterator<Map.Entry<String, List<String>>> paramEntryItr = this.parameters.entrySet().iterator(); paramEntryItr.hasNext(); ) {
                final Entry<String, List<String>> paramEntry = paramEntryItr.next();
                String name = paramEntry.getKey();
                final List<String> values = paramEntry.getValue();

                name = this.encode(name);
                
                if (values == null || values.size() == 0) {
                    url.append(name);
                }
                else {
                    for (final Iterator<String> valueItr = values.iterator(); valueItr.hasNext(); ) {
                        String value = valueItr.next();
                        if (value == null) {
                            value = "";
                        }

                        value = this.encode(value);
                        url.append(name).append("=").append(value);
                        
                        if (valueItr.hasNext()) {
                            url.append("&");
                        }
                    }
                }
                
                if (paramEntryItr.hasNext()) {
                    url.append("&");
                }
            }
        }

        return url.toString();
    }
}
