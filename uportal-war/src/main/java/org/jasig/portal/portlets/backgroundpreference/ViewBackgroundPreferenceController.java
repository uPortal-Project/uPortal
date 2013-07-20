package org.jasig.portal.portlets.backgroundpreference;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.portlet.ActionRequest;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.PortletPreferences;

@Controller
@RequestMapping("VIEW")
public class ViewBackgroundPreferenceController {

    BackgroundSetSelectionStrategy imageSetSelectionStrategy = new RoleBasedBackgroundSetSelectionStrategy();

    public void setImageSetSelectionStrategy(BackgroundSetSelectionStrategy imageSetSelectionStrategy) {
        this.imageSetSelectionStrategy = imageSetSelectionStrategy;
    }

    /**
     * Display the main user-facing view of the portlet.
     * 
     * @param request
     * @return
     */
    @RenderMapping
    public String getView(RenderRequest req, Model model) {

        String[] images = imageSetSelectionStrategy.getImageSet(req);
        model.addAttribute("images", images);

        String preferredBackgroundImage = imageSetSelectionStrategy.getSelectedImage(req);
        model.addAttribute("backgroundImage", preferredBackgroundImage);

        String backgroundContainerSelector = imageSetSelectionStrategy.getBackgroundContainerSelector(req);
        model.addAttribute("backgroundContainerSelector", backgroundContainerSelector);

        PortletPreferences prefs = req.getPreferences();
        model.addAttribute("applyOpacityTo", prefs.getValue("applyOpacityTo", null));
        model.addAttribute("opacityCssValue", prefs.getValue("opacityCssValue", "1.0"));

        return "/jsp/BackgroundPreference/viewBackgroundPreference";
    }

    @ActionMapping(params = {"action=savePreferences"})
    public void savePreferences(ActionRequest req, ActionResponse res, 
            @RequestParam(required=false) String backgroundImage, 
            @RequestParam String redirectLocation) throws Exception {

        imageSetSelectionStrategy.setSelectedImage(req, backgroundImage);

        // Reirect the user whence he came because the mobile rendering will 
        // otherwise attempt to MAXIMIZE the portlet... which is nonsense
        res.sendRedirect(redirectLocation);

    }

}
