package org.jasig.portal.channels;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;



public class SaferHTMLHandler implements ContentHandler{
	
	Node currentNode;
	StringBuffer chars = new StringBuffer();
	Document doc;

	// See FeedParser for information on sanitizing html  
	// http://feedparser.org/docs/html-sanitization.html#advanced.sanitization.why
	
	private static final String[] SAFE_ELEMNTS =  {"a", "abbr", "acronym", "address", "area", "b", "big",
	      "blockquote", "br", "button", "caption", "center", "cite", "code", "col",
	      "colgroup", "dd", "del", "dfn", "dir", "div", "dl", "dt", "em", "fieldset",
	      "font", "form", "h1", "h2", "h3", "h4", "h5", "h6", "hr", "i", "img", "input",
	      "ins", "kbd", "label", "legend", "li", "map", "menu", "ol", "optgroup",
	      "option", "p", "pre", "q", "s", "samp", "select", "small", "span", "strike",
	      "strong", "sub", "sup", "table", "tbody", "td", "textarea", "tfoot", "th",
	      "thead", "tr", "tt", "u", "ul", "var"};
		

	private static final String[] SAFE_ATTS = {"abbr", "accept", "accept-charset", "accesskey",
	    "action", "align", "alt", "axis", "border", "cellpadding", "cellspacing",
	    "char", "charoff", "charset", "checked", "cite", "class", "clear", "cols",
	    "colspan", "color", "compact", "coords", "datetime", "dir", "disabled",
	    "enctype", "for", "frame", "headers", "height", "href", "hreflang", "hspace",
	    "id", "ismap", "label", "lang", "longdesc", "maxlength", "media", "method",
	    "multiple", "name", "nohref", "noshade", "nowrap", "prompt", "readonly",
	    "rel", "rev", "rows", "rowspan", "rules", "scope", "selected", "shape", "size",
	    "span", "src", "start", "summary", "tabindex", "target", "title", "type",
	    "usemap", "valign", "value", "vspace", "width"};
	
	private static final Set SAFE_ELEMENTS_SET = Collections.unmodifiableSet( 
			new HashSet(Arrays.asList(SAFE_ELEMNTS)));
	
	private static final Set SAFE_ATTS_SET = Collections.unmodifiableSet(
			new HashSet(Arrays.asList(SAFE_ATTS)));
	
	public SaferHTMLHandler(Document doc ,Node root){
		this.doc = doc;
		currentNode = root;
	}

	public void setDocumentLocator(Locator locator) {
	}

	public void startDocument() throws SAXException {
	}

	public void endDocument() throws SAXException {
		Node n = doc.createTextNode(chars.toString());
		chars = new StringBuffer();
		currentNode.appendChild(n);
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
	}

	public void endPrefixMapping(String prefix) throws SAXException {
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (SAFE_ELEMENTS_SET.contains(qName)){
			// all okay
			Node n = doc.createTextNode(chars.toString());
			chars = new StringBuffer();
			currentNode.appendChild(n);
			Element temp = doc.createElement(qName);
			
			// add attributes that are allowed
			int length = atts.getLength();
			for (int i = 0; i< length; i++){
				
				String attrName = atts.getQName(i);
				String value = atts.getValue(i);
				// only copy safe attributes 
				if (SAFE_ATTS_SET.contains(attrName) && value != null){
					temp.setAttribute(attrName,value);
				}
				
			}
			currentNode.appendChild(temp);
			currentNode = temp;
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (SAFE_ELEMENTS_SET.contains(qName)){
			currentNode = currentNode.getParentNode();
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		chars.append(ch,start,length);
	}

	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
	}

	public void processingInstruction(String target, String data) throws SAXException {
	}

	public void skippedEntity(String name) throws SAXException {
	}
}
