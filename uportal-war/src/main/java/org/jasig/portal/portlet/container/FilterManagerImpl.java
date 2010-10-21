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

/**
 * 
 */
package org.jasig.portal.portlet.container;

import java.io.IOException;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventPortlet;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceServingPortlet;

import org.apache.pluto.container.FilterManager;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.om.portlet.Filter;
import org.apache.pluto.container.om.portlet.FilterMapping;
import org.apache.pluto.container.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.container.om.portlet.PortletDefinition;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class FilterManagerImpl implements FilterManager {
    private FilterChainImpl filterchain;
    private PortletApplicationDefinition portletApp;
    private String portletName;
    private String lifeCycle;

    public FilterManagerImpl(PortletWindow portletWindow, String lifeCycle) {
        final PortletDefinition pd = portletWindow.getPortletDefinition();
        this.portletApp = pd.getApplication();
        this.portletName = pd.getPortletName();
        this.lifeCycle = lifeCycle;
        filterchain = new FilterChainImpl(lifeCycle);
        initFilterChain();
    }

    private void initFilterChain() {
        List<? extends FilterMapping> filterMappingList = portletApp.getFilterMappings();
        if (filterMappingList != null) {
            for (FilterMapping filterMapping : filterMappingList) {
                if (isFilter(filterMapping, portletName)) {
                    //the filter is specified for the portlet, check the filter for the lifecycle
                    List<? extends Filter> filterList = portletApp.getFilters();
                    for (Filter filter : filterList) {
                        //search for the filter in the filter
                        if (filter.getFilterName().equals(filterMapping.getFilterName())) {
                            //check the lifecycle
                            if (isLifeCycle(filter, lifeCycle)) {
                                //the filter match to the portlet and has the specified lifecycle -> add to chain
                                filterchain.addFilter(filter);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @see org.apache.pluto.container.FilterManager#processFilter(javax.portlet.EventRequest, javax.portlet.EventResponse, javax.portlet.EventPortlet, javax.portlet.PortletContext)
     */
    public void processFilter(EventRequest req, EventResponse res, EventPortlet eventPortlet,
            PortletContext portletContext) throws PortletException, IOException {
        filterchain.processFilter(req, res, eventPortlet, portletContext);
    }

    /**
     * @see org.apache.pluto.container.FilterManager#processFilter(javax.portlet.ResourceRequest, javax.portlet.ResourceResponse, javax.portlet.ResourceServingPortlet, javax.portlet.PortletContext)
     */
    public void processFilter(ResourceRequest req, ResourceResponse res, ResourceServingPortlet resourceServingPortlet,
            PortletContext portletContext) throws PortletException, IOException {
        filterchain.processFilter(req, res, resourceServingPortlet, portletContext);
    }

    /**
     * @see org.apache.pluto.container.FilterManager#processFilter(javax.portlet.RenderRequest, javax.portlet.RenderResponse, javax.portlet.Portlet, javax.portlet.PortletContext)
     */
    public void processFilter(RenderRequest req, RenderResponse res, Portlet portlet, PortletContext portletContext)
            throws PortletException, IOException {
        filterchain.processFilter(req, res, portlet, portletContext);
    }

    /**
     * @see org.apache.pluto.container.FilterManager#processFilter(javax.portlet.ActionRequest, javax.portlet.ActionResponse, javax.portlet.Portlet, javax.portlet.PortletContext)
     */
    public void processFilter(ActionRequest req, ActionResponse res, Portlet portlet, PortletContext portletContext)
            throws PortletException, IOException {
        filterchain.processFilter(req, res, portlet, portletContext);
    }

    private boolean isLifeCycle(Filter filter, String lifeCycle) {
        List<String> lifeCyclesList = filter.getLifecycles();
        for (String string : lifeCyclesList) {
            if (string.equals(lifeCycle))
                return true;
        }
        return false;
    }

    private boolean isFilter(FilterMapping filterMapping, String portletName) {
        List<String> portletNamesList = filterMapping.getPortletNames();
        for (String portletNameFromFilterList : portletNamesList) {
            if (portletNameFromFilterList.endsWith("*")) {
                if (portletNameFromFilterList.length() == 1) {
                    //if name contains only *
                    return true;
                }
                portletNameFromFilterList = portletNameFromFilterList.substring(0,
                        portletNameFromFilterList.length() - 1);
                if (portletName.length() >= portletNameFromFilterList.length()) {
                    if (portletName.substring(0, portletNameFromFilterList.length()).equals(portletNameFromFilterList)) {
                        return true;
                    }
                }
            }
            else if (portletNameFromFilterList.equals(portletName))
                return true;
        }
        return false;
    }

}
