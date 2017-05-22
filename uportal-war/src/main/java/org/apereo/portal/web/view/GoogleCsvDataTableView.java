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
import com.google.visualization.datasource.render.CsvRenderer;
import com.ibm.icu.util.ULocale;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Renders a google {@link DataTable} to a CSV
 *
 */
public class GoogleCsvDataTableView extends AbstractGoogleDataTableView {
    public static final String OUT_FILE_NAME_ATTRIBUTE = "outFileName";

    protected void renderDataTable(
            Map<String, Object> model,
            DataTable data,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        final CharSequence csvDataTable =
                CsvRenderer.renderDataTable(data, ULocale.forLocale(request.getLocale()), ",");

        response.setContentType("text/csv; charset=UTF-8");

        String outFileName = (String) model.get(OUT_FILE_NAME_ATTRIBUTE);
        if (outFileName == null) {
            outFileName = "dataTable.csv";
        }
        // For security reasons, make sure the file extension is ".csv".
        else if (!outFileName.toLowerCase().endsWith(".csv")) {
            outFileName = outFileName + ".csv";
        }
        response.setHeader("Content-Disposition", "attachment; filename=" + outFileName);
        response.setContentLength(csvDataTable.length());

        final PrintWriter writer = response.getWriter();
        writer.append(csvDataTable);
    }
}
