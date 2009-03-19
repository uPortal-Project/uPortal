/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlets.registerportal;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Collects data about the portal
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalDataCollator {
    /**
     * Implementation gets all possible data keys
     */
    public Set<String> getPossibleDataKeys();

    /**
     * Implementation gets all data the implementation knows how to collect
     */
    public Map<String, Properties> getCollectedData();

    /**
     * Implementation gets data for only the keys specified in the keysToCollect Set
     */
    public Map<String, Properties> getCollectedData(Set<String> keysToCollect);
}
