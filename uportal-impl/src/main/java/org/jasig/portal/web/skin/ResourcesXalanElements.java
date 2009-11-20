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
	
	/**
	 * Name of {@link Transformer} parameter used to retrieve the {@link ResourcesDao}.
	 */
	public static final String SKIN_RESOURCESDAO_PARAMETER_NAME = ResourcesXalanElements.class.getName() + "SKIN_RESOURCESDAO";
	/**
	 * Name of {@link System} property used to toggle default/aggregated skin output.
	 */
	public static final String AGGREGATED_THEME_PARAMETER = "org.jasig.portal.web.skin.aggregated_theme";
    /**
     * File name for default skin configuration (non-aggregated).
     */
	public static final String DEFAULT_SKIN_FILENAME = "skin.xml";
	/**
	 * File name for aggregated skin configuraiton.
	 */
	public static final String AGGREGATED_SKIN_FILENAME = "uportal3_aggr.skin.xml";
	
	private static final String OPEN_COND_COMMENT_PRE = "[";
	private static final String OPEN_COND_COMMENT_POST = "]> ";
	private static final String CLOSE_COND_COMMENT = " <![endif]";
	private static final String OPEN_SCRIPT = "<script type=\"text/javascript\" src=\"";
	private static final String CLOSE_SCRIPT = "\"></script>";
	private static final String OPEN_STYLE = "<link rel=\"stylesheet\" type=\"text/css\" href=\"";
	private static final String CLOSE_STYLE = "\"/>";
	
	private static final String SCRIPT = "script";
	private static final String LINK = "link";
	private static final String REL = "rel";
	private static final String SRC = "src";
	private static final String HREF = "href";
	private static final String TYPE = "type";
	private static final String MEDIA = "media";
	
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
		if(log.isDebugEnabled()) {
			log.debug("relativeRoot from element path: " + relativeRoot);
		}
		
		boolean aggregatedThemeEnabled = Boolean.parseBoolean(System.getProperty(AGGREGATED_THEME_PARAMETER, "true"));
		
		StringBuilder primaryPath = new StringBuilder(path);
		StringBuilder secondaryPath = new StringBuilder(path);
		if(aggregatedThemeEnabled) {
			primaryPath.append(AGGREGATED_SKIN_FILENAME);
			secondaryPath.append(DEFAULT_SKIN_FILENAME);
		} else {
			primaryPath.append(DEFAULT_SKIN_FILENAME);
			secondaryPath.append(AGGREGATED_SKIN_FILENAME);
		}
		
		Resources skinResources = resourcesDao.getResources(primaryPath.toString());
		if(null == skinResources) {		
			if(log.isWarnEnabled()) {
				log.warn(primaryPath.toString() + " not found, attempting " + secondaryPath.toString());
			}
			skinResources = resourcesDao.getResources(secondaryPath.toString());
		}
		// if it's still null, we have to bail out
		if(null == skinResources) {
			throw new IllegalStateException("no skin configuration found at " + primaryPath.toString() + " or " + secondaryPath.toString());
		}
		
		
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
	 * @param relativeRoot
	 */
	protected void appendJsNode(Document document, DocumentFragment head, Js js, String relativeRoot) {
		String scriptPath = js.getValue();
		if(!js.isAbsolute()) {
			scriptPath = FilenameUtils.normalize(relativeRoot + js.getValue());
			if(log.isDebugEnabled()) {
				log.debug("translated relative js value " + js.getValue() + " to " + scriptPath);
			}
		}
		
		if(js.isConditional()) {
			Comment c = document.createComment("");
			c.appendData(OPEN_COND_COMMENT_PRE);
			c.appendData(js.getConditional());
			c.appendData(OPEN_COND_COMMENT_POST);
			c.appendData(OPEN_SCRIPT);
			c.appendData(scriptPath);
			c.appendData(CLOSE_SCRIPT);
			c.appendData(CLOSE_COND_COMMENT);
			head.appendChild(c);
		} else {
			Element element = document.createElement(SCRIPT);
			element.setAttribute(TYPE, "text/javascript");
			element.setAttribute(SRC, scriptPath);
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
	 * @param relativeRoot
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
			c.appendData(OPEN_COND_COMMENT_PRE);
			c.appendData(css.getConditional());
			c.appendData(OPEN_COND_COMMENT_POST);
			c.appendData(OPEN_STYLE);
			c.appendData(stylePath);
			c.appendData("\" media=\"");
			c.appendData(css.getMedia());
			c.appendData(CLOSE_STYLE);
			c.appendData(CLOSE_COND_COMMENT);
			head.appendChild(c);
		} else {
			Element element = document.createElement(LINK);
			element.setAttribute(REL, "stylesheet");
			element.setAttribute(TYPE, "text/css");
			element.setAttribute(HREF, stylePath);
			element.setAttribute(MEDIA, css.getMedia());
			head.appendChild(element);
		}
	}
}
