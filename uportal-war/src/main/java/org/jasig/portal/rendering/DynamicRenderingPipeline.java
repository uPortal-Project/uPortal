/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.rendering;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.PortalException;
import org.jasig.portal.character.stream.CharacterEventReader;
import org.jasig.portal.character.stream.events.CharacterDataEvent;
import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.character.stream.events.CharacterEventTypes;
import org.jasig.portal.user.IUserInstance;

/**
 * Top level class that initiates rendering via a {@link CharacterPipelineComponent}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DynamicRenderingPipeline implements IPortalRenderingPipeline {
    private CharacterPipelineComponent pipeline;
    
    public void setPipeline(CharacterPipelineComponent pipeline) {
        this.pipeline = pipeline;
    }
    

    @SuppressWarnings("deprecation")
    @Override
    public void clearSystemCharacterCache() {
        // NOOP  
    }

    @Override
    public void renderState(HttpServletRequest req, HttpServletResponse res, IUserInstance userInstance) {
        final CacheableEventReader<CharacterEventReader, CharacterEvent> eventReader = this.pipeline.getEventReader(req, res);
        final PrintWriter writer;
        try {
            writer = res.getWriter();
        }
        catch (IOException e) {
            //TODO throw a more sane exception here
            throw new PortalException(e.getMessage(), e);
        }
        
        for (final CharacterEvent event : eventReader) {
            if (CharacterEventTypes.CHARACTER != event.getEventType()) {
                throw new RenderingPipelineConfigurationException("Only " + CharacterEventTypes.CHARACTER + " events are supported in the top level renderer");
            }
            
            final String data = ((CharacterDataEvent)event).getData();
            writer.print(data);
            writer.flush();
        }
    }
}
