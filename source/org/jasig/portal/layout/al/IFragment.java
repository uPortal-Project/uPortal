/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import org.jasig.portal.layout.al.common.ILayoutSubtree;

/**
 * An interface defining a single fragment
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @version $Revision$
 */
public interface IFragment extends ILayoutSubtree {
    /**
     * Obtain id of a given fragment
     * @return
     */
    public IFragmentId getFragmentId();
}
