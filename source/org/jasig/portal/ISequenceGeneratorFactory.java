package org.jasig.portal;

/**
 * A factory for ISequenceGenerators.
 * @author Dan Ellentuck
 * @version $Revision$
 */
public interface ISequenceGeneratorFactory {
/**
 * @return org.jasig.portal.ISequenceGenerator
 */
ISequenceGenerator getSequenceGenerator();
}
