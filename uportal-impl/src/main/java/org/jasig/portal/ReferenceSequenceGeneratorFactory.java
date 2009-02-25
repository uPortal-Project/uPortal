/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

/**
  * @author Dan Ellentuck
  * @version $Revision$
 * @deprecated Use {@link org.jasig.portal.utils.ICounterStore} instead
 */
@Deprecated
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
