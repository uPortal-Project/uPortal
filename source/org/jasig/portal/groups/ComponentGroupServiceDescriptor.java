/* Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
