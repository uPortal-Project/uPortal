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

package org.jasig.portal.events;

/**
 * Interface of classes that know how to handle a specific event. The concept of
 * handling an event usually implies that the handler will perform some form of
 * logging such as using Log4j or writing the information to a database.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface EventHandler {

    /**
     * Method to handle any processing of the event that is needed.
     * 
     * @param event the event to handle.
     */
    void handleEvent(PortalEvent event);

    /**
     * Method to check if this handler will be able to process the event.
     * 
     * @param event the event we want to check if we support.
     * @return true if the event is supported, false otherwise.
     */
    boolean supports(PortalEvent event);
}
