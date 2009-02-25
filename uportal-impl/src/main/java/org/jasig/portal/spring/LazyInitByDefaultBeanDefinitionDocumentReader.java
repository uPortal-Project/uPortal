/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.spring;

import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.w3c.dom.Element;

/**
 * Extends the Spring DefaultBeanDefinitionDocumentReader to set the {@link BeanDefinitionParserDelegate#DEFAULT_LAZY_INIT_ATTRIBUTE}
 * to true, usefull when loading a context during testing or with command line tools.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class LazyInitByDefaultBeanDefinitionDocumentReader extends DefaultBeanDefinitionDocumentReader {
    @Override
    protected BeanDefinitionParserDelegate createHelper(XmlReaderContext readerContext, Element root) {
        root.setAttribute(BeanDefinitionParserDelegate.DEFAULT_LAZY_INIT_ATTRIBUTE, "true");
        return super.createHelper(readerContext, root);
    }
}