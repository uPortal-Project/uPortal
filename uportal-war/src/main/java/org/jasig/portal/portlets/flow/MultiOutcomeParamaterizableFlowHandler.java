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

package org.jasig.portal.portlets.flow;

import java.io.IOException;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.mvc.portlet.AbstractFlowHandler;

/**
 * MultiOutcomeParamaterizableFlowHandler provides an alternative to 
 * ParamaterizableFlowHandler for flows that require different handling on
 * different outcomes.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class MultiOutcomeParamaterizableFlowHandler extends AbstractFlowHandler {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private String flowId = "view";
    private Map<String,String> redirectOnEnds;
    
    /**
     * @return the redirectOnEnd
     */
    public Map<String,String> getRedirectOnEnds() {
        return this.redirectOnEnds;
    }
    /**
     * @param redirectOnEnd The location to redirect to at the end of the flow
     */
    public void setRedirectOnEnds(Map<String,String> redirectOnEnds) {
        this.redirectOnEnds = redirectOnEnds;
    }

    /**
     * @param flowId the flowId to set
     */
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    /* (non-Javadoc)
     * @see org.springframework.webflow.mvc.portlet.AbstractFlowHandler#getFlowId()
     */
    @Override
    public String getFlowId() {
        return this.flowId;
    }

    /* (non-Javadoc)
     * @see org.springframework.webflow.mvc.portlet.AbstractFlowHandler#handleExecutionOutcome(org.springframework.webflow.execution.FlowExecutionOutcome, javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    @Override
    public boolean handleExecutionOutcome(FlowExecutionOutcome outcome, ActionRequest request, ActionResponse response) {
        if (this.redirectOnEnds != null && this.redirectOnEnds.containsKey(outcome.getId())) {
            final String redirectPath = request.getContextPath() + this.redirectOnEnds.get(outcome.getId());
            try {
                response.sendRedirect(redirectPath);
                return true;
            }
            catch (IOException e) {
                this.logger.warn("Failed to send flow-end redirect to '" + redirectPath +"'");
            }
        }
        
        return false;
    }
        
}
