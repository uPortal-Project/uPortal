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

package org.jasig.portal.security.xslt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides a Spring locating facade in front of an actual {@link IXalanGroupMembershipHelper} since the Xalan
 * elements can only instantiate classes directly.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class XalanGroupMembershipHelper {
    private static IXalanGroupMembershipHelper groupMembershipHelper;

    @Autowired
    public void setGroupMembershipHelper(IXalanGroupMembershipHelper groupMembershipHelper) {
        XalanGroupMembershipHelper.groupMembershipHelper = groupMembershipHelper;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.security.xslt.IXalanGroupMembershipHelper#isChannelDeepMemberOf(java.lang.String, java.lang.String)
     */
    public static boolean isChannelDeepMemberOf(String fname, String groupKey) {
        return groupMembershipHelper.isChannelDeepMemberOf(fname, groupKey);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.security.xslt.IXalanGroupMembershipHelper#isUserDeepMemberOf(java.lang.String, java.lang.String)
     */
    public static boolean isUserDeepMemberOf(String userName, String groupKey) {
        return groupMembershipHelper.isUserDeepMemberOf(userName, groupKey);
    }

    public static boolean isUserDeepMemberOfGroupName(String userName, String groupName) {
        return groupMembershipHelper.isUserDeepMemberOfGroupName(userName, groupName);
    }
}
