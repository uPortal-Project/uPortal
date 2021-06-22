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
package org.apereo.portal.portlets.portletadmin;

import java.util.Date;
import org.apache.commons.lang.StringUtils;
import org.apereo.portal.dao.usertype.FunctionalNameType;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.portletpublishing.xml.Parameter;
import org.apereo.portal.portletpublishing.xml.PortletPublishingDefinition;
import org.apereo.portal.portletpublishing.xml.Step;
import org.apereo.portal.portlets.portletadmin.xmlsupport.IChannelPublishingDefinitionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.stereotype.Service;

@Service("portletValidator")
public class PortletDefinitionFormValidator {

    private IChannelPublishingDefinitionDao channelPublishingDefinitionDao;
    private IPortletDefinitionRegistry portletDefinitionRegistry;

    @Autowired(required = true)
    public void setChannelPublishingDefinitionDao(
            IChannelPublishingDefinitionDao channelPublishingDefinitionDao) {
        this.channelPublishingDefinitionDao = channelPublishingDefinitionDao;
    }

    @Autowired(required = true)
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    public void validateChooseType(PortletDefinitionForm def, MessageContext context) {
        final int selectedTypeId = def.getTypeId();

        switch (selectedTypeId) {
            case 0:
                // No type selected...
                context.addMessage(
                        new MessageBuilder()
                                .error()
                                .source("typeId")
                                .code("please.choose.portlet.type")
                                .build());
                break;
            default:
                // User specified a typeId;  validate that it exists
                final PortletPublishingDefinition cpd =
                        channelPublishingDefinitionDao.getChannelPublishingDefinition(
                                selectedTypeId);
                if (cpd == null) {
                    context.addMessage(
                            new MessageBuilder()
                                    .error()
                                    .source("typeId")
                                    .code("please.choose.portlet.type")
                                    .build());
                }
                break;
        }
    }

    public void validatePortletConfig(PortletDefinitionForm def, ValidationContext context) {
        validateChooseType(def, context.getMessageContext());
        doBasicInfo(def, context.getMessageContext());
        doPublishingParameters(def, context.getMessageContext());
        doLifecycle(def, context.getMessageContext());
    }

    /*
     * Private Methods (break validation work into smaller parts)
     */

    private void doBasicInfo(PortletDefinitionForm def, MessageContext context) {

        // fname
        if (StringUtils.isEmpty(def.getFname())) {
            context.addMessage(
                    new MessageBuilder()
                            .error()
                            .source("fName")
                            .code("please.enter.fname")
                            .build());
        } else if (!FunctionalNameType.isValid(def.getFname())) {
            context.addMessage(
                    new MessageBuilder().error().source("fName").code("fname.invalid").build());
        } else if (def.getId() == null
                && portletDefinitionRegistry.getPortletDefinitionByFname(def.getFname()) != null) {
            // This is a new portlet and the fname is already taken
            context.addMessage(
                    new MessageBuilder().error().source("fName").code("fname.in.use").build());
        }

        // Title
        if (StringUtils.isEmpty(def.getTitle())) {
            context.addMessage(
                    new MessageBuilder()
                            .error()
                            .source("title")
                            .code("please.enter.title")
                            .build());
        }

        // Name
        if (StringUtils.isEmpty(def.getName())) {
            context.addMessage(
                    new MessageBuilder().error().source("name").code("please.enter.name").build());
        }
        if (def.getId() == null
                && portletDefinitionRegistry.getPortletDefinitionByName(def.getName()) != null) {
            // This is a new portlet and the name is already taken
            context.addMessage(
                    new MessageBuilder().error().source("name").code("name.in.use").build());
        }
    }

    private void doPublishingParameters(PortletDefinitionForm def, MessageContext context) {
        PortletPublishingDefinition cpd =
                channelPublishingDefinitionDao.getChannelPublishingDefinition(def.getTypeId());
        for (Step step : cpd.getSteps()) {
            if (step.getParameters() != null) {
                for (Parameter param : step.getParameters()) {

                    // if the user has entered a value for this parameter,
                    // check it against the CPD
                    if (def.getParameters().containsKey(param.getName())
                            && !StringUtils.isEmpty(
                                    def.getParameters().get(param.getName()).getValue())) {

                        /*
                         * NOTE:  Looks like this method used to provide
                         * validation logic for publishing parameters, but no
                         * longer does.  TODO:  Look into re-implementing it
                         * based on the updated model.
                         *
                         * Further Reading:
                         *   - https://issues.jasig.org/browse/UP-2973
                         *   - https://github.com/uPortal-Project/uPortal/commit/a21bdb181b7e03aabd926d96f3cea66dcdf2d979#diff-41ca747cf5d1a6b00f0ab31ee88d49d7L111
                         */

                    }
                }
            }
        }
    }

    private void doLifecycle(PortletDefinitionForm def, MessageContext context) {
        if (def.getLifecycleState() == null) {
            context.addMessage(
                    new MessageBuilder()
                            .error()
                            .source("lifecycle")
                            .code("please.select.lifecycle.stage")
                            .build());
        }
        Date now = new Date();
        if (def.getPublishDate() != null) {
            if (def.getPublishDateTime().before(now)) {
                context.addMessage(
                        new MessageBuilder()
                                .error()
                                .source("publishDate")
                                .code("auto.publish.date.must.be.future")
                                .build());
            }
        }
        if (def.getExpirationDate() != null) {
            if (def.getExpirationDateTime().before(now)) {
                context.addMessage(
                        new MessageBuilder()
                                .error()
                                .source("expirationDate")
                                .code("auto.expire.date.must.be.future")
                                .build());
            }
        }
        if (def.getPublishDate() != null && def.getExpirationDate() != null) {
            if (def.getExpirationDateTime().before(def.getPublishDateTime())) {
                context.addMessage(
                        new MessageBuilder()
                                .error()
                                .source("expirationDate")
                                .code("auto.expire.date.must.be.after.publish")
                                .build());
            }
        }
    }
}
