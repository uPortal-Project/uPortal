/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al;

import org.jasig.portal.layout.al.common.node.NodeIdFactory;

/**
 * The node Ids factory.
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public abstract class ALNodeIdFactory extends NodeIdFactory {
	
	
	protected static class FragmentNodeIdImpl extends NodeIdImpl implements IFragmentLocalNodeId {
	    
	    public FragmentNodeIdImpl(String id) {
	        super(id);
	    }

	    public FragmentNodeIdImpl(int id) {
	        super(id);
	    }
	    
	    public FragmentNodeIdImpl(long id) {
	        super(id);
	    }

	    public boolean equals(Object obj) {
	        return ( (obj instanceof IFragmentLocalNodeId) && id.equals(obj.toString()) );
	    }
	}
    
    
    public static IFragmentLocalNodeId createFragmentNodeId(String id) {
        return new FragmentNodeIdImpl(id);
    }
    
    public static IFragmentLocalNodeId createFragmentNodeId(int id) {
        return new FragmentNodeIdImpl(id);
    }
    
    public static IFragmentLocalNodeId createFragmentNodeId(long id) {
        return new FragmentNodeIdImpl(id);
    }
}
