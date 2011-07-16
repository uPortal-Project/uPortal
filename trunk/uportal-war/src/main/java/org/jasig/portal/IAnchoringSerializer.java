/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
