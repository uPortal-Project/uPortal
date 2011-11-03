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
package org.jasig.portal.portlets.translator;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.ResourceRequest;

import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

/**
 * This controller is used in order to respond to AJAX calls that require portlet entity list,
 * portlet entities and post those entities after form submission. Typical scenario is that entity
 * list is loaded using {@link #getEntityList()} and user can select one to translate. Upon
 * selection, exact entity must be retrieved using {@link #getEntity(String, String)} in order to
 * fill in the form. Upon form sumission, another AJAX call submits form values and
 * {@link #postTranslation(String, String, String)} updates the message.
 * 
 * @author Arvids Grabovskis
 * @version $Revision$
 */
@Controller
@RequestMapping(value = "VIEW", params = "entity=portlet")
public class PortletEntityTranslationController {
    
    public static final String ENTITY_TYPE_PORTLET = "protlet";
    
    private IPortletDefinitionDao portletDefinitionDao;
    
    @Autowired(required = true)
    public void setPortletDefinitionDao(IPortletDefinitionDao portletDefinitionDao) {
        this.portletDefinitionDao = portletDefinitionDao;
    }
    
    @ResourceMapping
    @RequestMapping(params = "action=getEntityList")
    public ModelAndView getEntityList() throws Exception {
        final List<IPortletDefinition> portletDefs = portletDefinitionDao.getPortletDefinitions();
        final List<TranslatableEntity> entities = new ArrayList<TranslatableEntity>();
        for (IPortletDefinition portletDef : portletDefs) {
            TranslatableEntity entity = new TranslatableEntity();
            entity.setId(portletDef.getPortletDefinitionId().getStringId());
            entity.setTitle(portletDef.getTitle());
            entities.add(entity);
        }
        
        return new ModelAndView("json", "entities", entities);
    }
    
    @ResourceMapping
    @RequestMapping(params = "action=getEntity")
    public ModelAndView getPortletDefinition(@RequestParam("id") String portletId, @RequestParam("locale") String locale)
            throws Exception {
        final IPortletDefinition definition = portletDefinitionDao.getPortletDefinition(portletId);
        
        final PortletDefinitionTranslation translation = new PortletDefinitionTranslation();
        translation.setId(portletId);
        translation.setLocale(locale);
        translation.setLocalized(new LocalizedPortletDefinition(definition, locale));
        translation.setOriginal(new LocalizedPortletDefinition(definition, null));
        
        return new ModelAndView("json", "portlet", translation);
    }
    
    @ResourceMapping
    @RequestMapping(params = "action=postTranslation")
    public ModelAndView postPortletTranslation(@RequestParam("id") String portletId,
            @RequestParam("locale") String locale, ResourceRequest request) throws Exception {
        final IPortletDefinition definition = portletDefinitionDao.getPortletDefinition(portletId);
        if (definition != null) {
            definition.addLocalizedTitle(locale, request.getParameter("title"));
            definition.addLocalizedName(locale, request.getParameter("name"));
            definition.addLocalizedDescription(locale, request.getParameter("description"));
            portletDefinitionDao.updatePortletDefinition(definition);
        }
        
        return new ModelAndView("json");
    }
}
