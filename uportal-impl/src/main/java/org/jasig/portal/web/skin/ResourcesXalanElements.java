/**
 * 
 */
package org.jasig.portal.web.skin;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

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

	public static final String SKIN_RESOURCES_PARAMETER_NAME = ResourcesXalanElements.class.getName() + "CURRENT_SKIN_RESOURCES";

	/**
	 * Pulls the {@link Resources} to render from the Transformer parameter
	 * named {@link #SKIN_RESOURCES_PARAMETER_NAME}.
	 * 
	 * @param context
	 * @param elem
	 * @return
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 */
	public DocumentFragment output(XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException, ParserConfigurationException {
		final TransformerImpl transformer = context.getTransformer();

		Resources skinResources = (Resources) transformer.getParameter(SKIN_RESOURCES_PARAMETER_NAME);

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.newDocument();
		DocumentFragment headFragment = doc.createDocumentFragment();

		for(Css css: skinResources.getCss()) {
			appendCssNode(doc, headFragment, css);
		}
		for(Js js: skinResources.getJs()) {
			appendJsNode(doc, headFragment, js);
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
	protected void appendJsNode(Document document, DocumentFragment head, Js js) {
		Element element = document.createElement("script");
		element.setAttribute("type", "text/javascript");
		element.setAttribute("src", js.getValue());

		if(js.isConditional()) {
			Comment c = document.createComment("");
			c.appendData("[");
			c.appendData(js.getConditional());
			c.appendData("]> ");
			//c.appendChild(element);
			// TODO there has got to be a better way?
			c.appendData("<script type=\"text/javascript\" src=\"");
			c.appendData(js.getValue());
			c.appendData("\"></script>");

			c.appendData(" <![endif]");
			head.appendChild(c);
		} else {
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
	protected void appendCssNode(Document document, DocumentFragment head, Css css) {
		Element element = document.createElement("link");
		element.setAttribute("rel", "stylesheet");
		element.setAttribute("type", "text/css");
		element.setAttribute("href", css.getValue());
		element.setAttribute("media", css.getMedia());
		if(css.isConditional()) {
			Comment c = document.createComment("");
			c.appendData("[");
			c.appendData(css.getConditional());
			c.appendData("]> ");

			//c.appendChild(element);
			// TODO there has got to be a better way?
			c.appendData("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
			c.appendData(css.getValue());
			c.appendData("\" media=\"");
			c.appendData(css.getMedia());
			c.appendData("\"/>");

			c.appendData(" <![endif]");
			head.appendChild(c);
		} else {
			head.appendChild(element);
		}
	}
}
