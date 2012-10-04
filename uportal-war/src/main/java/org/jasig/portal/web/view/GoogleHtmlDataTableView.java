package org.jasig.portal.web.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.render.HtmlRenderer;
import com.ibm.icu.util.ULocale;

/**
 * Renders a google {@link DataTable} to HTML
 * 
 * @author Eric Dalquist
 */
public class GoogleHtmlDataTableView extends AbstractGoogleDataTableView {
    protected void renderDataTable(Map<String, Object> model, DataTable data, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final CharSequence htmlDataTable = HtmlRenderer.renderDataTable(data, ULocale.forLocale(request.getLocale()));
        
        response.setContentType("text/html");
        response.setContentLength(htmlDataTable.length());
        
        final PrintWriter writer = response.getWriter();
        writer.append(htmlDataTable);
    }
}
