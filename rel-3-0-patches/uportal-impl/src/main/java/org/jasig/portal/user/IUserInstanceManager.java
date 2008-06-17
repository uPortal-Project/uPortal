package org.jasig.portal.user;

/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.PortalException;

/**
 * Determines which {@link IUserInstance} object to use for a given request.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IUserInstanceManager {
    /**
     * Returns the {@link IUserInstance} object that is associated with the given request.
     * 
     * @param request Incoming HttpServletRequest
     * @return IUserInstance object associated with the given request
     */
    public IUserInstance getUserInstance(HttpServletRequest request) throws PortalException;
}
