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

package org.jasig.portal.channels.jsp.tree;

import java.util.HashMap;

/**
 * Special class used in the tree rendering JSP to translate supported JSP Map
 * semantics to dynamic lookup of a URL for the supported tree actions of 
 * expanding or collapsing children and showing or hiding aspects.
 * 
 * @author Mark Boyd
 *
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
final class UrlResolver extends HashMap
{
    private ITreeActionUrlResolver resolver = null;
    private int urlType = -1;
    
    UrlResolver(ITreeActionUrlResolver resolver, int urlType)
    {
        this.resolver = resolver;
        this.urlType = urlType;
    }
    
    public Object get(Object key)
    {
        return resolver.getTreeActionUrl(urlType, (String) key);
    }
}
