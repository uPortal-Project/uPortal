package org.jasig.portal;

/**
  * @author Dan Ellentuck
  * @version $Revision$
 */
public class ReferenceSequenceGeneratorFactory implements ISequenceGeneratorFactory {
/**
 * ReferenceOIDGeneratorFactory constructor comment.
 */
public ReferenceSequenceGeneratorFactory() {
	super();
}
/**
 * @return org.jasig.portal.IOIDGenerator
 */
public ISequenceGenerator getSequenceGenerator() {
	return new ReferenceSequenceGenerator();
}
}
