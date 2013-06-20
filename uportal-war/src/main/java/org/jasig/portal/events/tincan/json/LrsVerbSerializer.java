package org.jasig.portal.events.tincan.json;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.jasig.portal.events.tincan.om.LrsVerb;

public class LrsVerbSerializer extends JsonSerializer<LrsVerb> {

    @Override
    public void serialize(LrsVerb value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {

        jgen.writeStartObject();
        jgen.writeStringField("id", value.getUri());
        
        //TODO need to make this localizable
        jgen.writeObjectFieldStart("display");
        jgen.writeStringField("en-US", value.name().toLowerCase());
        jgen.writeEndObject();
        
        jgen.writeEndObject();
    }

}
