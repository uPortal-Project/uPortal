/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.security.xslt;

import org.jasig.portal.events.aggr.tabs.AggregatedTabLookupDao;
import org.jasig.portal.events.aggr.tabs.AggregatedTabMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class contains convenience methods for resolving the tab ownership information for a layout node
 * 
 * @author Eric Dalquist
 */
@Service
public class XalanLayoutElementTabOwnerHelper {
    
    private static AggregatedTabLookupDao aggregatedTabLookupDao;

    @Autowired    
    public void setAggregatedTabLookupDao(AggregatedTabLookupDao aggregatedTabLookupDao) {
        XalanLayoutElementTabOwnerHelper.aggregatedTabLookupDao = aggregatedTabLookupDao;
    }

    public static String getFragementOwner(String layoutNodeId) {
        final AggregatedTabMapping mappedTabForLayoutId = aggregatedTabLookupDao.getMappedTabForLayoutId(layoutNodeId);
        if (mappedTabForLayoutId == null) {
            return AggregatedTabMapping.MISSING_TAB_FRAGMENT_NAME;
        }
        return mappedTabForLayoutId.getFragmentName();
    }
}
