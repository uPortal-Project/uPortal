package org.jasig.portal.portlets.sitemap;

import java.io.CharArrayWriter;
import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.xslt.XalanLayoutElementTitleHelper;
import org.jasig.portal.security.xslt.XalanMessageHelper;
import org.jasig.portal.security.xslt.XalanMessageHelperBean;
import org.jasig.portal.url.xml.XsltPortalUrlProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.xml.TransformerUtils;

@RunWith(MockitoJUnitRunner.class)
public class SitemapTest {
    
    private static final String STYLESHEET_LOCATION = "/org/jasig/portal/portlets/sitemap/sitemap.xsl";
    
    private static final String XML_LOCATION = "/org/jasig/portal/portlets/sitemap/layout.xml";
    
    private final Log logger = LogFactory.getLog(getClass());
    
    @InjectMocks
    private XsltPortalUrlProvider xsltPortalUrlProvider = new XsltPortalUrlProvider();
    
    private boolean useTabGroups = true;
    
    private MessageSource messageSource;
    
    public SitemapTest() {
        StaticMessageSource staticMessageSource = new StaticMessageSource();
        staticMessageSource.setUseCodeAsDefaultMessage(true);
        this.messageSource = staticMessageSource;
    }
    
    @Before
    public void setup() {
        new XalanLayoutElementTitleHelper().setMessageSource(messageSource);
        XalanMessageHelperBean messageHelper = new XalanMessageHelperBean();
        messageHelper.setMessageSource(messageSource);
        new XalanMessageHelper().setMessageHelper(messageHelper);
    }
    
    @Test
    public void testStylesheetCompilation() throws IOException {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        Resource resource = new ClassPathResource(STYLESHEET_LOCATION);
        Source source = new StreamSource(resource.getInputStream(), resource.getURI().toASCIIString());
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer(source);
            transformer.setParameter(SitemapPortletController.USE_TAB_GROUPS, useTabGroups);
            transformer.setParameter(SitemapPortletController.USER_LANG, "en_US");
            transformer.setParameter(XsltPortalUrlProvider.CURRENT_REQUEST, request);
            transformer.setParameter(XsltPortalUrlProvider.XSLT_PORTAL_URL_PROVIDER, this.xsltPortalUrlProvider);
            
            Source xmlSource = new StreamSource(new ClassPathResource(XML_LOCATION).getFile());
            CharArrayWriter buffer = new CharArrayWriter();
            TransformerUtils.enableIndenting(transformer);
            transformer.transform(xmlSource, new StreamResult(buffer));
            if (logger.isTraceEnabled()) {
                logger.trace("XML: " + new String(buffer.toCharArray()));
            }
        } catch (TransformerConfigurationException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
