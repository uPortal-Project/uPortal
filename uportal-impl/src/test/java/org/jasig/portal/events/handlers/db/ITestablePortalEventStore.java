/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.events.handlers.db;

import java.util.Map;
import java.util.Set;

import org.jasig.portal.security.IPerson;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface ITestablePortalEventStore extends IPortalEventStore {
    public Map<IPerson, Set<String>> getPersonGroups();

    public void setPersonGroups(Map<IPerson, Set<String>> personGroups);
    
    public void addPersonGroups(IPerson person, Set<String> groups);
}
