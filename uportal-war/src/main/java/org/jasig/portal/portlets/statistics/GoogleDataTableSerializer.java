package org.jasig.portal.portlets.statistics;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.google.visualization.datasource.render.JsonRenderer;

/**
 * GoogleDataTableSerializer configures a Google DataTable to be serialized 
 * using Google's Json serialization code. 
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
public class GoogleDataTableSerializer extends JsonSerializer<JsonDataTable> {

    @Override
    public void serialize(JsonDataTable dataTable, JsonGenerator gen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {
        final CharSequence formatted = JsonRenderer.renderDataTable(dataTable, true, true, false);
        gen.writeRawValue(formatted.toString());
    }

}
