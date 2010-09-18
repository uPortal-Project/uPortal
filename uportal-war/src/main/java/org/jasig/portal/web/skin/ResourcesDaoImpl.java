/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * 
 */
package org.jasig.portal.web.skin;

import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * {@link ResourcesDao} implementation that resolves the String argument using
 * the {@link ServletContext}.
 * 
 * Depends on {@link JAXBContext} to unmarshal the {@link Resources}.
 * 
 * @see ServletContextAware
 * @see JAXBContext
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
@Service
public class ResourcesDaoImpl implements ResourcesDao, ServletContextAware {
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
    
    protected final Log logger = LogFactory.getLog(this.getClass());

	private static final String LEADING_SLASH = "/";
	private final JAXBContext context;
	private ServletContext servletContext;
	
	/**
	 * Initializes re-usable {@link JAXBContext}.
	 * 
	 * See: https://jaxb.dev.java.net/guide/Performance_and_thread_safety.html
	 * 
	 * @throws IllegalStateException if a {@link JAXBException} is thrown during initialization
	 */
	public ResourcesDaoImpl() {
		try {
			context = JAXBContext.newInstance("org.jasig.portal.web.skin");
		} catch (JAXBException e) {
			throw new IllegalStateException("failed to initialize JAXB", e);
		}
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.web.skin.ResourcesDao#getResources(java.lang.String)
	 */
	public Resources getResources(String pathToSkinXml) {
		if(!StringUtils.startsWith(pathToSkinXml, LEADING_SLASH)) {
			pathToSkinXml = LEADING_SLASH + pathToSkinXml;
		}
		InputStream skinSource = servletContext.getResourceAsStream(pathToSkinXml);
		if(null == skinSource) {
			return null;
		}
		try {
			Unmarshaller u = context.createUnmarshaller();
			Resources result = (Resources) u.unmarshal(skinSource);
			return result;
		} catch (JAXBException e) {
			throw new IllegalArgumentException("invalid skin resources found at " + pathToSkinXml);
		} 
	}
	
	@Override
    public NodeList getResourcesFragment(String pathToSkinXml, String relativeRoot) throws ParserConfigurationException {
	    final Resources skinResources = this.getResources(pathToSkinXml);
	    if (skinResources == null) {
	        return null;
	    }
	    
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        final Document doc = builder.newDocument();
        final DocumentFragment headFragment = doc.createDocumentFragment();

        for(Css css: skinResources.getCss()) {
            appendCssNode(doc, headFragment, css, relativeRoot);
        }
        for(Js js: skinResources.getJs()) {
            appendJsNode(doc, headFragment, js, relativeRoot);
        }
        
        return headFragment.getChildNodes();
    }

    /* (non-Javadoc)
	 * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
	 */
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
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
            scriptPath = FilenameUtils.separatorsToUnix(scriptPath);
            if(logger.isDebugEnabled()) {
                logger.debug("translated relative js value " + js.getValue() + " to " + scriptPath);
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
            element.appendChild(document.createTextNode(""));
            
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
            stylePath = FilenameUtils.separatorsToUnix(stylePath);
            if(logger.isDebugEnabled()) {
                logger.debug("translated relative css value " + css.getValue() + " to " + stylePath);
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
