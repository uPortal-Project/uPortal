/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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



package org.jasig.portal.layout;

import java.util.Set;
import org.jasig.portal.PortalException;

/**
 * An aggregated-layout specific extension of the user layout interface
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version 1.1
 */
public interface IAggregatedLayout extends IUserLayout {

    // the tag names constants
    public static final String LAYOUT = "layout";
    public static final String FRAGMENT = "fragment";
    public static final String FOLDER = "folder";
    public static final String CHANNEL = "channel";
    public static final String PARAMETER = "parameter";
    public static final String RESTRICTION = "restriction";
    // The names for marking nodes
    public static final String ADD_TARGET = "add_target";
    public static final String MOVE_TARGET = "move_target";


    /**
     * Returns a list of fragment Ids existing in the layout.
     *
     * @return a <code>Set</code> of <code>String</code> fragment Ids.
     * @exception PortalException if an error occurs
     */
    public Set getFragmentIds() throws PortalException;

    /**
     * Returns an fragment Id for a given node.
     * Returns null if the node is not part of any fragments.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> fragment Id
     * @exception PortalException if an error occurs
     */
    public String getFragmentId(String nodeId) throws PortalException;

    /**
     * Returns an fragment root Id for a given fragment.
     *
     * @param fragmentId a <code>String</code> value
     * @return a <code>String</code> fragment root Id
     * @exception PortalException if an error occurs
     */
    public String getFragmentRootId(String fragmentId) throws PortalException;

}
