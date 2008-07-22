/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlets.flow;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.mvc.portlet.AbstractFlowHandler;

/**
 * Simple Flow handler just returns a specified flowId, defaults to 'view'.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ParamaterizableFlowHandler extends AbstractFlowHandler {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private String flowId = "view";
    private String redirectOnEnd = null;
    
    /**
     * @return the redirectOnEnd
     */
    public String getRedirectOnEnd() {
        return this.redirectOnEnd;
    }
    /**
     * @param redirectOnEnd The location to redirect to at the end of the flow
     */
    public void setRedirectOnEnd(String redirectOnEnd) {
        this.redirectOnEnd = redirectOnEnd;
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
        if (this.redirectOnEnd != null) {
            final String redirectPath = request.getContextPath() + this.redirectOnEnd;
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
