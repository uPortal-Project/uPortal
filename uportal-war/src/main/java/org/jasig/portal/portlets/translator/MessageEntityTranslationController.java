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
import java.util.Locale;
import java.util.Set;

import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.i18n.Message;
import org.jasig.portal.i18n.dao.IMessageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

/**
 * This controller is used in order to respond to AJAX calls that require message code list, message
 * entities and post those entities after form submission. Typical scenario is that entity list is
 * loaded using {@link #getEntityList()} and user can select one to translate. Upon selection, exact
 * entity must be retrieved using {@link #getEntity(String, String)} in order to fill in the form.
 * Upon form sumission, another AJAX call submits form values and
 * {@link #postTranslation(String, String, String)} updates the message.
 * 
 * @author Arvids Grabovskis
 * @version $Revision$
 */
@Controller
@RequestMapping(value = "VIEW", params = "entity=message")
public class MessageEntityTranslationController {
    
    private IMessageDao messageDao;
    
    @Autowired
    public void setMessageDao(IMessageDao messageDao) {
        this.messageDao = messageDao;
    }
    
    @ResourceMapping
    @RequestMapping(params = "action=getEntityList")
    public ModelAndView getEntityList() throws Exception {
        final Set<String> codes = messageDao.getCodes();
        final List<TranslatableEntity> entities = new ArrayList<TranslatableEntity>();
        for (String code : codes) {
            TranslatableEntity entity = new TranslatableEntity();
            entity.setId(code);
            entity.setTitle(code);
            entities.add(entity);
        }
        
        return new ModelAndView("json", "entities", entities);
    }
    
    @ResourceMapping
    @RequestMapping(params = "action=getEntity")
    public ModelAndView getEntity(@RequestParam("id") String code, @RequestParam("locale") String localeStr) {
        final Locale locale = LocaleManager.parseLocale(localeStr);
        final Message message = messageDao.getMessage(code, locale);
        return new ModelAndView("json", "message", message);
    }
    
    @ResourceMapping
    @RequestMapping(params = "action=postTranslation")
    public ModelAndView postTranslation(@RequestParam("id") String code, @RequestParam("locale") String localeStr,
            @RequestParam("value") String value) {
        final Locale locale = LocaleManager.parseLocale(localeStr);
        if (locale != null && StringUtils.hasText(code) && StringUtils.hasText(value)) {
            final Message message = messageDao.getMessage(code, locale);
            if (message != null) {
                message.setValue(value);
                messageDao.updateMessage(message);
            } else {
                // if message is not found in the backend storage, a new one must be created
                messageDao.createMessage(code, locale, value);
            }
        }
        return new ModelAndView("json");
    }
}
