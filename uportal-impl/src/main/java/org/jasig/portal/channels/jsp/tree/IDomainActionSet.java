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

/*
 * Created on Dec 16, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.jasig.portal.channels.jsp.tree;


/**
 * @author Mark Boyd
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface IDomainActionSet
{
    /**
     * Returns an array of keys representing the supported actions that will
     * appear in the tree.
     * 
     * @return
     */
    String[] getSupportedActions();

    /**
     * Provides information needed by a custom renderer to render the labeling
     * of an action for a given domain object. This information is made 
     * accessible to the custom renderer JSP via:
     * 
     * <pre>
     *   ${requestScope.model.actionLabelData}
     * </pre>
     *       
     * @param action
     * @param domainObject
     * @return
     */
    Object getLabelData(String action, Object domainObject);
    
}
