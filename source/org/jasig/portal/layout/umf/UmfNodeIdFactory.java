/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.umf;

import org.jasig.portal.layout.node.NodeIdFactory;

/**
 * The node Ids factory.
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public abstract class UmfNodeIdFactory extends NodeIdFactory {
	
	protected static class LayoutNodeIdImpl extends NodeIdImpl implements ILayoutNodeId {
	    
	    public LayoutNodeIdImpl(String id) {
	        super(id);
	    }

	    public LayoutNodeIdImpl(int id) {
	        super(id);
	    }
	    
	    public LayoutNodeIdImpl(long id) {
	        super(id);
	    }

	    public boolean equals(Object obj) {
	        return ( (obj instanceof ILayoutNodeId) && id.equals(obj.toString()) );
	    }
	}
	
	protected static class FragmentNodeIdImpl extends NodeIdImpl implements IFragmentNodeId {
	    
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
	        return ( (obj instanceof IFragmentNodeId) && id.equals(obj.toString()) );
	    }
	}
    
    public static ILayoutNodeId createLayoutNodeId(String id) {
        return new LayoutNodeIdImpl(id);
    }
    
    public static ILayoutNodeId createLayoutNodeId(int id) {
        return new LayoutNodeIdImpl(id);
    }
    
    public static ILayoutNodeId createLayoutNodeId(long id) {
        return new LayoutNodeIdImpl(id);
    }
    
    public static IFragmentNodeId createFragmentNodeId(String id) {
        return new FragmentNodeIdImpl(id);
    }
    
    public static IFragmentNodeId createFragmentNodeId(int id) {
        return new FragmentNodeIdImpl(id);
    }
    
    public static IFragmentNodeId createFragmentNodeId(long id) {
        return new FragmentNodeIdImpl(id);
    }
}
