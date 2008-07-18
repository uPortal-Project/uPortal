/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlets.flow;

import org.springframework.webflow.mvc.portlet.AbstractFlowHandler;

/**
 * Simple Flow handler just returns a specified flowId, defaults to 'view'.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ParamaterizableFlowHandler extends AbstractFlowHandler {
    private String flowId = "view";
    
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
}
