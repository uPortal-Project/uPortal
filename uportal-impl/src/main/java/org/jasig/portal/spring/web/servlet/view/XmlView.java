package org.jasig.portal.spring.web.servlet.view;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.io.XMLWriter;
import org.springframework.web.servlet.View;

/**
 * <p>Simple implementation of a Spring View for displaying XML Documents.</p>
 * 
 * @author Drew Mazurek
 */
public class XmlView implements View {
    
	protected final Log logger = LogFactory.getLog(this.getClass());
	protected String xmlKey = "xml";
	
	public String getContentType() {
		return "text/xml";
	}

	@SuppressWarnings("unchecked")
	public void render(Map model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		Document document = (Document) model.get(xmlKey);
		OutputStream out = response.getOutputStream();
		
		try {
			XMLWriter writer = new XMLWriter(out);
			writer.write(document);
			writer.flush();
		} catch (IOException ex) {
			logger.error("IOException writing XML",ex);
		}
	}

	public void setXmlKey(String xmlKey) {
		this.xmlKey = xmlKey;
	}
}
