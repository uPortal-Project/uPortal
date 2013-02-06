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
package org.jasig.portal.portlets.popular;


public final class PortletUsage implements Comparable<PortletUsage> {
    
    private final long id;
    private final String portletFName;
    private final String portletTitle;
    private final String portletDescription;
    private int count = 0;
    
    public PortletUsage(long id, String portletFName, String portletTitle, String portletDescription) {

        // Assertions
        if (portletFName == null) {
            String msg = "Argument 'portletFName' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        if (portletTitle == null) {
            String msg = "Argument 'portletTitle' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        // NB:  'portletDescription' actually can be null

        this.id = id;
        this.portletFName = portletFName;
        this.portletTitle = portletTitle;
        if (portletDescription != null) {
            this.portletDescription = portletDescription;
        }
        else {
            this.portletDescription = "[no description available]";
        }
    }

    public long getId() {
        return id;
    }

    public String getPortletFName() {
        return portletFName;
    }

    public String getPortletTitle() {
        return portletTitle;
    }

    public String getPortletDescription() {
        return portletDescription;
    }

    public int getCount() {
        return count;
    }
    
    void incrementCount(int count) {
        this.count += count;
    }

    @Override
    public int compareTo(PortletUsage tuple) {
        // Natural order for these is count
        return Integer.valueOf(count).compareTo(tuple.getCount());
    }
}