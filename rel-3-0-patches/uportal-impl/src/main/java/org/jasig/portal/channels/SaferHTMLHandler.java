/* Copyright 2006, 2007 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

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

/**
 * ContentHandler that will produce a document that only includes
 * a white listed list of elements, attributes and URL schemes. Only
 * HTML that is considered to be safe from cross-site scripting
 * attacks is passed on to the document. 
 *
 */

public class SaferHTMLHandler implements ContentHandler{

	Node currentNode;
	StringBuffer chars = new StringBuffer();
	Document doc;

	/*
	 *  See FeedParser for information on sanitizing HTML:
	 *  http://feedparser.org/docs/html-sanitization.html#advanced.sanitization.why
	 */  

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

	private static final String[] SAFE_URL_SCHEMES = {"http","https","ftp","mailto"};

	private static final Set<String> SAFE_ELEMENTS_SET = Collections.unmodifiableSet( 
			new HashSet<String>(Arrays.asList(SAFE_ELEMNTS)));

	private static final Set<String> SAFE_ATTS_SET = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList(SAFE_ATTS)));

	private static final Set<String> SAFE_URL_SCHEMES_SET = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList(SAFE_URL_SCHEMES)));


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
			if (chars.length()>0){
				Node n = doc.createTextNode(chars.toString());
				chars = new StringBuffer();
				currentNode.appendChild(n);
			}
			Element temp = doc.createElement(qName);

			// loop through each attribute
			int length = atts.getLength();
			for (int i = 0; i< length; i++){

				String attrName = atts.getQName(i);
				String value = atts.getValue(i);

				// only copy safe attributes
				if (SAFE_ATTS_SET.contains(attrName) && value != null){
					// special handling for src and href attributes 
					if (attrName.toLowerCase().trim().equals("src") || 
							attrName.toLowerCase().trim().equals("href")){
						value = sanitizeURL(value);
					}
					// safe so we set the attribute on the document
					temp.setAttribute(attrName,value);
				}

			}
			currentNode.appendChild(temp);
			currentNode = temp;
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (SAFE_ELEMENTS_SET.contains(qName)){
			if (chars.length()>0){
				Node n = doc.createTextNode(chars.toString());
				chars = new StringBuffer();
				currentNode.appendChild(n);
			}
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

	/**
	 * Returns just the scheme portion of a URL. Forces
	 * the scheme to be all lower case.
	 */
	private static String parseScheme(String url) {
		String scheme = "";
		if (url != null){
			url = url.trim();
			int pos = url.indexOf(':');
			if (pos >= 0){
				scheme = url.substring(0,pos);
			}
			scheme = scheme.toLowerCase();
		}
		return scheme;
	}

	/**
	 * Make sure to only allow safe URL schemes.
	 * This includes http, https, ftp, mailto. This will
	 * prevent dangerous javascript URLs and other things
	 * we never even thought about. Returns url unaltered
	 * if the scheme is save. Returns empty string if the
	 * scheme is unsafe.
	 * 
	 * We could add more URL schemes if we determine they are  
	 * need and safe.
	 */

	public static String sanitizeURL(String url){
		String scheme = parseScheme(url);
		if (SAFE_URL_SCHEMES_SET.contains(scheme)){
			return url;
		}
		return "";
	}
}
