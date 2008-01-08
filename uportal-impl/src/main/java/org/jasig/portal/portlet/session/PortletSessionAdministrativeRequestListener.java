/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.portlet.session;

import java.util.Enumeration;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;

import org.apache.pluto.spi.optional.AdministrativeRequestListener;

/**
 * Provides access to actions involving the PortletSession. Refer to {@link SessionAction} for supported
 * actions.
 * 
 * @author Eric Dalquist 
 * @version $Revision$
 */
public class PortletSessionAdministrativeRequestListener implements AdministrativeRequestListener {
    public static final String ACTION = "org.jasig.portal.portlet.session.ACTION";
    public static final String ARGUMENTS = "org.jasig.portal.portlet.session.ARGUMENTS";
    public static final String SCOPE = "org.jasig.portal.portlet.session.SCOPE";
    
    public enum SessionAction {
        /**
         * Removes all attributes from the session for the specified scope
         */
        CLEAR (false, 0), //No arguments
        
        /**
         * Stores an attribute in the session for the specified scope
         */
        SET_ATTRIBUTE (true, 2); // arg[0]=Attribute Name, arg[1]=Attribute Value
        
        private final boolean requiresCreation;
        private final int argumentCount;
        
        private SessionAction(boolean requiresCreation, int argumentCount) {
            this.requiresCreation = requiresCreation;
            this.argumentCount = argumentCount;
        }

        public boolean isRequiresCreation() {
            return this.requiresCreation;
        }

        public int getArgumentCount() {
            return this.argumentCount;
        }
    }

    /**
     * @see org.apache.pluto.spi.optional.AdministrativeRequestListener#administer(javax.portlet.PortletRequest, javax.portlet.PortletResponse)
     */
    @SuppressWarnings("unchecked")
    public void administer(PortletRequest request, PortletResponse response) {
        final SessionAction action = this.getAction(request);
        final Object[] arguments = this.getArguments(request);
        final int scope = this.getScope(request);
        
        //Check the argument count
        final int argumentCount = arguments != null ? arguments.length : 0;
        if (argumentCount != action.getArgumentCount()) {
           throw new IllegalArgumentException("SessionAction " + action + " requires " + action.getArgumentCount() + " arguments but " + argumentCount + " arguments were provided.");
        }
        
        //Get the session according to the action
        final PortletSession portletSession = request.getPortletSession(action.isRequiresCreation());
        
        switch (action) {
            case CLEAR: {
                if (portletSession != null) {
                    for (final Enumeration<String> attributeNames = (Enumeration<String>) portletSession.getAttributeNames(scope); attributeNames.hasMoreElements();) {
                        final String attributeName = attributeNames.nextElement();
                        portletSession.removeAttribute(attributeName, scope);
                    }
                }

            }
            break;
            case SET_ATTRIBUTE: {
                final String attributeName = (String) arguments[0];
                final Object value = arguments[1];
                portletSession.setAttribute(attributeName, value, scope);
            }
            break;
        }
    }

    protected SessionAction getAction(PortletRequest request) {
        return (SessionAction)request.getAttribute(ACTION);
    }

    protected Object[] getArguments(PortletRequest request) {
        return (Object[])request.getAttribute(ARGUMENTS);
    }

    protected int getScope(PortletRequest request) {
        final Integer scope = (Integer)request.getAttribute(SCOPE);
        if (scope != null) {
            return scope;
        }

        return PortletSession.PORTLET_SCOPE;
    }
}
