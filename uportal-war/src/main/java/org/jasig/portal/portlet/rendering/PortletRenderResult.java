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

package org.jasig.portal.portlet.rendering;


/**
 * The result of rendering a portlet
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletRenderResult {
    private final String title;
    private final long renderTime;
    private final int newItemCount;

    public PortletRenderResult(String title, int newItemCount, long renderTime) {
        this.title = title;
        this.renderTime = renderTime;
        this.newItemCount = newItemCount;
    }

    /**
     * @return The title set by the portlet, null if none was set
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return The time it took the portlet to render.
     */
    public long getRenderTime() {
        return this.renderTime;
    }
    
    /**
     * @return The number of new items reported by the portlet for the current user
     */
    public int getNewItemCount() {
        return this.newItemCount;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.renderTime ^ (this.renderTime >>> 32));
        result = prime * result + ((this.title == null) ? 0 : this.title.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PortletRenderResult other = (PortletRenderResult) obj;
        if (this.renderTime != other.renderTime) {
            return false;
        }
        if (this.title == null) {
            if (other.title != null) {
                return false;
            }
        }
        else if (!this.title.equals(other.title)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PortletRenderResult [renderTime=" + this.renderTime + ", title=" + this.title + "]";
    }
}
