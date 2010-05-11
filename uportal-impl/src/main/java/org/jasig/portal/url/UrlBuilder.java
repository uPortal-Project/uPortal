/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
public class UrlBuilder implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private final String encoding;
    private final String protocol;
    private final String host;
    private final Integer port;
    private final List<String> path = new LinkedList<String>();
    private final Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
    
    /**
     * Creates a URL with no host, protocol or port. The URL will start with a /
     * 
     * @param encoding The encoding to use for parameters
     */
    public UrlBuilder(String encoding) {
        Validate.notNull(encoding, "encoding can not be null");
        this.checkEncoding(encoding);

        this.encoding = encoding;
        this.protocol = null;
        this.host = null;
        this.port = null;
    }

    /**
     * Creates a URL with a protocol and host, no port will be specified
     * 
     * @param encoding The encoding to use for parameters
     * @param protocol The protocol for the URL, ex: http
     * @param host The host for the URL
     */
    public UrlBuilder(String encoding, String protocol, String host) {
        this(encoding, protocol, host, null);
    }

    /**
     * Creates a URL with a protocol, host and port
     * 
     * @param encoding The encoding to use for parameters
     * @param protocol The protocol for the URL, ex: http
     * @param host The host for the URL
     * @param port The port to use for the URL
     */
    public UrlBuilder(String encoding, String protocol, String host, Integer port) {
        Validate.notNull(encoding, "encoding can not be null");
        Validate.notNull(protocol, "protocol can not be null");
        Validate.notNull(host, "host can not be null");
        this.checkEncoding(encoding);

        this.encoding = encoding;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }
    
    /**
     * Copy constructor
     */
    public UrlBuilder(UrlBuilder urlBuilder) {
        Validate.notNull(urlBuilder, "urlBuilder can not be null");

        this.encoding = urlBuilder.encoding;
        this.host = urlBuilder.host;
        this.protocol = urlBuilder.protocol;
        this.port = urlBuilder.port;
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

    protected void checkEncoding(String encoding) {
        try {
            URLEncoder.encode("", encoding);
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Encoding '" + encoding + "' is not supported", e);
        }
    }
    
    protected <T> List<T> copy(List<T> l) {
        return l == null ? null : new ArrayList<T>(l);
    }
    
    /**
     * Sets a URL parameter, replacing any existing parameter with the same name.
     * 
     * @param name Parameter name, can not be null
     * @param values Values for the parameter, null is valid
     * @return this
     */
    public UrlBuilder setParameter(String name, String... values) {
        this.setParameter(name, values != null ? Arrays.asList(values) : null);
        return this;
    }
    
    /**
     * @see #setParameter(String, String...)
     */
    public UrlBuilder setParameter(String name, List<String> values) {
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
    public UrlBuilder addParameter(String name, String... values) {
        this.addParameter(name, values != null ? Arrays.asList(values) : null);
        return this;
    }
    
    /**
     * @see #addParameter(String, List)
     */
    public UrlBuilder addParameter(String name, List<String> values) {
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
    public UrlBuilder setParameters(Map<String, List<String>> parameters) {
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
    public UrlBuilder setParameters(String namespace, Map<String, List<String>> parameters) {
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
    public UrlBuilder addParameters(Map<String, List<String>> parameters) {
        this.addParameters("", parameters);
        return this;
    }
    
    /**
     * Adds the contents of the specified Map as the parameters.
     * 
     * @param namespace String to prepend to each parameter name in the Map
     * @param parameters Map of parameters to set
     * @return this
     */
    public UrlBuilder addParameters(String namespace, Map<String, List<String>> parameters) {
        for (final String name : parameters.keySet()) {
            Validate.notNull(name, "parameter map cannot contain any null keys");
        }
        
        for (final Map.Entry<String, List<String>> newParamEntry : parameters.entrySet()) {
            final String name = newParamEntry.getKey();
            List<String> values = this.copy(newParamEntry.getValue());
            
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
    public UrlBuilder setPath(String... elements) {
        Validate.noNullElements(elements, "elements cannot be null");
        
        this.path.clear();
        this.addPath(elements);
        return this;
    }

    /**
     * Adds the provided elements to the path
     * 
     * @param elements Path elements to add
     * @return this
     */
    public UrlBuilder addPath(String... elements) {
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
        return new UrlBuilder(this);
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
        //If no host/port and no path start with a /
        else if (this.path.size() == 0) {
            url.append("/");
        }

        //Add the path
        for (final String element : this.path) {
            url.append("/").append(element);
        }
        
        //Add parameters
        if (this.parameters.size() > 0) {
            url.append("?");

            for (final Iterator<Map.Entry<String, List<String>>> paramEntryItr = this.parameters.entrySet().iterator(); paramEntryItr.hasNext(); ) {
                final Entry<String, List<String>> paramEntry = paramEntryItr.next();
                String name = paramEntry.getKey();
                final List<String> values = paramEntry.getValue();

                try {
                    name = URLEncoder.encode(name, this.encoding);
                }
                catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Encoding '" + this.encoding + "' is not supported", e);
                }

                if (values == null || values.size() == 0) {
                    url.append(name).append("=");
                }
                else {
                    for (final Iterator<String> valueItr = values.iterator(); valueItr.hasNext(); ) {
                        String value = valueItr.next();
                        if (value == null) {
                            value = "";
                        }

                        try {
                            value = URLEncoder.encode(value, this.encoding);
                        }
                        catch (UnsupportedEncodingException e) {
                            throw new RuntimeException("Encoding '" + this.encoding + "' is not supported", e);
                        }

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
