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

package org.jasig.portal.layout;

/**
 * Defines an interface that the layout management implementation being used
 * can opt to support to provide localized names of folder nodes without
 * requiring such names to be embedded within the layouts for users. By using
 * this approach, when the user's locale changes the layout need not be
 * reloaded but the next rendering can inject the names.
 * 
 * @author Mark Boyd
 * 
 */
public interface IFolderLocalNameResolver
{
    /**
     * Returns the local folder label for the user's current locale as
     * determined by use of the LocaleManager. If no local version of the label
     * is available for the current locale nor for the default locale then null
     * is returned.
     * 
     * @param nodeId
     * @return
     */
    public String getFolderLabel(String nodeId);
}
