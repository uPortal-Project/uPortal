package org.jasig.portal.portlet.delegation;

import javax.portlet.RenderResponse;

/**
 * Delegates to a {@link RenderResponse}
 * 
 * @author Eric Dalquist
 */
public class RenderResponsePortletOutputHandler extends MimeResponsePortletOutputHandler {
    
    public RenderResponsePortletOutputHandler(RenderResponse renderResponse) {
        super(renderResponse);
    }
}
