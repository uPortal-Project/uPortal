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

package org.jasig.portal.layout.node;

import org.jasig.portal.core.ObjectIdFactory;

/**
 * The object Ids factory.
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public abstract class NodeIdFactory extends ObjectIdFactory {
	
	protected static class NodeIdImpl extends ObjectIdImpl implements INodeId {
	    
	    public NodeIdImpl(String id) {
	        super(id);
	    }

	    public NodeIdImpl(int id) {
	        super(id);
	    }
	    
	    public NodeIdImpl(long id) {
	        super(id);
	    }

	    public boolean equals(Object obj) {
	        return ( (obj instanceof INodeId) && id.equals(obj.toString()) );
	    }
	}
    
    public static INodeId createNodeId(String id) {
        return new NodeIdImpl(id);
    }
    
    public static INodeId createNodeId(int id) {
        return new NodeIdImpl(id);
    }
    
    public static INodeId createNodeId(long id) {
        return new NodeIdImpl(id);
    }
}
