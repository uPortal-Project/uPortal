/**
 * 
 */
package org.jasig.portal.web.skin;

/**
 * Interface to locate {@link Resources} (skin configuration).
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public interface ResourcesDao {

	/**
	 * Load a {@link Resources} from the specified {@link String} path.
	 * Implementations will likely need to resolve the path against the
	 * ServletContext, as implementation at time of writing locates the resources
	 * configuration in the same directory as the javascript/css it documents.
	 * 
	 * @param pathToSkinXml
	 * @return
	 */
	Resources getResources(String pathToSkinXml);
}
