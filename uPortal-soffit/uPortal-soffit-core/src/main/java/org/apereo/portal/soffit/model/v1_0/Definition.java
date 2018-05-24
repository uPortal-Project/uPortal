/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.soffit.model.v1_0;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Provides information about the publication record of the soffit within the portal.
 *
 * @since 5.0
 */
public class Definition extends AbstractTokenizable {

    private final String title;
    private final String fname;
    private final String description;

    private final List<String> categories;
    private final Map<String, List<String>> parameters;

    public Definition(
            String encryptedToken,
            String title,
            String fname,
            String description,
            List<String> categories,
            Map<String, List<String>> parameters) {
        super(encryptedToken);
        this.title = title;
        this.fname = fname;
        this.description = description;
        this.categories = Collections.unmodifiableList(categories);
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    /**
     * Supports proxying a missing data model element.
     *
     * @since 5.1
     */
    protected Definition() {
        super(null);
        this.title = null;
        this.fname = null;
        this.description = null;
        this.categories = null;
        this.parameters = null;
    }

    public String getTitle() {
        return title;
    }

    public String getFname() {
        return fname;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getCategories() {
        return categories;
    }

    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((categories == null) ? 0 : categories.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((fname == null) ? 0 : fname.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        Definition other = (Definition) obj;
        if (categories == null) {
            if (other.categories != null) return false;
        } else if (!categories.equals(other.categories)) return false;
        if (description == null) {
            if (other.description != null) return false;
        } else if (!description.equals(other.description)) return false;
        if (fname == null) {
            if (other.fname != null) return false;
        } else if (!fname.equals(other.fname)) return false;
        if (parameters == null) {
            if (other.parameters != null) return false;
        } else if (!parameters.equals(other.parameters)) return false;
        if (title == null) {
            if (other.title != null) return false;
        } else if (!title.equals(other.title)) return false;
        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("title", this.title)
                .append("fname", this.fname)
                .append("description", this.description)
                .append("categories", this.categories)
                .append("parameters", this.parameters)
                .append("getEncryptedToken()", this.getEncryptedToken())
                .toString();
    }
}
