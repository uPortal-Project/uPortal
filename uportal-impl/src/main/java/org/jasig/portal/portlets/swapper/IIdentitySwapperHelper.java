/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlets.swapper;

import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.webflow.context.ExternalContext;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IIdentitySwapperHelper {

    /**
     * Sets up the user's session to be elidgable for doign an identity swap.
     */
    public void swapAttributes(ExternalContext externalContext, IPersonAttributes person);

}