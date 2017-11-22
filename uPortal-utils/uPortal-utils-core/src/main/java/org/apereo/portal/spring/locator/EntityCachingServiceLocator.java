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
package org.apereo.portal.spring.locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.concurrency.IEntityCachingService;
import org.apereo.portal.utils.AbstractBeanLocator;
import org.apereo.portal.utils.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

/**
 * @deprecated code that needs an IEntityCachingService should use direct dependency injection where
 *     possible
 */
@Deprecated
public class EntityCachingServiceLocator extends AbstractBeanLocator<IEntityCachingService> {
    public static final String BEAN_NAME = "entityCachingService";

    private static final Log LOG = LogFactory.getLog(EntityCachingServiceLocator.class);
    private static AbstractBeanLocator<IEntityCachingService> locatorInstance;

    public static IEntityCachingService getEntityCachingService() {
        AbstractBeanLocator<IEntityCachingService> locator = locatorInstance;
        if (locator == null) {
            LOG.info(
                    "Looking up bean '"
                            + BEAN_NAME
                            + "' in ApplicationContext due to context not yet being initialized");
            final ApplicationContext applicationContext =
                    PortalApplicationContextLocator.getApplicationContext();
            applicationContext.getBean(EntityCachingServiceLocator.class.getName());

            locator = locatorInstance;
            if (locator == null) {
                LOG.warn(
                        "Instance of '"
                                + BEAN_NAME
                                + "' still null after portal application context has been initialized");
                return applicationContext.getBean(BEAN_NAME, IEntityCachingService.class);
            }
        }

        return locator.getInstance();
    }

    public EntityCachingServiceLocator(IEntityCachingService instance) {
        super(instance, IEntityCachingService.class);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.utils.AbstractBeanLocator#getLocator()
     */
    @Override
    protected AbstractBeanLocator<IEntityCachingService> getLocator() {
        return locatorInstance;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.utils.AbstractBeanLocator#setLocator(org.apereo.portal.utils.AbstractBeanLocator)
     */
    @Override
    protected void setLocator(AbstractBeanLocator<IEntityCachingService> locator) {
        locatorInstance = locator;
    }
}
