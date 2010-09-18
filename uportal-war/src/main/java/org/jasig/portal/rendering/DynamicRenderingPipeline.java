/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.rendering;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.ThemeStylesheetDescription;
import org.jasig.portal.character.stream.CharacterEventReader;
import org.jasig.portal.character.stream.events.CharacterDataEvent;
import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.character.stream.events.CharacterEventTypes;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Top level class that initiates rendering via a {@link CharacterPipelineComponent}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DynamicRenderingPipeline implements IPortalRenderingPipeline {
    public static final String CHARACTER_SET = "UTF-8";
    
    private IUserInstanceManager userInstanceManager;
    private CharacterPipelineComponent pipeline;

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    /**
     * The root element in the rendering pipeline. This element MUST only return {@link CharacterEventTypes#CHARACTER}
     * type events.
     */
    public void setPipeline(CharacterPipelineComponent pipeline) {
        this.pipeline = pipeline;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void clearSystemCharacterCache() {
        // NOOP  
    }

    @Override
    public void renderState(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        //Disable page caching
        res.setHeader("pragma", "no-cache");
        res.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate");
        res.setDateHeader("Expires", 0);
        // set the response mime type
        final String mimeType = this.getMimeType(req);
        res.setContentType(mimeType + "; charset=" + CHARACTER_SET);
        
        final PipelineEventReader<CharacterEventReader, CharacterEvent> pipelineEventReader = this.pipeline.getEventReader(req, res);
        final PrintWriter writer = res.getWriter();
        
        for (final CharacterEvent event : pipelineEventReader) {
            if (CharacterEventTypes.CHARACTER != event.getEventType()) {
                throw new RenderingPipelineConfigurationException("Only " + CharacterEventTypes.CHARACTER + " events are supported in the top level renderer");
            }
            
            final String data = ((CharacterDataEvent)event).getData();
            writer.print(data);
            writer.flush();
        }
    }
    
    /**
     * Get the mime type to return for the current request
     */
    protected String getMimeType(HttpServletRequest req) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(req);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final ThemeStylesheetDescription themeStylesheetDescription;
        try {
            themeStylesheetDescription = preferencesManager.getThemeStylesheetDescription();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return themeStylesheetDescription.getMimeType();
    }
}
