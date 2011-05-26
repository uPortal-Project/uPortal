package org.jasig.portal.json.rendering;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.character.stream.CharacterEventReader;
import org.jasig.portal.character.stream.events.CharacterDataEvent;
import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.character.stream.events.CharacterEventTypes;
import org.jasig.portal.rendering.CharacterPipelineComponent;
import org.jasig.portal.rendering.IPortalRenderingPipeline;
import org.jasig.portal.rendering.PipelineEventReader;
import org.jasig.portal.rendering.RenderingPipelineConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonLayoutRenderingPipeline implements IPortalRenderingPipeline {
    public static final String CHARACTER_SET = "UTF-8";
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private CharacterPipelineComponent pipeline;

    /**
     * The root element in the rendering pipeline. This element MUST only return {@link CharacterEventTypes#CHARACTER}
     * type events.
     */
    public void setPipeline(CharacterPipelineComponent pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public void renderState(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        //Disable page caching
        res.setHeader("pragma", "no-cache");
        res.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate");
        res.setDateHeader("Expires", 0);

        final PipelineEventReader<CharacterEventReader, CharacterEvent> pipelineEventReader = this.pipeline.getEventReader(req, res);

        // set the response mime type
        final String contentType = "application/json; charset=" + CHARACTER_SET;
        res.setContentType(contentType);
        
        final PrintWriter writer = res.getWriter();
        
        for (final CharacterEvent event : pipelineEventReader) {
            if (CharacterEventTypes.CHARACTER != event.getEventType()) {
                throw new RenderingPipelineConfigurationException("Only " + CharacterEventTypes.CHARACTER + " events are supported in the top level renderer. " + event.getEventType() + " is not supported.");
            }
            
            final String data = ((CharacterDataEvent)event).getData();
            writer.print(data);
            writer.flush();
            res.flushBuffer();
        }
    }

}
