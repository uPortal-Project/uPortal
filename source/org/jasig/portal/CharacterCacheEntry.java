/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.util.Vector;

/**
 * This class is used by UserInstance.
 * @author <a href="mailto:pkharchenko@unicon.net">Peter Kharchenko</a>
 * @version $Revision$
 */
public class CharacterCacheEntry {
    Vector systemBuffers;
    Vector channelIds;
    public CharacterCacheEntry() {
        systemBuffers = null;
        channelIds = null;
    }
}
