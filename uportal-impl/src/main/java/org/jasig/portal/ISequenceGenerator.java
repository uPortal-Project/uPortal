/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * An interface for returning sequences derived from named counters.
 *
 * The following methods are devoted to creating and using these counters,
 * which can be used as oids.  
 * <p>
 *    <code>createCounter(String name)</code>
 *    <code>getNextInt()</code>
 *    <code>getNextInt(String name)</code>
 *    <code>setCounter(String name)</code>
 * <p>
 * ISequenceGenerator inherits the following more general methods from 
 * IOIDGenerator, which return Strings:
 * <p>
 *    <code>getNext()</code>
 *    <code>getNext(String name)</code>
 * <p>
 *
 * @author Dan Ellentuck
 * @version $Revision$
 * @deprecated use {@link org.jasig.portal.utils.ICounterStore} instead
 */
@Deprecated
public interface ISequenceGenerator extends IOIDGenerator {
/**
 * @param name java.lang.String
 * @exception java.lang.Exception
 */
public void createCounter(String name) throws java.lang.Exception;
/**
 * @return int
 * @exception java.lang.Exception The exception description.
 */
public int getNextInt() throws java.lang.Exception;
/**
 * @return int
 * @param name java.lang.String
 * @exception java.lang.Exception The exception description.
 */
public int getNextInt(String name) throws java.lang.Exception;
/**
 * @param name java.lang.String
 * @param newValue int
 * @exception java.lang.Exception
 */
public void setCounter(String name, int newValue) throws java.lang.Exception;
}
