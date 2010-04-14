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

package org.jasig.portal.portlets.portletadmin;

import java.util.Date;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.dao.usertype.FunctionalNameType;
import org.jasig.portal.portlets.portletadmin.xmlsupport.CPDParameter;
import org.jasig.portal.portlets.portletadmin.xmlsupport.CPDParameterTypeRestriction;
import org.jasig.portal.portlets.portletadmin.xmlsupport.CPDStep;
import org.jasig.portal.portlets.portletadmin.xmlsupport.ChannelPublishingDefinition;
import org.jasig.portal.portlets.portletadmin.xmlsupport.IChannelPublishingDefinitionDao;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

public class ChannelDefinitionFormValidator {
	
	private IChannelPublishingDefinitionDao channelPublishingDefinitionDao;
	private IChannelRegistryStore channelStore;

	
	public void setChannelPublishingDefinitionDao(IChannelPublishingDefinitionDao channelPublishingDefinitionDao) {
        this.channelPublishingDefinitionDao = channelPublishingDefinitionDao;
    }

    public void setChannelRegistryStore(IChannelRegistryStore channelRegistryStore) {
		this.channelStore = channelRegistryStore;
	}
	
	public void validateChooseType(ChannelDefinitionForm def, MessageContext context) {
		if(def.getTypeId() == 0) {
			context.addMessage(new MessageBuilder().error().source("typeId")
					.code("errors.channelDefinition.type.empty")
					.defaultText("Please choose a channel type").build());
		}
	}
	
	public void validateBasicInfo(ChannelDefinitionForm def, MessageContext context) {
		Matcher matcher = FunctionalNameType.VALID_FNAME_PATTERN.matcher(def.getFname());
		if (StringUtils.isEmpty(def.getFname())) {
			context.addMessage(new MessageBuilder().error().source("fName")
					.code("errors.channelDefinition.fName.empty")
					.defaultText("Please enter an fname").build());
		} else if (!matcher.matches()) {
			context.addMessage(new MessageBuilder().error().source("fName")
					.code("errors.channelDefinition.fName.invalid")
					.defaultText("Fnames may only contain letters, numbers, dashes, and underscores").build());		
		} 
		
		// if this is a new channel and the fname is already taken
		else if (def.getId() == -1 && channelStore.getChannelDefinition(def.getFname()) != null) {
			context.addMessage(new MessageBuilder().error().source("fName")
					.code("errors.channelDefinition.fName.duplicate")
					.defaultText("This fname is already in use").build());
		}
		
		if (StringUtils.isEmpty(def.getTitle())) {
			context.addMessage(new MessageBuilder().error().source("title")
					.code("errors.channelDefinition.title.empty")
					.defaultText("Please enter a title").build());
		}
		
		if (StringUtils.isEmpty(def.getName())) {
			context.addMessage(new MessageBuilder().error().source("name")
					.code("errors.channelDefinition.name.empty")
					.defaultText("Please enter a name").build());
		}

		// if this is a new channel and the name is already taken
		if (def.getId() == -1 && channelStore.getChannelDefinitionByName(def.getName()) != null) {
			context.addMessage(new MessageBuilder().error().source("name")
					.code("errors.channelDefinition.name.duplicate")
					.defaultText("This name is already in use").build());
		}
		

	}
	
	public void validateSetParameters(ChannelDefinitionForm def, MessageContext context) {
		ChannelPublishingDefinition cpd = channelPublishingDefinitionDao.getChannelPublishingDefinition(def.getTypeId());
		for (CPDStep step : cpd.getParams().getSteps()) {
			if (step.getParameters() != null) {
				for (CPDParameter param : step.getParameters()) {
					
					// if the user has entered a value for this parameter, 
					// check it against the CPD
					if (def.getParameters().containsKey(param.getName()) && 
							!StringUtils.isEmpty(def.getParameters().get(param.getName()).getValue())) {
						
						String paramValue = def.getParameters().get(param.getName()).getValue();
						String paramPath = "parameters['" + param.getName() + "'].value";
						
						// if this parameter is intended to be a number, ensure
						// that it is
						String base = param.getType().getBase();
						if ("integer".equals(base)) {
							try {
								Integer.parseInt(paramValue);
							} catch (NumberFormatException e) {
								context.addMessage(new MessageBuilder().error().source(paramPath)
										.code("errors.channelDefinition.param.int")
										.defaultText("Value must be an integer").build());
							}
						} else if ("float".equals(base)) {
							try {
								Float.parseFloat(paramValue);
							} catch (NumberFormatException e) {
								context.addMessage(new MessageBuilder().error().source(paramPath)
										.code("errors.channelDefinition.param.float")
										.defaultText("Value must be a number").build());
							}
						}
						
						// if this parameter has a restriction in the CPD, 
						// check it against the restriction
						if (param.getType().getRestriction() != null 
								&& def.getParameters().containsKey(param.getName())) {
							
							CPDParameterTypeRestriction restriction = param.getType().getRestriction();
							if ("range".equals(restriction.getType())) {
								// For now, lets just not do anything.  It doesn't 
								// look like the existing channel manager logic 
								// actually uses this restriction for validation
							} else if ("enumeration".equals(restriction.getType())) {
								// if this restriction is an enumeration of allowed values, check to
								// make sure the entered value is in the enumerated list
								if (!restriction.getValues().contains(paramValue)) {
									context.addMessage(new MessageBuilder().error().source(paramPath)
											.code("errors.channelDefinition.param.enum")
											.defaultText("Invalid selection").build());
								}
							}
						}
						
					}
					
				}
			}
		}
	}
	
	public void validateChooseCategory(ChannelDefinitionForm def, MessageContext context) {
		// make sure the user has picked at least one category
		if (def.getCategories().size() == 0) {
			context.addMessage(new MessageBuilder().error().source("categories")
					.code("errors.channelDefinition.param.categories.empty")
					.defaultText("Please choose at least one category").build());
		}
	}
	
	public void validateChooseGroup(ChannelDefinitionForm def, MessageContext context) {
		// make sure the user has picked at least one group
		if (def.getGroups().size() == 0) {
			context.addMessage(new MessageBuilder().error().source("groups")
					.code("errors.channelDefinition.groups.empty")
					.defaultText("Please choose at least one group").build());
		}
	}
	
	public void validateLifecycle(ChannelDefinitionForm def, ValidationContext context) {
		MessageContext messageContext = context.getMessageContext();
		
		if (def.getLifecycleState() == null) {
			messageContext.addMessage(new MessageBuilder().error().source("lifecycle")
					.code("lifecycle.error.selectLifecycle")
					.defaultText("Please select a lifecycle stage").build());
		}
		Date now = new Date();
		if (def.getPublishDate() != null) {
			if (def.getPublishDateTime().before(now)) {
				messageContext.addMessage(new MessageBuilder().error().source("publishDate")
						.code("lifecycle.error.invalidPublishDate")
						.defaultText("The auto-publishing date must be in the future").build());
			}
		}
		if (def.getExpirationDate() != null) {
			if (def.getExpirationDateTime().before(now)) {
				messageContext.addMessage(new MessageBuilder().error().source("expirationDate")
						.code("lifecycle.error.invalidExpirationDate")
						.defaultText("The auto-expiration date must be in the future").build());
			}
		}
		if (def.getPublishDate() != null && def.getExpirationDate() != null) {
			if (def.getExpirationDateTime().before(def.getPublishDateTime())) {
				messageContext.addMessage(new MessageBuilder().error().source("expirationDate")
						.code("lifecycle.error.invalidPublishAndExpirationDate")
						.defaultText("The auto-expiration date must be after the auto-publish date").build());
			}
		}
	}
	
	public void checkSave(ChannelDefinitionForm def, ValidationContext context) {
		validateBasicInfo(def, context.getMessageContext());
		validateChooseType(def, context.getMessageContext());
		validateSetParameters(def, context.getMessageContext());
//		validateChooseCategories(def, context);
		validateChooseGroup(def, context.getMessageContext());
	}

}
