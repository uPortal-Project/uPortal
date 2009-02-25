/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package  org.jasig.portal.services.entityproperties;

import java.util.HashMap;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;


/**
 * A BasicEntity implementation used interally by the EntityPropertyRegistry
 * to cache property lookup results
 *
 * @author Alex Vigdor av317@columbia.edu
 * @version $Revision$
 */
public class EntityProperties
        implements IBasicEntity {
    protected String key;
    protected HashMap props;

    public EntityProperties(String key) {
        this.key = key;
        this.props = new HashMap();
    }

    public EntityIdentifier getEntityIdentifier() {
        return new EntityIdentifier(getKey(), getType());
    }

    public String getKey() {
        return  key;
    }

    public Class getType() {
        return  this.getClass();
    }

    public String getProperty(String name) {
        return  (String)props.get(name);
    }

    public void setProperty(String name, String value) {
        this.props.put(name, value);
    }

    public String[] getPropertyNames() {
        return  (String[])props.keySet().toArray(new String[0]);
    }
}



