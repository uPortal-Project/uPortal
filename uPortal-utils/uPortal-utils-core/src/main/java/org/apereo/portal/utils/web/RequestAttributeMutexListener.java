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
package org.apereo.portal.utils.web;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import org.apereo.portal.utils.SerializableObject;

/**
 * Servlet 2.3 HttpSessionListener that automatically exposes the request attribute mutex when an
 * ServletRequest gets created. To be registered as a listener in <code>web.xml</code>.
 *
 * <p>The request attribute mutex is guaranteed to be the same object during the entire lifetime of
 * the request, available under the key defined by the <code>REQUEST_MUTEX_ATTRIBUTE</code>
 * constant. It serves as a safe reference to synchronize on for locking on the current request
 * attributes.
 *
 */
public class RequestAttributeMutexListener implements ServletRequestListener {

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestListener#requestInitialized(javax.servlet.ServletRequestEvent)
     */
    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        sre.getServletRequest()
                .setAttribute(PortalWebUtils.REQUEST_MUTEX_ATTRIBUTE, new SerializableObject());
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestListener#requestDestroyed(javax.servlet.ServletRequestEvent)
     */
    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        sre.getServletRequest().removeAttribute(PortalWebUtils.REQUEST_MUTEX_ATTRIBUTE);
    }
}
