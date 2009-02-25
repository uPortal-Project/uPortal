/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author George Lindholm
 *
 * @version $Revision$
 * @since uPortal 2.5.3
 */
public class SAXErrorHandler implements ErrorHandler {

    private static final Log log = LogFactory.getLog(SAXErrorHandler.class);

	private String invoker;
	public SAXErrorHandler(final String invoker) {
		this.invoker = invoker;
	}
	/* (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	public void warning(SAXParseException exception) throws SAXException {
		log.warn(invoker, exception);
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
	 */
	public void error(SAXParseException exception) throws SAXException {
		log.error(invoker, exception);
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	public void fatalError(SAXParseException exception) throws SAXException {
		log.fatal(invoker, exception);
	}

}
