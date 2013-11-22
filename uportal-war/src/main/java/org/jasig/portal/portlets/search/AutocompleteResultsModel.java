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

package org.jasig.portal.portlets.search;

/**
 * Model object for the search-as-you-type ajax results
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */

public class AutocompleteResultsModel {
    String title;
    String description;
    String url;
    String category;

    public AutocompleteResultsModel() {
    }

    public AutocompleteResultsModel(String title, String description, String url, String category) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Consider a result equal if the URL is equal.  Though the url should not be null,
     * insure that doesn't blow up with an NPE.
     * @param o object
     * @return true if considered equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AutocompleteResultsModel that = (AutocompleteResultsModel) o;

        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }
}
