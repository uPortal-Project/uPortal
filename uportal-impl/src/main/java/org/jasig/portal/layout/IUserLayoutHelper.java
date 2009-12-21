/**
 * 
 */
package org.jasig.portal.layout;

import org.jasig.services.persondir.IPersonAttributes;

/**
 * Defines operations to assist in administering user layouts.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public interface IUserLayoutHelper {

	/**
	 * Reset the user layout for the {@link IPersonAttributes} argument.
	 * 
	 * @param person
	 */
	void resetUserLayout(IPersonAttributes person);
}
