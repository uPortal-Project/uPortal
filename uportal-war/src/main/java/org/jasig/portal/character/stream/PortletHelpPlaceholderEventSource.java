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

package org.jasig.portal.character.stream;

import java.util.Collection;
import java.util.regex.MatchResult;

import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.character.stream.events.PortletHelpPlaceholderEvent;
import org.jasig.portal.character.stream.events.PortletHelpPlaceholderEventImpl;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Generates a {@link PortletHelpPlaceholderEvent} for a regular expression match
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletHelpPlaceholderEventSource extends PortletPlaceholderEventSource {

    @Override
    protected void generateCharacterEvents(IPortletWindowId portletWindowId, MatchResult matchResult,
            Collection<CharacterEvent> eventBuffer) {
        eventBuffer.add(new PortletHelpPlaceholderEventImpl(portletWindowId));
    }
}
