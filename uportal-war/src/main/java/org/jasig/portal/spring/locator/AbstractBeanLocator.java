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

package org.jasig.portal.spring.locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Provides base functionality for a static reference bean locator. Used by legacy code that
 * is not managed within spring, avoids direct use of PortalApplicationContextLocator by
 * client code and in the case of an already created ApplicationConext uses the bean refernce
 * injected by the context.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractBeanLocator<T> implements DisposableBean, InitializingBean {
    protected final Log logger = LogFactory.getLog(AbstractBeanLocator.class);

    private final T instance;

    public AbstractBeanLocator(T instance, Class<T> type) {
        Assert.notNull(instance, "instance must not be null");
        Assert.notNull(type, "type must not be null");
        Assert.isInstanceOf(type, instance, instance + " must implement " + type);

        this.instance = instance;
    }

    protected abstract void setLocator(AbstractBeanLocator<T> locator);

    protected abstract AbstractBeanLocator<T> getLocator();

    public final T getInstance() {
        return this.instance;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public final void afterPropertiesSet() throws Exception {
        if (this.getLocator() != null) {
            this.logger.warn("Static " + this.getClass().getName()
                    + " reference has already been set and setInstance is being called");
        }
        this.setLocator(this);
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    @Override
    public final void destroy() throws Exception {
        if (this.getLocator() == null) {
            this.logger.warn("Static " + this.getClass().getName()
                    + " reference is already null and destroy is being called");
        }
        this.setLocator(null);
    }
}
