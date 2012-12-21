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
package org.jasig.portal.events.aggr.portletexec;

import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.PortletActionExecutionEvent;
import org.jasig.portal.events.PortletEventExecutionEvent;
import org.jasig.portal.events.PortletExecutionEvent;
import org.jasig.portal.events.PortletRenderExecutionEvent;
import org.jasig.portal.events.PortletResourceExecutionEvent;
import org.jasig.portal.events.aggr.BaseAggregationKey;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletMapping;

/**
 * Primary Key for a {@link PortletExecutionAggregation}
 * 
 * @author Eric Dalquist
 */
public interface PortletExecutionAggregationKey extends BaseAggregationKey {
    /**
     * @return The name of the tab
     */
    AggregatedPortletMapping getPortletMapping();
    
    /**
     * @return The type of portlet execution 
     */
    ExecutionType getExecutionType();
    
    /**
     * The type of execution being tracked
     */
    public enum ExecutionType {
        ALL(PortletExecutionEvent.class),
        ACTION(PortletActionExecutionEvent.class),
        EVENT(PortletEventExecutionEvent.class),
        RENDER(PortletRenderExecutionEvent.class),
        RESOURCE(PortletResourceExecutionEvent.class);
        
        private final Class<? extends PortalEvent> supportedType;
        private ExecutionType(Class<? extends PortalEvent> supportedType) {
            this.supportedType = supportedType;
        }

        public final boolean supports(Class<? extends PortalEvent> type) {
            return supportedType.isAssignableFrom(type);
        }

        public String getName() {
            return name();
        }
    }
}
