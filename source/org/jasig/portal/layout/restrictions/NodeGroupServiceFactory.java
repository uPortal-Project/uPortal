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
 *
 */

package org.jasig.portal.layout.restrictions;

import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IGroupService;
import org.jasig.portal.groups.IGroupServiceFactory;
import org.jasig.portal.services.LogService;

/**
 * Creates the UserLayout node implemetation of <code>IGroupService</code>.
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public class NodeGroupServiceFactory implements IGroupServiceFactory {
/**
 * NodeGroupServiceFactory constructor.
 */
 public NodeGroupServiceFactory() {
        super();
 }
/**
 * Return an instance of the service implementation.
 * @return org.jasig.portal.groups.IGroupService
 * @exception org.jasig.portal.groups.GroupsException
 */
 public IGroupService newGroupService() throws GroupsException {
    try {
        return NodeGroupService.getInstance();
    } catch ( GroupsException ge ) {
        LogService.log (LogService.ERROR, "NodeGroupServiceFactory.newGroupService(): " + ge);
        throw new GroupsException(ge.getMessage());
      }
 }

 /**
 * Return an instance of the service implementation. (Static version of the newGroupInstance)
 * @return org.jasig.portal.groups.IGroupService
 * @exception org.jasig.portal.groups.GroupsException
 */
 public static IGroupService newInstance() throws GroupsException {
    try {
        return NodeGroupService.getInstance();
    } catch ( GroupsException ge ) {
        LogService.log (LogService.ERROR, "NodeGroupServiceFactory.getGroupService(): " + ge);
        throw new GroupsException(ge.getMessage());
      }
 }

}
