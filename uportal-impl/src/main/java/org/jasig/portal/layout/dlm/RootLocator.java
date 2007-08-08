package org.jasig.portal.layout.dlm;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.layout.IUserLayout;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Provides centralized tool for obtaining the root folder in a user layout or
 * fragment layout. Uses a pre-compiled XPATH expression for increased
 * performance.
 * 
 * @author mboyd
 * 
 */
public class RootLocator
{
    private static final Log LOG = LogFactory.getLog(RootLocator.class);
    private static final String cfgPrefix = RootLocator.class.getName() + ".";
    private static XPathExpression rootLocatorXpathExpression = null;

    /**
     * Returns the folder having a type attribute containing the value specified
     * by IUserLayout.ROOT_NODE_NAME or null if such a folder does not exist in
     * the layout.
     * 
     * @param layout
     * @return 
     */
    public static Element getRootElement(Document layout)
    {
        Element root = null;
        try
        {
            if (rootLocatorXpathExpression == null)
                createRootLocatorXpathExpression();

            root = (Element) rootLocatorXpathExpression.evaluate(
                    layout, XPathConstants.NODE);
        } catch (Exception e)
        {
            LOG.error("Unable to locate layout element of type " 
                    + IUserLayout.ROOT_NODE_NAME, e);
        }
        return root;
    }

    /**
     * Creates a compiled version of the XPATH evaluator for obtaining the 
     * root element of a layout.
     */
    private static void createRootLocatorXpathExpression()
    {
        String expression = "//layout/folder";
        try
        {
            XPathFactory fac = XPathFactory.newInstance();
            XPath xpath = fac.newXPath();
            rootLocatorXpathExpression = xpath.compile(expression);
        } catch (XPathExpressionException e)
        {
            throw new RuntimeException("Unable to compile XPath expression '" + expression +
                    "' for obtaining root layout element.", e);
        }
    }
}
