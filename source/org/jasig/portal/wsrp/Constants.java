/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.wsrp;

/**
 * Contains strings referenced in the WSRP spec.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public interface Constants {
    
    // WSRP Spec 6.1.2: User authentication
    public static final String WSRP_NONE = "wsrp:none";
    public static final String WSRP_PASSWORD = "wsrp:password";
    public static final String WSRP_CERTIFICATE = "wsrp:certificate";

    // WSRP Spec 6.1.4: User scopes
    public static final String WSRP_PER_USER = "wsrp:perUser";
    public static final String WSRP_FOR_ALL = "wsrp:forAll";

    // WSRP Spec 6.8.x: Modes
    public static final String WSRP_VIEW = "wsrp:view";
    public static final String WSRP_EDIT = "wsrp:edit";
    public static final String WSRP_HELP = "wsrp:help";
    public static final String WSRP_PREVIEW = "wsrp:preview";
    
    public static final String UP_ABOUT = "up_about";

    // WSRP Spec 6.9.x: Window states
    public static final String WSRP_NORMAL = "wsrp:normal";
    public static final String WSRP_MINIMIZED = "wsrp:minimized";
    public static final String WSRP_MAXIMIZED = "wsrp:maximized";
    public static final String WSRP_SOLO = "wsrp:solo";
    
    // WSRP Spec B.1: Standard User Categories
    public static final String WSRP_FULL = "wsrp:full";
    public static final String WSRP_STANDARD = "wsrp:standard";
    public static final String WSRP_MINIMAL = "wsrp:minimal";
}
