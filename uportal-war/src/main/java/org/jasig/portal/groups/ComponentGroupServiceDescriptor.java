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
package org.jasig.portal.groups;

import java.util.HashMap;

/**
 * A data holder for configuration information about a specific component group service.  
 *
 * @author Dan Ellentuck
 * @see ICompositeGroupService
 */
public class ComponentGroupServiceDescriptor extends HashMap<String,Object> {

    private static final long serialVersionUID = 1L;

    // Known service attributes (there may optionally be others.)
    private static final String NAME="name";
    private static final String FACTORY="service_factory";
    private static final String ENTITY_STORE_FACTORY="entity_store_factory";
    private static final String GROUP_STORE_FACTORY="group_store_factory";
    private static final String ENTITY_SEARCHER_FACTORY="entity_searcher_factory";
    private static final String INTERNALLY_MANAGED="internally_managed";
    private static final String CACHE_ENABLED="cache_enabled";

public String getEntitySearcherFactoryName() 
{ 
    return (String)get(ENTITY_SEARCHER_FACTORY); 
}
public String getEntityStoreFactoryName() 
{ 
    return (String)get(ENTITY_STORE_FACTORY); 
}
public String getGroupStoreFactoryName() 
{ 
    return (String)get(GROUP_STORE_FACTORY); 
}
public String getName() 
{
    return (String)get(NAME);
}
public String getServiceFactoryName() 
{
    return (String)get(FACTORY);
}
public boolean isCachingEnabled() 
{ 
    Boolean result = (Boolean)get(CACHE_ENABLED);
    return (result == null) ? false : result.booleanValue();
}
public boolean isInternallyManaged() 
{ 
    Boolean result = (Boolean)get(INTERNALLY_MANAGED);
    return (result == null) ? false : result.booleanValue();
}
public void setAttribute(String attName, Object attValue) 
{ 
    put(attName, attValue); 
}
public void setCachingEnabled(boolean caching) 
{ 
    put(CACHE_ENABLED, new Boolean(caching)); 
}
public void setEntitySearcherFactoryName(String esfName) 
{ 
    put(ENTITY_SEARCHER_FACTORY, esfName); 
}
public void setEntityStoreFactoryName(String esfName) 
{ 
    put(ENTITY_STORE_FACTORY, esfName); 
}
public void setGroupStoreFactoryName(String gsfName) 
{ 
    put(GROUP_STORE_FACTORY, gsfName); 
}
public void setInternallyManaged(boolean internal) 
{ 
    put(INTERNALLY_MANAGED, new Boolean(internal)); 
}
public void setName(String name) 
{ 
    put(NAME, name);
}
public void setServiceFactoryName(String sfName) 
{ 
    put(FACTORY, sfName); 
}
}
