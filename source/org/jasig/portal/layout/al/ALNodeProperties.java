/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al;

import java.util.Collection;
import java.util.Iterator;
import java.util.HashSet;
import org.jasig.portal.layout.al.common.restrictions.IUserLayoutRestriction;
import org.jasig.portal.layout.al.common.restrictions.RestrictionPath;
import org.jasig.portal.layout.al.common.restrictions.RestrictionType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * A proxy class representing additional properties of the Aggregated Layout nodes.
 * Thanks to the java's inability to support multiple inheritance, we can make a mess out of an elegant class structure.
 * 
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version 1.0
 */
public class ALNodeProperties implements IALNodeProperties {

    protected IFragmentId fragmentId = null;
    protected IFragmentLocalNodeId fragmentNodeId = null;
    protected Collection restrictions = null;
    protected boolean fragmentRoot;
    protected String group = "";


    public ALNodeProperties() {
    	restrictions = new HashSet();
    }

    public ALNodeProperties(IALNodeProperties p) {
        this();
        this.fragmentId=p.getFragmentId();
        this.fragmentNodeId=p.getFragmentNodeId();
        this.restrictions=p.getRestrictions();
        this.fragmentRoot=p.isFragmentRoot();
        this.group=p.getGroup();
    }

    /**
     * Set fragment id
     *
     * @param fragmentId a fragment id
     */
    public void setFragmentId ( IFragmentId fragmentId ) {
      this.fragmentId = fragmentId;
    }

    /**
     * Get fragment id
     *
     * @return a fragment id
     */
    public IFragmentId getFragmentId() {
       return fragmentId;
    }

    /**
     * Set fragment-local node id
     *
     * @param fragmentNodeId an id of the node within the fragment
     */
    public void setFragmentNodeId ( IFragmentLocalNodeId fragmentNodeId ) {
      this.fragmentNodeId = fragmentNodeId;
    }

    /**
     * Get fragment-local node id
     *
     * @return an id of the node within the fragment
     */
    public IFragmentLocalNodeId getFragmentNodeId() {
       return fragmentNodeId;
    }

     /**
     * Sets the group identificator for this node.
     * @param group a <code>String</code> group identificator value
     */
     public void setGroup ( String group ) {
       this.group = group;
     }

     /**
     * Gets the priority value for this node.
     */
     public String getGroup() {
       return group;
     }


    /**
     * Sets the hashtable of restrictions bound to this node
     * @param restrictions a <code>Collection</code> of restriction expressions
     */
     public void setRestrictions ( Collection restrictions ) {
       this.restrictions = restrictions;
     }

     /**
     * Gets the hashtable of restrictions bound to this node
     * @return a set of restriction expressions
     */
     public Collection getRestrictions () {
       return restrictions;
     }


     /**
     * Adds the restriction for this node.
     * @param restriction a <code>IUserLayoutRestriction</code> a restriction
     */
     public void addRestriction( IUserLayoutRestriction restriction ) {
       restrictions.add(restriction);
     }

     /**
     * Gets a restriction by the type and the restriction path.
     * @param restrictionType a <code>RestrictionType</code> restriction type
     * @param restrictionPath a <code>RestrictionPath</code> restriction path
     * @return a IUserLayoutRestriction
     */
     public IUserLayoutRestriction getRestriction( RestrictionType restrictionType, RestrictionPath restrictionPath ) {
     	for ( Iterator i = restrictions.iterator(); i.hasNext(); ) {
            IUserLayoutRestriction restriction = (IUserLayoutRestriction) i.next();
            if ( restriction.is(restrictionType) && restrictionPath.equals(restriction.getRestrictionPath()) )
                 return restriction;
        }
     	         return null;
     }
     
     /**
      * Gets a local restriction by the given type.
      * @param restrictionType a <code>RestrictionType</code> restriction type
      * @return a IUserLayoutRestriction
      */
      public IUserLayoutRestriction getLocalRestriction( RestrictionType restrictionType ) {
      	return getRestriction(restrictionType,RestrictionPath.LOCAL_RESTRICTION_PATH);
      }

     /**
     * Gets a restrictions list by a restriction path.
     * @param restrictionPath a <code>RestrictionPath</code> restriction path
     * @return a IUserLayoutRestriction
     */
     public Collection getRestrictionsByPath( RestrictionPath restrictionPath ) {
       Collection list = new HashSet();
       if ( restrictions != null ) {
        for ( Iterator i = restrictions.iterator(); i.hasNext(); ) {
          IUserLayoutRestriction restriction = (IUserLayoutRestriction) i.next();
          if ( restrictionPath.equals(restriction.getRestrictionPath()) )
               list.add(restriction);
        }
       }
       return list;
     }

    public void addRestrictionChildren(Element node, Document root) {
       if ( restrictions != null )
       	for ( Iterator i = restrictions.iterator(); i.hasNext(); ) {
            Element pElement=root.createElement("restriction");
            IUserLayoutRestriction restriction = (IUserLayoutRestriction) i.next();
            pElement.setAttribute("path",restriction.getRestrictionPath().toString());
            pElement.setAttribute("value",restriction.getRestrictionExpression());
            pElement.setAttribute("type",restriction.getName());
            node.appendChild(pElement);
        }
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.IALNodeProperties#isFragmentRoot()
     */
    public boolean isFragmentRoot() {
        return this.fragmentRoot;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.IALNodeProperties#setFragmentRoot(boolean)
     */
    public void setFragmentRoot(boolean value) {
        this.fragmentRoot=value;
    }
}
