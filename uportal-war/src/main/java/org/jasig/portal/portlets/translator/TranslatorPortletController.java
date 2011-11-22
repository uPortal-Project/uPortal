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

import java.util.List;

import javax.portlet.RenderRequest;

import org.jasig.portal.portlets.localization.LocaleBean;
import org.jasig.portal.portlets.localization.UserLocaleHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

/**
 * This controller simply returns a single JSP page and populates "locales" request attribute by
 * setting it to locales supported by portal. All further interactions are using AJAX calls which
 * are handled by a controllers depending on selected entity type. For example, by selecting
 * "portlet" entity type to translate, {@link PortletEntityTranslationController} will be used.
 * 
 * @author Arvids Grabovskis
 * @version $Revision$
 */
@Controller
@RequestMapping("VIEW")
public class TranslatorPortletController {
    
    private UserLocaleHelper userLocaleHelper;
    
    @Autowired(required = true)
    public void setUserLocaleHelper(UserLocaleHelper userLocaleHelper) {
        this.userLocaleHelper = userLocaleHelper;
    }
    
    @RenderMapping
    public ModelAndView view(RenderRequest request) {
        List<LocaleBean> locales = userLocaleHelper.getLocales(request.getLocale());
        return new ModelAndView("/jsp/Translator/translator", "locales", locales);
    }
}
