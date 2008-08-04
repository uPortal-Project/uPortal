/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.spring.web.context.support;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * Wraps a {@link Scope} to provide functionality when the wrapped scope can't function
 * due to an illegal state (no current session, request, ...)
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class FailsafeSingletonScope implements Scope, DisposableBean {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private final Map<String, InstanceHolder> instances = new HashMap<String, InstanceHolder>();
    private final Scope delegateScope;
    
    public FailsafeSingletonScope(Scope delegateScope) {
        this.delegateScope = delegateScope;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    public void destroy() throws Exception {
        for (final InstanceHolder instanceHolder : this.instances.values()) {
            if (instanceHolder.destructionCallback != null) {
                try {
                    instanceHolder.destructionCallback.run();
                }
                catch (Exception e) {
                    this.logger.warn("Destruction callback for bean named '" + instanceHolder.name + "' failed.", e);
                }
            }
        }
        
        this.instances.clear();
    }

    /**
     * @see org.springframework.web.context.request.SessionScope#get(java.lang.String, org.springframework.beans.factory.ObjectFactory)
     */
    public Object get(String name, ObjectFactory objectFactory) {
        try {
            return this.delegateScope.get(name, objectFactory);
        }
        catch (IllegalStateException ise) {
            synchronized (this.instances) {
                InstanceHolder instanceHolder = this.instances.get(name);
                if (instanceHolder == null) {
                    if (this.logger.isInfoEnabled()) {
                        this.logger.info("Creating singleton instance for bean '" + name + "'");
                    }

                    //Add to instances map before creating to ensure if a destruction callback is added it is caught
                    instanceHolder = new InstanceHolder(name);
                    this.instances.put(name, instanceHolder);

                    instanceHolder.instance = objectFactory.getObject();
                }
                else if (this.logger.isInfoEnabled()) {
                    this.logger.info("Using existing singleton instance for bean '" + name + "'");
                }

                return instanceHolder.instance;
            }
        }
    }

    /**
     * @see org.springframework.web.context.request.SessionScope#getConversationId()
     */
    public String getConversationId() {
        try {
            return this.delegateScope.getConversationId();
        }
        catch (IllegalStateException ise) {
            return "NO_SESSION_SINGLETON";
        }
    }

    /**
     * @see org.springframework.web.context.request.AbstractRequestAttributesScope#registerDestructionCallback(java.lang.String, java.lang.Runnable)
     */
    public void registerDestructionCallback(String name, Runnable callback) {
        try {
            this.delegateScope.registerDestructionCallback(name, callback);
        }
        catch (IllegalStateException ise) {
            final InstanceHolder instanceHolder;
            synchronized (this.instances) {
                instanceHolder = this.instances.get(name);
            }

            if (instanceHolder != null) {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Adding destruction callback singleton for bean '" + name + "'");
                }
                
                instanceHolder.destructionCallback = callback;
            }
            else if (this.logger.isInfoEnabled()) {
                this.logger.info("Ignoring destruction callback for singleton bean '" + name + "' because there currently is no instance");
            }

        }
    }

    /**
     * @see org.springframework.web.context.request.SessionScope#remove(java.lang.String)
     */
    public Object remove(String name) {
        try {
            return this.delegateScope.remove(name);
        }
        catch (IllegalStateException ise) {
            final InstanceHolder instanceHolder;
            synchronized (this.instances) {
                instanceHolder = this.instances.remove(name);
            }
            
            if (instanceHolder != null) {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Removing singleton bean '" + name + "'");
                }
                
                return instanceHolder.instance;
            }
            
            return null;
        }
    }
    
    /**
     * Holder class for singleton instances
     */
    private static class InstanceHolder {
        public final String name;
        public Object instance;
        public Runnable destructionCallback;

        public InstanceHolder(String name) {
            this.name = name;
        }

        /**
         * @see java.lang.Object#equals(Object)
         */
        @Override
        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }
            if (!(object instanceof InstanceHolder)) {
                return false;
            }
            InstanceHolder rhs = (InstanceHolder) object;
            return new EqualsBuilder()
                .append(this.name, rhs.name)
                .isEquals();
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return new HashCodeBuilder(217891979, 1307635269)
                .append(this.name)
                .toHashCode();
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", this.name)
                .append("instance", this.instance)
                .append("destructionCallback", this.destructionCallback)
                .toString();
        }
    }
}
