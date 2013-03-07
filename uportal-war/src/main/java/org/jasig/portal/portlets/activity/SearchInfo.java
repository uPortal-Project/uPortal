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
package org.jasig.portal.portlets.activity;

import org.apache.commons.lang.StringUtils;

/**
 * @author Chris Waymire (chris@waymire.net)
 */
public class SearchInfo implements Comparable<SearchInfo> {
    private final String searchTerm;
    private int count;

    public SearchInfo(String searchTerm,int count)
    {
        this.searchTerm = StringUtils.trim(searchTerm);
        this.count = count;
    }

    public String getSearchTerm()
    {
        return searchTerm;
    }

    public int getCount()
    {
        return count;
    }

    public void incrementCount(int count)
    {
        this.count += count;
    }

    @Override
    public int compareTo(SearchInfo other)
    {
        return Integer.valueOf(count).compareTo(other.getCount());
    }
}
