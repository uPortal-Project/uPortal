/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
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



package org.jasig.portal.layout;

import org.jasig.portal.PortalException;


/**
 * An interface representing the user layout fragment.
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

   
}
