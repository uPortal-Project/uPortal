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
package org.jasig.portal.portlets.translator;

import java.io.Serializable;

/**
 * This class is for response on AJAX requests in order to populate list of translateable entities.
 * 
 * @author Arvids Grabovskis
 * @version $Revision$
 */
class TranslatableEntity implements Serializable {
    
    private static final long serialVersionUID = 8803614182718390566L;
    
    /**
     * Entity identifier. This might be portlet definition id, message code or something like that.
     * This will be used in order to retrieve the exact entity while loading the form contents.
     */
    private String id;
    
    /**
     * This is the title of entity to be shown in the entity list. It might be a title in original
     * language (in case of portlet definitions) or message codes.
     */
    private String title;
    
    /**
     * Get the entity identifier.
     * 
     * @return entity identifier.
     */
    public String getId() {
        return id;
    }
    
    /**
     * Set the entity identifier.
     * 
     * @param id entity identifier.
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Get the title of entity.
     * 
     * @return entity title.
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Set the title of entity.
     * 
     * @param title entity title.
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
