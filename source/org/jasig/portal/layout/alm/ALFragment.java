/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.alm;

import org.jasig.portal.PortalException;

import java.util.Set;
import java.util.HashSet;


/**
 * The implementation of the user layout fragment.
 * 
 * Prior to uPortal 2.5, this class existed in the package org.jasig.portal.layout.
 * It was moved to its present package to reflect that it is part of Aggregated
 * Layouts.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */
public class ALFragment extends AggregatedLayout implements ILayoutFragment {
	
	protected String functionalName;
	protected String description;
	protected boolean pushedFragment = false;
	

	public ALFragment (  String fragmentId, IAggregatedUserLayoutManager layoutManager ) throws PortalException {
	 super ( fragmentId, layoutManager );
	}

	public ALFragment (  String fragmentId ) throws PortalException {
	 super ( fragmentId );
	}
	
	
	/**
				 * Answers whether the fragment is pushed or pulled
				 *
				 * @return an boolean value
				 */
	public boolean isPushedFragment() {
		 return pushedFragment;
	}
    
	/**
				   * Marks the fragment as pushed
				   */
	public void setPushedFragment() {
	   pushedFragment = true;
	}
	
	/**
					   * Marks the fragment as pulled
					   */
	public void setPulledFragment() {
	   pushedFragment = false;
	}
	
	/**
			 * Returns a fragment functional name
			 *
			 * @return an <code>String</code> fragment name
			 */
    public String getFunctionalName() {
      return functionalName;
    }
    
	/**
			   * Sets a fragment functional name
			   * @param functionalName a <code>String</code> value
			   */
	public void setFunctionalName(String functionalName) {
		this.functionalName = functionalName;
	}

    /**
     * Returns a fragment name
     *
     * @return an <code>String</code> fragment name
     */
    public String getName() {
      return getFunctionalName();	
    }
    
	/**
		 * Returns a fragment description
		 *
		 * @return an <code>String</code> fragment description
		 */
	public String getDescription() {
	  return description;	
	}
	
	/**
		* Sets a fragment name
		* @param name a <code>String</code> value
		*/
	public void setName( String name ) {
	  setFunctionalName(name);	
	}
    
	/**
	   * Sets a fragment description
	   * @param description a <code>String</code> value
	   */
    public void setDescription( String description ) {
      this.description = description;	
    }
    
	/**
		 * Returns a list of fragment Ids existing in the layout.
		 *
		 * @return a <code>Set</code> of <code>String</code> fragment Ids.
		 * @exception PortalException if an error occurs
		 */
	public Set getFragmentIds() throws PortalException {
		  Set set = new HashSet();
		  set.add(getId());
		  return set;
	}

		/**
		 * Returns an fragment Id for a given node.
		 * Returns null if the node is not part of any fragments.
		 *
		 * @param nodeId a <code>String</code> value
		 * @return a <code>String</code> fragment Id
		 * @exception PortalException if an error occurs
		 */
	public String getFragmentId(String nodeId) throws PortalException {
		  return getId();
	}

		/**
		 * Returns the fragment root Id.
		 *
		 * @return a <code>String</code> fragment root Id
		 * @exception PortalException if an error occurs
		 */
	public String getFragmentRootId() throws PortalException {
		  return getFragmentRootId(getId());
	}
   
}
