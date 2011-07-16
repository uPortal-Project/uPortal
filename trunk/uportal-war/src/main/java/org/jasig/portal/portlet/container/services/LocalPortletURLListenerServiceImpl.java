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

package org.jasig.portal.portlet.container.services;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletURLGenerationListener;

import org.apache.pluto.container.PortletURLListenerService;
import org.apache.pluto.container.om.portlet.Listener;
import org.apache.pluto.container.om.portlet.PortletApplicationDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 * @version $Revision$
 */
@Service("portletURLListenerService")
public class LocalPortletURLListenerServiceImpl implements
		PortletURLListenerService {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<PortletURLGenerationListener> getPortletURLGenerationListeners(PortletApplicationDefinition app)
    {
        List<PortletURLGenerationListener> listeners = new ArrayList<PortletURLGenerationListener>();
        //this list is needed for the classnames
        List<? extends Listener> portletURLFilterList = app.getListeners();
        //Iterate over the classnames and for each entry in the list the filter..URL is called.
        if (portletURLFilterList != null){
            for (Listener listener : portletURLFilterList) {
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                Class<? extends Object> clazz;
                try {
                    clazz = loader.loadClass(listener.getListenerClass() );
                    if (clazz != null){
                        listeners.add((PortletURLGenerationListener)clazz.newInstance());
                    }
                } catch (ClassNotFoundException e) {
                    logger.error("class not found for " + listener.getListenerClass(), e);
                } catch (InstantiationException e) {
                    logger.error("instantiation failed for " + listener.getListenerClass(), e);
                } catch (IllegalAccessException e) {
                    logger.error("IllegalAccessException for " + listener.getListenerClass(), e);
                }
            }
        }
        return listeners;
    }

}
