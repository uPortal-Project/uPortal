package org.jasig.portal.events;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.security.IPerson;

/**
 * Publishing authentication related events
 * 
 * @author Eric Dalquist
 */
public interface IPortalAuthEventFactory {

    void publishLoginEvent(HttpServletRequest request, Object source, IPerson person);

    void publishLogoutEvent(HttpServletRequest request, Object source, IPerson person);

}