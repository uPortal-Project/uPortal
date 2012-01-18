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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.events.PortalEvent.PortalEventBuilder;
import org.jasig.portal.security.IPerson;

/**
 * Utility used to create portal events for testing. Events all use package-private constructors but for tests
 * a way to create them is needed.
 * <p/>
 * Add events creation methods here as needed for tests
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class TestEventFactory {
    private TestEventFactory() {
    }
    
    public static LoginEvent newLoginEvent(Object source, String serverName, String eventSessionId, IPerson person, 
            Set<String> groups, Map<String, List<String>> attributes) {
        
        final PortalEventBuilder portalEventBuilder = new PortalEventBuilder(source, serverName, eventSessionId, person);
        return new LoginEvent(portalEventBuilder, groups, attributes);
    }
    
}
