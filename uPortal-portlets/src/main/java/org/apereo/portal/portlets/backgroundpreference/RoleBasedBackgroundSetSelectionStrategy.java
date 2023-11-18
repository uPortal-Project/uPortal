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
package org.apereo.portal.portlets.backgroundpreference;

import java.util.Arrays;
import javax.annotation.PostConstruct;
import javax.portlet.ActionRequest;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.servlet.ServletContext;
import org.apache.commons.lang.StringUtils;
import org.apereo.portal.spring.spel.IPortalSpELService;
import org.apereo.portal.spring.spel.PortalSpELServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * Determines set of images to display by checking to see if user is in a mobile user group as
 * determined by the portal's security roles.
 */
@Component
public class RoleBasedBackgroundSetSelectionStrategy implements BackgroundSetSelectionStrategy {

    private enum PreferenceNames {
        DEFAULT("default"),

        MOBILE("mobile");

        /** <b>Must match</a> the name of the corresponding PAGS group perfectly. */
        private static final String MOBILE_DEVICE_ROLE_NAME = "Mobile Device Access";

        public static PreferenceNames getInstance(PortletRequest req) {
            return req.isUserInRole(MOBILE_DEVICE_ROLE_NAME) ? MOBILE : DEFAULT;
        }

        private final String prefix;

        private PreferenceNames(String prefix) {
            this.prefix = prefix;
        }

        public String getImageSetPreferenceName() {
            return prefix + "BackgroundImages";
        }

        public String getImageThumbnailSetPreferenceName() {
            return prefix + "BackgroundThumbnailImages";
        }

        public String getSelectedBackgroundImagePreferenceName() {
            return prefix + "SelectedBackgroundImage";
        }

        public String getBackgroundContainerSelectorPreferenceName() {
            return prefix + "BackgroundContainerSelector";
        }

        /**
         * The portlet-preference containing the image captions. This preference name will not
         * change.
         */
        public static final String IMAGE_CAPTIONS_PREFERENCE_NAME = "backgroundImageCaptions";
    }

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    @Autowired private ServletContext servletContext;

    @Autowired private IPortalSpELService portalSpELService;

    final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

    @PostConstruct
    public void init() {
        final String contextPath = servletContext.getContextPath();
        evaluationContext.setRootObject(new RootObjectImpl(contextPath));
    }

    @Override
    public String[] getImageSet(PortletRequest req) {
        final PreferenceNames names = PreferenceNames.getInstance(req);
        final PortletPreferences prefs = req.getPreferences();
        final String[] images =
                prefs.getValues(names.getImageSetPreferenceName(), EMPTY_STRING_ARRAY);
        for (int i = 0; i < images.length; i++) {
            images[i] = evaluateImagePath(images[i]);
        }
        return images;
    }

    @Override
    public String[] getImageThumbnailSet(PortletRequest req) {
        PreferenceNames names = PreferenceNames.getInstance(req);
        PortletPreferences prefs = req.getPreferences();
        final String[] images = prefs.getValues(names.getImageThumbnailSetPreferenceName(), null);
        for (int i = 0; i < images.length; i++) {
            images[i] = evaluateImagePath(images[i]);
        }
        return images;
    }

    @Override
    public String[] getImageCaptions(PortletRequest req) {
        PreferenceNames names = PreferenceNames.getInstance(req);
        PortletPreferences prefs = req.getPreferences();
        return prefs.getValues(names.IMAGE_CAPTIONS_PREFERENCE_NAME, null);
    }

    @Override
    public String getSelectedImage(PortletRequest req) {
        // No evaluation required (already processed)
        PreferenceNames names = PreferenceNames.getInstance(req);
        PortletPreferences prefs = req.getPreferences();
        return prefs.getValue(names.getSelectedBackgroundImagePreferenceName(), null);
    }

    @Override
    public String getBackgroundContainerSelector(PortletRequest req) {
        PreferenceNames names = PreferenceNames.getInstance(req);
        PortletPreferences prefs = req.getPreferences();
        return prefs.getValue(names.getBackgroundContainerSelectorPreferenceName(), null);
    }

    @Override
    public void setSelectedImage(ActionRequest req, String backgroundImage) {

        PreferenceNames names = PreferenceNames.getInstance(req);
        PortletPreferences prefs = req.getPreferences();

        if (StringUtils.isNotBlank(backgroundImage)) {

            // We are trying to choose a background;  first verify the requested image is actually
            // in the set...
            String[] images =
                    prefs.getValues(names.getImageSetPreferenceName(), EMPTY_STRING_ARRAY);
            for (int i = 0; i < images.length; i++) {
                images[i] = evaluateImagePath(images[i]);
            }
            if (Arrays.asList(images).contains(backgroundImage)) {
                try {
                    prefs.setValue(
                            names.getSelectedBackgroundImagePreferenceName(), backgroundImage);
                    prefs.store();
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to store the user's choice of background image", e);
                }
            }

        } else {

            // We are trying to clear a previous selection
            try {
                prefs.reset(names.getSelectedBackgroundImagePreferenceName());
                prefs.store();
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to reset the user's choice of background image", e);
            }
        }
    }

    /*
     * Implementation
     */

    private String evaluateImagePath(String pathBeforeProcessing) {
        final Expression x =
                portalSpELService.parseExpression(
                        pathBeforeProcessing, PortalSpELServiceImpl.TemplateParserContext.INSTANCE);
        final String result = x.getValue(evaluationContext, String.class);
        return result;
    }

    /*
     * Nested Types
     */

    /**
     * Allows us to evaluate '${portalContext}' to '/uPortal' (or whatever it is) within image
     * paths.
     */
    private static final class RootObjectImpl {
        private final String portalContext;

        public RootObjectImpl(String portalContext) {
            this.portalContext = portalContext;
        }

        @SuppressWarnings("unused")
        public String getPortalContext() {
            return portalContext;
        }
    }
}
