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

package org.jasig.portal.core;


/**
 * The object Ids factory.
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public abstract class ObjectIdFactory {
	
	protected static class ObjectIdImpl implements IObjectId {

	    protected String id;
	    
	    public ObjectIdImpl(String id) {
	        this.id = id;
	    }

	    public ObjectIdImpl(int id) {
	        this(Integer.toString(id));
	    }
	    
	    public ObjectIdImpl(long id) {
	        this(Long.toString(id));
	    }

	    public boolean equals(Object obj) {
	        return ( (obj instanceof IObjectId) && id.equals(obj.toString()) );
	    }
	    
	    public String toString() {
	        return id;
	    }
	    
	    public int toInt() {
	        return Integer.parseInt(id);
	    }
	    
	    public long toLong() {
	        return Long.parseLong(id);
	    }

	}
    
    public static IObjectId createId(String id) {
        return new ObjectIdImpl(id);
    }
    
    public static IObjectId createId(int id) {
        return new ObjectIdImpl(id);
    }
    
    public static IObjectId createId(long id) {
        return new ObjectIdImpl(id);
    }
}
