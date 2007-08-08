/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

import java.util.HashMap;

/**
 * A data holder for configuration information about a specific component group service.  
 *
 * @author Dan Ellentuck
 * @version $Revision$
 * @see ICompositeGroupService
 */
public class ComponentGroupServiceDescriptor extends HashMap
{
    // Known service attributes (there may optionally be others.)
    public static final String NAME="name";
    public static final String FACTORY="service_factory";
    public static final String ENTITY_STORE_FACTORY="entity_store_factory";
    public static final String GROUP_STORE_FACTORY="group_store_factory";
    public static final String ENTITY_SEARCHER_FACTORY="entity_searcher_factory";
    public static final String INTERNALLY_MANAGED="internally_managed";
    public static final String CACHE_ENABLED="cache_enabled";
/**
 * ComponentGroupServiceDescriptor constructor comment.
 */
public ComponentGroupServiceDescriptor() {
	super();
}
public Object getAttribute(String attName) 
{ 
    return get(attName); 
}
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
