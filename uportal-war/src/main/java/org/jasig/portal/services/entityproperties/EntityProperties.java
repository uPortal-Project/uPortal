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



