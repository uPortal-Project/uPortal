/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
