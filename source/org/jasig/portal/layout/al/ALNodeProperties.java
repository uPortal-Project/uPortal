/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import org.jasig.portal.layout.al.common.restrictions.IUserLayoutRestriction;
import org.jasig.portal.utils.CommonUtils;
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
    protected Hashtable restrictions = null;
    protected boolean fragmentRoot;
    protected String group = "";


    public ALNodeProperties() {
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
     * @param restrictions a <code>Hashtable</code> of restriction expressions
     */
     public void setRestrictions ( Hashtable restrictions ) {
       this.restrictions = restrictions;
     }

     /**
     * Gets the hashtable of restrictions bound to this node
     * @return a set of restriction expressions
     */
     public Hashtable getRestrictions () {
       return restrictions;
     }


     /**
     * Adds the restriction for this node.
     * @param restriction a <code>IUserLayoutRestriction</code> a restriction
     */
     public void addRestriction( IUserLayoutRestriction restriction ) {
       if ( restrictions == null ) restrictions = new Hashtable();
       restrictions.put(restriction.getUniqueKey(), restriction);
     }

     /**
     * Gets a restriction by the type.
     * @param restrictionName a <code>String</code>  name of the restriction
     * @return a IUserLayoutRestriction
     */
     public IUserLayoutRestriction getRestriction( String restrictionName ) {
      if ( restrictions != null )
       return (IUserLayoutRestriction) restrictions.get(restrictionName);
       return null;
     }

     /**
     * Gets a restrictions list by a restriction path.
     * @param restrictionPath a <code>String</code> restriction path
     * @return a IUserLayoutRestriction
     */
     public List getRestrictionsByPath( String restrictionPath ) {
       Vector list = new Vector();
       if ( restrictions != null ) {
        for ( Enumeration enum = restrictions.elements(); enum.hasMoreElements(); ) {
          IUserLayoutRestriction restriction = (IUserLayoutRestriction) enum.nextElement();
          if ( CommonUtils.nvl(restrictionPath).equals(CommonUtils.nvl(restriction.getRestrictionPath())) )
               list.add(restriction);
        }
       }
         return list;
     }

    public void addRestrictionChildren(Element node, Document root) {
       if ( restrictions != null )
        for ( Enumeration enum = restrictions.elements(); enum.hasMoreElements(); ) {
            Element pElement=root.createElement("restriction");
            IUserLayoutRestriction restriction = (IUserLayoutRestriction) enum.nextElement();
            pElement.setAttribute("path",restriction.getRestrictionPath());
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
