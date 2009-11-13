/**
 * 
 */
package org.jasig.portal.web.skin;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * Test harness for {@link ResourcesXalanElements}.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class ResourcesXalanElementsTest {


	/**
	 * Simulate Xalan usage of {@link ResourcesXalanElements#output(org.apache.xalan.extensions.XSLProcessorContext, org.apache.xalan.templates.ElemExtensionCall)}.
	 * Loads a sample {@link Resources} from "org/jasig/portal/web/skin/resources1.xml" (classpath).
	 * Loads a new XSLT {@link Transformer} using "org/jasig/portal/web/skin/resources1.xsl" (classpath).
	 * Transforms the file and uses {@link XMLUnit} to compare the result with the expected ("org/jasig/portal/web/skin/resources1-result.xml" on classpath).
	 * 
	 * @throws Exception
	 */
	@Test
	public void testResourcesOutput() throws Exception {
		JAXBContext context = JAXBContext.newInstance("org.jasig.portal.web.skin");
		Unmarshaller u = context.createUnmarshaller();
		Resources sampleResources = (Resources) u.unmarshal(new ClassPathResource("org/jasig/portal/web/skin/resources1.xml").getInputStream());

		ResourcesDao mockResourcesDao = EasyMock.createMock(ResourcesDao.class);
		EasyMock.expect(mockResourcesDao.getResources(EasyMock.isA(String.class))).andReturn(sampleResources);
		EasyMock.replay(mockResourcesDao);
		
		final TransformerFactory tFactory = TransformerFactory.newInstance();
		final Transformer transformer = tFactory.newTransformer(new StreamSource(new ClassPathResource("org/jasig/portal/web/skin/resources1.xsl").getInputStream()));

		transformer.setParameter(ResourcesXalanElements.SKIN_RESOURCESDAO_PARAMETER_NAME, 
				mockResourcesDao);

		final StringWriter resultWriter = new StringWriter();

		// set up configuration in the transformer impl
		final StreamSource sourceStream = new StreamSource(new ClassPathResource("org/jasig/portal/web/skin/testSourceStream.xml").getInputStream());
		transformer.transform(sourceStream, new StreamResult(resultWriter));
		
		final String result = resultWriter.getBuffer().toString();
        final String expected = IOUtils.toString(new ClassPathResource("org/jasig/portal/web/skin/resources1-result.xml").getInputStream());
        
        XMLUnit.setIgnoreWhitespace(true);
        Diff d = new Diff(expected, result);
        Assert.assertTrue("Transformation result differs from what's expected" + d, d.similar());
       
        EasyMock.verify(mockResourcesDao);
	}
}
