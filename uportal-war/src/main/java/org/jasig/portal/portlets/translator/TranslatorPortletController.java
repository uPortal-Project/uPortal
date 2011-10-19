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
