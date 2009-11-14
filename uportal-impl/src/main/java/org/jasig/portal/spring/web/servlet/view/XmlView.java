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
import org.springframework.web.servlet.view.AbstractView;

/**
 * <p>Simple implementation of a Spring View for displaying XML Documents.</p>
 * 
 * @author Drew Mazurek
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class XmlView extends AbstractView {
    
	protected final Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * Construct a new XmlView instance.
	 */
	public XmlView() {
		setContentType("text/xml");
	}

	private String xmlKey = "xml";

	/**
	 * Set the model key containing the XML.
	 * 
	 * @param xmlKey
	 */
	public void setXmlKey(String xmlKey) {
		this.xmlKey = xmlKey;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.view.AbstractView#renderMergedOutputModel(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void renderMergedOutputModel(Map model,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		response.setContentType(getContentType());
		
		OutputStream out = response.getOutputStream();

		Object obj = model.get(xmlKey);
		
		// if the object is a Document, convert it to XML
		if (obj instanceof Document) {
			Document document = (Document) model.get(xmlKey);
			
			try {
				XMLWriter writer = new XMLWriter(out);
				writer.write(document);
				writer.flush();
			} catch (IOException ex) {
				logger.error("IOException writing XML",ex);
			}
			
		} 
		
		// if the object is a String, just send it straight to the output
		else if (obj instanceof String) {
			String xml = (String) model.get(xmlKey);
			out.write(xml.getBytes());
			out.flush();
		}

	}
}
