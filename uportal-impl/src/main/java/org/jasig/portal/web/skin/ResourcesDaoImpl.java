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

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;

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

	/* (non-Javadoc)
	 * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
	 */
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

}
