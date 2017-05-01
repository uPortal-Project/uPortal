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
package org.apereo.portal.web.view;

import com.google.visualization.datasource.datatable.DataTable;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.view.AbstractView;

/**
 * Base class for views that serialize {@link DataTable} objects
 *
 */
public abstract class AbstractGoogleDataTableView extends AbstractView {
    public static final String DATA_TABLE_MODEL_ATTRIBUTE = "dataTable";

    @Override
    protected final void renderMergedOutputModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        try {
            DataTable data = (DataTable) model.get(DATA_TABLE_MODEL_ATTRIBUTE);
            if (data == null) {
                for (final Object value : model.values()) {
                    if (value instanceof DataTable) {
                        if (data == null) {
                            data = (DataTable) value;
                        } else {
                            throw new IllegalArgumentException(
                                    "Multiple DataTable objects exist in the model but none of them are stored under the key: "
                                            + DATA_TABLE_MODEL_ATTRIBUTE);
                        }
                    }
                }
            }

            if (data == null) {
                throw new IllegalArgumentException("No DataTable could be found in the model");
            }

            renderDataTable(model, data, request, response);
        } catch (RuntimeException rte) {
            this.logger.error("Failed to generate response for DataTable view serialization", rte);
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to generate response for DataTable view serialization");
        }
    }

    protected abstract void renderDataTable(
            Map<String, Object> model,
            DataTable data,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException;
}
