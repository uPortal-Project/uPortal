/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * A very basic interface for returning oids.
 *
 * <p>
 *    <code>getNext()</code>
 *    <code>getNext(String name)</code>
 * <p>
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public interface IOIDGenerator {
/**
 * @return java.lang.String
 * @exception java.lang.Exception The exception description.
 */
public String getNext() throws java.lang.Exception;
/**
 * @param name String 
 * @return java.lang.String
 * @exception java.lang.Exception
 */
public String getNext(String name) throws java.lang.Exception;
}
