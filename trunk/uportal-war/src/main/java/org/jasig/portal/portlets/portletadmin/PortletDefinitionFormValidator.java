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

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.dao.usertype.FunctionalNameType;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portletpublishing.xml.Parameter;
import org.jasig.portal.portletpublishing.xml.PortletPublishingDefinition;
import org.jasig.portal.portletpublishing.xml.Step;
import org.jasig.portal.portlets.portletadmin.xmlsupport.IChannelPublishingDefinitionDao;
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
	public void setChannelPublishingDefinitionDao(IChannelPublishingDefinitionDao channelPublishingDefinitionDao) {
        this.channelPublishingDefinitionDao = channelPublishingDefinitionDao;
    }

	@Autowired(required = true)
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
		this.portletDefinitionRegistry = portletDefinitionRegistry;
	}
	
	public void validateChooseType(PortletDefinitionForm def, MessageContext context) {
		if(def.getTypeId() == 0) {
			context.addMessage(new MessageBuilder().error().source("typeId")
					.code("please.choose.portlet.type").build());
		}
	}
	
	public void validateBasicInfo(PortletDefinitionForm def, MessageContext context) {
		if (StringUtils.isEmpty(def.getFname())) {
			context.addMessage(new MessageBuilder().error().source("fName")
					.code("please.enter.fname").build());
		} else if (!FunctionalNameType.isValid(def.getFname())) {
			context.addMessage(new MessageBuilder().error().source("fName")
					.code("fname.invalid").build());		
		} 
		
		// if this is a new portlet and the fname is already taken
		else if (def.getId() == null && portletDefinitionRegistry.getPortletDefinitionByFname(def.getFname()) != null) {
			context.addMessage(new MessageBuilder().error().source("fName")
					.code("fname.in.use").build());
		}
		
		if (StringUtils.isEmpty(def.getTitle())) {
			context.addMessage(new MessageBuilder().error().source("title")
					.code("please.enter.title").build());
		}
		
		if (StringUtils.isEmpty(def.getName())) {
			context.addMessage(new MessageBuilder().error().source("name")
					.code("please.enter.name").build());
		}

		// if this is a new portlet and the name is already taken
		if (def.getId() == null && portletDefinitionRegistry.getPortletDefinitionByName(def.getName()) != null) {
			context.addMessage(new MessageBuilder().error().source("name")
					.code("name.in.use").build());
		}
		

	}
	
	public void validateSetParameters(PortletDefinitionForm def, MessageContext context) {
		PortletPublishingDefinition cpd = channelPublishingDefinitionDao.getChannelPublishingDefinition(def.getTypeId());
		for (Step step : cpd.getSteps()) {
			if (step.getParameters() != null) {
				for (Parameter param : step.getParameters()) {
					
					// if the user has entered a value for this parameter, 
					// check it against the CPD
					if (def.getParameters().containsKey(param.getName()) && 
							!StringUtils.isEmpty(def.getParameters().get(param.getName()).getValue())) {
						
						String paramValue = def.getParameters().get(param.getName()).getValue();
						String paramPath = "parameters['" + param.getName() + "'].value";
						
						// if this parameter is intended to be a number, ensure
						// that it is
//						String base = param.getType().getBase();
//						if ("integer".equals(base)) {
//							try {
//								Integer.parseInt(paramValue);
//							} catch (NumberFormatException e) {
//								context.addMessage(new MessageBuilder().error().source(paramPath)
//										.code("value.must.be.int").build());
//							}
//						} else if ("float".equals(base)) {
//							try {
//								Float.parseFloat(paramValue);
//							} catch (NumberFormatException e) {
//								context.addMessage(new MessageBuilder().error().source(paramPath)
//										.code("value.must.be.num").build());
//							}
//						}
						
						// if this parameter has a restriction in the CPD, 
						// check it against the restriction
//						if (param.getType().getRestriction() != null 
//								&& def.getParameters().containsKey(param.getName())) {
//							
//							Restriction restriction = param.getType().getRestriction();
//							if ("range".equals(restriction.getType())) {
//								// For now, lets just not do anything.  It doesn't 
//								// look like the existing portlet manager logic 
//								// actually uses this restriction for validation
//							} else if ("enumeration".equals(restriction.getType())) {
//								// if this restriction is an enumeration of allowed values, check to
//								// make sure the entered value is in the enumerated list
//								if (!restriction.getValue().contains(paramValue)) {
//									context.addMessage(new MessageBuilder().error().source(paramPath)
//											.code("invalid.selection").build());
//								}
//							}
//						}
						
					}
					
				}
			}
		}
	}
	
	public void validateChooseCategory(PortletDefinitionForm def, MessageContext context) {
		// make sure the user has picked at least one category
		if (def.getCategories().size() == 0) {
			context.addMessage(new MessageBuilder().error().source("categories")
					.code("please.choose.at.least.one.category").build());
		}
	}
	
	public void validateChooseGroup(PortletDefinitionForm def, MessageContext context) {
		// make sure the user has picked at least one group
		if (def.getGroups().size() == 0) {
			context.addMessage(new MessageBuilder().error().source("groups")
					.code("please.choose.at.least.one.group").build());
		}
	}
	
	public void validateLifecycle(PortletDefinitionForm def, ValidationContext context) {
		MessageContext messageContext = context.getMessageContext();
		
		if (def.getLifecycleState() == null) {
			messageContext.addMessage(new MessageBuilder().error().source("lifecycle")
					.code("please.select.lifecycle.stage").build());
		}
		Date now = new Date();
		if (def.getPublishDate() != null) {
			if (def.getPublishDateTime().before(now)) {
				messageContext.addMessage(new MessageBuilder().error().source("publishDate")
						.code("auto.publish.date.must.be.future").build());
			}
		}
		if (def.getExpirationDate() != null) {
			if (def.getExpirationDateTime().before(now)) {
				messageContext.addMessage(new MessageBuilder().error().source("expirationDate")
						.code("auto.expire.date.must.be.future").build());
			}
		}
		if (def.getPublishDate() != null && def.getExpirationDate() != null) {
			if (def.getExpirationDateTime().before(def.getPublishDateTime())) {
				messageContext.addMessage(new MessageBuilder().error().source("expirationDate")
						.code("auto.expire.date.must.be.after.publish").build());
			}
		}
	}
	
	public void checkSave(PortletDefinitionForm def, ValidationContext context) {
		validateBasicInfo(def, context.getMessageContext());
		validateChooseType(def, context.getMessageContext());
		validateSetParameters(def, context.getMessageContext());
//		validateChooseCategories(def, context);
		validateChooseGroup(def, context.getMessageContext());
	}

}
