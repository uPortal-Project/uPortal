/**
 * Copyright © 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
