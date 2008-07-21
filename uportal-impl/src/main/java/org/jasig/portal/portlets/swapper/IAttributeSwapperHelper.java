/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlets.swapper;

import java.util.Set;

import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.webflow.context.ExternalContext;

/**
 * Defines helper methods for the attribute-swapper flow.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IAttributeSwapperHelper {
    /**
     * Portlet preference name to use to specify a List of attributes that will be displayed for the user to work with.
     */
    public static final String ATTRIBUTE_SWAPPER_ATTRIBUTES_FORM_SWAPPABLE_ATTRIBUTES = "attribute-swapper.attributesForm.swappableAttributes";


    /**
     * Gets the Set of attribtues to allow the user to swap values for.
     * 
     * @param externalContext The {@link ExternalContext} to get the flows's configuration from
     * @return Set of attributes that can be swapped
     */
    public Set<String> getSwappableAttributes(ExternalContext externalContext);

    /**
     * Get the base attributes for a person, this bypasses the overwriting dao.
     * 
     * @param uid The user name of the user to get the attributes for.
     * @return The original (non overwritten) attributes.
     */
    public IPersonAttributes getOriginalUserAttributes(String uid);

    /**
     * Initialize the command object used for the swapper form. 
     * 
     * @param externalContext The {@link ExternalContext} to get the flows's configuration from
     * @param attributeSwapRequest The command object to initialize
     */
    public void populateSwapRequest(ExternalContext externalContext, AttributeSwapRequest attributeSwapRequest);

    /**
     * Perform the attribute swap request.
     * 
     * @param uid The user name of the user to override attributes for
     * @param attributeSwapRequest The swap request with the new attributes
     */
    public void swapAttributes(String uid, AttributeSwapRequest attributeSwapRequest);

    /**
     * Removes the swapped attributes for the specified user
     * 
     * @param uid The user name of the user to remove the attribute override for
     */
    public void resetAttributes(String uid);

}