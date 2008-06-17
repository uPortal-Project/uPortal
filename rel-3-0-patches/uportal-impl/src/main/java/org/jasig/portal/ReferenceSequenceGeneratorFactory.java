/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

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
