package org.jasig.portal.web.view;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.AbstractView;

import com.google.visualization.datasource.datatable.DataTable;

/**
 * Base class for views that serialize {@link DataTable} objects
 * 
 * @author Eric Dalquist
 */
public abstract class AbstractGoogleDataTableView extends AbstractView {
    public static final String DATA_TABLE_MODEL_ATTRIBUTE = "dataTable";

    @Override
    protected final void renderMergedOutputModel(Map<String, Object> model,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        try {
            DataTable data = (DataTable) model.get(DATA_TABLE_MODEL_ATTRIBUTE);
            if (data == null) {
                for (final Object value : model.values()) {
                    if (value instanceof DataTable) {
                        if (data == null) {
                            data = (DataTable) value;
                        } else {
                            throw new IllegalArgumentException("Multiple DataTable objects exist in the model but none of them are stored under the key: " + DATA_TABLE_MODEL_ATTRIBUTE);
                        }
                    }
                }
            }

            if (data == null) {
                throw new IllegalArgumentException( "No DataTable could be found in the model");
            }

            renderDataTable(model, data, request, response);
        } catch (RuntimeException rte) {
            this.logger.error("Failed to generate response for DataTable view serialization", rte);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate response for DataTable view serialization");
        }
    }
    
    protected abstract void renderDataTable(Map<String, Object> model, DataTable data, HttpServletRequest request, HttpServletResponse response) throws IOException;
}