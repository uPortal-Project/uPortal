/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.events.tincan.converters;

import java.net.URI;
import org.apereo.portal.events.PortalEvent;
import org.apereo.portal.events.tincan.ILrsActorService;
import org.apereo.portal.events.tincan.IPortalEventToLrsStatementConverter;
import org.apereo.portal.events.tincan.UrnBuilder;
import org.apereo.portal.events.tincan.om.LrsActor;
import org.apereo.portal.events.tincan.om.LrsObject;
import org.apereo.portal.events.tincan.om.LrsStatement;
import org.apereo.portal.events.tincan.om.LrsVerb;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for PortalEvent to LrsStatement converters.
 *
 */
public abstract class AbstractPortalEventToLrsStatementConverter
        implements IPortalEventToLrsStatementConverter {
    private ILrsActorService actorService;
    private String defaultObjectType = "Activity";
    private LrsVerb verb;

    protected abstract LrsObject getLrsObject(PortalEvent event);

    /**
     * Set the service that creates the LrsActor.
     *
     * @param actorService the actor service
     */
    @Autowired
    public void setActorService(ILrsActorService actorService) {
        this.actorService = actorService;
    }

    /**
     * Set the verb to use for this conversion.
     *
     * @param verb the verb to use.
     */
    public void setVerb(LrsVerb verb) {
        this.verb = verb;
    }

    /**
     * Get the default object type for the Lrs Object.
     *
     * @return the activity type
     */
    public String getDefaultObjectType() {
        return defaultObjectType;
    }

    /**
     * Override the default object type. Defaults to "Activity".
     *
     * @return the default object type
     */
    public void setDefaultObjectType(String defaultObjectType) {
        this.defaultObjectType = defaultObjectType;
    }

    /**
     * Check if the converter supports the specific event. Subclassess should override.
     *
     * @param event the event to check
     * @return false
     */
    @Override
    public boolean supports(PortalEvent event) {
        return false;
    }

    /**
     * Convert an event to an LrsStatement.
     *
     * @param event the portal event.
     * @return the new LrsStatement
     */
    @Override
    public LrsStatement toLrsStatement(PortalEvent event) {
        return new LrsStatement(getActor(event), getVerb(event), getLrsObject(event));
    }

    /**
     * Get the actor for an event.
     *
     * @param event the portal event
     * @return the LrsActor
     */
    protected LrsActor getActor(PortalEvent event) {
        String username = event.getUserName();
        return actorService.getLrsActor(username);
    }

    /**
     * Get the verb for the converted LrsStatement. Can be overridden to support different verbs
     * based on the event.
     *
     * @param event the portal event.
     * @return the verb.
     */
    protected LrsVerb getVerb(PortalEvent event) {
        return verb;
    }

    /**
     * Build the URN for the LrsStatement. This method attaches creates the base URN. Additional
     * elements can be attached.
     *
     * @param parts Additional URN elements.
     * @return The formatted URI
     */
    protected URI buildUrn(String... parts) {
        UrnBuilder builder = new UrnBuilder("UTF-8", "tincan", "uportal", "activities");
        builder.add(parts);

        return builder.getUri();
    }
}
