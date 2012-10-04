package org.jasig.portal.web.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.render.CsvRenderer;
import com.ibm.icu.util.ULocale;

/**
 * Renders a google {@link DataTable} to a CSV
 * 
 * @author Eric Dalquist
 */
public class GoogleCsvDataTableView extends AbstractGoogleDataTableView {
    public static final String OUT_FILE_NAME_ATTRIBUTE = "outFileName";
    
    protected void renderDataTable(Map<String, Object> model, DataTable data, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final CharSequence csvDataTable = CsvRenderer.renderDataTable(data, ULocale.forLocale(request.getLocale()), ",");
        
        response.setContentType("text/csv; charset=UTF-8");
        
        String outFileName = (String)model.get(OUT_FILE_NAME_ATTRIBUTE);
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
