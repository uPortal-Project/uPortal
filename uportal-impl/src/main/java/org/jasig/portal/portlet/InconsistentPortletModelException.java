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

package org.jasig.portal.portlet;

import org.jasig.portal.PortalException;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Indicates that there was a problem loading or reading part of the portlet's object model
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class InconsistentPortletModelException extends PortalException {
    private static final long serialVersionUID = 1L;
    
    private final IPortletWindowId portletWindowId; 

    public InconsistentPortletModelException(String msg, IPortletWindowId portletWindowId) {
        super(msg, false, true);
        this.portletWindowId = portletWindowId;
    }
    
    public InconsistentPortletModelException(String msg, IPortletWindowId portletWindowId, Throwable cause) {
        super(msg, cause, false, true);
        this.portletWindowId = portletWindowId;
    }

    /**
     * @return the portletWindowId
     */
    public IPortletWindowId getPortletWindowId() {
        return this.portletWindowId;
    }
}
