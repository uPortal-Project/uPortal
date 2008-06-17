/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * An anchoring serializer allows an external client to control
 * its anchor identifier.  When set (when it isn't null), it should
 * append this anchor identifier to its URLs so that when a user
 * clicks on one of the URLs, the user's browser will jump
 * down to the anchor.  The original use of this interface will be
 * to make it possible to add anchors to channel links and forms
 * where the anchor is set to the channel's subscribtion ID.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public interface IAnchoringSerializer {

    /**
     * Signify that the serializer should begin to
     * append the anchor ID to URLs of its choosing.
     * @param anchorId the anchor identifier
     */
    public void startAnchoring(String anchorId);
    
    /**
     * Signify that anchoring is no longer desired by
     * the serializer.
     */
    public void stopAnchoring();
    
}
