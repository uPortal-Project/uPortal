/**
 * 
 */
package org.jasig.portal.web.skin;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.transformer.TransformerImpl;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * Used by Xalan to transform a {@link Resources} to the correct HTML head elements
 * (script tags for javascript and link tags for css).
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class ResourcesXalanElements {

	private Log log = LogFactory.getLog(this.getClass());
	
	public static final String SKIN_RESOURCESDAO_PARAMETER_NAME = ResourcesXalanElements.class.getName() + "SKIN_RESOURCESDAO";
	
	public static final String AGGREGATED_THEME_PARAMETER = "org.jasig.portal.web.skin.aggregated_theme";
    
	/**
	 * Pulls the {@link Resources} to render from the Transformer parameter
	 * named {@link #SKIN_RESOURCESDAO_PARAMETER_NAME}.
	 * 
	 * @param context
	 * @param elem
	 * @return
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 */
	public DocumentFragment output(XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException, ParserConfigurationException {
		final TransformerImpl transformer = context.getTransformer();

		ResourcesDao resourcesDao = (ResourcesDao) transformer.getParameter(SKIN_RESOURCESDAO_PARAMETER_NAME);
		
		final String path = elem.getAttribute("path", context.getContextNode(), transformer);
		final String relativeRoot = FilenameUtils.getPath(path);
		
		//TODO default value for aggregated_theme? temporarily set to true
		boolean aggregatedThemeEnabled = Boolean.parseBoolean(System.getProperty(AGGREGATED_THEME_PARAMETER, "true"));
		
		StringBuilder pathToSkinXml = new StringBuilder(path);
		if(aggregatedThemeEnabled) {
			pathToSkinXml.append("uportal3_aggr.skin.xml");
		} else {
			pathToSkinXml.append("skin.xml");
		}
		
		Resources skinResources = resourcesDao.getResources(pathToSkinXml.toString());
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.newDocument();
		DocumentFragment headFragment = doc.createDocumentFragment();

		for(Css css: skinResources.getCss()) {
			appendCssNode(doc, headFragment, css, relativeRoot);
		}
		for(Js js: skinResources.getJs()) {
			appendJsNode(doc, headFragment, js, relativeRoot);
		}

		return headFragment;
	}

	/**
	 * Convert the {@link Js} argument to an HTML script tag and append it
	 * to the {@link DocumentFragment}.
	 * 
	 * @param document
	 * @param head
	 * @param js
	 */
	protected void appendJsNode(Document document, DocumentFragment head, Js js, String relativeRoot) {
		String scriptPath = js.getValue();
		if(!js.isAbsolute()) {
			scriptPath = FilenameUtils.normalize(relativeRoot + js.getValue());
			if(log.isDebugEnabled()) {
				log.debug("translated js value " + js.getValue() + " to " + scriptPath);
			}
		}
		
		if(js.isConditional()) {
			Comment c = document.createComment("");
			c.appendData("[");
			c.appendData(js.getConditional());
			c.appendData("]> ");
			c.appendData("<script type=\"text/javascript\" src=\"");
			c.appendData(scriptPath);
			c.appendData("\"></script>");
			c.appendData(" <![endif]");
			head.appendChild(c);
		} else {
			Element element = document.createElement("script");
			element.setAttribute("type", "text/javascript");
			element.setAttribute("src", scriptPath);
			head.appendChild(element);
		}
	}

	/**
	 * Convert the {@link Css} argument to an HTML link tag and append it
	 * to the {@link DocumentFragment}.
	 * 
	 * @param document
	 * @param head
	 * @param css
	 */
	protected void appendCssNode(Document document, DocumentFragment head, Css css, String relativeRoot) {
		String stylePath = css.getValue();
		if(!css.isAbsolute()) {
			stylePath = FilenameUtils.normalize(relativeRoot + css.getValue());
			if(log.isDebugEnabled()) {
				log.debug("translated relative css value " + css.getValue() + " to " + stylePath);
			}
		}
		
		if(css.isConditional()) {
			Comment c = document.createComment("");
			c.appendData("[");
			c.appendData(css.getConditional());
			c.appendData("]> ");
			c.appendData("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
			c.appendData(stylePath);
			c.appendData("\" media=\"");
			c.appendData(css.getMedia());
			c.appendData("\"/>");
			c.appendData(" <![endif]");
			head.appendChild(c);
		} else {
			Element element = document.createElement("link");
			element.setAttribute("rel", "stylesheet");
			element.setAttribute("type", "text/css");
			element.setAttribute("href", stylePath);
			element.setAttribute("media", css.getMedia());
			head.appendChild(element);
		}
	}
}
